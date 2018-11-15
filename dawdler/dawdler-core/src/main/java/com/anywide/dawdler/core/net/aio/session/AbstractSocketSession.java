/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anywide.dawdler.core.net.aio.session;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.core.handler.IoHandlerFactory;
import com.anywide.dawdler.core.net.buffer.BufferFactory;
import com.anywide.dawdler.core.serializer.Serializer;
import com.anywide.dawdler.util.HashedWheelTimerSingleCreator;
import com.anywide.dawdler.util.InvokeFuture;
import com.anywide.dawdler.util.Timeout;
import com.anywide.dawdler.util.TimerTask;
import sun.nio.ch.DirectBuffer;
/**
 * 
 * @Title:  AbstractSocketSession.java
 * @Description:    抽象session类 提供了  读写超时重连、心跳处理等方式。   
 * @author: jackson.song    
 * @date:   2015年03月11日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public abstract class AbstractSocketSession {
	private static Logger logger = LoggerFactory.getLogger(AbstractSocketSession.class);
	private Object writeLock=new Object();
	protected final AsynchronousSocketChannel channel;
	protected SocketAddress remoteAddress;
	protected String describe;
	protected int remotePort;
	protected volatile long lastReadTime;
	protected volatile long lastWriteTime;
	protected Timeout readerIdleTimeout;
	protected Timeout writerIdleTimeout;
    protected ByteBuffer readBuffer;
    protected ByteBuffer writeBuffer;
    protected int dataLength;
    protected int packageSize;
	protected int alreadyRead;
	protected byte[] appendData;
	private CountDownLatch sessionInitLatch = new CountDownLatch(1);
	protected boolean compress;
	protected Serializer serializer; 
	protected boolean needNext;
	protected AtomicBoolean close = new AtomicBoolean();
	protected AtomicBoolean markClose = new AtomicBoolean();
	protected byte headData;
	protected int position;
	private static final long writerIdleTimeMillis=8000;
	private static final long readerIdleTimeMillis=writerIdleTimeMillis*15;
	private AtomicLong sequence = new AtomicLong(0);
	private boolean server;
	protected Map<Long, InvokeFuture<?>> futures = new ConcurrentHashMap<Long, InvokeFuture<?>>();
	private String groupName; 
	private boolean authored;
	public boolean isAuthored() {
		return authored;
	}
	public void setAuthored(boolean authored) {
		this.authored = authored;
	} 
	public Timeout getReaderIdleTimeout() {
		return readerIdleTimeout;
	}
	public void setReaderIdleTimeout(Timeout readerIdleTimeout) {
		this.readerIdleTimeout = readerIdleTimeout;
	}
	public Timeout getWriterIdleTimeout() {
		return writerIdleTimeout;
	}
	public void setWriterIdleTimeout(Timeout writerIdleTimeout) {
		this.writerIdleTimeout = writerIdleTimeout;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public Map<Long, InvokeFuture<?>> getFutures() {
		return futures;
	}
	public void setFutures(Map<Long, InvokeFuture<?>> futures) {
		this.futures = futures;
	}
	public ByteBuffer getWriteBuffer() {
		return writeBuffer;
	}
	public abstract void parseHead(ByteBuffer buffer);
	public int getHeadData() {
		return headData;
	}
	public CountDownLatch getInitLatch() {
		return sessionInitLatch;
	}
	public AbstractSocketSession(AsynchronousSocketChannel channel,boolean init) throws IOException {
		this.channel=channel;
		if((server=init))
			init();
	}
	public AbstractSocketSession(AsynchronousSocketChannel channel) throws IOException {
		this(channel,true);
	}
	public final static int CAPACITY = 1024*16;
	public void init() throws IOException{
		remoteAddress=channel.getRemoteAddress();
		describe= "local:" + channel.getLocalAddress() + " remote:"
				+ remoteAddress + " hashCode/" + hashCode();
		remotePort = Integer.parseInt(this.toString().split(" ")[1]
				.split(":")[2]);
		readBuffer = BufferFactory.createDirectBuffer(CAPACITY); 
		writeBuffer = BufferFactory.createDirectBuffer(CAPACITY); 
		writerIdleTimeout = HashedWheelTimerSingleCreator.getHashedWheelTimer().newTimeout(new WriterIdleTimeoutTask(),writerIdleTimeMillis, TimeUnit.MILLISECONDS);
		readerIdleTimeout = HashedWheelTimerSingleCreator.getHashedWheelTimer().newTimeout(new ReaderIdleTimeoutTask(),readerIdleTimeMillis, TimeUnit.MILLISECONDS);
	}

	public void clean(final ByteBuffer byteBuffer) {
		if (byteBuffer.isDirect()) {
			((DirectBuffer) byteBuffer).cleaner().clean();
		}else
			byteBuffer.clear();
	}
	public void close(){
		close(true);
	}
	public abstract void close(boolean reconnect);
	public boolean isClose(){
		return close.get();
	}
	public boolean isReceived(){
		return state==SessionState.RECEIVE;
	}
	public boolean isConnected(){
		return state==SessionState.CONNECTION;
	}
	public void appendData(byte[] data){
		if(data.length>0){
			System.arraycopy(data, 0, appendData,position, data.length);
			position+=data.length;
		}
			
	}
	public byte[] getAppendData() {
		return appendData;
	}
	public void appendReadLenth(int length){
		alreadyRead+=length;
	}
	public int getDataLength() {
		return dataLength;
	}
	public int getRemanentDataLength(){
		return packageSize-alreadyRead;
	}
	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}
	public int getPackageSize() {
		return packageSize;
	}
	public void setPackageSize(int packageSize) {
		this.packageSize = packageSize;
	}
	public static enum SessionState{
		RECEIVE,
		CONNECTION,
		FAIL
	}
	private SessionState state=SessionState.RECEIVE;
	public void toConnectionState(){
		state=SessionState.CONNECTION;
	}
	public void toReceiveState(){
		state=SessionState.RECEIVE;
	}
	public void toPrepare(){
		appendData=null;
		dataLength=0;
		packageSize=0;
		alreadyRead=0;	
		position=0;
		state=SessionState.RECEIVE;
		serializer=null;
		compress=false;
		needNext=false;
	}
	public void clearBuffer(ByteBuffer buffer){
		buffer.clear();
	}
	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}
	public long getLastReadTime() {
		return lastReadTime;
	}
	public void setLastReadTime(long lastReadTime) {
		this.lastReadTime = lastReadTime;
	}
	public long getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(long lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
	public AsynchronousSocketChannel getChannel() {
		return channel;
	}
	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	public String getDescribe() {
		return describe;
	}
	public int getRemotePort() {
		return remotePort;
	}
	@Override
	public String toString() {
		return describe+"\tlastRead:"+(System.currentTimeMillis()-lastReadTime)+"\tLastWrite"+(System.currentTimeMillis()-lastWriteTime);
	}
	public void markClose(){
		markClose.set(true);
	}
	
	public abstract void messageCmpleted();
	public void sentHeartbeat(){
		ByteBuffer bf = ByteBuffer.allocate(4);
		bf.putInt(0);
		bf.flip();
		write(bf);
	}

	public void write(ByteBuffer obj) {
		setLastWriteTime(System.currentTimeMillis());
		synchronized (channel) {
			try {
				while (obj.hasRemaining()) {
					Future<Integer> future = channel.write(obj);
					future.get(1000, TimeUnit.MILLISECONDS);
				}
			} catch (Exception e) {
				logger.error("", e);
				close();
			} finally {
				obj.clear();
				obj = null;
				// release buffer
			}
		}
	} 
	
 	
	
	public Object getWriteLock() {
		return writeLock;
	}


	public boolean isNeedNext() {
		return needNext;
	}


	public void setNeedNext(boolean needNext) {
		this.needNext = needNext;
	}
	private final class WriterIdleTimeoutTask implements TimerTask{
		@Override
		public void run(Timeout timeout) throws Exception {
			 if (timeout.isCancelled() || isClose())
	                return;
	            long currentTime = System.currentTimeMillis();
	            long lastWriteTime = AbstractSocketSession.this.lastWriteTime;
	            long nextDelay = writerIdleTimeMillis - (currentTime - lastWriteTime);
	            if (nextDelay <= 0) {
	            	IoHandlerFactory.getInstance().channelIdle(AbstractSocketSession.this, SessionIdleType.WRITE);
	            	AbstractSocketSession.this.writerIdleTimeout =
	            			timeout.timer().newTimeout(this,writerIdleTimeMillis, TimeUnit.MILLISECONDS);
	            	AbstractSocketSession.this.sentHeartbeat();
	            } else {
	            	AbstractSocketSession.this.writerIdleTimeout =
	            			timeout.timer().newTimeout(this,nextDelay, TimeUnit.MILLISECONDS);
	            }
		}
	}
	private final class ReaderIdleTimeoutTask implements TimerTask{
		@Override
		public void run(Timeout timeout) throws Exception {
			 if (timeout.isCancelled() || isClose())
	                return;
	            long currentTime = System.currentTimeMillis();
	            long lastReadTime = AbstractSocketSession.this.lastReadTime;
	            long nextDelay = readerIdleTimeMillis - (currentTime - lastReadTime);
	            if (nextDelay <= 0) {
	            	IoHandlerFactory.getInstance().channelIdle(AbstractSocketSession.this, SessionIdleType.READ);
	            	AbstractSocketSession.this.readerIdleTimeout =
	            			timeout.timer().newTimeout(this,readerIdleTimeMillis, TimeUnit.MILLISECONDS);
		            if(server){
		            	AbstractSocketSession.this.close(false);
		            }
		            else{
		            	AbstractSocketSession.this.close();
		            }
	            } else {
	            	AbstractSocketSession.this.readerIdleTimeout =
	            			timeout.timer().newTimeout(this,nextDelay, TimeUnit.MILLISECONDS);
	            }
		}
		
	}
	public long getSequence(){
		return sequence.incrementAndGet();
	}
}
