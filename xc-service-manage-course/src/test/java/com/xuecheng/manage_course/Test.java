package com.xuecheng.manage_course;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Test {
	public static void main(String[] args) {
		int[] arr = new int[50];
		Map<Integer, Integer> map = new HashMap<>();
		int index = 0;
		int key = 7;
		for (int i = 1; i < 100; i++) {
			if (i % 2 == 0) {
				map.put(key ++, i);
			} else {
				arr[index ++] = i;
			}
		}
		System.out.println(Arrays.toString(arr));
		System.out.println(map);
	}
}