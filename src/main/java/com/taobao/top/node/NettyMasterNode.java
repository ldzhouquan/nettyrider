package com.taobao.top.node;

import java.util.concurrent.atomic.AtomicLong;

import com.taobao.top.State;
import com.taobao.top.command.CommandDispatcher;
import com.taobao.top.command.CommandHandler;
import com.taobao.top.command.MasterGreetCommandHandler;
import com.taobao.top.command.MasterHeartbeatCommandHandler;
import com.taobao.top.command.SampleCommandDispatcher;
import com.taobao.top.config.NettyriderConfig;
import com.taobao.top.master.MasterState;
import com.taobao.top.network.NetWorkServer;
import com.taobao.top.network.netty.NettyNetWorkServer;
import com.taobao.top.session.NettySessionManager;

/**
 * NettyMasterNode
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-27
 */
public class NettyMasterNode implements MasterNode {

	private NettyriderConfig config; 								// 运行配置
	private NetWorkServer netWorkServer;							// 底层网络服务
	private CommandDispatcher commandDispatcher; 					// 命令分发器
	private AtomicLong stateIDGenrator = new AtomicLong(0); 		// 状态计数器
	private NettySessionManager sessionManager;

	/**
	 * @param config
	 */
	public NettyMasterNode(NettyriderConfig config) {
		super();
		this.config = config;
		sessionManager = new NettySessionManager(this.config);
		netWorkServer = new NettyNetWorkServer(this.config, sessionManager);
		commandDispatcher = new SampleCommandDispatcher();
	}

	@Override
	public void addCommandHandler(Long command, CommandHandler handler) {
		if (command == null || command.equals(0L)) {
			throw new IllegalArgumentException("command must not be null or 0");
		}
		commandDispatcher.addCommandHandler(command, handler);
	}

	@Override
	public State gatherStatistics() {
		MasterState masterState = new MasterState();
		masterState.setId(stateIDGenrator.addAndGet(1));
		masterState.setIp(netWorkServer.getIp());
		masterState.setPort(netWorkServer.getPort());
		masterState.setSessionStateList(sessionManager.generateSessionState());
		return masterState;
	}

	@Override
	public void acceptStatistics(State state) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean init() {
		commandDispatcher.addCommandHandler(0l, new MasterHeartbeatCommandHandler());
		commandDispatcher.addCommandHandler(1l, new MasterGreetCommandHandler());
		sessionManager.setCommandDispatcher(commandDispatcher);
		return netWorkServer.init() && sessionManager.init();
	}

	@Override
	public boolean start() {
		return sessionManager.start() && netWorkServer.start();
	}

	@Override
	public boolean stop() {
		return sessionManager.stop() && netWorkServer.stop();
	}

	@Override
	public boolean restart() {
		return sessionManager.restart() && netWorkServer.restart();
	}

}
