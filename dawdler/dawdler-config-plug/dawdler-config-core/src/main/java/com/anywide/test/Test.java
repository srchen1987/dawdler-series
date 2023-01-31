package com.anywide.test;

import java.util.concurrent.atomic.AtomicInteger;

public class Test {

	public static AtomicInteger num = new AtomicInteger(0);

	public static void main(String[] args) throws InterruptedException {
		Runnable runnable = () -> {
			for (long i = 0; i < 1000000000; i++) {
				num.getAndAdd(1);
			}
			System.out.println(Thread.currentThread().getName() + "执行结束!");
		};

		Thread t1 = new Thread(runnable);
		Thread t2 = new Thread(runnable);
		t1.start();
		t2.start();
		Thread.sleep(1000);
		System.out.println("num = " + num);
	}

}
