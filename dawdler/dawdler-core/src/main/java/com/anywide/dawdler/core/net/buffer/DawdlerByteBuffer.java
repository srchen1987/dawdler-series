package com.anywide.dawdler.core.net.buffer;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class DawdlerByteBuffer {
	private ByteBuffer byteBuffer;
	private long addr;

	public long getAddr() {
		return addr;
	}

	public void setAddr(long addr) {
		this.addr = addr;
	}

	public DawdlerByteBuffer(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
	}

	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	public void setByteBuffer(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
	}

	public void close() {
		if (byteBuffer.isDirect()) {
			try {
				DirectBufferCreator.freeMemory(addr);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
		} else {
			byteBuffer.clear();
		}
	}
}
