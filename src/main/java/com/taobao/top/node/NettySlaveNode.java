package com.taobao.top.node;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;

import com.taobao.top.State;
import com.taobao.top.command.CommandDispatcher;
import com.taobao.top.command.CommandHandler;
import com.taobao.top.command.CommandProvider;
import com.taobao.top.command.CommandProviderMannager;
import com.taobao.top.command.NettyCommandProviderManager;
import com.taobao.top.command.SampleCommandDispatcher;
import com.taobao.top.command.SlaveGreetCommandHandler;
import com.taobao.top.config.NettyriderConfig;
import com.taobao.top.master.MasterState;
import com.taobao.top.network.netty.NettyNetWorkClient;
import com.taobao.top.slave.SlaveState;
import com.taobao.top.slave.failure.slave.DefaultMasterFailureHandler;
import com.taobao.top.slave.failure.slave.DefaultMasterFailureMonitor;
import com.taobao.top.slave.failure.slave.MasterFailureMonitor;

/**
 * NettySlaveNode
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-28
 */
public class NettySlaveNode implements SlaveNode {

	private static final Log logger = LogFactory.getLog(NettySlaveNode.class);
	
	// Config
	private NettyriderConfig config;

	// Command provide
	private CommandProviderMannager<Channel> commandProviderMannager;

	// 命令分发器
	private CommandDispatcher commandDispatcher; 	
	
	// Failure monitor
	private MasterFailureMonitor masterFailureMonitor;

	// Network
	private NettyNetWorkClient netWorkClient;
	
	private AtomicLong stateIDGenrator = new AtomicLong(0);
	
	/**
	 * Constructors
	 * @param config
	 */
	public NettySlaveNode(NettyriderConfig config) {
		super();
		this.config = config;
		this.commandProviderMannager = new NettyCommandProviderManager(config);
		this.commandDispatcher = new SampleCommandDispatcher();
	}

	@Override
	public void addCommandHandler(Long command, CommandHandler handler) {
		if(command == null || command.equals(0L)) {
			throw new IllegalArgumentException("command must not be null or 0");
		}
		commandDispatcher.addCommandHandler(command, handler);
	}
	
	@Override
	public State gatherStatistics() {
		SlaveState slaveState = new SlaveState();
		slaveState.setId(stateIDGenrator.addAndGet(1));
		slaveState.setIsMasterCandidate(config.isMasterCandidate());
		return slaveState;
	}
	
	@Override
	public void acceptStatistics(State state) {
		logger.info(new StringBuilder("Slave Accept Master state : ").append(((MasterState)state).toString()).toString());
		masterFailureMonitor.process((MasterState)state);
	}

	@Override
	public boolean init() {
		commandDispatcher.addCommandHandler(1l, new SlaveGreetCommandHandler());
		
//		commandProviderMannager.addCommandProvider(new GreetCommandProvider());
		
		netWorkClient = new NettyNetWorkClient(config.getMasterAddress(), config.getPort());
		netWorkClient.withCommandDispatcher(commandDispatcher).withCommandProviderMannager(commandProviderMannager);
		netWorkClient.init();
		
		masterFailureMonitor = new DefaultMasterFailureMonitor(new DefaultMasterFailureHandler(this), MasterFailureMonitor.DEFAULT_FAILURE_MONITOR_INTERVAL, MasterFailureMonitor.DEFAULT_FAILURE_MONITOR_WAIT_MASTER_STATE_TIME_OUT);
		masterFailureMonitor.init();
		
		return false;
	}

	@Override
	public boolean start() {
		masterFailureMonitor.start();
		try {
			netWorkClient.start();
		} catch (Exception e) {
			logger.error(e);
			if (e.getCause() instanceof IOException || e.getCause() instanceof ConnectException) {
				try {
					logger.error("Can not connect to master , sleep 60s, then try again");
					Thread.sleep(60 * 1000);
				} catch (InterruptedException ex) {
					logger.error(ex);
					Thread.currentThread().interrupt();
				}
			}
		}
		return true;
	}

	@Override
	public boolean stop() {
		netWorkClient.stop();
		masterFailureMonitor.stop();
		commandProviderMannager.stop();
		return true;
	}

	@Override
	public boolean restart() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addCommandProvider(CommandProvider commandProvider) {
		commandProviderMannager.addCommandProvider(commandProvider);
	}
	
}
