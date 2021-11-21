package com.d2lcoder.ndarray;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;

public class ChNDArray {
	public static void main(String[] args) {
		NDManager manager = NDManager.newBaseManager();

		NDArray x = manager.create(new float[] { 1f, 2f, 4f, 8f });
		NDArray y = manager.create(new float[] { 2f, 2f, 2f, 2f });

		// x + y
		System.out.println("x + y :");
		System.out.println(x.add(y));

		// x -y
		System.out.println("x - y :");
		System.out.println(x.sub(y));

		// x*y
		System.out.println("x * y :");
		System.out.println(x.mul(y));

		// x/y
		System.out.println("x / y :");
		System.out.println(x.div(y));
		
		// x^y
		System.out.println("x ^ y :");
		System.out.println(x.pow(y));
		
		System.out.println(x.exp());
	}
	
}
