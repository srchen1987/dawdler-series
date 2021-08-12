package com.anywide.dawdler.rabbitmq.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.anywide.dawdler.rabbitmq.channel.ChannelWarpper;
import com.anywide.dawdler.rabbitmq.connection.pool.ConnectionPool;
import com.rabbitmq.client.BlockedCallback;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ExceptionHandler;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.UnblockedCallback;

/**
*
* @Title AMQPConnectionWarpper.java
* @Description Rabbitmq的Connection包装类
* @author jackson.song
* @date 2021年4月11日
* @version V1.0
* @email suxuan696@gmail.com
*/
public class AMQPConnectionWarpper implements Connection {
	private Connection target;
	private ConnectionPool connectionPool;
	private int channelSize;
	private int getChannelTimeOut;
	private Semaphore semaphore;
	LinkedList<ChannelWarpper> channels = new LinkedList<ChannelWarpper>();

	public AMQPConnectionWarpper(Connection target, ConnectionPool connectionPool, int channelSize,
			int getChannelTimeOut) {
		this.target = target;
		this.connectionPool = connectionPool;
		this.channelSize = channelSize;
		this.getChannelTimeOut = getChannelTimeOut;
		this.semaphore = new Semaphore(channelSize);
	}

	@Override
	public void addShutdownListener(ShutdownListener listener) {
		target.addShutdownListener(listener);
	}

	@Override
	public void removeShutdownListener(ShutdownListener listener) {
		target.removeShutdownListener(listener);
	}

	@Override
	public ShutdownSignalException getCloseReason() {
		return target.getCloseReason();
	}

	@Override
	public void notifyListeners() {
		target.notifyListeners();
	}

	@Override
	public boolean isOpen() {
		return target.isOpen();
	}

	@Override
	public InetAddress getAddress() {
		return target.getAddress();
	}

	@Override
	public int getPort() {
		return target.getPort();
	}

	@Override
	public int getChannelMax() {
		return target.getChannelMax();
	}

	@Override
	public int getFrameMax() {
		return target.getFrameMax();
	}

	@Override
	public int getHeartbeat() {
		return target.getHeartbeat();
	}

	@Override
	public Map<String, Object> getClientProperties() {
		return target.getClientProperties();
	}

	@Override
	public String getClientProvidedName() {
		return target.getClientProvidedName();
	}

	@Override
	public Map<String, Object> getServerProperties() {
		return target.getServerProperties();
	}

	@Override
	public Channel createChannel() throws IOException {
		try {
			if (semaphore.tryAcquire(getChannelTimeOut, TimeUnit.MILLISECONDS)) {
				synchronized (channels) {
					if (channels.size() > 0) {
						return channels.removeFirst();
					}
				}
				Channel channel = target.createChannel();
				return new ChannelWarpper(channel, channels, semaphore);
			} else {
				throw new IOException(new TimeoutException("timed out after " + getChannelTimeOut + " milliseconds !"));
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		}

	}

	@Override
	public Channel createChannel(int channelNumber) throws IOException {
		return createChannel();
	}

	@Override
	public void close() throws IOException {
		connectionPool.returnObject(this);
	}

	public void physicsClose() throws IOException {
		target.close();
	}

	@Override
	public void close(int closeCode, String closeMessage) throws IOException {
		close();
	}

	@Override
	public void close(int timeout) throws IOException {
		close();
	}

	@Override
	public void close(int closeCode, String closeMessage, int timeout) throws IOException {
		close();
	}

	public Connection getTarget() {
		return target;
	}

	@Override
	public void abort() {
		target.abort();
	}

	@Override
	public void abort(int closeCode, String closeMessage) {
		target.abort(closeCode, closeMessage);
	}

	@Override
	public void abort(int timeout) {
		target.abort(timeout);
	}

	@Override
	public void abort(int closeCode, String closeMessage, int timeout) {
		target.abort(timeout);
	}

	@Override
	public void addBlockedListener(BlockedListener listener) {
		target.addBlockedListener(listener);
	}

	@Override
	public BlockedListener addBlockedListener(BlockedCallback blockedCallback, UnblockedCallback unblockedCallback) {
		return target.addBlockedListener(blockedCallback, unblockedCallback);
	}

	@Override
	public boolean removeBlockedListener(BlockedListener listener) {
		return target.removeBlockedListener(listener);
	}

	@Override
	public void clearBlockedListeners() {
		target.clearBlockedListeners();
	}

	@Override
	public ExceptionHandler getExceptionHandler() {
		return target.getExceptionHandler();
	}

	@Override
	public String getId() {
		return target.getId();
	}

	@Override
	public void setId(String id) {
		target.setId(id);
	}

	public int getChannelSize() {
		return channelSize;
	}
}
