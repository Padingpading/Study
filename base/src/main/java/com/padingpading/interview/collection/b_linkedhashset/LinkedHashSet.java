//
//
//package com.padingpading.interview.collection.b_linkedhashset;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.ConcurrentModificationException;
//import java.util.HashSet;
//import java.util.Hashtable;
//import java.util.Set;
//import java.util.Spliterator;
//import java.util.Spliterators;
//import java.util.TreeSet;
//
///**
// * HashSet:封装具体的map实现,当创建LinkedHashSet时,在hashset内部创建的是LinkedHashMap。
// */
//public class LinkedHashSet<E>
//    extends HashSet<E>
//    implements Set<E>, Cloneable, java.io.Serializable {
//
//    private static final long serialVersionUID = -2851667679971038690L;
//
//    /**初始化
//     */
//    public LinkedHashSet(int initialCapacity, float loadFactor) {
//        super(initialCapacity, loadFactor, true);
//    }
//
//    /**初始化
//     */
//    public LinkedHashSet(int initialCapacity) {
//        super(initialCapacity, .75f, true);
//    }
//
//    /**初始化
//     */
//    public LinkedHashSet() {
//        super(16, .75f, true);
//    ····}
//
//    /**初始化
//     */
//    public LinkedHashSet(Collection<? extends E> c) {
//        super(Math.max(2*c.size(), 11), .75f, true);
//        addAll(c);
//    }
//
//    /**
//     */
//    @Override
//    public Spliterator<E> spliterator() {
//        return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.ORDERED);
//    }
//}
