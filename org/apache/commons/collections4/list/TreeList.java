package org.apache.commons.collections4.list;

import java.util.AbstractList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.apache.commons.collections4.ArrayStack;
import org.apache.commons.collections4.OrderedIterator;

public class TreeList<E> extends AbstractList<E> {
  private AVLNode<E> root;
  
  private int size;
  
  public TreeList() {}
  
  public TreeList(Collection<? extends E> coll) {
    if (!coll.isEmpty()) {
      this.root = new AVLNode<E>(coll);
      this.size = coll.size();
    } 
  }
  
  public E get(int index) {
    checkInterval(index, 0, size() - 1);
    return this.root.get(index).getValue();
  }
  
  public int size() {
    return this.size;
  }
  
  public Iterator<E> iterator() {
    return listIterator(0);
  }
  
  public ListIterator<E> listIterator() {
    return listIterator(0);
  }
  
  public ListIterator<E> listIterator(int fromIndex) {
    checkInterval(fromIndex, 0, size());
    return new TreeListIterator<E>(this, fromIndex);
  }
  
  public int indexOf(Object object) {
    if (this.root == null)
      return -1; 
    return this.root.indexOf(object, this.root.relativePosition);
  }
  
  public boolean contains(Object object) {
    return (indexOf(object) >= 0);
  }
  
  public Object[] toArray() {
    Object[] array = new Object[size()];
    if (this.root != null)
      this.root.toArray(array, this.root.relativePosition); 
    return array;
  }
  
  public void add(int index, E obj) {
    this.modCount++;
    checkInterval(index, 0, size());
    if (this.root == null) {
      this.root = new AVLNode<E>(index, obj, null, null);
    } else {
      this.root = this.root.insert(index, obj);
    } 
    this.size++;
  }
  
  public boolean addAll(Collection<? extends E> c) {
    if (c.isEmpty())
      return false; 
    this.modCount += c.size();
    AVLNode<E> cTree = new AVLNode<E>(c);
    this.root = (this.root == null) ? cTree : this.root.addAll(cTree, this.size);
    this.size += c.size();
    return true;
  }
  
  public E set(int index, E obj) {
    checkInterval(index, 0, size() - 1);
    AVLNode<E> node = this.root.get(index);
    E result = node.value;
    node.setValue(obj);
    return result;
  }
  
  public E remove(int index) {
    this.modCount++;
    checkInterval(index, 0, size() - 1);
    E result = get(index);
    this.root = this.root.remove(index);
    this.size--;
    return result;
  }
  
  public void clear() {
    this.modCount++;
    this.root = null;
    this.size = 0;
  }
  
  private void checkInterval(int index, int startIndex, int endIndex) {
    if (index < startIndex || index > endIndex)
      throw new IndexOutOfBoundsException("Invalid index:" + index + ", size=" + size()); 
  }
  
  static class AVLNode<E> {
    private AVLNode<E> left;
    
    private boolean leftIsPrevious;
    
    private AVLNode<E> right;
    
    private boolean rightIsNext;
    
    private int height;
    
    private int relativePosition;
    
    private E value;
    
    private AVLNode(int relativePosition, E obj, AVLNode<E> rightFollower, AVLNode<E> leftFollower) {
      this.relativePosition = relativePosition;
      this.value = obj;
      this.rightIsNext = true;
      this.leftIsPrevious = true;
      this.right = rightFollower;
      this.left = leftFollower;
    }
    
    private AVLNode(Collection<? extends E> coll) {
      this(coll.iterator(), 0, coll.size() - 1, 0, null, null);
    }
    
    private AVLNode(Iterator<? extends E> iterator, int start, int end, int absolutePositionOfParent, AVLNode<E> prev, AVLNode<E> next) {
      int mid = start + (end - start) / 2;
      if (start < mid) {
        this.left = new AVLNode(iterator, start, mid - 1, mid, prev, this);
      } else {
        this.leftIsPrevious = true;
        this.left = prev;
      } 
      this.value = iterator.next();
      this.relativePosition = mid - absolutePositionOfParent;
      if (mid < end) {
        this.right = new AVLNode(iterator, mid + 1, end, mid, this, next);
      } else {
        this.rightIsNext = true;
        this.right = next;
      } 
      recalcHeight();
    }
    
    E getValue() {
      return this.value;
    }
    
    void setValue(E obj) {
      this.value = obj;
    }
    
    AVLNode<E> get(int index) {
      int indexRelativeToMe = index - this.relativePosition;
      if (indexRelativeToMe == 0)
        return this; 
      AVLNode<E> nextNode = (indexRelativeToMe < 0) ? getLeftSubTree() : getRightSubTree();
      if (nextNode == null)
        return null; 
      return nextNode.get(indexRelativeToMe);
    }
    
    int indexOf(Object object, int index) {
      if (getLeftSubTree() != null) {
        int result = this.left.indexOf(object, index + this.left.relativePosition);
        if (result != -1)
          return result; 
      } 
      if ((this.value == null) ? (this.value == object) : this.value.equals(object))
        return index; 
      if (getRightSubTree() != null)
        return this.right.indexOf(object, index + this.right.relativePosition); 
      return -1;
    }
    
    void toArray(Object[] array, int index) {
      array[index] = this.value;
      if (getLeftSubTree() != null)
        this.left.toArray(array, index + this.left.relativePosition); 
      if (getRightSubTree() != null)
        this.right.toArray(array, index + this.right.relativePosition); 
    }
    
    AVLNode<E> next() {
      if (this.rightIsNext || this.right == null)
        return this.right; 
      return this.right.min();
    }
    
    AVLNode<E> previous() {
      if (this.leftIsPrevious || this.left == null)
        return this.left; 
      return this.left.max();
    }
    
    AVLNode<E> insert(int index, E obj) {
      int indexRelativeToMe = index - this.relativePosition;
      if (indexRelativeToMe <= 0)
        return insertOnLeft(indexRelativeToMe, obj); 
      return insertOnRight(indexRelativeToMe, obj);
    }
    
    private AVLNode<E> insertOnLeft(int indexRelativeToMe, E obj) {
      if (getLeftSubTree() == null) {
        setLeft(new AVLNode(-1, obj, this, this.left), null);
      } else {
        setLeft(this.left.insert(indexRelativeToMe, obj), null);
      } 
      if (this.relativePosition >= 0)
        this.relativePosition++; 
      AVLNode<E> ret = balance();
      recalcHeight();
      return ret;
    }
    
    private AVLNode<E> insertOnRight(int indexRelativeToMe, E obj) {
      if (getRightSubTree() == null) {
        setRight(new AVLNode(1, obj, this.right, this), null);
      } else {
        setRight(this.right.insert(indexRelativeToMe, obj), null);
      } 
      if (this.relativePosition < 0)
        this.relativePosition--; 
      AVLNode<E> ret = balance();
      recalcHeight();
      return ret;
    }
    
    private AVLNode<E> getLeftSubTree() {
      return this.leftIsPrevious ? null : this.left;
    }
    
    private AVLNode<E> getRightSubTree() {
      return this.rightIsNext ? null : this.right;
    }
    
    private AVLNode<E> max() {
      return (getRightSubTree() == null) ? this : this.right.max();
    }
    
    private AVLNode<E> min() {
      return (getLeftSubTree() == null) ? this : this.left.min();
    }
    
    AVLNode<E> remove(int index) {
      int indexRelativeToMe = index - this.relativePosition;
      if (indexRelativeToMe == 0)
        return removeSelf(); 
      if (indexRelativeToMe > 0) {
        setRight(this.right.remove(indexRelativeToMe), this.right.right);
        if (this.relativePosition < 0)
          this.relativePosition++; 
      } else {
        setLeft(this.left.remove(indexRelativeToMe), this.left.left);
        if (this.relativePosition > 0)
          this.relativePosition--; 
      } 
      recalcHeight();
      return balance();
    }
    
    private AVLNode<E> removeMax() {
      if (getRightSubTree() == null)
        return removeSelf(); 
      setRight(this.right.removeMax(), this.right.right);
      if (this.relativePosition < 0)
        this.relativePosition++; 
      recalcHeight();
      return balance();
    }
    
    private AVLNode<E> removeMin() {
      if (getLeftSubTree() == null)
        return removeSelf(); 
      setLeft(this.left.removeMin(), this.left.left);
      if (this.relativePosition > 0)
        this.relativePosition--; 
      recalcHeight();
      return balance();
    }
    
    private AVLNode<E> removeSelf() {
      if (getRightSubTree() == null && getLeftSubTree() == null)
        return null; 
      if (getRightSubTree() == null) {
        if (this.relativePosition > 0)
          this.left.relativePosition += this.relativePosition + ((this.relativePosition > 0) ? 0 : 1); 
        this.left.max().setRight(null, this.right);
        return this.left;
      } 
      if (getLeftSubTree() == null) {
        this.right.relativePosition += this.relativePosition - ((this.relativePosition < 0) ? 0 : 1);
        this.right.min().setLeft(null, this.left);
        return this.right;
      } 
      if (heightRightMinusLeft() > 0) {
        AVLNode<E> rightMin = this.right.min();
        this.value = rightMin.value;
        if (this.leftIsPrevious)
          this.left = rightMin.left; 
        this.right = this.right.removeMin();
        if (this.relativePosition < 0)
          this.relativePosition++; 
      } else {
        AVLNode<E> leftMax = this.left.max();
        this.value = leftMax.value;
        if (this.rightIsNext)
          this.right = leftMax.right; 
        AVLNode<E> leftPrevious = this.left.left;
        this.left = this.left.removeMax();
        if (this.left == null) {
          this.left = leftPrevious;
          this.leftIsPrevious = true;
        } 
        if (this.relativePosition > 0)
          this.relativePosition--; 
      } 
      recalcHeight();
      return this;
    }
    
    private AVLNode<E> balance() {
      switch (heightRightMinusLeft()) {
        case -1:
        case 0:
        case 1:
          return this;
        case -2:
          if (this.left.heightRightMinusLeft() > 0)
            setLeft(this.left.rotateLeft(), null); 
          return rotateRight();
        case 2:
          if (this.right.heightRightMinusLeft() < 0)
            setRight(this.right.rotateRight(), null); 
          return rotateLeft();
      } 
      throw new RuntimeException("tree inconsistent!");
    }
    
    private int getOffset(AVLNode<E> node) {
      if (node == null)
        return 0; 
      return node.relativePosition;
    }
    
    private int setOffset(AVLNode<E> node, int newOffest) {
      if (node == null)
        return 0; 
      int oldOffset = getOffset(node);
      node.relativePosition = newOffest;
      return oldOffset;
    }
    
    private void recalcHeight() {
      this.height = Math.max((getLeftSubTree() == null) ? -1 : (getLeftSubTree()).height, (getRightSubTree() == null) ? -1 : (getRightSubTree()).height) + 1;
    }
    
    private int getHeight(AVLNode<E> node) {
      return (node == null) ? -1 : node.height;
    }
    
    private int heightRightMinusLeft() {
      return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
    }
    
    private AVLNode<E> rotateLeft() {
      AVLNode<E> newTop = this.right;
      AVLNode<E> movedNode = getRightSubTree().getLeftSubTree();
      int newTopPosition = this.relativePosition + getOffset(newTop);
      int myNewPosition = -newTop.relativePosition;
      int movedPosition = getOffset(newTop) + getOffset(movedNode);
      setRight(movedNode, newTop);
      newTop.setLeft(this, null);
      setOffset(newTop, newTopPosition);
      setOffset(this, myNewPosition);
      setOffset(movedNode, movedPosition);
      return newTop;
    }
    
    private AVLNode<E> rotateRight() {
      AVLNode<E> newTop = this.left;
      AVLNode<E> movedNode = getLeftSubTree().getRightSubTree();
      int newTopPosition = this.relativePosition + getOffset(newTop);
      int myNewPosition = -newTop.relativePosition;
      int movedPosition = getOffset(newTop) + getOffset(movedNode);
      setLeft(movedNode, newTop);
      newTop.setRight(this, null);
      setOffset(newTop, newTopPosition);
      setOffset(this, myNewPosition);
      setOffset(movedNode, movedPosition);
      return newTop;
    }
    
    private void setLeft(AVLNode<E> node, AVLNode<E> previous) {
      this.leftIsPrevious = (node == null);
      this.left = this.leftIsPrevious ? previous : node;
      recalcHeight();
    }
    
    private void setRight(AVLNode<E> node, AVLNode<E> next) {
      this.rightIsNext = (node == null);
      this.right = this.rightIsNext ? next : node;
      recalcHeight();
    }
    
    private AVLNode<E> addAll(AVLNode<E> otherTree, int currentSize) {
      AVLNode<E> maxNode = max();
      AVLNode<E> otherTreeMin = otherTree.min();
      if (otherTree.height > this.height) {
        AVLNode<E> leftSubTree = removeMax();
        ArrayStack<AVLNode<E>> arrayStack = new ArrayStack();
        AVLNode<E> aVLNode1 = otherTree;
        int i = aVLNode1.relativePosition + currentSize;
        int j = 0;
        while (aVLNode1 != null && aVLNode1.height > getHeight(leftSubTree)) {
          j = i;
          arrayStack.push(aVLNode1);
          aVLNode1 = aVLNode1.left;
          if (aVLNode1 != null)
            i += aVLNode1.relativePosition; 
        } 
        maxNode.setLeft(leftSubTree, null);
        maxNode.setRight(aVLNode1, otherTreeMin);
        if (leftSubTree != null) {
          leftSubTree.max().setRight(null, maxNode);
          leftSubTree.relativePosition -= currentSize - 1;
        } 
        if (aVLNode1 != null) {
          aVLNode1.min().setLeft(null, maxNode);
          aVLNode1.relativePosition = i - currentSize + 1;
        } 
        maxNode.relativePosition = currentSize - 1 - j;
        otherTree.relativePosition += currentSize;
        aVLNode1 = maxNode;
        while (!arrayStack.isEmpty()) {
          AVLNode<E> sAncestor = (AVLNode<E>)arrayStack.pop();
          sAncestor.setLeft(aVLNode1, null);
          aVLNode1 = sAncestor.balance();
        } 
        return aVLNode1;
      } 
      otherTree = otherTree.removeMin();
      ArrayStack<AVLNode<E>> sAncestors = new ArrayStack();
      AVLNode<E> s = this;
      int sAbsolutePosition = s.relativePosition;
      int sParentAbsolutePosition = 0;
      while (s != null && s.height > getHeight(otherTree)) {
        sParentAbsolutePosition = sAbsolutePosition;
        sAncestors.push(s);
        s = s.right;
        if (s != null)
          sAbsolutePosition += s.relativePosition; 
      } 
      otherTreeMin.setRight(otherTree, null);
      otherTreeMin.setLeft(s, maxNode);
      if (otherTree != null) {
        otherTree.min().setLeft(null, otherTreeMin);
        otherTree.relativePosition++;
      } 
      if (s != null) {
        s.max().setRight(null, otherTreeMin);
        s.relativePosition = sAbsolutePosition - currentSize;
      } 
      otherTreeMin.relativePosition = currentSize - sParentAbsolutePosition;
      s = otherTreeMin;
      while (!sAncestors.isEmpty()) {
        AVLNode<E> sAncestor = (AVLNode<E>)sAncestors.pop();
        sAncestor.setRight(s, null);
        s = sAncestor.balance();
      } 
      return s;
    }
    
    public String toString() {
      return "AVLNode(" + this.relativePosition + ',' + ((this.left != null) ? 1 : 0) + ',' + this.value + ',' + ((getRightSubTree() != null) ? 1 : 0) + ", faedelung " + this.rightIsNext + " )";
    }
  }
  
  static class TreeListIterator<E> implements ListIterator<E>, OrderedIterator<E> {
    private final TreeList<E> parent;
    
    private TreeList.AVLNode<E> next;
    
    private int nextIndex;
    
    private TreeList.AVLNode<E> current;
    
    private int currentIndex;
    
    private int expectedModCount;
    
    protected TreeListIterator(TreeList<E> parent, int fromIndex) throws IndexOutOfBoundsException {
      this.parent = parent;
      this.expectedModCount = parent.modCount;
      this.next = (parent.root == null) ? null : parent.root.get(fromIndex);
      this.nextIndex = fromIndex;
      this.currentIndex = -1;
    }
    
    protected void checkModCount() {
      if (this.parent.modCount != this.expectedModCount)
        throw new ConcurrentModificationException(); 
    }
    
    public boolean hasNext() {
      return (this.nextIndex < this.parent.size());
    }
    
    public E next() {
      checkModCount();
      if (!hasNext())
        throw new NoSuchElementException("No element at index " + this.nextIndex + "."); 
      if (this.next == null)
        this.next = this.parent.root.get(this.nextIndex); 
      E value = this.next.getValue();
      this.current = this.next;
      this.currentIndex = this.nextIndex++;
      this.next = this.next.next();
      return value;
    }
    
    public boolean hasPrevious() {
      return (this.nextIndex > 0);
    }
    
    public E previous() {
      checkModCount();
      if (!hasPrevious())
        throw new NoSuchElementException("Already at start of list."); 
      if (this.next == null) {
        this.next = this.parent.root.get(this.nextIndex - 1);
      } else {
        this.next = this.next.previous();
      } 
      E value = this.next.getValue();
      this.current = this.next;
      this.currentIndex = --this.nextIndex;
      return value;
    }
    
    public int nextIndex() {
      return this.nextIndex;
    }
    
    public int previousIndex() {
      return nextIndex() - 1;
    }
    
    public void remove() {
      checkModCount();
      if (this.currentIndex == -1)
        throw new IllegalStateException(); 
      this.parent.remove(this.currentIndex);
      if (this.nextIndex != this.currentIndex)
        this.nextIndex--; 
      this.next = null;
      this.current = null;
      this.currentIndex = -1;
      this.expectedModCount++;
    }
    
    public void set(E obj) {
      checkModCount();
      if (this.current == null)
        throw new IllegalStateException(); 
      this.current.setValue(obj);
    }
    
    public void add(E obj) {
      checkModCount();
      this.parent.add(this.nextIndex, obj);
      this.current = null;
      this.currentIndex = -1;
      this.nextIndex++;
      this.expectedModCount++;
    }
  }
}
