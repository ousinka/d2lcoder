package com.d2lcoder.ndarray;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;

public class ChConcat {
	public static void main(String[] args) {
		NDManager manager = NDManager.newBaseManager();

		NDArray x = manager.arange(12f).reshape(3, 4);
		NDArray y = manager.ones(new Shape(3, 4)); // 全1
		System.out.println(x);
		System.out.println(y);

		System.out.println(x.concat(y));
		System.out.println(x.concat(y, 1));
	}
}
