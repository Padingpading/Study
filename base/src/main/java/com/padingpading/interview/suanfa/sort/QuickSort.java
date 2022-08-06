package com.padingpading.interview.suanfa.sort;

/**
 * @author libin
 * @description
 * @date 2021-08-13
 */
public class QuickSort {

    public static void sort(int[] arr) {
        quickSort(arr, 0, arr.length - 1);
    }

    public static void quickSort(int[] arr, int l, int r) {
        if (l >= r) {
            return;
        }
        int lt = l;
        int i = l + 1;
        int gt = r + 1;
        while (i < gt) {
            if (arr[l] > arr[i]) {
                lt++;
                swap(arr, lt, i);
                i++;
            }
            if (arr[l] < arr[i]) {
                gt--;
                swap(arr,i,gt);
            } else {
                i++;
            }
        }
        swap(arr,l,lt);
        quickSort(arr,l,lt-1);
        quickSort(arr,gt,r);
    }

    public static void swap(int[] arr, int i, int j) {
        int b = arr[i];
        arr[i] = arr[j];
        arr[j] = b;
    }

    public static void main(String[] args) {
        int[] arr = {5,6,37,4,9,3};
        sort(arr);
        System.out.println(arr);
    }
}
