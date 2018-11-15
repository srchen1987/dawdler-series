package client;

import com.anywide.dawdler.client.PropertiesCenter;

public class Main {
	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		PropertiesCenter p = PropertiesCenter.getInstance();
		long t2 = System.currentTimeMillis();
		System.out.println(p);
		System.out.println(t2-t1);
		
	}
}
