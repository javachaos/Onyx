package com.onyx.quadcopter.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentStack<E> {
    AtomicReference<Node<E>> head = new AtomicReference<Node<E>>();
    AtomicInteger size = new AtomicInteger();
    
    public void push(final E item) {
	size.incrementAndGet();
	final Node<E> newHead = new Node<E>(item);
	Node<E> oldHead;
	do {
	    oldHead = head.get();
	    newHead.next = oldHead;
	} while (!head.compareAndSet(oldHead, newHead));
    }

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

    public E peek() {
	Node<E> top = head.get();
	if (top == null)
	    return null;
	return top.item;
    }
}
