///*
// * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// */
//
///*
// *
// *
// *
// *
// *
// * Written by Doug Lea with assistance from members of JCP JSR-166
// * Expert Group and released to the public domain, as explained at
// * http://creativecommons.org/publicdomain/zero/1.0/
// */
//
//package com.padingpading.interview.thread.wangwenjun.blockqueue;
//
//import java.lang.ref.WeakReference;
//import java.util.AbstractQueue;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.NoSuchElementException;
//import java.util.Spliterator;
//import java.util.Spliterators;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.Condition;
//import java.util.concurrent.locks.ReentrantLock;
//
///*
//* ArrayBlockingQueue:基于数组实现的阻塞队列。
//* Queue
//* BlockingQueue:
//* 特点:
//*   1、数组大小确定,不能修改。
//*
//*
//*
//* */
//public class ArrayBlockingQueue<E> extends AbstractQueue<E>
//        implements BlockingQueue<E>, java.io.Serializable {
//
//    /**
//     * Serialization ID. This class relies on default serialization
//     * even for the items array, which is default-serialized, even if
//     * it is empty. Otherwise it could not be declared final, which is
//     * necessary here.
//     */
//    private static final long serialVersionUID = -817911632652898426L;
//
//    /** 一个数组，用来存储放入队列中的元素 */
//    final Object[] items;
//
//    /** 此变量用来记录下一次从队列中拿出的元素，它在数组中的下标，可以理解为队列的头节点 */
//    int takeIndex;
//
//    /** 此变量存储下一次往队列中添加元素时，这个元素在数组中的下标，也就是记录队列尾的上一个位置 */
//    int putIndex;
//
//    /** 记录队列中元素的个数 */
//    int count;
//
//    /** 一个锁，用来保证向队列中插入、删除等操作的线程安全 */
//    final ReentrantLock lock;
//
//    /** 用来在队列为空时阻塞获取元素的线程，也就是用来阻塞消费者，
//     * 这个变量叫notEmpty（不空），可以理解为队列空时将会被阻塞，不空时可以正常运行
//     */
//    private final Condition notEmpty;
//    /** 用来在队列满时阻塞添加元素的线程，也就是用来阻塞生产者
//     * 这个变量叫notFull（不满），可以理解为队列满时将会阻塞，不满时可以正常运行
//     */
//    private final Condition notFull;
//    /** 遍历使用的迭代器 */
//    transient Itrs itrs = null;
//    /*======================================================构造方法===================================================*/
//
//    // 仅仅指定队列容量的构造方法
//    public ArrayBlockingQueue(int capacity) {
//        this(capacity, false);
//    }
//
//    /**
//     * 此构造方法接收两个参数：
//     * 1、capacity：指定阻塞队列的容量
//     * 2、fair：指定创建的ReentrantLock是否是公平锁
//     */
//    public ArrayBlockingQueue(int capacity, boolean fair) {
//        // 容量必须大于0
//        if (capacity <= 0)
//            throw new IllegalArgumentException();
//        // 初始化存储元素的数组
//        this.items = new Object[capacity];
//        // 创建用于线程同步的锁lock，若fair为true，
//        // 则此时创建的将是一个公平锁，反之则是非公平锁
//        lock = new ReentrantLock(fair);
//        // 初始化notEmpty变量，用以阻塞和唤醒消费者线程
//        notEmpty = lock.newCondition();
//        // 初始化notFull变量，用以阻塞生产者线程
//        notFull =  lock.newCondition();
//    }
//
//    /*=======================================================添加=====================================================*/
//    /**PUT
//     * 添加成功:返回
//     * 添加不成功:线程阻塞。
//     * 向队列中添加一个新元素，若队列已经满了，则当前线程被阻塞，等待队列不满时被唤醒；
//     * 当前线程成功添加元素后，将唤醒正在等待的消费者线程（如果有的话），消费者线程则从队列中获取元素。
//     */
//    public void put(E e) throws InterruptedException {
//        // 判断元素是否为null，若为null将抛出异常
//        checkNotNull(e);
//        // 获取锁对象lock
//        final ReentrantLock lock = this.lock;
//        // 调用lock的lockInterruptibly方法加锁，lockInterruptibly可以响应中断
//        // 加锁是为了防止多个线程同时操作队列，造成线程安全问题
//        lock.lockInterruptibly();
//        try {
//            // 如果当前队列中的元素的个数为数组长度，表示队列满了，
//            // 这时调用notFull.await()让当前线程阻塞，也就是让生产者阻塞
//            // 而此处使用while循环而不是if，是考虑到线程被唤醒后，队列可能还是满的
//            // 所以线程被唤醒后，需要再次判断，若依旧是满的，则再次阻塞
//
//            while (count == items.length)
//                //阻塞线程
//                notFull.await();
//            // 调用enqueue方法将元素加入数组中
//            enqueue(e);
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**offer:不会阻塞。
//     * 添加成功:返回true
//     * 添加不成功:返回false。
//     */
//    public boolean offer(E e) {
//        // 判断加入的元素是否为null，若为null将抛出异常
//        checkNotNull(e);
//        // 获取锁对象
//        final ReentrantLock lock = this.lock;
//        // 加锁防止线程安全问题，注意这里调用的是lock()方法，这个方法并不响应中断
//        // 而之前的put方法会响应中断，以为put会阻塞，为了防止它长期阻塞，所以需要响应中断
//        // 但是这个方法并不会被阻塞，所以不需要响应中断
//        lock.lock();
//        try {
//            // 若当前队列已满，则不进行添加，直接返回false，表示添加失败
//            if (count == items.length)
//                return false;
//            else {
//                // 若队列不满，则直接调用enqueue方法添加元素，并返回true
//                enqueue(e);
//                return true;
//            }
//        } finally {
//            lock.unlock();
//        }
//    }
//
//
//    /**
//     * Circularly decrement i.
//     */
//    final int dec(int i) {
//        return ((i == 0) ? items.length : i) - 1;
//    }
//
//    /**
//     * Returns item at index i.
//     */
//    @SuppressWarnings("unchecked")
//    final E itemAt(int i) {
//        return (E) items[i];
//    }
//
//    /**
//     * Throws NullPointerException if argument is null.
//     *
//     * @param v the element
//     */
//    private static void checkNotNull(Object v) {
//        if (v == null)
//            throw new NullPointerException();
//    }
//
//    /**add
//     * 添加成功:返回true。
//     * 添加失败:抛出异常。
//     */
//    public boolean add(E e) {
//        return super.add(e);
//    }
//
//
//    /**此方法将新元素加入到数组中
//     */
//    private void enqueue(E x) {
//        // 获得存储元素的数组
//        final Object[] items = this.items;
//        // 将新元素x放入到数组中，且放入的位置就是putIndex指向的位置
//        items[putIndex] = x;
//        // putIndex加1，如果超过了数组的最大长度，则将其置为0，也就是数组的第一个位置
//        if (++putIndex == items.length)
//            putIndex = 0;
//        // 元素数量+1
//        count++;
//        // 因为我们已经向队列中添加了元素，所以可以唤醒那些需要获取元素的线程，也就是消费者
//        // 之前说过，notEmpty就是用来阻塞和唤醒消费者的
//        notEmpty.signal();
//    }
//    /**此方法用来阻塞式地添加元素，但是需要指定阻塞的超时时间
//     * 1、timeout：需要阻塞时间的数量级，一个long类型的整数；
//     * 2、unit：用以指定时间的单位，比如TimeUnit.SECONDS表示秒，
//     * 若timeout为10，而unit为TimeUnit.SECONDS，则表示最多阻塞10秒
//     */
//    public boolean offer(E e, long timeout, TimeUnit unit)
//            throws InterruptedException {
//        // 判断元素是否为null
//        // 获取线程需要阻塞的时间的纳秒值
//        checkNotNull(e);
//        long nanos = unit.toNanos(timeout);
//        // 获取锁对象
//        final ReentrantLock lock = this.lock;
//        // 加锁，并且lockInterruptibly方法会响应中断
//        lock.lockInterruptibly();
//        try {
//            // 若当前队列中元素已满
//            while (count == items.length) {
//                // 若等待的剩余时间小于0，表示超过了等待时间，则直接返回
//                if (nanos <= 0)
//                    return false;
//                // 让当前线程等待指定的时间，使用notFull对象让线程等待一段时间
//                // 方法会返回剩余的需要等待的时间
//                nanos = notFull.awaitNanos(nanos);
//            }
//            // 调用enqueue方法将元素添加到数组中
//            enqueue(e);
//            // 返回true表示添加成功
//            return true;
//        } finally {
//            lock.unlock();
//        }
//    }
//    /*=====================================================出队=======================================================*/
//
//    /**获得阻塞队列中队头的元素，若队列为空，则当前线程被阻塞，直到有线程向队列中添加了元素，
//     * 获取成功后，将队头元素从队列中删除，然后唤醒一个被阻塞的生产者线程（如果有的话）
//     */
//    public E take() throws InterruptedException {
//        // 获取锁对象
//        final ReentrantLock lock = this.lock;
//        // 使用lock对象加锁，lockInterruptibly方法会响应中断
//        // 目的是防止线程一直在此处阻塞，无法退出
//        lock.lockInterruptibly();
//        try {
//            // 若当前队列中元素为0，则调用notEmpty对象的await()方法，
//            // 让当前获取元素的线程阻塞，也就是阻塞消费者线程，直到被生产者线程唤醒
//            while (count == 0)
//                //唤醒put被阻塞的线程,
//                notEmpty.await();
//            // 调用dequeue方法获取队投元素，并直接返回
//            return dequeue();
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**此方法用来获取队头元素，同时将它从数组中删除
//    *
//    */
//    private E dequeue() {
//        // 获取存储元素的数组
//        final Object[] items = this.items;
//        @SuppressWarnings("unchecked")
//        // takeIndex记录的就是队头元素的下标，使用变量x记录它
//        E x = (E) items[takeIndex];
//        // 将队头元素从数组中删除
//        items[takeIndex] = null;
//        // 队头元素删除后，原队头的下一个元素就成了新的队头，所以takeIndex + 1
//        // 若takeIndex加1后超过数组的范围，则将takeIndex置为0，也就是循环使用数组空间
//        // 为什么是加不是减，因为在数组中，队头在左边，队尾在右边
//        if (++takeIndex == items.length)
//            // 元素数量-1
//            takeIndex = 0;
//        count--;
//        // 这里是在干嘛我也没仔细研究，好像是和队列的迭代器有关
//        if (itrs != null)
//            itrs.elementDequeued();
//        // 当有元素出队后，队列不满，就可以被阻塞的生产者线程向队列中添加元素
//        notFull.signal();
//        // 返回获取到的元素值
//        return x;
//    }
//
//    /**出队 poll
//     * 有元素返回:返回true,
//     * 没有元素返回:false
//     */
//    public E poll() {
//        final ReentrantLock lock = this.lock;
//        // 获取元素前线加锁
//        lock.lock();
//        try {
//            // 若队列为空，直接返回null，否则调用dequeue获取队头元素；
//            return (count == 0) ? null : dequeue();
//        } finally {
//            // 解锁
//            lock.unlock();
//        }
//    }
//
//    /**
//     * Deletes item at array index removeIndex.
//     * Utility for remove(Object) and iterator.remove.
//     * Call only when holding lock.
//     */
//    void removeAt(final int removeIndex) {
//        // assert lock.getHoldCount() == 1;
//        // assert items[removeIndex] != null;
//        // assert removeIndex >= 0 && removeIndex < items.length;
//        final Object[] items = this.items;
//        if (removeIndex == takeIndex) {
//            // removing front item; just advance
//            items[takeIndex] = null;
//            if (++takeIndex == items.length)
//                takeIndex = 0;
//            count--;
//            if (itrs != null)
//                itrs.elementDequeued();
//        } else {
//            // an "interior" remove
//
//            // slide over all others up through putIndex.
//            final int putIndex = this.putIndex;
//            for (int i = removeIndex;;) {
//                int next = i + 1;
//                if (next == items.length)
//                    next = 0;
//                if (next != putIndex) {
//                    items[i] = items[next];
//                    i = next;
//                } else {
//                    items[i] = null;
//                    this.putIndex = i;
//                    break;
//                }
//            }
//            count--;
//            if (itrs != null)
//                itrs.removedAt(removeIndex);
//        }
//        notFull.signal();
//    }
//
//
//    /**
//     * Creates an {@code ArrayBlockingQueue} with the given (fixed)
//     * capacity, the specified access policy and initially containing the
//     * elements of the given collection,
//     * added in traversal order of the collection's iterator.
//     *
//     * @param capacity the capacity of this queue
//     * @param fair if {@code true} then queue accesses for threads blocked
//     *        on insertion or removal, are processed in FIFO order;
//     *        if {@code false} the access order is unspecified.
//     * @param c the collection of elements to initially contain
//     * @throws IllegalArgumentException if {@code capacity} is less than
//     *         {@code c.size()}, or less than 1.
//     * @throws NullPointerException if the specified collection or any
//     *         of its elements are null
//     */
//    public ArrayBlockingQueue(int capacity, boolean fair,
//                              Collection<? extends E> c) {
//        this(capacity, fair);
//
//        final ReentrantLock lock = this.lock;
//        lock.lock(); // Lock only for visibility, not mutual exclusion
//        try {
//            int i = 0;
//            try {
//                for (E e : c) {
//                    checkNotNull(e);
//                    items[i++] = e;
//                }
//            } catch (ArrayIndexOutOfBoundsException ex) {
//                throw new IllegalArgumentException();
//            }
//            count = i;
//            putIndex = (i == capacity) ? 0 : i;
//        } finally {
//            lock.unlock();
//        }
//    }
//
//
//
//    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
//        long nanos = unit.toNanos(timeout);
//        final ReentrantLock lock = this.lock;
//        lock.lockInterruptibly();
//        try {
//            while (count == 0) {
//                if (nanos <= 0)
//                    return null;
//                nanos = notEmpty.awaitNanos(nanos);
//            }
//            return dequeue();
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    public E peek() {
//        final ReentrantLock lock = this.lock;
//        lock.lock();
//        try {
//            return itemAt(takeIndex); // null when queue is empty
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    // this doc comment is overridden to remove the reference to collections
//    // greater in size than Integer.MAX_VALUE
//    /**
//     * Returns the number of elements in this queue.
//     *
//     * @return the number of elements in this queue
//     */
//    public int size() {
//        final ReentrantLock lock = this.lock;
//        lock.lock();
//        try {
//            return count;
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    // this doc comment is a modified copy of the inherited doc comment,
//    // without the reference to unlimited queues.
//    /**
//     * Returns the number of additional elements that this queue can ideally
//     * (in the absence of memory or resource constraints) accept without
//     * blocking. This is always equal to the initial capacity of this queue
//     * less the current {@code size} of this queue.
//     *
//     * <p>Note that you <em>cannot</em> always tell if an attempt to insert
//     * an element will succeed by inspecting {@code remainingCapacity}
//     * because it may be the case that another thread is about to
//     * insert or remove an element.
//     */
//    public int remainingCapacity() {
//        final ReentrantLock lock = this.lock;
//        lock.lock();
//        try {
//            return items.length - count;
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**
//     * Removes a single instance of the specified element from this queue,
//     * if it is present.  More formally, removes an element {@code e} such
//     * that {@code o.equals(e)}, if this queue contains one or more such
//     * elements.
//     * Returns {@code true} if this queue contained the specified element
//     * (or equivalently, if this queue changed as a result of the call).
//     *
//     * <p>Removal of interior elements in circular array based queues
//     * is an intrinsically slow and disruptive operation, so should
//     * be undertaken only in exceptional circumstances, ideally
//     * only when the queue is known not to be accessible by other
//     * threads.
//     *
//     * @param o element to be removed from this queue, if present
//     * @return {@code true} if this queue changed as a result of the call
//     */
//    public boolean remove(Object o) {
//        if (o == null) return false;
//        final Object[] items = this.items;
//        final ReentrantLock lock = this.lock;
//        lock.lock();
//        try {
//            if (count > 0) {
//                final int putIndex = this.putIndex;
//                int i = takeIndex;
//                do {
//                    if (o.equals(items[i])) {
//                        removeAt(i);
//                        return true;
//                    }
//                    if (++i == items.length)
//                        i = 0;
//                } while (i != putIndex);
//            }
//            return false;
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**
//     * Returns {@code true} if this queue contains the specified element.
//     * More formally, returns {@code true} if and only if this queue contains
//     * at least one element {@code e} such that {@code o.equals(e)}.
//     *
//     * @param o object to be checked for containment in this queue
//     * @return {@code true} if this queue contains the specified element
//     */
//    public boolean contains(Object o) {
//        if (o == null) return false;
//        final Object[] items = this.items;
//        final ReentrantLock lock = this.lock;
//        lock.lock();
//        try {
//            if (count > 0) {
//                final int putIndex = this.putIndex;
//                int i = takeIndex;
//                do {
//                    if (o.equals(items[i]))
//                        return true;
//                    if (++i == items.length)
//                        i = 0;
//                } while (i != putIndex);
//            }
//            return false;
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**
//     * Returns an array containing all of the elements in this queue, in
//     * proper sequence.
//     *
//     * <p>The returned array will be "safe" in that no references to it are
//     * maintained by this queue.  (In other words, this method must allocate
//     * a new array).  The caller is thus free to modify the returned array.
//     *
//     * <p>This method acts as bridge between array-based and collection-based
//     * APIs.
//     *
//     * @return an array containing all of the elements in this queue
//     */
//    public Object[] toArray() {
//        Object[] a;
//        final ReentrantLock lock = this.lock;
//        lock.lock();
//        try {
//            final int count = this.count;
//            a = new Object[count];
//            int n = items.length - takeIndex;
//            if (count <= n)
//                System.arraycopy(items, takeIndex, a, 0, count);
//            else {
//                System.arraycopy(items, takeIndex, a, 0, n);
//                System.arraycopy(items, 0, a, n, count - n);
//            }
//        } finally {
//            lock.unlock();
//        }
//        return a;
//    }
//
//    /**
//     * Returns an array containing all of the elements in this queue, in
//     * proper sequence; the runtime type of the returned array is that of
//     * the specified array.  If the queue fits in the specified array, it
//     * is returned therein.  Otherwise, a new array is allocated with the
//     * runtime type of the specified array and the size of this queue.
//     *
//     * <p>If this queue fits in the specified array with room to spare
//     * (i.e., the array has more elements than this queue), the element in
//     * the array immediately following the end of the queue is set to
//     * {@code null}.
//     *
//     * <p>Like the {@link #toArray()} method, this method acts as bridge between
//     * array-based and collection-based APIs.  Further, this method allows
//     * precise control over the runtime type of the output array, and may,
//     * under certain circumstances, be used to save allocation costs.
//     *
//     * <p>Suppose {@code x} is a queue known to contain only strings.
//     * The following code can be used to dump the queue into a newly
//     * allocated array of {@code String}:
//     *
//     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
//     *
//     * Note that {@code toArray(new Object[0])} is identical in function to
//     * {@code toArray()}.
//     *
//     * @param a the array into which the elements of the queue are to
//     *          be stored, if it is big enough; otherwise, a new array of the
//     *          same runtime type is allocated for this purpose
//     * @return an array containing all of the elements in this queue
//     * @throws ArrayStoreException if the runtime type of the specified array
//     *         is not a supertype of the runtime type of every element in
//     *         this queue
//     * @throws NullPointerException if the specified array is null
//     */
//    @SuppressWarnings("unchecked")
//    public <T> T[] toArray(T[] a) {
//        final Object[] items = this.items;
//        final ReentrantLock lock = this.lock;
//        lock.lock();
//        try {
//            final int count = this.count;
//            final int len = a.length;
//            if (len < count)
//                a = (T[])java.lang.reflect.Array.newInstance(
//                    a.getClass().getComponentType(), count);
//            int n = items.length - takeIndex;
//            if (count <= n)
//                System.arraycopy(items, takeIndex, a, 0, count);
//            else {
//                System.arraycopy(items, takeIndex, a, 0, n);
//                System.arraycopy(items, 0, a, n, count - n);
//            }
//            if (len > count)
//                a[count] = null;
//        } finally {
//            lock.unlock();
//        }
//        return a;
//    }
//
//    public String toString() {
//        final ReentrantLock lock = this.lock;
//        lock.lock();
//        try {
//            int k = count;
//            if (k == 0)
//                return "[]";
//
//            final Object[] items = this.items;
//            StringBuilder sb = new StringBuilder();
//            sb.append('[');
//            for (int i = takeIndex; ; ) {
//                Object e = items[i];
//                sb.append(e == this ? "(this Collection)" : e);
//                if (--k == 0)
//                    return sb.append(']').toString();
//                sb.append(',').append(' ');
//                if (++i == items.length)
//                    i = 0;
//            }
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**
//     * Atomically removes all of the elements from this queue.
//     * The queue will be empty after this call returns.
//     */
//    public void clear() {
//        final Object[] items = this.items;
//        final ReentrantLock lock = this.lock;
//        lock.lock();
//        try {
//            int k = count;
//            if (k > 0) {
//                final int putIndex = this.putIndex;
//                int i = takeIndex;
//                do {
//                    items[i] = null;
//                    if (++i == items.length)
//                        i = 0;
//                } while (i != putIndex);
//                takeIndex = putIndex;
//                count = 0;
//                if (itrs != null)
//                    itrs.queueIsEmpty();
//                for (; k > 0 && lock.hasWaiters(notFull); k--)
//                    notFull.signal();
//            }
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**
//     * @throws UnsupportedOperationException {@inheritDoc}
//     * @throws ClassCastException            {@inheritDoc}
//     * @throws NullPointerException          {@inheritDoc}
//     * @throws IllegalArgumentException      {@inheritDoc}
//     */
//    public int drainTo(Collection<? super E> c) {
//        return drainTo(c, Integer.MAX_VALUE);
//    }
//
//    /**
//     * @throws UnsupportedOperationException {@inheritDoc}
//     * @throws ClassCastException            {@inheritDoc}
//     * @throws NullPointerException          {@inheritDoc}
//     * @throws IllegalArgumentException      {@inheritDoc}
//     */
//    public int drainTo(Collection<? super E> c, int maxElements) {
//        checkNotNull(c);
//        if (c == this)
//            throw new IllegalArgumentException();
//        if (maxElements <= 0)
//            return 0;
//        final Object[] items = this.items;
//        final ReentrantLock lock = this.lock;
//        lock.lock();
//        try {
//            int n = Math.min(maxElements, count);
//            int take = takeIndex;
//            int i = 0;
//            try {
//                while (i < n) {
//                    @SuppressWarnings("unchecked")
//                    E x = (E) items[take];
//                    c.add(x);
//                    items[take] = null;
//                    if (++take == items.length)
//                        take = 0;
//                    i++;
//                }
//                return n;
//            } finally {
//                // Restore invariants even if c.add() threw
//                if (i > 0) {
//                    count -= i;
//                    takeIndex = take;
//                    if (itrs != null) {
//                        if (count == 0)
//                            itrs.queueIsEmpty();
//                        else if (i > take)
//                            itrs.takeIndexWrapped();
//                    }
//                    for (; i > 0 && lock.hasWaiters(notFull); i--)
//                        notFull.signal();
//                }
//            }
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**
//     * Returns an iterator over the elements in this queue in proper sequence.
//     * The elements will be returned in order from first (head) to last (tail).
//     *
//     * <p>The returned iterator is
//     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
//     *
//     * @return an iterator over the elements in this queue in proper sequence
//     */
//    public Iterator<E> iterator() {
//        return new Itr();
//    }
//
//    /**
//     * Shared data between iterators and their queue, allowing queue
//     * modifications to update iterators when elements are removed.
//     *
//     * This adds a lot of complexity for the sake of correctly
//     * handling some uncommon operations, but the combination of
//     * circular-arrays and supporting interior removes (i.e., those
//     * not at head) would cause iterators to sometimes lose their
//     * places and/or (re)report elements they shouldn't.  To avoid
//     * this, when a queue has one or more iterators, it keeps iterator
//     * state consistent by:
//     *
//     * (1) keeping track of the number of "cycles", that is, the
//     *     number of times takeIndex has wrapped around to 0.
//     * (2) notifying all iterators via the callback removedAt whenever
//     *     an interior element is removed (and thus other elements may
//     *     be shifted).
//     *
//     * These suffice to eliminate iterator inconsistencies, but
//     * unfortunately add the secondary responsibility of maintaining
//     * the list of iterators.  We track all active iterators in a
//     * simple linked list (accessed only when the queue's lock is
//     * held) of weak references to Itr.  The list is cleaned up using
//     * 3 different mechanisms:
//     *
//     * (1) Whenever a new iterator is created, do some O(1) checking for
//     *     stale list elements.
//     *
//     * (2) Whenever takeIndex wraps around to 0, check for iterators
//     *     that have been unused for more than one wrap-around cycle.
//     *
//     * (3) Whenever the queue becomes empty, all iterators are notified
//     *     and this entire data structure is discarded.
//     *
//     * So in addition to the removedAt callback that is necessary for
//     * correctness, iterators have the shutdown and takeIndexWrapped
//     * callbacks that help remove stale iterators from the list.
//     *
//     * Whenever a list element is examined, it is expunged if either
//     * the GC has determined that the iterator is discarded, or if the
//     * iterator reports that it is "detached" (does not need any
//     * further state updates).  Overhead is maximal when takeIndex
//     * never advances, iterators are discarded before they are
//     * exhausted, and all removals are interior removes, in which case
//     * all stale iterators are discovered by the GC.  But even in this
//     * case we don't increase the amortized complexity.
//     *
//     * Care must be taken to keep list sweeping methods from
//     * reentrantly invoking another such method, causing subtle
//     * corruption bugs.
//     */
//    class Itrs {
//
//        /**
//         * Node in a linked list of weak iterator references.
//         */
//        private class Node extends WeakReference<Itr> {
//            Node next;
//
//            Node(Itr iterator, Node next) {
//                super(iterator);
//                this.next = next;
//            }
//        }
//
//        /** Incremented whenever takeIndex wraps around to 0 */
//        int cycles = 0;
//
//        /** Linked list of weak iterator references */
//        private Node head;
//
//        /** Used to expunge stale iterators */
//        private Node sweeper = null;
//
//        private static final int SHORT_SWEEP_PROBES = 4;
//        private static final int LONG_SWEEP_PROBES = 16;
//
//        Itrs(Itr initial) {
//            register(initial);
//        }
//
//        /**
//         * Sweeps itrs, looking for and expunging stale iterators.
//         * If at least one was found, tries harder to find more.
//         * Called only from iterating thread.
//         *
//         * @param tryHarder whether to start in try-harder mode, because
//         * there is known to be at least one iterator to collect
//         */
//        void doSomeSweeping(boolean tryHarder) {
//            // assert lock.getHoldCount() == 1;
//            // assert head != null;
//            int probes = tryHarder ? LONG_SWEEP_PROBES : SHORT_SWEEP_PROBES;
//            Node o, p;
//            final Node sweeper = this.sweeper;
//            boolean passedGo;   // to limit search to one full sweep
//
//            if (sweeper == null) {
//                o = null;
//                p = head;
//                passedGo = true;
//            } else {
//                o = sweeper;
//                p = o.next;
//                passedGo = false;
//            }
//
//            for (; probes > 0; probes--) {
//                if (p == null) {
//                    if (passedGo)
//                        break;
//                    o = null;
//                    p = head;
//                    passedGo = true;
//                }
//                final Itr it = p.get();
//                final Node next = p.next;
//                if (it == null || it.isDetached()) {
//                    // found a discarded/exhausted iterator
//                    probes = LONG_SWEEP_PROBES; // "try harder"
//                    // unlink p
//                    p.clear();
//                    p.next = null;
//                    if (o == null) {
//                        head = next;
//                        if (next == null) {
//                            // We've run out of iterators to track; retire
//                            itrs = null;
//                            return;
//                        }
//                    }
//                    else
//                        o.next = next;
//                } else {
//                    o = p;
//                }
//                p = next;
//            }
//
//            this.sweeper = (p == null) ? null : o;
//        }
//
//        /**
//         * Adds a new iterator to the linked list of tracked iterators.
//         */
//        void register(Itr itr) {
//            // assert lock.getHoldCount() == 1;
//            head = new Node(itr, head);
//        }
//
//        /**
//         * Called whenever takeIndex wraps around to 0.
//         *
//         * Notifies all iterators, and expunges any that are now stale.
//         */
//        void takeIndexWrapped() {
//            // assert lock.getHoldCount() == 1;
//            cycles++;
//            for (Node o = null, p = head; p != null;) {
//                final Itr it = p.get();
//                final Node next = p.next;
//                if (it == null || it.takeIndexWrapped()) {
//                    // unlink p
//                    // assert it == null || it.isDetached();
//                    p.clear();
//                    p.next = null;
//                    if (o == null)
//                        head = next;
//                    else
//                        o.next = next;
//                } else {
//                    o = p;
//                }
//                p = next;
//            }
//            if (head == null)   // no more iterators to track
//                itrs = null;
//        }
//
//        /**
//         * Called whenever an interior remove (not at takeIndex) occurred.
//         *
//         * Notifies all iterators, and expunges any that are now stale.
//         */
//        void removedAt(int removedIndex) {
//            for (Node o = null, p = head; p != null;) {
//                final Itr it = p.get();
//                final Node next = p.next;
//                if (it == null || it.removedAt(removedIndex)) {
//                    // unlink p
//                    // assert it == null || it.isDetached();
//                    p.clear();
//                    p.next = null;
//                    if (o == null)
//                        head = next;
//                    else
//                        o.next = next;
//                } else {
//                    o = p;
//                }
//                p = next;
//            }
//            if (head == null)   // no more iterators to track
//                itrs = null;
//        }
//
//        /**
//         * Called whenever the queue becomes empty.
//         *
//         * Notifies all active iterators that the queue is empty,
//         * clears all weak refs, and unlinks the itrs datastructure.
//         */
//        void queueIsEmpty() {
//            // assert lock.getHoldCount() == 1;
//            for (Node p = head; p != null; p = p.next) {
//                Itr it = p.get();
//                if (it != null) {
//                    p.clear();
//                    it.shutdown();
//                }
//            }
//            head = null;
//            itrs = null;
//        }
//
//        /**
//         * Called whenever an element has been dequeued (at takeIndex).
//         */
//        void elementDequeued() {
//            // assert lock.getHoldCount() == 1;
//            if (count == 0)
//                queueIsEmpty();
//            else if (takeIndex == 0)
//                takeIndexWrapped();
//        }
//    }
//
//    /**
//     * Iterator for ArrayBlockingQueue.
//     *
//     * To maintain weak consistency with respect to puts and takes, we
//     * read ahead one slot, so as to not report hasNext true but then
//     * not have an element to return.
//     *
//     * We switch into "detached" mode (allowing prompt unlinking from
//     * itrs without help from the GC) when all indices are negative, or
//     * when hasNext returns false for the first time.  This allows the
//     * iterator to track concurrent updates completely accurately,
//     * except for the corner case of the user calling Iterator.remove()
//     * after hasNext() returned false.  Even in this case, we ensure
//     * that we don't remove the wrong element by keeping track of the
//     * expected element to remove, in lastItem.  Yes, we may fail to
//     * remove lastItem from the queue if it moved due to an interleaved
//     * interior remove while in detached mode.
//     */
//    private class Itr implements Iterator<E> {
//        /** Index to look for new nextItem; NONE at end */
//        private int cursor;
//
//        /** Element to be returned by next call to next(); null if none */
//        private E nextItem;
//
//        /** Index of nextItem; NONE if none, REMOVED if removed elsewhere */
//        private int nextIndex;
//
//        /** Last element returned; null if none or not detached. */
//        private E lastItem;
//
//        /** Index of lastItem, NONE if none, REMOVED if removed elsewhere */
//        private int lastRet;
//
//        /** Previous value of takeIndex, or DETACHED when detached */
//        private int prevTakeIndex;
//
//        /** Previous value of iters.cycles */
//        private int prevCycles;
//
//        /** Special index value indicating "not available" or "undefined" */
//        private static final int NONE = -1;
//
//        /**
//         * Special index value indicating "removed elsewhere", that is,
//         * removed by some operation other than a call to this.remove().
//         */
//        private static final int REMOVED = -2;
//
//        /** Special value for prevTakeIndex indicating "detached mode" */
//        private static final int DETACHED = -3;
//
//        Itr() {
//            // assert lock.getHoldCount() == 0;
//            lastRet = NONE;
//            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
//            lock.lock();
//            try {
//                if (count == 0) {
//                    // assert itrs == null;
//                    cursor = NONE;
//                    nextIndex = NONE;
//                    prevTakeIndex = DETACHED;
//                } else {
//                    final int takeIndex = ArrayBlockingQueue.this.takeIndex;
//                    prevTakeIndex = takeIndex;
//                    nextItem = itemAt(nextIndex = takeIndex);
//                    cursor = incCursor(takeIndex);
//                    if (itrs == null) {
//                        itrs = new Itrs(this);
//                    } else {
//                        itrs.register(this); // in this order
//                        itrs.doSomeSweeping(false);
//                    }
//                    prevCycles = itrs.cycles;
//                    // assert takeIndex >= 0;
//                    // assert prevTakeIndex == takeIndex;
//                    // assert nextIndex >= 0;
//                    // assert nextItem != null;
//                }
//            } finally {
//                lock.unlock();
//            }
//        }
//
//        boolean isDetached() {
//            // assert lock.getHoldCount() == 1;
//            return prevTakeIndex < 0;
//        }
//
//        private int incCursor(int index) {
//            // assert lock.getHoldCount() == 1;
//            if (++index == items.length)
//                index = 0;
//            if (index == putIndex)
//                index = NONE;
//            return index;
//        }
//
//        /**
//         * Returns true if index is invalidated by the given number of
//         * dequeues, starting from prevTakeIndex.
//         */
//        private boolean invalidated(int index, int prevTakeIndex,
//                                    long dequeues, int length) {
//            if (index < 0)
//                return false;
//            int distance = index - prevTakeIndex;
//            if (distance < 0)
//                distance += length;
//            return dequeues > distance;
//        }
//
//        /**
//         * Adjusts indices to incorporate all dequeues since the last
//         * operation on this iterator.  Call only from iterating thread.
//         */
//        private void incorporateDequeues() {
//            // assert lock.getHoldCount() == 1;
//            // assert itrs != null;
//            // assert !isDetached();
//            // assert count > 0;
//
//            final int cycles = itrs.cycles;
//            final int takeIndex = ArrayBlockingQueue.this.takeIndex;
//            final int prevCycles = this.prevCycles;
//            final int prevTakeIndex = this.prevTakeIndex;
//
//            if (cycles != prevCycles || takeIndex != prevTakeIndex) {
//                final int len = items.length;
//                // how far takeIndex has advanced since the previous
//                // operation of this iterator
//                long dequeues = (cycles - prevCycles) * len
//                    + (takeIndex - prevTakeIndex);
//
//                // Check indices for invalidation
//                if (invalidated(lastRet, prevTakeIndex, dequeues, len))
//                    lastRet = REMOVED;
//                if (invalidated(nextIndex, prevTakeIndex, dequeues, len))
//                    nextIndex = REMOVED;
//                if (invalidated(cursor, prevTakeIndex, dequeues, len))
//                    cursor = takeIndex;
//
//                if (cursor < 0 && nextIndex < 0 && lastRet < 0)
//                    detach();
//                else {
//                    this.prevCycles = cycles;
//                    this.prevTakeIndex = takeIndex;
//                }
//            }
//        }
//
//        /**
//         * Called when itrs should stop tracking this iterator, either
//         * because there are no more indices to update (cursor < 0 &&
//         * nextIndex < 0 && lastRet < 0) or as a special exception, when
//         * lastRet >= 0, because hasNext() is about to return false for the
//         * first time.  Call only from iterating thread.
//         */
//        private void detach() {
//            // Switch to detached mode
//            // assert lock.getHoldCount() == 1;
//            // assert cursor == NONE;
//            // assert nextIndex < 0;
//            // assert lastRet < 0 || nextItem == null;
//            // assert lastRet < 0 ^ lastItem != null;
//            if (prevTakeIndex >= 0) {
//                // assert itrs != null;
//                prevTakeIndex = DETACHED;
//                // try to unlink from itrs (but not too hard)
//                itrs.doSomeSweeping(true);
//            }
//        }
//
//        /**
//         * For performance reasons, we would like not to acquire a lock in
//         * hasNext in the common case.  To allow for this, we only access
//         * fields (i.e. nextItem) that are not modified by update operations
//         * triggered by queue modifications.
//         */
//        public boolean hasNext() {
//            // assert lock.getHoldCount() == 0;
//            if (nextItem != null)
//                return true;
//            noNext();
//            return false;
//        }
//
//        private void noNext() {
//            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
//            lock.lock();
//            try {
//                // assert cursor == NONE;
//                // assert nextIndex == NONE;
//                if (!isDetached()) {
//                    // assert lastRet >= 0;
//                    incorporateDequeues(); // might update lastRet
//                    if (lastRet >= 0) {
//                        lastItem = itemAt(lastRet);
//                        // assert lastItem != null;
//                        detach();
//                    }
//                }
//                // assert isDetached();
//                // assert lastRet < 0 ^ lastItem != null;
//            } finally {
//                lock.unlock();
//            }
//        }
//
//        public E next() {
//            // assert lock.getHoldCount() == 0;
//            final E x = nextItem;
//            if (x == null)
//                throw new NoSuchElementException();
//            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
//            lock.lock();
//            try {
//                if (!isDetached())
//                    incorporateDequeues();
//                // assert nextIndex != NONE;
//                // assert lastItem == null;
//                lastRet = nextIndex;
//                final int cursor = this.cursor;
//                if (cursor >= 0) {
//                    nextItem = itemAt(nextIndex = cursor);
//                    // assert nextItem != null;
//                    this.cursor = incCursor(cursor);
//                } else {
//                    nextIndex = NONE;
//                    nextItem = null;
//                }
//            } finally {
//                lock.unlock();
//            }
//            return x;
//        }
//
//        public void remove() {
//            // assert lock.getHoldCount() == 0;
//            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
//            lock.lock();
//            try {
//                if (!isDetached())
//                    incorporateDequeues(); // might update lastRet or detach
//                final int lastRet = this.lastRet;
//                this.lastRet = NONE;
//                if (lastRet >= 0) {
//                    if (!isDetached())
//                        removeAt(lastRet);
//                    else {
//                        final E lastItem = this.lastItem;
//                        // assert lastItem != null;
//                        this.lastItem = null;
//                        if (itemAt(lastRet) == lastItem)
//                            removeAt(lastRet);
//                    }
//                } else if (lastRet == NONE)
//                    throw new IllegalStateException();
//                // else lastRet == REMOVED and the last returned element was
//                // previously asynchronously removed via an operation other
//                // than this.remove(), so nothing to do.
//
//                if (cursor < 0 && nextIndex < 0)
//                    detach();
//            } finally {
//                lock.unlock();
//                // assert lastRet == NONE;
//                // assert lastItem == null;
//            }
//        }
//
//        /**
//         * Called to notify the iterator that the queue is empty, or that it
//         * has fallen hopelessly behind, so that it should abandon any
//         * further iteration, except possibly to return one more element
//         * from next(), as promised by returning true from hasNext().
//         */
//        void shutdown() {
//            // assert lock.getHoldCount() == 1;
//            cursor = NONE;
//            if (nextIndex >= 0)
//                nextIndex = REMOVED;
//            if (lastRet >= 0) {
//                lastRet = REMOVED;
//                lastItem = null;
//            }
//            prevTakeIndex = DETACHED;
//            // Don't set nextItem to null because we must continue to be
//            // able to return it on next().
//            //
//            // Caller will unlink from itrs when convenient.
//        }
//
//        private int distance(int index, int prevTakeIndex, int length) {
//            int distance = index - prevTakeIndex;
//            if (distance < 0)
//                distance += length;
//            return distance;
//        }
//
//        /**
//         * Called whenever an interior remove (not at takeIndex) occurred.
//         *
//         * @return true if this iterator should be unlinked from itrs
//         */
//        boolean removedAt(int removedIndex) {
//            // assert lock.getHoldCount() == 1;
//            if (isDetached())
//                return true;
//
//            final int cycles = itrs.cycles;
//            final int takeIndex = ArrayBlockingQueue.this.takeIndex;
//            final int prevCycles = this.prevCycles;
//            final int prevTakeIndex = this.prevTakeIndex;
//            final int len = items.length;
//            int cycleDiff = cycles - prevCycles;
//            if (removedIndex < takeIndex)
//                cycleDiff++;
//            final int removedDistance =
//                (cycleDiff * len) + (removedIndex - prevTakeIndex);
//            // assert removedDistance >= 0;
//            int cursor = this.cursor;
//            if (cursor >= 0) {
//                int x = distance(cursor, prevTakeIndex, len);
//                if (x == removedDistance) {
//                    if (cursor == putIndex)
//                        this.cursor = cursor = NONE;
//                }
//                else if (x > removedDistance) {
//                    // assert cursor != prevTakeIndex;
//                    this.cursor = cursor = dec(cursor);
//                }
//            }
//            int lastRet = this.lastRet;
//            if (lastRet >= 0) {
//                int x = distance(lastRet, prevTakeIndex, len);
//                if (x == removedDistance)
//                    this.lastRet = lastRet = REMOVED;
//                else if (x > removedDistance)
//                    this.lastRet = lastRet = dec(lastRet);
//            }
//            int nextIndex = this.nextIndex;
//            if (nextIndex >= 0) {
//                int x = distance(nextIndex, prevTakeIndex, len);
//                if (x == removedDistance)
//                    this.nextIndex = nextIndex = REMOVED;
//                else if (x > removedDistance)
//                    this.nextIndex = nextIndex = dec(nextIndex);
//            }
//            else if (cursor < 0 && nextIndex < 0 && lastRet < 0) {
//                this.prevTakeIndex = DETACHED;
//                return true;
//            }
//            return false;
//        }
//
//        /**
//         * Called whenever takeIndex wraps around to zero.
//         *
//         * @return true if this iterator should be unlinked from itrs
//         */
//        boolean takeIndexWrapped() {
//            // assert lock.getHoldCount() == 1;
//            if (isDetached())
//                return true;
//            if (itrs.cycles - prevCycles > 1) {
//                // All the elements that existed at the time of the last
//                // operation are gone, so abandon further iteration.
//                shutdown();
//                return true;
//            }
//            return false;
//        }
//
////         /** Uncomment for debugging. */
////         public String toString() {
////             return ("cursor=" + cursor + " " +
////                     "nextIndex=" + nextIndex + " " +
////                     "lastRet=" + lastRet + " " +
////                     "nextItem=" + nextItem + " " +
////                     "lastItem=" + lastItem + " " +
////                     "prevCycles=" + prevCycles + " " +
////                     "prevTakeIndex=" + prevTakeIndex + " " +
////                     "size()=" + size() + " " +
////                     "remainingCapacity()=" + remainingCapacity());
////         }
//    }
//
//    /**
//     * Returns a {@link Spliterator} over the elements in this queue.
//     *
//     * <p>The returned spliterator is
//     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
//     *
//     * <p>The {@code Spliterator} reports {@link Spliterator#CONCURRENT},
//     * {@link Spliterator#ORDERED}, and {@link Spliterator#NONNULL}.
//     *
//     * @implNote
//     * The {@code Spliterator} implements {@code trySplit} to permit limited
//     * parallelism.
//     *
//     * @return a {@code Spliterator} over the elements in this queue
//     * @since 1.8
//     */
//    public Spliterator<E> spliterator() {
//        return Spliterators.spliterator
//            (this, Spliterator.ORDERED | Spliterator.NONNULL |
//             Spliterator.CONCURRENT);
//    }
//
//    /**
//     * Deserializes this queue and then checks some invariants.
//     *
//     * @param s the input stream
//     * @throws ClassNotFoundException if the class of a serialized object
//     *         could not be found
//     * @throws java.io.InvalidObjectException if invariants are violated
//     * @throws java.io.IOException if an I/O error occurs
//     */
//    private void readObject(java.io.ObjectInputStream s)
//        throws java.io.IOException, ClassNotFoundException {
//
//        // Read in items array and various fields
//        s.defaultReadObject();
//
//        // Check invariants over count and index fields. Note that
//        // if putIndex==takeIndex, count can be either 0 or items.length.
//        if (items.length == 0 ||
//            takeIndex < 0 || takeIndex >= items.length ||
//            putIndex  < 0 || putIndex  >= items.length ||
//            count < 0     || count     >  items.length ||
//            Math.floorMod(putIndex - takeIndex, items.length) !=
//            Math.floorMod(count, items.length)) {
//            throw new java.io.InvalidObjectException("invariants violated");
//        }
//    }
//}
