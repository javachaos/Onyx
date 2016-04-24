package com.onyx.common.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentStack<E> {
  AtomicReference<Node<E>> head = new AtomicReference<Node<E>>();
  AtomicInteger size = new AtomicInteger();

  /**
   * Push a value onto this stack.
   * 
   * @param item
   *    the item to be pushed on this stack.
   */
  public void push(final E item) {
    size.incrementAndGet();
    final Node<E> newHead = new Node<E>(item);
    Node<E> oldHead;
    do {
      oldHead = head.get();
      newHead.next = oldHead;
    } while (!head.compareAndSet(oldHead, newHead));
  }

  /**
   * Remove and return the top value of this stack.
   * @return
   *    the last value at the top of this stack.
   */
  public E pop() {
    size.decrementAndGet();
    Node<E> oldHead;
    Node<E> newHead;
    do {
      oldHead = head.get();
      if (oldHead == null) {
        return null;
      }
      newHead = oldHead.next;
    } while (!head.compareAndSet(oldHead, newHead));
    return oldHead.item;
  }

  public int size() {
    return size.get();
  }

  static class Node<E> {
    final E item;
    Node<E> next;

    public Node(final E item) {
      this.item = item;
    }
  }

  public void clear() {
    head = new AtomicReference<Node<E>>();
  }

  /**
   * View the top of this stack.
   * @return
   *    the top element of this stack.
   */
  public E peek() {
    Node<E> top = head.get();
    if (top == null) {
      return null;
    }
    return top.item;
  }

  /**
   * Return true if this stack is empty.
   * @return
   *    true if this stack is empty.
   */
  public boolean isEmpty() {
    return size() == 0;
  }
}
