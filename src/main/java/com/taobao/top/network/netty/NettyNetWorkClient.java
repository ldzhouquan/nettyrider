package com.taobao.top.network.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;

import com.taobao.top.command.CommandDispatcher;
import com.taobao.top.command.CommandProviderMannager;
import com.taobao.top.common.NettyriderThreadFactory;
import com.taobao.top.config.NettyriderConfig;
import com.taobao.top.network.NetWorkClient;
import com.taobao.top.network.netty.factory.CommandClientPipelineFactory;

/**
 * NettyClient
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-13
 */
public class NettyNetWorkClient implements NetWorkClient {

	// Options
	private String hostName;
	private int port;
	private String NETTY_NET_WORK_CLIENT_THREAD_NAME = "Waverider-NettyNetWorkClient";

	// Variables
	private ClientBootstrap bootstrap;
	private Executor bossExecutor;
	private Executor workerExecutor;

	private CommandDispatcher commandDispatcher;
	private CommandProviderMannager<Channel> commandProviderMannager;

	public NettyNetWorkClient() {
		this(null, NettyriderConfig.NETTYRIDER_DEFAULT_PORT);
	}

	public NettyNetWorkClient(String hostName) {
		this(hostName, NettyriderConfig.NETTYRIDER_DEFAULT_PORT);
	}

	public NettyNetWorkClient(String hostName, int port) {
		this.hostName = hostName;
		this.port = port;
	}

	@Override
	public boolean init() {
		// init bossExecutor
		bossExecutor = Executors.newCachedThreadPool(new NettyriderThreadFactory(NETTY_NET_WORK_CLIENT_THREAD_NAME
				+ "-BossExecutor"));
		// init workerExecutor
		workerExecutor = Executors.newCachedThreadPool(new NettyriderThreadFactory(NETTY_NET_WORK_CLIENT_THREAD_NAME
				+ "-WorkerExecutor"));
		// init bootstrap
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(bossExecutor, workerExecutor));

		return true;
	}

	@Override
	public boolean start() {
		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new CommandClientPipelineFactory(commandDispatcher, commandProviderMannager,
				new HashedWheelTimer()));
		// Start the connection attempt.
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(hostName, port));
		// Wait until the connection is closed or the connection attempt fails.
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		// Shut down thread pools to exit.
		this.stop();

		return true;
	}

	@Override
	public boolean restart() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stop() {
		bootstrap.releaseExternalResources();
		return true;
	}

	/*------------------with variables------------------*/

	public NettyNetWorkClient withCommandDispatcher(CommandDispatcher commandDispatcher) {
		this.commandDispatcher = commandDispatcher;
		return this;
	}

	public NettyNetWorkClient withCommandProviderMannager(CommandProviderMannager<Channel> commandProviderMannager) {
		this.commandProviderMannager = commandProviderMannager;
		return this;
	}

}
