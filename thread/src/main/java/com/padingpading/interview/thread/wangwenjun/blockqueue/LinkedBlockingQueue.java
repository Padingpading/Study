/*
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

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.padingpading.interview.thread.wangwenjun.blockqueue;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;


/**
 * LinkedBlockingQueue:基于单链表,执行并发操作的有界阻塞队列。
 * AbstractQueue:队列部分方法实现
 * BlockingDeque:阻塞队列。
 * 特点:
 * 1、容量可选,默认为Integer.MAX_VALUE。
 * 2、单向链表。
   3、由于消费线程只操作队头，而生产线程只操作队尾，这里巧妙地采用了两把锁，对插入数据采用putLock，
 对移除数据采用takeLock，即生产者锁和消费者锁，这样避免了生产线程和消费线程竞争同一把锁的现象（比如ArrayBlockingQueue就只用了一把锁），
 因此LinkedBlockingQueue在高并发的情况下，性能会比ArrayBlockingQueue好很多，
 但是在需要遍历整个队列的情况下则要把两把锁都锁住（比如clear、contains操作）
 * 4、工作模式都是非公平的，也不能手动指定为公平模式，即获取锁的实际线程顺序不能保证是等待获取锁的线程顺序，这样的好处是可以提升并发量！
 **/
public class LinkedBlockingQueue<E> extends AbstractQueue<E>
        implements java.util.concurrent.BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -6903933977591709194L;
    
    /**
     * 节点类，用于存储数据
     */
    static class Node<E> {
        //节点值
        E item;
        //下一个节点
        Node<E> next;

        Node(E x) { item = x; }
    }
    
    /** 阻塞队列的大小，默认为Integer.MAX_VALUE */
    private final int capacity;
    
    /** 当前阻塞队列中的元素个数 *
     *  使用原子类保证计数安全。
     */
    private final AtomicInteger count = new AtomicInteger();

    /**
     * 阻塞队列的头结点
     */
    transient Node<E> head;

    /**
     * 阻塞队列的尾节点
     */
    private transient Node<E> last;
    
    /** 获取并移除元素时使用的锁，如take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();
    
    //消费线程阻塞和唤醒。
    private final Condition notEmpty = takeLock.newCondition();
    
    /** 添加元素时使用的锁如 put, offer, etc */
    private final ReentrantLock putLock = new ReentrantLock();
    //生产线程的阻塞和唤醒。
    private final Condition notFull = putLock.newCondition();
    
    /*======================================================构造函数===================================================*/
    //默认无界队列
    public LinkedBlockingQueue() {
        // 默认大小为Integer.MAX_VALUE
        this(Integer.MAX_VALUE);
    }
    
    /**创建一个具有指定容量的 LinkedBlockingQueue。
     * @param capacity
     */
    public LinkedBlockingQueue(int capacity) {
        //capacity大小校验
        if (capacity <= 0) throw new IllegalArgumentException();
        //capacity赋值
        this.capacity = capacity;
        //初始化头结点和尾节点，指向同一个值为null的哨兵结点
        last = head = new Node<E>(null);
    }
    
    /** * 创建一个容量是 Integer.MAX_VALUE 的 LinkedBlockingQueue，最初包含指定集合的远不元素，元素按该集合迭代器的遍历顺序添加。
     *
     */
    public LinkedBlockingQueue(Collection<? extends E> c) {
        //调用另一个构造器，初始化队列，容量为Integer.MAX_VALUE
        this(Integer.MAX_VALUE);
        final ReentrantLock putLock = this.putLock;
        //这里和ArrayBlockingQueue是一样的，需要加锁来保证数据的可见性，因为头、尾结点没有使用volatile修饰
        //获取生产者锁
        putLock.lock();
        try {
            //n作为计数器
            int n = 0;
            //遍历指定集合
            for (E e : c) {
                //null校验
                if (e == null)
                    throw new NullPointerException();
                //容量校验
                if (n == capacity)
                    throw new IllegalStateException("Queue full");
                //调用enqueue方法插入新结点到队列尾部
                enqueue(new Node<E>(e));
                //计数器自增1
                ++n;
            }
            //设置队列的元素数量
            count.set(n);
        } finally {
            //释放生产者锁
            putLock.unlock();
        }
    }
    /*=====================================================添加元素=====================================================*/
    
    
    /**将指定的元素插入此队列的尾部，如果该队列已满，则线程等待。
     *在ArrayBlockingQueue中，生产（放入数据）线程阻塞的时候，需要消费（移除数据）线程才能唤醒，
     * 并且因为它们获取的同一个锁，消费和生产不能并发进行（假设一个线程仅仅从事生产或者消费工作的一种）。
     * 在LinkedBlockingQueue中，如果有线程因为获取不到锁或者队列已满而导致生产（放入数据）线程阻塞，
     * 那么他可能被后面的消费线程唤醒也可能被后面的生产线程唤醒。因为它内部有两个锁，生产和消费获取不同的锁，
     * 可以并行执行生产和消费任务，不仅在消费数据的时候会唤醒阻塞的生产线程，在生产数据的时候如果队列容量还没满
     * ，也会唤醒此前阻塞的生产线程继续生产。
     */
    public void put(E e) throws InterruptedException {
        //e的null校验
        if (e == null) throw new NullPointerException();
        int c = -1;
        //新建结点
        Node<E> node = new Node<E>(e);
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        //可中断的等待获取生产者锁，即响应中断
        putLock.lockInterruptibly();
        try {
            /*
             * while循环判断此时结点数量是否等于容量，即队列是否满了
             */
            while (count.get() == capacity) {
                //如果满了，那么该线程在notFull条件队列中等待并释放锁，被唤醒之后会继续尝试获取锁、并循环判断
                notFull.await();
            }
            // 队列没有满，结点添加到链表尾部
            enqueue(node);
            //获取此时计数器的值赋给c，并且计数器值自增1
            c = count.getAndIncrement();
            //如果c+1小于capacity，说明还可以入队
            if (c + 1 < capacity)
                //唤醒一个在notFull条件队列中等待的生产线程
                notFull.signal();
        } finally {
            //释放生产者锁
            putLock.unlock();
        }
        //如果前面没有抛出异常，那么在finally之后会执行下面的代码
        //如果c为0，那么此时队列中还可能有存在1条数据，刚放进去的
        //那么由于刚才队列没有数据，可能此时有消费者线程在等待，这里需要唤醒一个消费者线程
        //如果此前队列中就有数据没有消费完毕，那么也不必唤醒唤醒消费者
        if (c == 0)
            //获取消费者锁并且尝试唤醒一个消费者线程
            signalNotEmpty();
    }
    
    
    /**
     * 唤醒一个在notEmpty条件队列中等待的消费线程，需要先获取消费者锁
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        //阻塞式的获取消费者锁，即不响应中断
        takeLock.lock();
        try {
            //唤醒一个在notEmpty条件队列中等待的消费线程
            //要想调用Condition对象的方法，必须先要获取该Condition对象对应的lock锁
            notEmpty.signal();
        } finally {
            //释放消费者锁
            takeLock.unlock();
        }
    }

    /**
     * Signals a waiting put. Called only from take/poll.
     */
    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    /** 指定结点链接到队列尾部成为新的尾结点，在获取锁之后才会调用该方法
     */
    private void enqueue(Node<E> node) {
        //很简单，原尾结点的next引用指向node结点，然后last指向最新node结点
        last = last.next = node;
    }
    
    /**将指定的元素插入到此队列的尾部。在成功时返回 true，如果此队列已满，则不阻塞，立即返回 false。
     *
     */
    public boolean offer(E e) {
        //e的null校验
        if (e == null) throw new NullPointerException();
        final AtomicInteger count = this.count;
        //在获取锁之前就判断一次，如果容量满了
        if (count.get() == capacity)
            //直接返回false，可以节省锁的获取和释放的开销
            return false;
        //初始化c为-1，表示存放元素失败
        int c = -1;
        Node<E> node = new Node<E>(e);
        final ReentrantLock putLock = this.putLock;
        //不可中断的等待获取生产者锁，即不响应中断
        putLock.lock();
        try {
            //如果队列未满
            if (count.get() < capacity) {
                //调用enqueue插入元素
                enqueue(node);
                //获取此时计数器的值赋给c，并且计数器值自增1，这里的c一定是大于等于0的值
                c = count.getAndIncrement();
                //如果c+1小于capacity，说明还可以入队
                if (c + 1 < capacity)
                    //唤醒一个在notFull条件队列中等待的生产线程
                    notFull.signal();
            }
        } finally {
            //释放生产者锁
            putLock.unlock();
        }
        if (c == 0)
            //如果前面没有抛出异常，那么在finally之后会执行下面的代码
            //如果c为0，那么此时队列中还可能有存在1条数据，刚放进去的
            //那么由于刚才队列没有数据，可能此时有消费者线程在等待，这里需要唤醒一个消费者线程
            //如果此前队列中就有数据没有消费完毕，那么也不必唤醒唤醒消费者
            signalNotEmpty();
        //如果c>=0，表示该元素已添加到此队列，则返回 true；否则返回 false
        return c >= 0;
    }
    
    
    /**将指定的元素插入此队列的尾部，如果该队列已满，则在到达指定的等待时间之前等待可用的空间。
     * @return 如果插入成功，则返回 true；如果在空间可用前超过了指定的等待时间，则返回 false。
     * @throws InterruptedException 如果因为获取不到锁而在同步队列中等待的时候被中断则抛出InterruptedException，即响应中断
     *                              如果因为队列满了在条件队列中等待的时候在其他线程调用signal、signalAll方法唤醒该线程之前就因为中断而被唤醒了，也会抛出InterruptedException。
     * @throws NullPointerException 如果指定元素为 null
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException {
        //e的null校验
        if (e == null) throw new NullPointerException();
        //计算超时时间纳秒
        long nanos = unit.toNanos(timeout);
        //初始化c为-1，表示存放元素失败
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        //可中断的等待获取生产者锁，即响应中断
        putLock.lockInterruptibly();
        try {
            /*
             * while循环判断此时结点数量是否等于容量，即队列是否满了
             */
            while (count.get() == capacity) {
                if (nanos <= 0)
                    return false;
                nanos = notFull.awaitNanos(nanos);
            }
            // 队列没有满，结点添加到链表尾部
            enqueue(new Node<E>(e));
            //获取此时计数器的值赋给c，并且计数器值自增1
            c = count.getAndIncrement();
            //如果c+1小于capacity，说明还可以入队
            if (c + 1 < capacity)
                //唤醒一个在notFull条件队列中等待的生产线程
                notFull.signal();
        } finally {
            //释放生产者锁
            putLock.unlock();
        }
        //如果前面没有抛出异常，那么在finally之后会执行下面的代码
        //如果c为0，那么此时队列中还可能有存在1条数据，刚放进去的
        //那么由于刚才队列没有数据，可能此时有消费者线程在等待，这里需要唤醒一个消费者线程
        //如果此前队列中就有数据没有消费完毕，那么也不必唤醒唤醒消费者
        if (c == 0)
            //获取消费者锁并且尝试唤醒一个消费者线程
            signalNotEmpty();
        //到这里，一定是插入成功了，返回true
        return true;
    }
    
    
    /*=======================================================出队=====================================================*/
    
    /**
     * 获取并移除此队列的头部
     *
     * @return 被移除的队列头部元素
     * @throws InterruptedException 因为获取不到锁而等待的时候被中断
     */
    public E take() throws InterruptedException {
        E x;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        //可中断的等待获取消费者锁，即响应中断
        takeLock.lockInterruptibly();
        try {
            /*
             * while循环判断此时结点数量是否等于0，即队列是否空了
             */
            while (count.get() == 0) {
                //如果空了，那么该线程在notEmpty条件队列中等待并释放锁，被唤醒之后会继续尝试获取锁、并循环判断
                notEmpty.await();
            }
            // 队列没有空，获取并移除此队列的头部
            x = dequeue();
            //获取此时计数器的值赋给c，并且计数器值自减1
            c = count.getAndDecrement();
            //如果c大于1，说明还可以出队列
            if (c > 1)
                //唤醒一个在notEmpty条件队列中等待的消费线程
                notEmpty.signal();
        } finally {
            //释放消费者锁
            takeLock.unlock();
        }
        //如果前面没有抛出异常，那么在finally之后会执行下面的代码
        //如果c为capacity，那么此前队列中可能具有满的数据，可能此时有生产者线程在等待，
        //这里需要唤醒一个生产者线程
        //如果此前队列中的数据没有满，那么也不必唤醒唤醒生产者
        if (c == capacity)
            //获取生产者锁并且尝试唤醒一个生产者线程
            signalNotFull();
        //返回被移除的队列头部元素
        return x;
    }
    
    
    /** 获取并移除此队列的头部，这里面的新的头部元素会变成哨兵结点，即item置为null
     */
    private E dequeue() {
        //获取此时头部元素
        Node<E> h = head;
        //first指向下一个元素
        Node<E> first = h.next;
        //原头结点的next指向自己，为什么不指向null呢？
        //因为在LinkedBlockingQueue中，一个结点的next为null的话
        //那么表示遍历到了队列末尾，这在迭代器的时候会用到，如果指向自己则表示头结点出了队列
        h.next = h; // help GC
        //head指向此时的头部元素
        head = first;
        //获取此时头部元素的值
        E x = first.item;
        //此时头部元素的值置空，即变成哨兵结点
        first.item = null;
        //返回头部元素的值
        return x;
    }
    
    /**
     * @return 获取并移除此队列的头，如果此队列为空，则返回 null。
     */
    public E poll() {
        final AtomicInteger count = this.count;
        //在获取锁之前就判断一次，如果队列空了
        if (count.get() == 0)
            //直接返回null
            return null;
        //表示移除的队列头部元素，默认为null
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        //不可中断的等待获取消费者锁，即不响应中断
        takeLock.lock();
        try {
            //判断此时结点数量是否大于0，即队列是否不为空
            if (count.get() > 0) {
                //获取此时计数器的值赋给c，并且计数器值自减1
                x = dequeue();
                c = count.getAndDecrement();
                //如果c大于1，说明还可以出队列
                if (c > 1)
                    //唤醒一个在notEmpty条件队列中等待的消费线程
                    notEmpty.signal();
            }
        } finally {
            //释放消费者锁
            takeLock.unlock();
        }
        //如果前面没有抛出异常，那么在finally之后会执行下面的代码
        //如果c为capacity，那么此前队列中可能具有满的数据，可能此时有生产者线程在等待，
        //这里需要唤醒一个生产者线程
        //如果此前队列中的数据没有满，那么也不必唤醒唤醒生产者
        if (c == capacity)
            //获取生产者锁并且尝试唤醒一个生产者线程
            signalNotFull();
        //返回被移除的队列头部元素
        return x;
    }
    
    /**
     * 获取并移除此队列的头部，在指定的等待时间前等待可用的元素（如果有必要）。
     *
     * @param timeout 时间
     * @param unit    时间单位
     * @return 此队列的头部；如果在元素可用前超过了指定的等待时间，则返回 null
     * @throws InterruptedException 因为获取不到锁而等待时被中断
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        //表示移除的队列头部元素，默认为null
        E x = null;
        int c = -1;
        //计算超时时间纳秒
        long nanos = unit.toNanos(timeout);
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        //可中断的等待获取锁，即响应中断
        takeLock.lockInterruptibly();
        try {
            /*
             * while循环判断此时结点数量是否等于0，即队列是否空了
             */
            while (count.get() == 0) {
                if (nanos <= 0)
                    //如果剩余超时时间小于等于0，说明时间到了
                    return null;
                //否则，该线程在notEmpty条件队列中等待nanos时间
                nanos = notEmpty.awaitNanos(nanos);
            }
            x = dequeue();
            //获取此时计数器的值赋给c，并且计数器值自减1
            c = count.getAndDecrement();
            if (c > 1)
                //唤醒一个在notEmpty条件队列中等待的消费线程
                notEmpty.signal();
        } finally {
            //释放消费者锁
            takeLock.unlock();
        }
        //如果前面没有抛出异常，那么在finally之后会执行下面的代码
        //如果c为capacity，那么此前队列中可能具有满的数据，可能此时有生产者线程在等待，
        //这里需要唤醒一个生产者线程
        //如果此前队列中的数据没有满，那么也不必唤醒唤醒生产者
        if (c == capacity)
            //获取生产者锁并且尝试唤醒一个生产者线程
            signalNotFull();
        //返回被移除的队列头部元素
        return x;
    }
    
    /*======================================================检查======================================================*/
    
    /**
     * @return 获取但不移除此队列的头；如果此队列为空，则返回 null。
     */
    public E peek() {
        //在获取锁之前就判断一次，如果队列空了
        if (count.get() == 0)
            return null;
        final ReentrantLock takeLock = this.takeLock;
        //不可中断的等待获取消费者锁，即不响应中断
        takeLock.lock();
        try {
            //获取head的next结点，它才可能是真正的头结点
            Node<E> first = head.next;
            //如果为null，说明队列空了
            if (first == null)
                //直接返回nul
                return null;
            else
                //否则，返回值
                return first.item;
        } finally {
            //释放消费者锁
            takeLock.unlock();
        }
    }
    
    public int size() {
        //由于计数器是一个AtomicInteger的原子变量，因此可以获取此时最新的元素数量值
        return count.get();
    }
    
    
    
    /**
     * Locks to prevent both puts and takes.
     */
    void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    /**
     * Unlocks to allow both puts and takes.
     */
    void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

//     /**
//      * Tells whether both locks are held by current thread.
//      */
//     boolean isFullyLocked() {
//         return (putLock.isHeldByCurrentThread() &&
//                 takeLock.isHeldByCurrentThread());
//     }


    // this doc comment is a modified copy of the inherited doc comment,
    // without the reference to unlimited queues.
    /**
     * Returns the number of additional elements that this queue can ideally
     * (in the absence of memory or resource constraints) accept without
     * blocking. This is always equal to the initial capacity of this queue
     * less the current {@code size} of this queue.
     *
     * <p>Note that you <em>cannot</em> always tell if an attempt to insert
     * an element will succeed by inspecting {@code remainingCapacity}
     * because it may be the case that another thread is about to
     * insert or remove an element.
     */
    public int remainingCapacity() {
        return capacity - count.get();
    }

   



    /**
     * Unlinks interior Node p with predecessor trail.
     */
    void unlink(Node<E> p, Node<E> trail) {
        // assert isFullyLocked();
        // p.next is not changed, to allow iterators that are
        // traversing p to maintain their weak-consistency guarantee.
        p.item = null;
        trail.next = p.next;
        if (last == p)
            last = trail;
        if (count.getAndDecrement() == capacity)
            notFull.signal();
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.
     * Returns {@code true} if this queue contained the specified element
     * (or equivalently, if this queue changed as a result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
        if (o == null) return false;
        fullyLock();
        try {
            for (Node<E> trail = head, p = trail.next;
                 p != null;
                 trail = p, p = p.next) {
                if (o.equals(p.item)) {
                    unlink(p, trail);
                    return true;
                }
            }
            return false;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns {@code true} if this queue contains the specified element.
     * More formally, returns {@code true} if and only if this queue contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this queue
     * @return {@code true} if this queue contains the specified element
     */
    public boolean contains(Object o) {
        if (o == null) return false;
        fullyLock();
        try {
            for (Node<E> p = head.next; p != null; p = p.next)
                if (o.equals(p.item))
                    return true;
            return false;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue, in
     * proper sequence.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this queue
     */
    public Object[] toArray() {
        fullyLock();
        try {
            int size = count.get();
            Object[] a = new Object[size];
            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next)
                a[k++] = p.item;
            return a;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue, in
     * proper sequence; the runtime type of the returned array is that of
     * the specified array.  If the queue fits in the specified array, it
     * is returned therein.  Otherwise, a new array is allocated with the
     * runtime type of the specified array and the size of this queue.
     *
     * <p>If this queue fits in the specified array with room to spare
     * (i.e., the array has more elements than this queue), the element in
     * the array immediately following the end of the queue is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of {@code String}:
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this queue
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            int size = count.get();
            if (a.length < size)
                a = (T[])java.lang.reflect.Array.newInstance
                    (a.getClass().getComponentType(), size);

            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next)
                a[k++] = (T)p.item;
            if (a.length > k)
                a[k] = null;
            return a;
        } finally {
            fullyUnlock();
        }
    }

    public String toString() {
        fullyLock();
        try {
            Node<E> p = head.next;
            if (p == null)
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (;;) {
                E e = p.item;
                sb.append(e == this ? "(this Collection)" : e);
                p = p.next;
                if (p == null)
                    return sb.append(']').toString();
                sb.append(',').append(' ');
            }
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Atomically removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        fullyLock();
        try {
            for (Node<E> p, h = head; (p = h.next) != null; h = p) {
                h.next = h;
                p.item = null;
            }
            head = last;
            // assert head.item == null && head.next == null;
            if (count.getAndSet(0) == capacity)
                notFull.signal();
        } finally {
            fullyUnlock();
        }
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        boolean signalNotFull = false;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            int n = Math.min(maxElements, count.get());
            // count.get provides visibility to first n Nodes
            Node<E> h = head;
            int i = 0;
            try {
                while (i < n) {
                    Node<E> p = h.next;
                    c.add(p.item);
                    p.item = null;
                    h.next = h;
                    h = p;
                    ++i;
                }
                return n;
            } finally {
                // Restore invariants even if c.add() threw
                if (i > 0) {
                    // assert h.item == null;
                    head = h;
                    signalNotFull = (count.getAndAdd(-i) == capacity);
                }
            }
        } finally {
            takeLock.unlock();
            if (signalNotFull)
                signalNotFull();
        }
    }

    /**
     * Returns an iterator over the elements in this queue in proper sequence.
     * The elements will be returned in order from first (head) to last (tail).
     *
     * <p>The returned iterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * @return an iterator over the elements in this queue in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        /*
         * Basic weakly-consistent iterator.  At all times hold the next
         * item to hand out so that if hasNext() reports true, we will
         * still have it to return even if lost race with a take etc.
         */

        private Node<E> current;
        private Node<E> lastRet;
        private E currentElement;

        Itr() {
            fullyLock();
            try {
                current = head.next;
                if (current != null)
                    currentElement = current.item;
            } finally {
                fullyUnlock();
            }
        }

        public boolean hasNext() {
            return current != null;
        }

        /**
         * Returns the next live successor of p, or null if no such.
         *
         * Unlike other traversal methods, iterators need to handle both:
         * - dequeued nodes (p.next == p)
         * - (possibly multiple) interior removed nodes (p.item == null)
         */
        private Node<E> nextNode(Node<E> p) {
            for (;;) {
                Node<E> s = p.next;
                if (s == p)
                    return head.next;
                if (s == null || s.item != null)
                    return s;
                p = s;
            }
        }

        public E next() {
            fullyLock();
            try {
                if (current == null)
                    throw new NoSuchElementException();
                E x = currentElement;
                lastRet = current;
                current = nextNode(current);
                currentElement = (current == null) ? null : current.item;
                return x;
            } finally {
                fullyUnlock();
            }
        }

        public void remove() {
            if (lastRet == null)
                throw new IllegalStateException();
            fullyLock();
            try {
                Node<E> node = lastRet;
                lastRet = null;
                for (Node<E> trail = head, p = trail.next;
                     p != null;
                     trail = p, p = p.next) {
                    if (p == node) {
                        unlink(p, trail);
                        break;
                    }
                }
            } finally {
                fullyUnlock();
            }
        }
    }

    /** A customized variant of Spliterators.IteratorSpliterator */
    static final class LBQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final LinkedBlockingQueue<E> queue;
        Node<E> current;    // current node; null until initialized
        int batch;          // batch size for splits
        boolean exhausted;  // true when no more nodes
        long est;           // size estimate
        LBQSpliterator(LinkedBlockingQueue<E> queue) {
            this.queue = queue;
            this.est = queue.size();
        }

        public long estimateSize() { return est; }

        public Spliterator<E> trySplit() {
            Node<E> h;
            final LinkedBlockingQueue<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((h = current) != null || (h = q.head.next) != null) &&
                h.next != null) {
                Object[] a = new Object[n];
                int i = 0;
                Node<E> p = current;
                q.fullyLock();
                try {
                    if (p != null || (p = q.head.next) != null) {
                        do {
                            if ((a[i] = p.item) != null)
                                ++i;
                        } while ((p = p.next) != null && i < n);
                    }
                } finally {
                    q.fullyUnlock();
                }
                if ((current = p) == null) {
                    est = 0L;
                    exhausted = true;
                }
                else if ((est -= i) < 0L)
                    est = 0L;
                if (i > 0) {
                    batch = i;
                    return Spliterators.spliterator
                        (a, 0, i, Spliterator.ORDERED | Spliterator.NONNULL |
                         Spliterator.CONCURRENT);
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) throw new NullPointerException();
            final LinkedBlockingQueue<E> q = this.queue;
            if (!exhausted) {
                exhausted = true;
                Node<E> p = current;
                do {
                    E e = null;
                    q.fullyLock();
                    try {
                        if (p == null)
                            p = q.head.next;
                        while (p != null) {
                            e = p.item;
                            p = p.next;
                            if (e != null)
                                break;
                        }
                    } finally {
                        q.fullyUnlock();
                    }
                    if (e != null)
                        action.accept(e);
                } while (p != null);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) throw new NullPointerException();
            final LinkedBlockingQueue<E> q = this.queue;
            if (!exhausted) {
                E e = null;
                q.fullyLock();
                try {
                    if (current == null)
                        current = q.head.next;
                    while (current != null) {
                        e = current.item;
                        current = current.next;
                        if (e != null)
                            break;
                    }
                } finally {
                    q.fullyUnlock();
                }
                if (current == null)
                    exhausted = true;
                if (e != null) {
                    action.accept(e);
                    return true;
                }
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL |
                Spliterator.CONCURRENT;
        }
    }

    /**
     * Returns a {@link Spliterator} over the elements in this queue.
     *
     * <p>The returned spliterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#CONCURRENT},
     * {@link Spliterator#ORDERED}, and {@link Spliterator#NONNULL}.
     *
     * @implNote
     * The {@code Spliterator} implements {@code trySplit} to permit limited
     * parallelism.
     *
     * @return a {@code Spliterator} over the elements in this queue
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new LBQSpliterator<E>(this);
    }

    /**
     * Saves this queue to a stream (that is, serializes it).
     *
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     * @serialData The capacity is emitted (int), followed by all of
     * its elements (each an {@code Object}) in the proper order,
     * followed by a null
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        fullyLock();
        try {
            // Write out any hidden stuff, plus capacity
            s.defaultWriteObject();

            // Write out all elements in the proper order.
            for (Node<E> p = head.next; p != null; p = p.next)
                s.writeObject(p.item);

            // Use trailing null as sentinel
            s.writeObject(null);
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Reconstitutes this queue from a stream (that is, deserializes it).
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws java.io.IOException if an I/O error occurs
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in capacity, and any hidden stuff
        s.defaultReadObject();

        count.set(0);
        last = head = new Node<E>(null);

        // Read in all elements and place in queue
        for (;;) {
            @SuppressWarnings("unchecked")
            E item = (E)s.readObject();
            if (item == null)
                break;
            add(item);
        }
    }
}
