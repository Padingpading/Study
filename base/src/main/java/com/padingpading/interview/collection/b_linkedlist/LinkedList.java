/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.padingpading.interview.collection.b_linkedlist;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 *特点:有序,可以重复,可以为null。
 *性能特点:增删快,不支持随机读写。
 *List:有序,可以重复,
 *Deque:双端队列 同事具备队列和栈
 *Serializable:支持序列化
 */
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
    //LinkedList 要保存链表的第一个节点和最后一个节点是因为，我们都知道，
    // 链表数据结构相对于数组结构，有点在于增删，缺点在于查找。
    // 如果我们保存了LinkedList 的头尾两端，当我们需要以索引来查找节点的时候，
    // 我们可以根据 index 和 size/2 的大小,来决定从头查找还是从尾部查找，
    // 这也算是一定程度上弥补了单链表数据结构的缺点。

   /*=================================成员变量=======================*/
    transient int size = 0;

    transient Node<E> first;

    transient Node<E> last;
    
    /*=================================构造方法=======================*/
    
    //空参数的构造由于生成一个空链表 first = last = null
    public LinkedList() {
    }
    
    /**
     * 传入一个集合类，来构造一个具有一定元素的 LinkedList 集合
     * list set 元素
     * @param  c  其内部的元素将按顺序作为 LinkedList 节点
     * @throws NullPointerException 如果 参数 collection 为空将抛出空指针异常
     */
    public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }
    
    /*=================================方法=======================*/

    /**
     * 添加一个元素在链表的头节点位置
     */
    private void linkFirst(E e) {
        // 添加元素之前的头节点
        final Node<E> f = first;
        //以添加的元素为节点值构建新的头节点 并将 next 指针指向 之前的头节点
        final Node<E> newNode = new Node<>(null, e, f);
        // first 索引指向将新的节点
        first = newNode;
        // 如果添加之前链表空则新的节点也作为未节点
        if (f == null)
            last = newNode;
        else
            f.prev = newNode;
        //否则之前头节点的 prev 指针指向新节点
        size++;
        //操作数++
        modCount++;
    }

    /**
     * 在链表末尾添加一个节点
     */
    void linkLast(E e) {
        final Node<E> l = last;
        //构建新的未节点，并将新节点 prev 指针指向 之前的未节点
        final Node<E> newNode = new Node<>(l, e, null);
        //last 索引指向末节点
        last = newNode;
        //如果之前链表为空则新节点也作为头节点
        if (l == null)
            first = newNode;
        else
            //否则将之前的未节点的 next 指针指向新节点
            l.next = newNode;
        size++;
        //操作数++
        modCount++;
    }

    /**
     * 在节点succ前插入。
     */
    void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
        // 由于 succ 一定不为空，所以可以直接获取 prev 节点
        final Node<E> pred = succ.prev;
        // 新节点 prev 节点为 pred，next 节点为 succ
        final Node<E> newNode = new Node<>(pred, e, succ);
        // 原节点的 prev 指向新节点
        succ.prev = newNode;
        // 如果 pred 为空即头节点出插入了一个节点，则将新的节点赋值给 first 索引
        if (pred == null)
            first = newNode;
        else
            //否则 pred 的下一个节点改为新节点
            pred.next = newNode;
        size++;
        modCount++;
    }

    /**
     * 移除头结点
     */
    private E unlinkFirst(Node<E> f) {
        // 头节点的 element 这里作为返回值使用
        final E element = f.item;
        // 头节点下个节点
        final Node<E> next = f.next;
        // 释放头节点的 next 指针，和 element 下次 gc 的时候回收这个内部类
        f.item = null;
        f.next = null; // help GC
        // 将 first 索引指向新的节点
        first = next;
        // 如果 next 节点为空，即链表只有一个节点的时候，last 指向 null
        if (next == null)
            last = null;
        else
            //否则 next 的 prev 指针指向 null
            next.prev = null;
        //改变链表长度
        size--;
        //修改操作数
        modCount++;
        //返回删除节点的值
        return element;
    }

    /**
     *  移除未节点
     */
    private E unlinkLast(Node<E> l) {
        // assert l == last && l != null;
        final E element = l.item;
        //未节点的前一个节点，
        final Node<E> prev = l.prev;
        //释放未节点的内容
        l.item = null;
        l.prev = null; // help GC
        //将 last 索引指向新的未节点
        last = prev;
        // 链表只有一个节点的时候，first 指向 null
        if (prev == null)
            first = null;
        else
            prev.next = null;
        size--;
        modCount++;
        return element;
    }

    /**
     *
     */
    E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;
        //保存 index 节点的前后两个节点
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;
        
        // 如果节点为头节点，则做 unlinkFirst 相同操作
        if (prev == null) {
            first = next;
        } else {
            //否则将上一个节点的 next 指针指向下个节点
            prev.next = next;
            x.prev = null;
        }
        // 如果节点为尾节点，则将 last 索引指向上个节点
        if (next == null) {
            last = prev;
        } else {
            //否则下个节点 prev 指针指向上个节点
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        modCount++;
        return element;
    }

    /**
     *  返回 first 索引指向的节点的内容
     *  双端队列:从尾部获取元素,抛出异常
     */
    public E getFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return f.item;
    }

    /**
     * 返回 last 索引指向的节点的内容
     */
    public E getLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }

    /**
     * 移除头节点
     * 队列:尾部移除元素,抛异常
     */
    public E removeFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return unlinkFirst(f);
    }

    /**
     * 删除尾节点
     */
    public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }

    /**
     * 添加到链表头
     */
    public void addFirst(E e) {
        linkFirst(e);
    }

    /**
     * 添加到链表尾部
     * 链表:添加元素到头部·
     */
    public void addLast(E e) {
        linkLast(e);
    }

    /**
     * 判断你元素是否存在
     */
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**添加元素
     * 队列:添加元素到尾部
     */
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    /**
     * 删除节点
     */
    public boolean remove(Object o) {
        // 区别对待 null 元素，比较元素时候使用 == 而不是 equals
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            //遍历链表
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }
    
    
    /**
     * 添加Collection元素
     */
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }
    
    
    /**
     * 指定位置添加集合
     * 1、获取指定位置元素
     * 2、获取前一个节点 +  新插入节点s + 指定位置节点 + 原有的节点。
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        // 查看索引是否满足 0 =< index =< size 的要求
        checkPositionIndex(index);
        
        // 调用对应 Collection 实现类的 toArray 方法将集合转为数组
        Object[] a = c.toArray();
        //检查数组长度，如果为 0 则直接返回 false 表示没有添加任何元素
        int numNew = a.length;
        if (numNew == 0)
            return false;
        
        // 保存 index 当前的节点为 succ，
        // 当前节点的上一个节点为 pred
        Node<E> pred, succ;
        if (index == size) {
            // 如果 index = size 表示在链表尾部插入
            succ = null;
            pred = last;
        } else {
            //获取index节点
            succ = node(index);
            //index节点的上一个及诶单
            pred = succ.prev;
        }
        
        // 遍历数组将对应的元素包装成节点添加到链表中
        for (Object o : a) {
            @SuppressWarnings("unchecked") E e = (E) o;
            Node<E> newNode = new Node<>(pred, e, null);
            //如果 pred 为空表示 LinkedList 集合中还没有元素
            //生成的第一个节点将作为头节点 赋值给 first 成员变量
            if (pred == null)
                first = newNode;
            else
                pred.next = newNode;
            //新节点作为上一个节点
            pred = newNode;
        }
        // 如果 index 位置的元素为 null 则遍历数组后 pred 所指向的节点即为新链表的末节点，赋值给 last 成员变量
        if (succ == null) {
            last = pred;
        } else {
            // 否则将 pred 的 next 索引指向 succ ，succ 的 prev 索引指向 pred
            pred.next = succ;
            succ.prev = pred;
        }
        // 更新当前链表的长度 size 并返回 true 表示添加成功
        size += numNew;
        modCount++;
        return true;
    }

    /**
     * 清除所有元素
     */
    public void clear() {
        // 依次清除节点，帮助释放内存空间
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        first = last = null;
        size = 0;
        modCount++;
    }


    // Positional Access Operations

    /**
     * 获取指定位置元素
     */
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    /**
     *
     */
    public E set(int index, E element) {
        // 判断角标是否越界
        checkElementIndex(index);
        // 采用 node 方法查找对应索引的节点
        Node<E> x = node(index);
        //保存节点原有的内容值
        E oldVal = x.item;
        // 设置新值
        x.item = element;
        // 返回旧的值
        return oldVal;
    }

    /**
     * 在指定 index 位置插入节点
     */
    public void add(int index, E element) {
        // 检查角标是否越界
        checkPositionIndex(index);
        if (index == size)
            //直接插入尾部
            linkLast(element);
        else
            //插入到指定节点后面
            linkBefore(element, node(index));
    }

    /**
     * 删除指定位置元素
     */
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    /**
     * Tells if the argument is the index of an existing element.
     */
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    /**
     * Tells if the argument is the index of a valid position for an
     * iterator or an add operation.
     */
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 获取索引位置节点
     */
    Node<E> node(int index) {
        // 如果 index < size/2 则从0开始寻找指定角标的节点
        //移位除2
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            // 如果 index >= size/2 则从 size-1 开始寻找指定角标的节点
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    // Search Operations

    /**
     * 返回参数元素在链表的节点索引，如果有重复元素，那么返回值为从**头节点**起的第一相同的元素节点索引，
     *  如果没有值为该元素的节点，则返回 -1；
     */
    public int indexOf(Object o) {
        int index = 0;
        // 区别对待 null 元素，用 == 判断，非空元素用 equels 方法判断
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null)
                    return index;
                index++;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item))
                    return index;
                index++;
            }
        }
        return -1;
    }

    /**
     * *返回参数元素在链表的节点索引，如果有重复元素，那么返回值为从**尾节点起**的第一相同的元素节点索引，
     *  如果没有值为该元素的节点，则返回 -1；
     */
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (x.item == null)
                    return index;
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item))
                    return index;
            }
        }
        return -1;
    }

    // Queue operations.

    /**
     * 队列:从头部获取元素,返回null
     *
     */
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     *
     * @return the head of this list
     * @throws NoSuchElementException if this list is empty
     * @since 1.5
     */
    public E element() {
        return getFirst();
    }

    /**
     * 队列:从头移出队列,返回null
     */
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     * 队列:从头部移除队列,抛异常
     */
    public E remove() {
        return removeFirst();
    }

    /**
     * 队列:添加元素到尾部
     */
    public boolean offer(E e) {
        return add(e);
    }

    // Deque operations
    /**
     * Inserts the specified element at the front of this list.
     *
     * @param e the element to insert
     * @return {@code true} (as specified by {@link Deque#offerFirst})
     * @since 1.6
     */
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * 队列:添加元素到头部
     */
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    /**
     * 双端队列:从尾部回去元素
     */
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
     }

    /**
     * Retrieves, but does not remove, the last element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the last element of this list, or {@code null}
     *         if this list is empty
     * @since 1.6
     */
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

    /**
     * 双端队列:从尾部移除元素,返回null
     */
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     * Retrieves and removes the last element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the last element of this list, or {@code null} if
     *     this list is empty
     * @since 1.6
     */
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }

    /**
     * Pushes an element onto the stack represented by this list.  In other
     * words, inserts the element at the front of this list.
     *
     * <p>This method is equivalent to {@link #addFirst}.
     *
     * @param e the element to push
     * @since 1.6
     */
    public void push(E e) {
        addFirst(e);
    }

    /**
     * Pops an element from the stack represented by this list.  In other
     * words, removes and returns the first element of this list.
     *
     * <p>This method is equivalent to {@link #removeFirst()}.
     *
     * @return the element at the front of this list (which is the top
     *         of the stack represented by this list)
     * @throws NoSuchElementException if this list is empty
     * @since 1.6
     */
    public E pop() {
        return removeFirst();
    }

    /**
     * Removes the first occurrence of the specified element in this
     * list (when traversing the list from head to tail).  If the list
     * does not contain the element, it is unchanged.
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if the list contained the specified element
     * @since 1.6
     */
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    /**
     * Removes the last occurrence of the specified element in this
     * list (when traversing the list from head to tail).  If the list
     * does not contain the element, it is unchanged.
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if the list contained the specified element
     * @since 1.6
     */
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a list-iterator of the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * Obeys the general contract of {@code List.listIterator(int)}.<p>
     *
     * The list-iterator is <i>fail-fast</i>: if the list is structurally
     * modified at any time after the Iterator is created, in any way except
     * through the list-iterator's own {@code remove} or {@code add}
     * methods, the list-iterator will throw a
     * {@code ConcurrentModificationException}.  Thus, in the face of
     * concurrent modification, the iterator fails quickly and cleanly, rather
     * than risking arbitrary, non-deterministic behavior at an undetermined
     * time in the future.
     *
     * @param index index of the first element to be returned from the
     *              list-iterator (by a call to {@code next})
     * @return a ListIterator of the elements in this list (in proper
     *         sequence), starting at the specified position in the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see List#listIterator(int)
     */
    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }

    private class ListItr implements ListIterator<E> {
        // 上一个遍历的节点
        private Node<E> lastReturned;
        // 下一次遍历返回的节点
        private Node<E> next;
        // cursor 指针下一次遍历返回的节点
        private int nextIndex;
        // 期望的操作数
        private int expectedModCount = modCount;
        
        // 根据参数 index 确定生成的迭代器 cursor 的位置
        ListItr(int index) {
            // assert isPositionIndex(index);
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }
        
        // 判断指针是否还可以移动
        public boolean hasNext() {
            return nextIndex < size;
        }
        
        // 返回下一个带遍历的元素
        public E next() {
            // 检查操作数是否合法
            checkForComodification();
            // 如果 hasNext 返回 false 抛出异常，所以我们在调用 next 前应先调用 hasNext 检查
            if (!hasNext())
                throw new NoSuchElementException();
            // 移动 lastReturned 指针
            lastReturned = next;
            // 移动 next 指针
            next = next.next;
            // 移动 nextIndex cursor
            nextIndex++;
            // 返回移动后 lastReturned
            return lastReturned.item;
        }
        
        // 当前游标位置是否还有前一个元素
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public E previous() {
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();

            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }
        
        // 删除链表当前节点也就是调用 next/previous 返回的这节点，也就 lastReturned
        public void remove() {
            checkForComodification();
            if (lastReturned == null)
                throw new IllegalStateException();

            Node<E> lastNext = lastReturned.next;
            unlink(lastReturned);
            if (next == lastReturned)
                next = lastNext;
            else
                nextIndex--;
            lastReturned = null;
            expectedModCount++;
        }
        // 设置当前遍历的节点的值
        public void set(E e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.item = e;
        }
        
        //简单哈操作数是否合法
        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            if (next == null)
                linkLast(e);
            else
                linkBefore(e, next);
            nextIndex++;
            expectedModCount++;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private static class Node<E> {
        // 当前节点的元素值
        E item;
        // 下一个节点的索引
        Node<E> next;
        // 上一个节点的索引
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    /**
     * @since 1.6
     */
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    /**
     * Adapter to provide descending iterators via ListItr.previous
     */
    private class DescendingIterator implements Iterator<E> {
        private final ListItr itr = new ListItr(size());
        public boolean hasNext() {
            return itr.hasPrevious();
        }
        public E next() {
            return itr.previous();
        }
        public void remove() {
            itr.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedList<E> superClone() {
        try {
            return (LinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * Returns a shallow copy of this {@code LinkedList}. (The elements
     * themselves are not cloned.)
     *
     * @return a shallow copy of this {@code LinkedList} instance
     */
    public Object clone() {
        LinkedList<E> clone = superClone();

        // Put clone into "virgin" state
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;

        // Initialize clone with our elements
        for (Node<E> x = first; x != null; x = x.next)
            clone.add(x.item);

        return clone;
    }

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this list
     *         in proper sequence
     */
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;
        return result;
    }

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence (from first to last element); the runtime type of
     * the returned array is that of the specified array.  If the list fits
     * in the specified array, it is returned therein.  Otherwise, a new
     * array is allocated with the runtime type of the specified array and
     * the size of this list.
     *
     * <p>If the list fits in the specified array with room to spare (i.e.,
     * the array has more elements than the list), the element in the array
     * immediately following the end of the list is set to {@code null}.
     * (This is useful in determining the length of the list <i>only</i> if
     * the caller knows that the list does not contain any null elements.)
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a list known to contain only strings.
     * The following code can be used to dump the list into a newly
     * allocated array of {@code String}:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size);
        int i = 0;
        Object[] result = a;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;

        if (a.length > size)
            a[size] = null;

        return a;
    }

    private static final long serialVersionUID = 876323262645176354L;

    /**
     *
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (Node<E> x = first; x != null; x = x.next)
            s.writeObject(x.item);
    }

    /**
     * Reconstitutes this {@code LinkedList} instance from a stream
     * (that is, deserializes it).
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in size
        int size = s.readInt();

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++)
            linkLast((E)s.readObject());
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * list.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED} and
     * {@link Spliterator#ORDERED}.  Overriding implementations should document
     * the reporting of additional characteristic values.
     *
     * @implNote
     * The {@code Spliterator} additionally reports {@link Spliterator#SUBSIZED}
     * and implements {@code trySplit} to permit limited parallelism..
     *
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new LLSpliterator<E>(this, -1, 0);
    }

    /** A customized variant of Spliterators.IteratorSpliterator */
    static final class LLSpliterator<E> implements Spliterator<E> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final LinkedList<E> list; // null OK unless traversed
        Node<E> current;      // current node; null until initialized
        int est;              // size estimate; -1 until first needed
        int expectedModCount; // initialized when est set
        int batch;            // batch size for splits

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEst() {
            int s; // force initialization
            final LinkedList<E> lst;
            if ((s = est) < 0) {
                if ((lst = list) == null)
                    s = est = 0;
                else {
                    expectedModCount = lst.modCount;
                    current = lst.first;
                    s = est = lst.size;
                }
            }
            return s;
        }

        public long estimateSize() { return (long) getEst(); }

        public Spliterator<E> trySplit() {
            Node<E> p;
            int s = getEst();
            if (s > 1 && (p = current) != null) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j++] = p.item; } while ((p = p.next) != null && j < n);
                current = p;
                batch = j;
                est = s - j;
                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p; int n;
            if (action == null) throw new NullPointerException();
            if ((n = getEst()) > 0 && (p = current) != null) {
                current = null;
                est = 0;
                do {
                    E e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            if (getEst() > 0 && (p = current) != null) {
                --est;
                E e = p.item;
                current = p.next;
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

}
