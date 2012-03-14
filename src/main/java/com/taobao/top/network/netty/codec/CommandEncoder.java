package com.taobao.top.network.netty.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.taobao.top.command.Command;

/**
 * MessageEncoder
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-10
 */
public class CommandEncoder extends OneToOneEncoder{

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if(!(msg instanceof Command)){
			return msg;
		}
		
		Command command = (Command) msg;
		
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeBytes(command.marshall());
		
		return buffer;
	}

}
