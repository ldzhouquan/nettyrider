package com.taobao.top.network.netty.factory;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import com.taobao.top.network.netty.codec.CommandDecoder;
import com.taobao.top.network.netty.codec.CommandEncoder;
import com.taobao.top.network.netty.handler.CommandServerChannelHandler;
import com.taobao.top.session.NettySessionManager;

/**
 * CommandPipelineFactory
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-13
 */
public class CommandServerPipelineFactory implements ChannelPipelineFactory {

	private final NettySessionManager nettySessionManager;

	public CommandServerPipelineFactory(NettySessionManager nettySessionManager) {
		super();
		this.nettySessionManager = nettySessionManager;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("encoder", new CommandEncoder());
		pipeline.addLast("decoder", new CommandDecoder());
		pipeline.addLast("handler", new CommandServerChannelHandler(nettySessionManager));

		return pipeline;
	}

}
