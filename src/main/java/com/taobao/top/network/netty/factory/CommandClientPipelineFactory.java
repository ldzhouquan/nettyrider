package com.taobao.top.network.netty.factory;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.Timer;

import com.taobao.top.command.CommandDispatcher;
import com.taobao.top.command.CommandProviderMannager;
import com.taobao.top.network.netty.codec.CommandDecoder;
import com.taobao.top.network.netty.codec.CommandEncoder;
import com.taobao.top.network.netty.handler.CommandClientChannelHandler;
import com.taobao.top.network.netty.handler.HeartBeatHandler;

/**
 * CommandPipelineFactory
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-13
 */
public class CommandClientPipelineFactory implements ChannelPipelineFactory {

	// 任务分发器
	private final CommandDispatcher commandDispatcher;
	
	private final CommandProviderMannager<Channel> commandProviderMannager;

	private Timer timer;
	
	public CommandClientPipelineFactory(CommandDispatcher commandDispatcher,CommandProviderMannager<Channel> commandProviderMannager,Timer timer) {
		this.timer = timer;
		this.commandDispatcher = commandDispatcher;
		this.commandProviderMannager = commandProviderMannager;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("encoder", new CommandEncoder());
		pipeline.addLast("decoder", new CommandDecoder());
		pipeline.addLast("handler", new CommandClientChannelHandler(commandDispatcher,commandProviderMannager));

		// 添加心跳机制，当客户端channel读(写)空闲60s秒时
		// 一个状态为IdleState.READER_IDLE(IdleState.WRITER_IDLE)的IdleStateEvent被触发
		pipeline.addLast("timeout", new IdleStateHandler(timer, 60, 60, 0));
		pipeline.addLast("heartbeat", new HeartBeatHandler());
		return pipeline;
	}

}
