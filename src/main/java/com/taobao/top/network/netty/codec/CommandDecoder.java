package com.taobao.top.network.netty.codec;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import com.taobao.top.command.Command;

/**
 * CommandDecoder
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-24
 */
public class CommandDecoder extends FrameDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

		// buffer校验
		int headerSize = Command.getHeaderSize();
		if (buffer == null || buffer.readableBytes() < headerSize) {
			return buffer;
		}

		// 读取报文头
		long type = buffer.readLong();
		int length = buffer.readInt();

		// 如果buffer长度不符合Command，则返回buffer
		if (buffer.readableBytes() < length - headerSize) {
			return buffer;
		}

		// 读取payLoad
		ByteBuffer payLoad = ByteBuffer.allocate(length - headerSize);
		buffer.readBytes(payLoad);

		// 生成Command
		Command command = new Command();
		command.setLength(length);
		command.setType(type);
		command.setPayLoad(payLoad);

		return command;
	}
}
