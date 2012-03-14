package com.taobao.top.session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;

import com.taobao.top.SlaveWorker;
import com.taobao.top.command.Command;
import com.taobao.top.command.CommandDispatcher;
import com.taobao.top.common.NettyriderThreadFactory;

/**
 * Netty Session,每一个Clinet链接到Server就创建一个Session实例
 * 每个实例运行一个SessionTask线程,定时扫描inputQueue,把inputQueue中的任务取出来执行，并将执行结果放入outputQueue中
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-24
 */
public class NettySession implements Session {

	private static final Log logger = LogFactory.getLog(NettySession.class);
	private static final String SESSION_THREAD_NAME_PREFIX = "Nettyrider-Session";

	private Long id; 										// Sessio ID
	private volatile SessionStateEnum state;				// Session 状态, 初始空闲状态
	private Channel channel; 								// 网络通道
	private Thread thread; 									// 工作线程
	private SlaveWorker slaveWorker; 						// Slave信息
	private int commandQueueSize; 							// 网络数据输入缓冲大小
	private BlockingQueue<Command> commandQueue; 			// 待执行任务队列
	private CommandDispatcher commandDispatcher; 			// 命令分发器

	// 用于控制SessionTask是否执行
	private ReentrantLock runLock;
	private Condition run;
	private boolean isRun;

	private NettyriderThreadFactory threadFactory;

	/**
	 * Construtors
	 * 
	 * @param id
	 * @param inBufferSize
	 * @param outBufferSize
	 */
	public NettySession(Long id, int commandQueueSize, Channel channel) {
		super();
		this.id = id;
		this.commandQueueSize = commandQueueSize;
		this.channel = channel;
		this.state = SessionStateEnum.NETTYRIDER_SESSION_STATE_FREE;
		this.runLock = new ReentrantLock();
		this.run = runLock.newCondition();
		this.isRun = false;
	}

	/**
	 * 获取id
	 * 
	 * @return id
	 */
	public Long getId() {
		return id;
	}

	@Override
	public boolean init() {
		commandQueue = new LinkedBlockingDeque<Command>(commandQueueSize);
		threadFactory = new NettyriderThreadFactory(SESSION_THREAD_NAME_PREFIX + "-" + String.valueOf(id), null, true);
		thread = threadFactory.newThread(new SessionTask());
		thread.start();
		return true;
	}

	@Override
	public boolean start() {
		if (channel == null || commandQueue == null || commandDispatcher == null) {
			throw new IllegalArgumentException("Seesion not set corrected");
		}
		control(true);
		state = SessionStateEnum.NETTYRIDER_SESSION_STATE_ALIVE;
		return true;
	}

	@Override
	public boolean stop() {
		thread.interrupt();
		commandQueue.clear();
		closeChannel();
		channel = null;
		state = SessionStateEnum.NETTYRIDER_SESSION_STATE_FREE;
		return true;
	}

	@Override
	public boolean restart() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void free() {
		control(false);
		commandQueue.clear();
		closeChannel();
		channel = null;
		state = SessionStateEnum.NETTYRIDER_SESSION_STATE_FREE;
	}

	@Override
	public void transit() {
		SessionStateEnum oldState = state;
		if (oldState == SessionStateEnum.NETTYRIDER_SESSION_STATE_ALIVE) {
			state = SessionStateEnum.NETTYRIDER_SESSION_STATE_WAITING_0;
		} else if (oldState == SessionStateEnum.NETTYRIDER_SESSION_STATE_WAITING_1) {
			state = SessionStateEnum.NETTYRIDER_SESSION_STATE_WAITING_2;
		} else if (oldState == SessionStateEnum.NETTYRIDER_SESSION_STATE_WAITING_2) {
			state = SessionStateEnum.NETTYRIDER_SESSION_STATE_DEAD;
		}
	}

	@Override
	public void alive() {
		state = SessionStateEnum.NETTYRIDER_SESSION_STATE_ALIVE;
	}

	@Override
	public boolean isDead() {
		return state == SessionStateEnum.NETTYRIDER_SESSION_STATE_DEAD;
	}

	@Override
	@Deprecated
	public void onRead() throws IOException, InterruptedException {
	}

	@Override
	@Deprecated
	public void onWrite() throws IOException {
	}

	@Override
	public void onException() {
		// TODO Auto-generated method stub

	}

	@Override
	public SlaveWorker getSlaveWorker() {
		return slaveWorker;
	}

	@Override
	public SessionStateEnum getState() {
		return state;
	}

	@Override
	@Deprecated
	public SocketChannel getChannel() {
		return null;
	}

	public Channel getNettyChannel() {
		return channel;
	}

	public void addCommand(Command command) {
		try {
			commandQueue.put(command);
		} catch (InterruptedException e) {
			logger.error(e.getStackTrace());
		}
	}

	private void _process_() throws Exception {
		logger.info("Session try to process one command");
		Command input = null;
		while ((input = commandQueue.peek()) == null) {
			Thread.sleep(100);
		}
		Command output = this.commandDispatcher.dispatch(input);
		commandQueue.remove();
		input.getPayLoad().clear();
		if (output != null) {
			channel.write(output);
			logger.info("Session execute one command");
		}
	}

	private void control(boolean isRun) {
		try {
			runLock.lock();
			this.isRun = isRun;
			this.run.signalAll();
		} finally {
			runLock.unlock();
		}
	}

	private void closeChannel() {
		channel.close();
	}

	/*---------------------------with variables---------------------------*/

	public NettySession withChannel(Channel channel) {
		this.channel = channel;
		if (channel != null) {
			InetSocketAddress socketAddress = (InetSocketAddress) channel.getRemoteAddress();
			this.slaveWorker = new SlaveWorker(this.id, "SlaveWorker" + this.id, socketAddress.getHostName(),
					socketAddress.getPort());
		}
		return this;
	}

	public NettySession withSlaveWorker(SlaveWorker slaveWorker) {
		this.slaveWorker = slaveWorker;
		return this;
	}

	public NettySession withCommandDispatcher(CommandDispatcher CommandDispatcher) {
		this.commandDispatcher = CommandDispatcher;
		return this;
	}

	/*------------------------Private thread task-------------------------*/
	private class SessionTask implements Runnable {

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					runLock.lock();
					while (!isRun) {
						// idle
						logger.info(new StringBuilder(Thread.currentThread().getName()).append(" idle"));
						run.await();
					}
					logger.info(new StringBuilder(Thread.currentThread().getName()).append(" started"));
					_process_();
				} catch (InterruptedException e) {
					logger.error(e);
					e.printStackTrace();
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				} finally {
					runLock.unlock();
				}
			}
			logger.info(new StringBuilder(Thread.currentThread().getName()).append(" stoped"));
		}
	}

}
