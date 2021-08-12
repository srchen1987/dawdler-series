package com.anywide.dawdler.rabbitmq.channel;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.Basic.RecoverOk;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Exchange.BindOk;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.AMQP.Exchange.DeleteOk;
import com.rabbitmq.client.AMQP.Exchange.UnbindOk;
import com.rabbitmq.client.AMQP.Queue.PurgeOk;
import com.rabbitmq.client.AMQP.Tx.CommitOk;
import com.rabbitmq.client.AMQP.Tx.RollbackOk;
import com.rabbitmq.client.AMQP.Tx.SelectOk;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Command;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerShutdownSignalCallback;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ReturnCallback;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
*
* @Title ChannelWarpper.java
* @Description Rabbitmq的Channel包装类
* @author jackson.song
* @date 2021年4月11日
* @version V1.0
* @email suxuan696@gmail.com
*/
public class ChannelWarpper implements Channel {
	private Channel target;
	private LinkedList<ChannelWarpper> channels;
	private Semaphore semaphore;
	private boolean needResponse;

	public ChannelWarpper(Channel target, LinkedList<ChannelWarpper> channels, Semaphore semaphore) {
		this.target = target;
		this.channels = channels;
		this.semaphore = semaphore;
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
	public int getChannelNumber() {
		return target.getChannelNumber();
	}

	@Override
	public Connection getConnection() {
		return target.getConnection();
	}

	@Override
	public void close() throws IOException, TimeoutException {
		if (needResponse) {
			target.close();
		} else {
			synchronized (channels) {
				channels.addLast(this);
			}
		}
		semaphore.release();
	}

	@Override
	public void close(int closeCode, String closeMessage) throws IOException, TimeoutException {
		if (needResponse) {
			target.close(closeCode, closeMessage);
		} else {
			synchronized (channels) {
				channels.addLast(this);
			}
		}
		semaphore.release();
	}

	@Override
	public void abort() throws IOException {
		target.abort();
	}

	@Override
	public void abort(int closeCode, String closeMessage) throws IOException {
		target.abort(closeCode, closeMessage);
	}

	@Override
	public void addReturnListener(ReturnListener listener) {
		needResponse = true;
		target.addReturnListener(listener);
	}

	@Override
	public ReturnListener addReturnListener(ReturnCallback returnCallback) {
		needResponse = true;
		return target.addReturnListener(returnCallback);
	}

	@Override
	public boolean removeReturnListener(ReturnListener listener) {
		return target.removeReturnListener(listener);
	}

	@Override
	public void clearReturnListeners() {
		target.clearReturnListeners();
	}

	@Override
	public void addConfirmListener(ConfirmListener listener) {
		needResponse = true;
		target.addConfirmListener(listener);
	}

	@Override
	public ConfirmListener addConfirmListener(ConfirmCallback ackCallback, ConfirmCallback nackCallback) {
		needResponse = true;
		return target.addConfirmListener(ackCallback, nackCallback);
	}

	@Override
	public boolean removeConfirmListener(ConfirmListener listener) {
		return target.removeConfirmListener(listener);
	}

	@Override
	public void clearConfirmListeners() {
		target.clearConfirmListeners();
	}

	@Override
	public Consumer getDefaultConsumer() {
		return target.getDefaultConsumer();
	}

	@Override
	public void setDefaultConsumer(Consumer consumer) {
		target.setDefaultConsumer(consumer);
	}

	@Override
	public void basicQos(int prefetchSize, int prefetchCount, boolean global) throws IOException {
		target.basicQos(prefetchSize, prefetchCount, global);
	}

	@Override
	public void basicQos(int prefetchCount, boolean global) throws IOException {
		target.basicQos(prefetchCount, global);
	}

	@Override
	public void basicQos(int prefetchCount) throws IOException {
		target.basicQos(prefetchCount);

	}

	@Override
	public void basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body)
			throws IOException {
		target.basicPublish(exchange, routingKey, props, body);

	}

	@Override
	public void basicPublish(String exchange, String routingKey, boolean mandatory, BasicProperties props, byte[] body)
			throws IOException {
		target.basicPublish(exchange, routingKey, mandatory, props, body);

	}

	@Override
	public void basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate,
			BasicProperties props, byte[] body) throws IOException {
		target.basicPublish(exchange, routingKey, mandatory, immediate, props, body);
	}

	@Override
	public DeclareOk exchangeDeclare(String exchange, String type) throws IOException {
		return target.exchangeDeclare(exchange, type);
	}

	@Override
	public DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type) throws IOException {
		return target.exchangeDeclare(exchange, type);
	}

	@Override
	public DeclareOk exchangeDeclare(String exchange, String type, boolean durable) throws IOException {
		return target.exchangeDeclare(exchange, type, durable);
	}

	@Override
	public DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable) throws IOException {
		return target.exchangeDeclare(exchange, type, durable);
	}

	@Override
	public DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete,
			Map<String, Object> arguments) throws IOException {
		return target.exchangeDeclare(exchange, type, durable, autoDelete, arguments);
	}

	@Override
	public DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete,
			Map<String, Object> arguments) throws IOException {
		return target.exchangeDeclare(exchange, type, durable, autoDelete, arguments);
	}

	@Override
	public DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete,
			boolean internal, Map<String, Object> arguments) throws IOException {
		return target.exchangeDeclare(exchange, type, durable, autoDelete, internal, arguments);
	}

	@Override
	public DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete,
			boolean internal, Map<String, Object> arguments) throws IOException {
		return target.exchangeDeclare(exchange, type, durable, autoDelete, internal, arguments);
	}

	@Override
	public void exchangeDeclareNoWait(String exchange, String type, boolean durable, boolean autoDelete,
			boolean internal, Map<String, Object> arguments) throws IOException {
		target.exchangeDeclareNoWait(exchange, type, durable, autoDelete, internal, arguments);
	}

	@Override
	public void exchangeDeclareNoWait(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete,
			boolean internal, Map<String, Object> arguments) throws IOException {
		target.exchangeDeclareNoWait(exchange, type, durable, autoDelete, internal, arguments);

	}

	@Override
	public DeclareOk exchangeDeclarePassive(String name) throws IOException {
		return target.exchangeDeclarePassive(name);
	}

	@Override
	public DeleteOk exchangeDelete(String exchange, boolean ifUnused) throws IOException {
		return target.exchangeDelete(exchange, ifUnused);
	}

	@Override
	public void exchangeDeleteNoWait(String exchange, boolean ifUnused) throws IOException {
		target.exchangeDeleteNoWait(exchange, ifUnused);

	}

	@Override
	public DeleteOk exchangeDelete(String exchange) throws IOException {
		return target.exchangeDelete(exchange);
	}

	@Override
	public BindOk exchangeBind(String destination, String source, String routingKey) throws IOException {
		return target.exchangeBind(destination, source, routingKey);
	}

	@Override
	public BindOk exchangeBind(String destination, String source, String routingKey, Map<String, Object> arguments)
			throws IOException {
		return target.exchangeBind(destination, source, routingKey, arguments);
	}

	@Override
	public void exchangeBindNoWait(String destination, String source, String routingKey, Map<String, Object> arguments)
			throws IOException {
		target.exchangeBindNoWait(destination, source, routingKey, arguments);
	}

	@Override
	public UnbindOk exchangeUnbind(String destination, String source, String routingKey) throws IOException {
		return target.exchangeUnbind(destination, source, routingKey);
	}

	@Override
	public UnbindOk exchangeUnbind(String destination, String source, String routingKey, Map<String, Object> arguments)
			throws IOException {
		return target.exchangeUnbind(destination, source, routingKey, arguments);
	}

	@Override
	public void exchangeUnbindNoWait(String destination, String source, String routingKey,
			Map<String, Object> arguments) throws IOException {
		target.exchangeUnbindNoWait(destination, source, routingKey, arguments);
	}

	@Override
	public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclare() throws IOException {
		return target.queueDeclare();
	}

	@Override
	public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclare(String queue, boolean durable, boolean exclusive,
			boolean autoDelete, Map<String, Object> arguments) throws IOException {
		return target.queueDeclare(queue, durable, exclusive, autoDelete, arguments);
	}

	@Override
	public void queueDeclareNoWait(String queue, boolean durable, boolean exclusive, boolean autoDelete,
			Map<String, Object> arguments) throws IOException {
		target.queueDeclareNoWait(queue, durable, exclusive, autoDelete, arguments);
	}

	@Override
	public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclarePassive(String queue) throws IOException {
		return target.queueDeclarePassive(queue);
	}

	@Override
	public com.rabbitmq.client.AMQP.Queue.DeleteOk queueDelete(String queue) throws IOException {
		return target.queueDelete(queue);
	}

	@Override
	public com.rabbitmq.client.AMQP.Queue.DeleteOk queueDelete(String queue, boolean ifUnused, boolean ifEmpty)
			throws IOException {
		return target.queueDelete(queue, ifUnused, ifEmpty);
	}

	@Override
	public void queueDeleteNoWait(String queue, boolean ifUnused, boolean ifEmpty) throws IOException {
		target.queueDeleteNoWait(queue, ifUnused, ifEmpty);
	}

	@Override
	public com.rabbitmq.client.AMQP.Queue.BindOk queueBind(String queue, String exchange, String routingKey)
			throws IOException {
		return target.queueBind(queue, exchange, routingKey);
	}

	@Override
	public com.rabbitmq.client.AMQP.Queue.BindOk queueBind(String queue, String exchange, String routingKey,
			Map<String, Object> arguments) throws IOException {
		return target.queueBind(queue, exchange, routingKey, arguments);
	}

	@Override
	public void queueBindNoWait(String queue, String exchange, String routingKey, Map<String, Object> arguments)
			throws IOException {
		target.queueBindNoWait(queue, exchange, routingKey, arguments);
	}

	@Override
	public com.rabbitmq.client.AMQP.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey)
			throws IOException {
		return target.queueUnbind(queue, exchange, routingKey);
	}

	@Override
	public com.rabbitmq.client.AMQP.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey,
			Map<String, Object> arguments) throws IOException {
		return target.queueUnbind(queue, exchange, routingKey, arguments);
	}

	@Override
	public PurgeOk queuePurge(String queue) throws IOException {
		return target.queuePurge(queue);
	}

	@Override
	public GetResponse basicGet(String queue, boolean autoAck) throws IOException {
		return target.basicGet(queue, autoAck);
	}

	@Override
	public void basicAck(long deliveryTag, boolean multiple) throws IOException {
		target.basicAck(deliveryTag, multiple);
	}

	@Override
	public void basicNack(long deliveryTag, boolean multiple, boolean requeue) throws IOException {
		target.basicNack(deliveryTag, multiple, requeue);
	}

	@Override
	public void basicReject(long deliveryTag, boolean requeue) throws IOException {
		target.basicReject(deliveryTag, requeue);
	}

	@Override
	public String basicConsume(String queue, Consumer callback) throws IOException {
		return target.basicConsume(queue, callback);
	}

	@Override
	public String basicConsume(String queue, DeliverCallback deliverCallback, CancelCallback cancelCallback)
			throws IOException {
		return target.basicConsume(queue, deliverCallback, cancelCallback);
	}

	@Override
	public String basicConsume(String queue, DeliverCallback deliverCallback,
			ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
		return target.basicConsume(queue, deliverCallback, shutdownSignalCallback);
	}

	@Override
	public String basicConsume(String queue, DeliverCallback deliverCallback, CancelCallback cancelCallback,
			ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
		return target.basicConsume(queue, deliverCallback, cancelCallback, shutdownSignalCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, Consumer callback) throws IOException {
		return target.basicConsume(queue, autoAck, callback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback,
			CancelCallback cancelCallback) throws IOException {
		return target.basicConsume(queue, autoAck, deliverCallback, cancelCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback,
			ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
		return target.basicConsume(queue, autoAck, deliverCallback, shutdownSignalCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback,
			CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
		return target.basicConsume(queue, autoAck, deliverCallback, cancelCallback, shutdownSignalCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, Consumer callback)
			throws IOException {
		return target.basicConsume(queue, autoAck, arguments, callback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments,
			DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException {
		return target.basicConsume(queue, autoAck, arguments, deliverCallback, cancelCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments,
			DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
		return target.basicConsume(queue, autoAck, arguments, deliverCallback, shutdownSignalCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments,
			DeliverCallback deliverCallback, CancelCallback cancelCallback,
			ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
		return target.basicConsume(queue, autoAck, arguments, deliverCallback, cancelCallback, shutdownSignalCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, String consumerTag, Consumer callback)
			throws IOException {
		return target.basicConsume(queue, autoAck, consumerTag, callback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback,
			CancelCallback cancelCallback) throws IOException {
		return target.basicConsume(queue, autoAck, consumerTag, deliverCallback, cancelCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback,
			ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
		return target.basicConsume(queue, autoAck, consumerTag, deliverCallback, shutdownSignalCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback,
			CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
		return target.basicConsume(queue, autoAck, consumerTag, deliverCallback, cancelCallback,
				shutdownSignalCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive,
			Map<String, Object> arguments, Consumer callback) throws IOException {
		return target.basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, callback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive,
			Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback)
			throws IOException {
		return target.basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, deliverCallback,
				cancelCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive,
			Map<String, Object> arguments, DeliverCallback deliverCallback,
			ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
		return target.basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, deliverCallback,
				shutdownSignalCallback);
	}

	@Override
	public String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive,
			Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback,
			ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
		return target.basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, deliverCallback,
				cancelCallback, shutdownSignalCallback);
	}

	@Override
	public void basicCancel(String consumerTag) throws IOException {
		target.basicCancel(consumerTag);
	}

	@Override
	public RecoverOk basicRecover() throws IOException {
		return target.basicRecover();
	}

	@Override
	public RecoverOk basicRecover(boolean requeue) throws IOException {
		return target.basicRecover(requeue);
	}

	@Override
	public SelectOk txSelect() throws IOException {
		return target.txSelect();
	}

	@Override
	public CommitOk txCommit() throws IOException {
		return target.txCommit();
	}

	@Override
	public RollbackOk txRollback() throws IOException {
		return target.txRollback();
	}

	@Override
	public com.rabbitmq.client.AMQP.Confirm.SelectOk confirmSelect() throws IOException {
		return target.confirmSelect();
	}

	@Override
	public long getNextPublishSeqNo() {
		return target.getNextPublishSeqNo();
	}

	@Override
	public boolean waitForConfirms() throws InterruptedException {
		return target.waitForConfirms();
	}

	@Override
	public boolean waitForConfirms(long timeout) throws InterruptedException, TimeoutException {
		return target.waitForConfirms(timeout);
	}

	@Override
	public void waitForConfirmsOrDie() throws IOException, InterruptedException {
		target.waitForConfirmsOrDie();
	}

	@Override
	public void waitForConfirmsOrDie(long timeout) throws IOException, InterruptedException, TimeoutException {
		target.waitForConfirmsOrDie(timeout);

	}

	@Override
	public void asyncRpc(Method method) throws IOException {
		target.asyncRpc(method);
	}

	@Override
	public Command rpc(Method method) throws IOException {
		return target.rpc(method);
	}

	@Override
	public long messageCount(String queue) throws IOException {
		return target.messageCount(queue);
	}

	@Override
	public long consumerCount(String queue) throws IOException {
		return target.consumerCount(queue);
	}

	@Override
	public CompletableFuture<Command> asyncCompletableRpc(Method method) throws IOException {
		return target.asyncCompletableRpc(method);
	}

}
