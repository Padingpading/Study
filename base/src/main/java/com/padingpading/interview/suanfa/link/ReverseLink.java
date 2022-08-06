package com.padingpading.interview.suanfa.link;

/**
 * @author libin
 * @description
 * @date 2021-08-13
 */
public class ReverseLink {

    public static void main(String[] args) {


    }


    public  static Node reverseList(Node head){
        if(head==null||head.next==null){
            return head;
        }
        Node res = reverseList(head.next);
        head.next.next = head;
        head.next = null;
        return res;
    }


    public static class Node{
        int value;
        Node next;
    }
}
