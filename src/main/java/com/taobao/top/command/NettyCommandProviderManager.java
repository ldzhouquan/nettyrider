package com.taobao.top.command;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;

import com.taobao.top.common.NettyriderThreadFactory;
import com.taobao.top.config.NettyriderConfig;

/**
 * NettyCommandProviderManager
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-3-12
 */
public class NettyCommandProviderManager implements CommandProviderMannager<Channel>{

	private static final Log logger = LogFactory.getLog(NettyCommandProviderManager.class);
	
	// 命令发生器列表
	private List<CommandProvider> commandProviderList;
	
	// 对应要写入Command的channel
	private Channel channel;
	
	// nettyrider 配置
	private NettyriderConfig config;
	
	// 命令发生器执行器
	private ScheduledExecutorService commandProvidereExecutor;
	
	/**
	 * Construtcor
	 */
	public NettyCommandProviderManager(NettyriderConfig config) {
		super();
		this.config = config;
		this.commandProviderList  = new LinkedList<CommandProvider>();
	}

	@Override
	public void addCommandProvider(CommandProvider commandProvider) {
		commandProviderList.add(commandProvider);
	}

	@Override
	public boolean init() {
		commandProvidereExecutor = Executors.newScheduledThreadPool(1, new NettyriderThreadFactory("Top-Task-Scheduler-Slave-Command-Provider", null, true));
		return true;
	}

	@Override
	public boolean start() {
		commandProvidereExecutor.scheduleAtFixedRate(new CommandProviderTask(), 0, config.getSlaveCommandProduceInterval(), TimeUnit.SECONDS);
		return true;
	}

	@Override
	public boolean stop() {
		commandProvidereExecutor.shutdown();
		return true;
	}

	@Override
	public boolean restart() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void withChannel(Channel t) {
		this.channel = t;
	}
	
	// 收集所有的CommandProvider产生的命令, 并通netWorkClient过发送出去
	private class CommandProviderTask implements Runnable {
		@Override
		public void run() {
			int total = 0;
			Iterator<CommandProvider> iterator = null;
			CommandProvider commandProvider = null;
			Command command = null;
			total = 0;
			iterator = commandProviderList.iterator();
			while (iterator.hasNext()) {
				commandProvider = iterator.next();
				command = commandProvider.produce();
				if (command != null) {
					channel.write(command);
					total++;
				}
			}
			logger.info("send " + total + "commands");
		}
	}
}
