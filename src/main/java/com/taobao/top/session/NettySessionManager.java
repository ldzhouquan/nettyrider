package com.taobao.top.session;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;

import com.taobao.top.command.CommandDispatcher;
import com.taobao.top.common.NettyriderThreadFactory;
import com.taobao.top.config.NettyriderConfig;

/**
 * NettySessionMannager
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-24
 */
public class NettySessionManager implements SessionManager {
	private static final Log logger = LogFactory.getLog(NettySessionManager.class);

	private NettyriderConfig config; 												// 配置
	private AtomicLong sessionCount = new AtomicLong(0); 							// 当前session个数
	private AtomicLong sessionId = new AtomicLong(0); 								// 为session分配id

	private CommandDispatcher commandDispatcher;
	private CopyOnWriteArrayList<NettySession> idleSessionList;
	private ConcurrentHashMap<Long, NettySession> sessionMap;

	private ScheduledExecutorService sessionRecycleScheduler;

	public NettySessionManager(NettyriderConfig config) {
		this.config = config;
		idleSessionList = new CopyOnWriteArrayList<NettySession>();
		sessionMap = new ConcurrentHashMap<Long, NettySession>();
	}

	@Override
	public boolean init() {

		logger.info(new StringBuilder("Pre init ").append(config.getPreInitSessionCount()).append(" sessions.")
				.toString());
		increment(config.getPreInitSessionCount());
		sessionRecycleScheduler = Executors.newScheduledThreadPool(1, new NettyriderThreadFactory(
				SESSION_RECYCLE_THREAD_NAME_PREFIX, null, false));
		return true;
	}

	@Override
	public boolean start() {
		sessionRecycleScheduler.scheduleAtFixedRate(new SessionRecycleTask(), config.getSessionRecycleInterval(),
				config.getSessionRecycleInterval(), TimeUnit.SECONDS);
		return true;
	}

	@Override
	public boolean stop() {
		sessionRecycleScheduler.shutdown();
		idleSessionList.clear();
		idleSessionList = null;
		Long key = null;
		Session session = null;
		Iterator<Long> iterator = sessionMap.keySet().iterator();
		while (iterator.hasNext()) {
			key = iterator.next();
			session = sessionMap.get(key);
			if (session != null) {
				session.stop();
			}
		}
		sessionMap.clear();
		sessionMap = null;

		return true;
	}

	@Override
	public boolean restart() {

		return stop() && init() && start();
	}

	private void increment() {
		if (sessionCount.get() >= config.getMaxSessionCount()) {
			logger.warn(new StringBuilder("Reach max session supported.").toString());
			return;
		}

		long count = config.getMaxSessionCount() - sessionCount.get();
		count = count < config.getIncreaseSessionCount() ? count : config.getPreInitSessionCount();
		increment(count);
	}

	/**
	 * 调用本函数的肯定只有一个线程
	 */
	private void increment(long count) {
		logger.warn(new StringBuilder("Increase ").append(count).append(" sessions").toString());
		for (long i = 0; i < count; i++) {
			NettySession session = NettySessionFactory.newSession(sessionId.getAndIncrement(), null, 1024,
					commandDispatcher);
			session.init();
			idleSessionList.add(session);
		}
	}

	@Override
	public void setCommandDispatcher(CommandDispatcher commandDispatcher) {
		this.commandDispatcher = commandDispatcher;
	}

	@Override
	public Session newSession(Channel channel, boolean start) {
		logger.info(new StringBuilder("New session for ").append(channel.getRemoteAddress().toString()).toString());

		if (idleSessionList.isEmpty()) {
			increment();
		}

		NettySession session = idleSessionList.remove(0);

		session.withChannel(channel);

		if (start) {
			session.start();
		}
		sessionMap.put(session.getId(), session);
		return session;
	}

	@Override
	public void freeSession(Session session) {
		NettySession ds = (NettySession) session;
		logger.warn(new StringBuilder("Free session : id = ").append(ds.getId()).append(", for : ")
				.append(ds.getSlaveWorker()).toString());
		sessionMap.remove(ds.getId());
		session.free();
		idleSessionList.add(ds);

	}

	public void sessionRecycle() {
		logger.debug("=======Begin session recycle.=======");
		dump();
		Iterator<Long> iterator = sessionMap.keySet().iterator();
		NettySession session = null;
		while (iterator.hasNext()) {
			Long sessionId = iterator.next();
			session = sessionMap.get(sessionId);
			if (session == null) {
				continue;
			}

			if (session.isDead()) {
				iterator.remove();
				session.free();
				idleSessionList.add(session);
				continue;
			}

			session.transit();
		}
		logger.debug("=======End session recycle.=========");
		dump();
	}

	private void dump() {
		Long key = null;
		NettySession session = null;
		Iterator<Long> iterator = sessionMap.keySet().iterator();
		logger.debug("Dump session state:");
		while (iterator.hasNext()) {
			key = iterator.next();
			session = sessionMap.get(key);
			if (session != null) {
				logger.debug(new StringBuilder("Session: ").append(session.getId()).append(" , State: ")
						.append(session.getState().desc()).toString());
			}
		}
	}

	@Override
	public List<SessionState> generateSessionState() {
		SessionState sessionState = null;
		List<SessionState> sessionStateList = new LinkedList<SessionState>();
		Long key = null;
		Session session = null;
		Iterator<Long> iterator = sessionMap.keySet().iterator();
		while (iterator.hasNext()) {
			key = iterator.next();
			session = sessionMap.get(key);
			if (session != null && session.getState() != SessionStateEnum.NETTYRIDER_SESSION_STATE_DEAD) {
				sessionState = new SessionState();
				sessionState.setIp(session.getSlaveWorker().getIp());
				sessionState.setPriority(computePriority(session));
				// FIXME
				sessionState.setIsMasterCandidate(false);
				sessionStateList.add(sessionState);
			}
		}

		return sessionStateList;
	}

	/**
	 * 计算Slave的转换成Master的优先级
	 * 
	 * @return
	 */
	private Long computePriority(Session session) {
		return 0L;
	}

	private class SessionRecycleTask implements Runnable {
		public void run() {
			try {
				sessionRecycle();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

}
