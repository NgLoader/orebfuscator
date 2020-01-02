package com.lishid.orebfuscator.utils;

import java.util.Random;

public class ArrayUtil {

	private static final Random RANDOM = new Random();

	public static void shuffle(Integer[] array) {
		for (int index = 1; index < array.length; index++) {
			int newIndex = ArrayUtil.RANDOM.nextInt(index);
			int oldIndex = array[index];

			array[index] = array[newIndex];
			array[newIndex] = oldIndex;
		}
	}

}