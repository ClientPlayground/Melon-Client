package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;

public class SpscLinkedAtomicQueue<E> extends BaseLinkedAtomicQueue<E> {
  public SpscLinkedAtomicQueue() {
    LinkedQueueAtomicNode<E> node = newNode();
    spProducerNode(node);
    spConsumerNode(node);
    node.soNext(null);
  }
  
  public boolean offer(E e) {
    if (null == e)
      throw new NullPointerException(); 
    LinkedQueueAtomicNode<E> nextNode = newNode(e);
    lpProducerNode().soNext(nextNode);
    spProducerNode(nextNode);
    return true;
  }
  
  public E poll() {
    return (E)relaxedPoll();
  }
  
  public E peek() {
    return (E)relaxedPeek();
  }
  
  public int fill(MessagePassingQueue.Supplier<E> s) {
    long result = 0L;
    while (true) {
      fill(s, 4096);
      result += 4096L;
      if (result > 2147479551L)
        return (int)result; 
    } 
  }
  
  public int fill(MessagePassingQueue.Supplier<E> s, int limit) {
    if (limit == 0)
      return 0; 
    LinkedQueueAtomicNode<E> tail = newNode((E)s.get());
    LinkedQueueAtomicNode<E> head = tail;
    for (int i = 1; i < limit; i++) {
      LinkedQueueAtomicNode<E> temp = newNode((E)s.get());
      tail.soNext(temp);
      tail = temp;
    } 
    LinkedQueueAtomicNode<E> oldPNode = lpProducerNode();
    oldPNode.soNext(head);
    spProducerNode(tail);
    return limit;
  }
  
  public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
    LinkedQueueAtomicNode<E> chaserNode = this.producerNode;
    while (exit.keepRunning()) {
      for (int i = 0; i < 4096; i++) {
        LinkedQueueAtomicNode<E> nextNode = newNode((E)s.get());
        chaserNode.soNext(nextNode);
        chaserNode = nextNode;
        this.producerNode = chaserNode;
      } 
    } 
  }
}
