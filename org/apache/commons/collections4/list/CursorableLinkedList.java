package org.apache.commons.collections4.list;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class CursorableLinkedList<E> extends AbstractLinkedList<E> implements Serializable {
  private static final long serialVersionUID = 8836393098519411393L;
  
  private transient List<WeakReference<Cursor<E>>> cursors;
  
  public CursorableLinkedList() {
    init();
  }
  
  public CursorableLinkedList(Collection<? extends E> coll) {
    super(coll);
  }
  
  protected void init() {
    super.init();
    this.cursors = new ArrayList<WeakReference<Cursor<E>>>();
  }
  
  public Iterator<E> iterator() {
    return super.listIterator(0);
  }
  
  public ListIterator<E> listIterator() {
    return cursor(0);
  }
  
  public ListIterator<E> listIterator(int fromIndex) {
    return cursor(fromIndex);
  }
  
  public Cursor<E> cursor() {
    return cursor(0);
  }
  
  public Cursor<E> cursor(int fromIndex) {
    Cursor<E> cursor = new Cursor<E>(this, fromIndex);
    registerCursor(cursor);
    return cursor;
  }
  
  protected void updateNode(AbstractLinkedList.Node<E> node, E value) {
    super.updateNode(node, value);
    broadcastNodeChanged(node);
  }
  
  protected void addNode(AbstractLinkedList.Node<E> nodeToInsert, AbstractLinkedList.Node<E> insertBeforeNode) {
    super.addNode(nodeToInsert, insertBeforeNode);
    broadcastNodeInserted(nodeToInsert);
  }
  
  protected void removeNode(AbstractLinkedList.Node<E> node) {
    super.removeNode(node);
    broadcastNodeRemoved(node);
  }
  
  protected void removeAllNodes() {
    if (size() > 0) {
      Iterator<E> it = iterator();
      while (it.hasNext()) {
        it.next();
        it.remove();
      } 
    } 
  }
  
  protected void registerCursor(Cursor<E> cursor) {
    for (Iterator<WeakReference<Cursor<E>>> it = this.cursors.iterator(); it.hasNext(); ) {
      WeakReference<Cursor<E>> ref = it.next();
      if (ref.get() == null)
        it.remove(); 
    } 
    this.cursors.add(new WeakReference<Cursor<E>>(cursor));
  }
  
  protected void unregisterCursor(Cursor<E> cursor) {
    for (Iterator<WeakReference<Cursor<E>>> it = this.cursors.iterator(); it.hasNext(); ) {
      WeakReference<Cursor<E>> ref = it.next();
      Cursor<E> cur = ref.get();
      if (cur == null) {
        it.remove();
        continue;
      } 
      if (cur == cursor) {
        ref.clear();
        it.remove();
        break;
      } 
    } 
  }
  
  protected void broadcastNodeChanged(AbstractLinkedList.Node<E> node) {
    Iterator<WeakReference<Cursor<E>>> it = this.cursors.iterator();
    while (it.hasNext()) {
      WeakReference<Cursor<E>> ref = it.next();
      Cursor<E> cursor = ref.get();
      if (cursor == null) {
        it.remove();
        continue;
      } 
      cursor.nodeChanged(node);
    } 
  }
  
  protected void broadcastNodeRemoved(AbstractLinkedList.Node<E> node) {
    Iterator<WeakReference<Cursor<E>>> it = this.cursors.iterator();
    while (it.hasNext()) {
      WeakReference<Cursor<E>> ref = it.next();
      Cursor<E> cursor = ref.get();
      if (cursor == null) {
        it.remove();
        continue;
      } 
      cursor.nodeRemoved(node);
    } 
  }
  
  protected void broadcastNodeInserted(AbstractLinkedList.Node<E> node) {
    Iterator<WeakReference<Cursor<E>>> it = this.cursors.iterator();
    while (it.hasNext()) {
      WeakReference<Cursor<E>> ref = it.next();
      Cursor<E> cursor = ref.get();
      if (cursor == null) {
        it.remove();
        continue;
      } 
      cursor.nodeInserted(node);
    } 
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    doWriteObject(out);
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    doReadObject(in);
  }
  
  protected ListIterator<E> createSubListListIterator(AbstractLinkedList.LinkedSubList<E> subList, int fromIndex) {
    SubCursor<E> cursor = new SubCursor<E>(subList, fromIndex);
    registerCursor(cursor);
    return cursor;
  }
  
  public static class Cursor<E> extends AbstractLinkedList.LinkedListIterator<E> {
    boolean valid = true;
    
    boolean nextIndexValid = true;
    
    boolean currentRemovedByAnother = false;
    
    protected Cursor(CursorableLinkedList<E> parent, int index) {
      super(parent, index);
      this.valid = true;
    }
    
    public void remove() {
      if (this.current != null || !this.currentRemovedByAnother) {
        checkModCount();
        this.parent.removeNode(getLastNodeReturned());
      } 
      this.currentRemovedByAnother = false;
    }
    
    public void add(E obj) {
      super.add(obj);
      this.next = this.next.next;
    }
    
    public int nextIndex() {
      if (!this.nextIndexValid) {
        if (this.next == this.parent.header) {
          this.nextIndex = this.parent.size();
        } else {
          int pos = 0;
          AbstractLinkedList.Node<E> temp = this.parent.header.next;
          while (temp != this.next) {
            pos++;
            temp = temp.next;
          } 
          this.nextIndex = pos;
        } 
        this.nextIndexValid = true;
      } 
      return this.nextIndex;
    }
    
    protected void nodeChanged(AbstractLinkedList.Node<E> node) {}
    
    protected void nodeRemoved(AbstractLinkedList.Node<E> node) {
      if (node == this.next && node == this.current) {
        this.next = node.next;
        this.current = null;
        this.currentRemovedByAnother = true;
      } else if (node == this.next) {
        this.next = node.next;
        this.currentRemovedByAnother = false;
      } else if (node == this.current) {
        this.current = null;
        this.currentRemovedByAnother = true;
        this.nextIndex--;
      } else {
        this.nextIndexValid = false;
        this.currentRemovedByAnother = false;
      } 
    }
    
    protected void nodeInserted(AbstractLinkedList.Node<E> node) {
      if (node.previous == this.current) {
        this.next = node;
      } else if (this.next.previous == node) {
        this.next = node;
      } else {
        this.nextIndexValid = false;
      } 
    }
    
    protected void checkModCount() {
      if (!this.valid)
        throw new ConcurrentModificationException("Cursor closed"); 
    }
    
    public void close() {
      if (this.valid) {
        ((CursorableLinkedList<E>)this.parent).unregisterCursor(this);
        this.valid = false;
      } 
    }
  }
  
  protected static class SubCursor<E> extends Cursor<E> {
    protected final AbstractLinkedList.LinkedSubList<E> sub;
    
    protected SubCursor(AbstractLinkedList.LinkedSubList<E> sub, int index) {
      super((CursorableLinkedList<E>)sub.parent, index + sub.offset);
      this.sub = sub;
    }
    
    public boolean hasNext() {
      return (nextIndex() < this.sub.size);
    }
    
    public boolean hasPrevious() {
      return (previousIndex() >= 0);
    }
    
    public int nextIndex() {
      return super.nextIndex() - this.sub.offset;
    }
    
    public void add(E obj) {
      super.add(obj);
      this.sub.expectedModCount = this.parent.modCount;
      this.sub.size++;
    }
    
    public void remove() {
      super.remove();
      this.sub.expectedModCount = this.parent.modCount;
      this.sub.size--;
    }
  }
}
