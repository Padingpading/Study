///*
// * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
//package com.padingpading.interview.collection.b_linkedhashmap;
//
//import java.io.IOException;
//import java.util.AbstractCollection;
//import java.util.AbstractSet;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.ConcurrentModificationException;
//import java.util.HashMap;
//import java.util.Hashtable;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.NoSuchElementException;
//import java.util.Set;
//import java.util.Spliterator;
//import java.util.Spliterators;
//import java.util.TreeMap;
//import java.util.function.BiConsumer;
//import java.util.function.BiFunction;
//import java.util.function.Consumer;
//
//
///**
// *
// */
//public class LinkedHashMap<K,V> extends HashMap<K,V> implements Map<K,V>
//{
//
//    private static final long serialVersionUID = 3801124242820219131L;
//
//    /**双向链表头结点
//     */
//    transient Entry<K,V> head;
//
//    /**双向连败尾节点
//     */
//    transient Entry<K,V> tail;
//
//    /**是否维护双向链表的<访问>顺序,默认为false。
//     * 可以看出当我们使用 access 为 true 后，我们访问元素的顺序将会在下次遍历的时候体现，最后访问的元素将最后获得。
//     * 不可变
//     */
//    final boolean accessOrder;
//
//    /**双向链表节点,在原有的node节点拓展。
//     */
//    static class Entry<K,V> extends Node<K,V> {
//        Entry<K,V> before, after;
//        Entry(int hash, K key, V value, Node<K,V> next) {
//            super(hash, key, value, next);
//        }
//    }
//
//
//    /**创建添加的节点,覆写Hashmap中。
//     */
//    Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
//        Entry<K,V> p =
//                new Entry<K,V>(hash, key, value, e);
//        // 将 Entry 接在双向链表的尾部
//        linkNodeLast(p);
//        return p;
//    }
//
//    /**将节点添加到链表中的尾部
//     */
//    private void linkNodeLast(Entry<K,V> p) {
//        // 添加元素之前双向链表尾部节点
//        Entry<K,V> last = tail;
//        // tail 指向新添加的节点
//        tail = p;
//        //如果之前 tail 指向 null 那么集合为空新添加的节点 head = tail = p
//        if (last == null)
//            head = p;
//        else {
//            // 否则将新节点的 before 引用指向之前当前链表尾部
//            p.before = last;
//            // 当前链表尾部节点的 after 指向新节点
//            last.after = p;
//        }
//    }
//
//    /**将节点从链表中删除.
//     * hashmap删除节点后会调用该方法。
//     */
//    void afterNodeRemoval(Node<K,V> e) {
//        //  从双向链表中删除对应的节点 e 为已经删除的节点
//        Entry<K,V> p =
//                (Entry<K,V>)e, b = p.before, a = p.after;
//        // 将 p 节点的前后指针引用置为 null 便于内存释放
//        p.before = p.after = null;
//        // p.before 为 null，表明 p 是头节点
//        if (b == null)
//            head = a;
//        else//否则将 p 的前驱节点连接到 p 的后驱节点
//            b.after = a;
//        if (a == null)
//            // a 为 null，表明 p 是尾节点
//            tail = b;
//        else
//            //否则将 a 的前驱节点连接到 b
//            a.before = b;
//    }
//
//    // apply src's links to dst
//    private void transferLinks(Entry<K,V> src,
//                               Entry<K,V> dst) {
//        Entry<K,V> b = dst.before = src.before;
//        Entry<K,V> a = dst.after = src.after;
//        if (b == null)
//            head = dst;
//        else
//            b.after = dst;
//        if (a == null)
//            tail = dst;
//        else
//            a.before = dst;
//    }
//
//    // overrides of HashMap hook methods
//
//    void reinitialize() {
//        super.reinitialize();
//        head = tail = null;
//    }
//
//
//    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
//        Entry<K,V> q = (Entry<K,V>)p;
//        Entry<K,V> t =
//            new Entry<K,V>(q.hash, q.key, q.value, next);
//        transferLinks(q, t);
//        return t;
//    }
//
//    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
//        TreeNode<K,V> p = new TreeNode<K,V>(hash, key, value, next);
//        linkNodeLast(p);
//        return p;
//    }
//
//    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
//        Entry<K,V> q = (Entry<K,V>)p;
//        TreeNode<K,V> t = new TreeNode<K,V>(q.hash, q.key, q.value, next);
//        transferLinks(q, t);
//        return t;
//    }
//
//   // 由上述说明大部分情况下都传 true 表示表处于创建模式
//    //由于 evict = true 那么当链表不为空的时候 且 removeEldestEntry(first) 返回 true 的时候进入if 内部
//    void afterNodeInsertion(boolean evict) {
//        Entry<K,V> first;
//        //removeEldestEntry最老的节点,开放给子类实现,返回true,进行移除。
//        if (evict && (first = head) != null && removeEldestEntry(first)) {
//            K key = first.key;
//            //移除双向链表中处于 head 的节点
//            removeNode(hash(key), key, null, false, true);
//        }
//    }
//
//    /**维护节点的访问顺序,//将被访问节点移动到链表最后
//     * 将节点e添加到链表的末尾。
//     */
//    void afterNodeAccess(Node<K,V> e) {
//        Entry<K,V> last;
//        if (accessOrder && (last = tail) != e) {
//            Entry<K,V> p =
//                (Entry<K,V>)e, b = p.before, a = p.after;
//            p.after = null;
//            //如访问节点的前驱为 null 则说明 p = head
//            if (b == null)
//                head = a;
//            else
//                b.after = a;
//            //如果 p 不为尾节点 那么将 a 的前驱设置为 b
//            if (a != null)
//                a.before = b;
//            else
//                last = b;
//            if (last == null)
//                head = p;
//            else {
//                p.before = last;
//                last.after = p;// 将 p 接在双向链表的最后
//            }
//            tail = p;
//            ++modCount;
//        }
//    }
//
//    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
//        for (Entry<K,V> e = head; e != null; e = e.after) {
//            s.writeObject(e.key);
//            s.writeObject(e.value);
//        }
//    }
//
//    /**
//     * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
//     * with the specified initial capacity and load factor.
//     *
//     * @param  initialCapacity the initial capacity
//     * @param  loadFactor      the load factor
//     * @throws IllegalArgumentException if the initial capacity is negative
//     *         or the load factor is nonpositive
//     */
//    public LinkedHashMap(int initialCapacity, float loadFactor) {
//        super(initialCapacity, loadFactor);
//        accessOrder = false;
//    }
//
//    /**
//     * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
//     * with the specified initial capacity and a default load factor (0.75).
//     *
//     * @param  initialCapacity the initial capacity
//     * @throws IllegalArgumentException if the initial capacity is negative
//     */
//    public LinkedHashMap(int initialCapacity) {
//        super(initialCapacity);
//        accessOrder = false;
//    }
//
//    /**
//     * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
//     * with the default initial capacity (16) and load factor (0.75).
//     */
//    public LinkedHashMap() {
//        super();
//        accessOrder = false;
//    }
//
//    /**
//     * Constructs an insertion-ordered <tt>LinkedHashMap</tt> instance with
//     * the same mappings as the specified map.  The <tt>LinkedHashMap</tt>
//     * instance is created with a default load factor (0.75) and an initial
//     * capacity sufficient to hold the mappings in the specified map.
//     *
//     * @param  m the map whose mappings are to be placed in this map
//     * @throws NullPointerException if the specified map is null
//     */
//    public LinkedHashMap(Map<? extends K, ? extends V> m) {
//        super();
//        accessOrder = false;
//        putMapEntries(m, false);
//    }
//
//    /**
//     * Constructs an empty <tt>LinkedHashMap</tt> instance with the
//     * specified initial capacity, load factor and ordering mode.
//     *
//     * @param  initialCapacity the initial capacity
//     * @param  loadFactor      the load factor
//     * @param  accessOrder     the ordering mode - <tt>true</tt> for
//     *         access-order, <tt>false</tt> for insertion-order
//     * @throws IllegalArgumentException if the initial capacity is negative
//     *         or the load factor is nonpositive
//     */
//    public LinkedHashMap(int initialCapacity,
//                         float loadFactor,
//                         boolean accessOrder) {
//        super(initialCapacity, loadFactor);
//        this.accessOrder = accessOrder;
//    }
//
//
//    /**
//     * Returns <tt>true</tt> if this map maps one or more keys to the
//     * specified value.
//     *
//     * @param value value whose presence in this map is to be tested
//     * @return <tt>true</tt> if this map maps one or more keys to the
//     *         specified value
//     */
//    public boolean containsValue(Object value) {
//        for (Entry<K,V> e = head; e != null; e = e.after) {
//            V v = e.value;
//            if (v == value || (value != null && value.equals(v)))
//                return true;
//        }
//        return false;
//    }
//
//    /**
//     * Returns the value to which the specified key is mapped,
//     * or {@code null} if this map contains no mapping for the key.
//     *
//     * <p>More formally, if this map contains a mapping from a key
//     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
//     * key.equals(k))}, then this method returns {@code v}; otherwise
//     * it returns {@code null}.  (There can be at most one such mapping.)
//     *
//     * <p>A return value of {@code null} does not <i>necessarily</i>
//     * indicate that the map contains no mapping for the key; it's also
//     * possible that the map explicitly maps the key to {@code null}.
//     * The {@link #containsKey containsKey} operation may be used to
//     * distinguish these two cases.
//     */
//    public V get(Object key) {
//        Node<K,V> e;
//        if ((e = getNode(hash(key), key)) == null)
//            return null;
//        if (accessOrder)
//            afterNodeAccess(e);
//        return e.value;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public V getOrDefault(Object key, V defaultValue) {
//       Node<K,V> e;
//       if ((e = getNode(hash(key), key)) == null)
//           return defaultValue;
//       if (accessOrder)
//           afterNodeAccess(e);
//       return e.value;
//   }
//
//    /**
//     * {@inheritDoc}
//     */
//    public void clear() {
//        super.clear();
//        head = tail = null;
//    }
//    //LinkedHashMap 默认返回 false 则不删除节点。 返回 true 双向链表中处于 head 的节点
//    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
//        return false;
//    }
//
//    /**
//     * Returns a {@link Set} view of the keys contained in this map.
//     * The set is backed by the map, so changes to the map are
//     * reflected in the set, and vice-versa.  If the map is modified
//     * while an iteration over the set is in progress (except through
//     * the iterator's own <tt>remove</tt> operation), the results of
//     * the iteration are undefined.  The set supports element removal,
//     * which removes the corresponding mapping from the map, via the
//     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
//     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
//     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
//     * operations.
//     * Its {@link Spliterator} typically provides faster sequential
//     * performance but much poorer parallel performance than that of
//     * {@code HashMap}.
//     *
//     * @return a set view of the keys contained in this map
//     */
//    public Set<K> keySet() {
//        Set<K> ks = keySet;
//        if (ks == null) {
//            ks = new LinkedKeySet();
//            keySet = ks;
//        }
//        return ks;
//    }
//
//    final class LinkedKeySet extends AbstractSet<K> {
//        public final int size()                 { return size; }
//        public final void clear()               { LinkedHashMap.this.clear(); }
//        public final Iterator<K> iterator() {
//            return new LinkedKeyIterator();
//        }
//        public final boolean contains(Object o) { return containsKey(o); }
//        public final boolean remove(Object key) {
//            return removeNode(hash(key), key, null, false, true) != null;
//        }
//        public final Spliterator<K> spliterator()  {
//            return Spliterators.spliterator(this, Spliterator.SIZED |
//                                            Spliterator.ORDERED |
//                                            Spliterator.DISTINCT);
//        }
//        public final void forEach(Consumer<? super K> action) {
//            if (action == null)
//                throw new NullPointerException();
//            int mc = modCount;
//            for (Entry<K,V> e = head; e != null; e = e.after)
//                action.accept(e.key);
//            if (modCount != mc)
//                throw new ConcurrentModificationException();
//        }
//    }
//
//    /**
//     * Returns a {@link Collection} view of the values contained in this map.
//     * The collection is backed by the map, so changes to the map are
//     * reflected in the collection, and vice-versa.  If the map is
//     * modified while an iteration over the collection is in progress
//     * (except through the iterator's own <tt>remove</tt> operation),
//     * the results of the iteration are undefined.  The collection
//     * supports element removal, which removes the corresponding
//     * mapping from the map, via the <tt>Iterator.remove</tt>,
//     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
//     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
//     * support the <tt>add</tt> or <tt>addAll</tt> operations.
//     * Its {@link Spliterator} typically provides faster sequential
//     * performance but much poorer parallel performance than that of
//     * {@code HashMap}.
//     *
//     * @return a view of the values contained in this map
//     */
//    public Collection<V> values() {
//        Collection<V> vs = values;
//        if (vs == null) {
//            vs = new LinkedValues();
//            values = vs;
//        }
//        return vs;
//    }
//
//    final class LinkedValues extends AbstractCollection<V> {
//        public final int size()                 { return size; }
//        public final void clear()               { LinkedHashMap.this.clear(); }
//        public final Iterator<V> iterator() {
//            return new LinkedValueIterator();
//        }
//        public final boolean contains(Object o) { return containsValue(o); }
//        public final Spliterator<V> spliterator() {
//            return Spliterators.spliterator(this, Spliterator.SIZED |
//                                            Spliterator.ORDERED);
//        }
//        public final void forEach(Consumer<? super V> action) {
//            if (action == null)
//                throw new NullPointerException();
//            int mc = modCount;
//            for (Entry<K,V> e = head; e != null; e = e.after)
//                action.accept(e.value);
//            if (modCount != mc)
//                throw new ConcurrentModificationException();
//        }
//    }
//
//    /**
//     * Returns a {@link Set} view of the mappings contained in this map.
//     * The set is backed by the map, so changes to the map are
//     * reflected in the set, and vice-versa.  If the map is modified
//     * while an iteration over the set is in progress (except through
//     * the iterator's own <tt>remove</tt> operation, or through the
//     * <tt>setValue</tt> operation on a map entry returned by the
//     * iterator) the results of the iteration are undefined.  The set
//     * supports element removal, which removes the corresponding
//     * mapping from the map, via the <tt>Iterator.remove</tt>,
//     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
//     * <tt>clear</tt> operations.  It does not support the
//     * <tt>add</tt> or <tt>addAll</tt> operations.
//     * Its {@link Spliterator} typically provides faster sequential
//     * performance but much poorer parallel performance than that of
//     * {@code HashMap}.
//     *
//     * @return a set view of the mappings contained in this map
//     */
//    public Set<Map.Entry<K,V>> entrySet() {
//        Set<Map.Entry<K,V>> es;
//        return (es = entrySet) == null ? (entrySet = new LinkedEntrySet()) : es;
//    }
//
//    final class LinkedEntrySet extends AbstractSet<Map.Entry<K,V>> {
//        public final int size()                 { return size; }
//        public final void clear()               { LinkedHashMap.this.clear(); }
//        public final Iterator<Map.Entry<K,V>> iterator() {
//            return new LinkedEntryIterator();
//        }
//        public final boolean contains(Object o) {
//            if (!(o instanceof Map.Entry))
//                return false;
//            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
//            Object key = e.getKey();
//            Node<K,V> candidate = getNode(hash(key), key);
//            return candidate != null && candidate.equals(e);
//        }
//        public final boolean remove(Object o) {
//            if (o instanceof Map.Entry) {
//                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
//                Object key = e.getKey();
//                Object value = e.getValue();
//                return removeNode(hash(key), key, value, true, true) != null;
//            }
//            return false;
//        }
//        public final Spliterator<Map.Entry<K,V>> spliterator() {
//            return Spliterators.spliterator(this, Spliterator.SIZED |
//                                            Spliterator.ORDERED |
//                                            Spliterator.DISTINCT);
//        }
//        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
//            if (action == null)
//                throw new NullPointerException();
//            int mc = modCount;
//            for (Entry<K,V> e = head; e != null; e = e.after)
//                action.accept(e);
//            if (modCount != mc)
//                throw new ConcurrentModificationException();
//        }
//    }
//
//    // Map overrides
//
//    public void forEach(BiConsumer<? super K, ? super V> action) {
//        if (action == null)
//            throw new NullPointerException();
//        int mc = modCount;
//        for (Entry<K,V> e = head; e != null; e = e.after)
//            action.accept(e.key, e.value);
//        if (modCount != mc)
//            throw new ConcurrentModificationException();
//    }
//
//    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
//        if (function == null)
//            throw new NullPointerException();
//        int mc = modCount;
//        for (Entry<K,V> e = head; e != null; e = e.after)
//            e.value = function.apply(e.key, e.value);
//        if (modCount != mc)
//            throw new ConcurrentModificationException();
//    }
//
//    // Iterators
//
//    abstract class LinkedHashIterator {
//        Entry<K,V> next;
//        Entry<K,V> current;
//        int expectedModCount;
//
//        LinkedHashIterator() {
//            next = head;
//            expectedModCount = modCount;
//            current = null;
//        }
//
//        public final boolean hasNext() {
//            return next != null;
//        }
//
//        final Entry<K,V> nextNode() {
//            Entry<K,V> e = next;
//            if (modCount != expectedModCount)
//                throw new ConcurrentModificationException();
//            if (e == null)
//                throw new NoSuchElementException();
//            current = e;
//            next = e.after;
//            return e;
//        }
//
//        public final void remove() {
//            Node<K,V> p = current;
//            if (p == null)
//                throw new IllegalStateException();
//            if (modCount != expectedModCount)
//                throw new ConcurrentModificationException();
//            current = null;
//            K key = p.key;
//            removeNode(hash(key), key, null, false, false);
//            expectedModCount = modCount;
//        }
//    }
//
//    final class LinkedKeyIterator extends LinkedHashIterator
//        implements Iterator<K> {
//        public final K next() { return nextNode().getKey(); }
//    }
//
//    final class LinkedValueIterator extends LinkedHashIterator
//        implements Iterator<V> {
//        public final V next() { return nextNode().value; }
//    }
//
//    final class LinkedEntryIterator extends LinkedHashIterator
//        implements Iterator<Map.Entry<K,V>> {
//        public final Map.Entry<K,V> next() { return nextNode(); }
//    }
//
//
//}
