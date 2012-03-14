package com.taobao.top.network.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.taobao.top.common.NettyriderThreadFactory;
import com.taobao.top.config.NettyriderConfig;
import com.taobao.top.network.NetWorkServer;
import com.taobao.top.network.netty.factory.CommandServerPipelineFactory;
import com.taobao.top.session.NettySessionManager;
import com.taobao.top.session.SessionManager;

/**
 * NettyNetWorkServer
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-16
 */
public class NettyNetWorkServer implements NetWorkServer {

	// Options
	private int port;
	private String NETTY_NET_WORK_SERVER_THREAD_NAME = "Nettyrider-NettyNetWorkServer";

	// Variables
	private ServerBootstrap bootstrap;
	private Executor bossExecutor;
	private Executor workerExecutor;

	private NettySessionManager nettySessionManager;
	
	/**
	 * Constructor
	 * @param port
	 * @param config
	 */
	public NettyNetWorkServer(NettyriderConfig config,NettySessionManager sessionManager) {
		super();
		this.port = config.getPort();
		this.nettySessionManager = sessionManager;
	}

	@Override
	public boolean init() {
		// init bossExecutor
		bossExecutor = Executors.newCachedThreadPool(new NettyriderThreadFactory(NETTY_NET_WORK_SERVER_THREAD_NAME
				+ "-BossExecutor"));
		// init workerExecutor
		workerExecutor = Executors.newCachedThreadPool(new NettyriderThreadFactory(NETTY_NET_WORK_SERVER_THREAD_NAME
				+ "-WorkerExecutor"));
		// Configure the server.
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));
		return true;
	}

	@Override
	public boolean start() {
		// Set up the default event pipeline.
		bootstrap.setPipelineFactory(new CommandServerPipelineFactory(nettySessionManager));
		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(port));
		return true;
	}

	@Override
	public boolean stop() {
		bootstrap.releaseExternalResources();
		return true;
	}

	@Override
	public boolean restart() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getIp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		if(sessionManager instanceof NettySessionManager){
			this.nettySessionManager = (NettySessionManager) sessionManager;
		}
	}

}
