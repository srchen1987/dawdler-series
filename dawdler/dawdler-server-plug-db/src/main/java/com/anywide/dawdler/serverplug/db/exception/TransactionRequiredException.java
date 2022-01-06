
package com.anywide.dawdler.serverplug.db.exception;

/**
*
* @Title TransactionRequiredException.java
* @Description 代替javax.transaction.TransactionRequiredException，jdk8之后移除了TransactionRequiredException注解
* @author jackson.song
* @date 2021年12月18日
* @version V1.0
* @email suxuan696@gmail.com
*/
public class TransactionRequiredException extends RuntimeException{

	private static final long serialVersionUID = -883131451486676208L;
	public TransactionRequiredException(String message) {
		super(message);
	}
}
