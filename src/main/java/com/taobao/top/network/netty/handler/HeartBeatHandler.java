package com.taobao.top.network.netty.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

import com.taobao.top.command.CommandProvider;
import com.taobao.top.command.HeartbeatCommandProvider;

/**
 * HeartBeatHandler
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-27
 */
public class HeartBeatHandler extends IdleStateAwareChannelHandler {

	private static final CommandProvider commandProvider = new HeartbeatCommandProvider();
	
	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
		super.channelIdle(ctx, e);
		// 如果写空闲，或者读空闲，则发送一个心跳包
		if (e.getState() == IdleState.WRITER_IDLE || e.getState() == IdleState.READER_IDLE) {
			e.getChannel().write(commandProvider.produce());
		}
	}

}