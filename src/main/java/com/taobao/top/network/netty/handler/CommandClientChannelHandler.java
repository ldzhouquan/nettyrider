package com.taobao.top.network.netty.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.taobao.top.command.Command;
import com.taobao.top.command.CommandDispatcher;
import com.taobao.top.command.CommandProviderMannager;

/**
 * CommandClientHandler
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-13
 */
public class CommandClientChannelHandler extends SimpleChannelHandler {
	
	private static final Log logger = LogFactory.getLog(CommandClientChannelHandler.class);
	
	// 命令分发器
	private final CommandDispatcher commandDispatcher;
	
	// 命令产生器
	private final CommandProviderMannager<Channel> commandProviderMannager;


	public CommandClientChannelHandler(CommandDispatcher commandDispatcher,CommandProviderMannager<Channel> commandProviderMannager) {
		this.commandDispatcher = commandDispatcher;
		this.commandProviderMannager = commandProviderMannager;
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// channel连接完毕后，开始创造command，并写入channel中
		commandProviderMannager.withChannel(ctx.getChannel());
		commandProviderMannager.init();
		commandProviderMannager.start();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (!(e.getMessage() instanceof Command)) {
			return;
		}
		// 解析Command并分发
		Command input = (Command) e.getMessage();
		Command output = commandDispatcher.dispatch(input);
		if(output!=null){
			ctx.getChannel().write(output);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		e.getCause().printStackTrace();
		logger.equals(e.getCause().getMessage());
	}
}
