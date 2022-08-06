package com.padingpading.interview.suanfa.sort;

import java.util.function.IntPredicate;

/**
 * @author libin
 * @description
 * @date 2021-08-14
 */
public class BubbleSort {

    public static  void bubbleSort(int[] arr){

        for (int i = 0; i < arr.length-1;) {
            int lastSwapIndex = 0;
            for (int j = 0; j < arr.length-1-i; j++) {
                if(arr[j]>arr[j+1]){
                    swap(arr,j,j+1);
                    lastSwapIndex = j+1;
                }
            }
            i = arr.length-lastSwapIndex;
        }

    }

    public static void swap(int[] arr, int i, int j) {
        int b = arr[i];
        arr[i] = arr[j];
        arr[j] = b;
    }

    public static void main(String[] args) {
        int[] arr = {5,6,37,4,9,3};
        bubbleSort(arr);
        System.out.println(arr.toString());
    }
}
