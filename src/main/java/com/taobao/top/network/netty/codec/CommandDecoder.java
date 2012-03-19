package com.taobao.top.network.netty.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static final Log log = LogFactory.getLog(CommandDecoder.class);

	// 临时buffer，用于存储数据不完整时的数据包
	private ByteBuffer tempBuffer;

	private Command tempCommand;

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

		// buffer校验
		int headerSize = Command.getHeaderSize();
		if (buffer == null) {
			return buffer;
		}

		List<Command> commands = new ArrayList<Command>();
		while (buffer.readableBytes() > 0) {
			// 若剩余数据小于headerSize，则缓存起来，等待完整的数据包头
			if (buffer.readableBytes() < headerSize) {
				// 重用tempBuffer
				if (tempBuffer != null) {
					tempBuffer.clear();
				} else {
					tempBuffer = ByteBuffer.allocate(headerSize);
				}
				readBytes(buffer, tempBuffer);
				break;
			}
			Command command = parseCommand(buffer, headerSize);
			if (command != null) {
				commands.add(command);
				log.info("Decode a command,comandType:" + command.getType() + ",commandLength:" + command.getSize()
						+ ",payLoadSize:" + command.getPayLoad().remaining());
			}
		}

		return commands;
	}

	/**
	 * 解析ChannelBuffer
	 * 
	 * @param buffer
	 * @return
	 */
	private Command parseCommand(ChannelBuffer buffer, int headerSize) {

		// 如果存在上次还未传输完毕的数据，则继续读取数据
		if (tempCommand != null) {
			readBytes(buffer, tempCommand.getPayLoad());
			if (tempCommand.getPayLoad().hasRemaining()) {
				return null;
			} else {
				Command command = tempCommand;
				tempCommand = null;
				command.getPayLoad().flip();
				return command;
			}
		}

		long type = 0;
		int length = 0;

		if (tempBuffer != null && tempBuffer.hasRemaining()) {
			readBytes(buffer, tempBuffer);
			tempBuffer.flip();
			type = tempBuffer.getLong();
			length = tempBuffer.getInt();
		}

		// 读取报文头
		type = buffer.readLong();
		length = buffer.readInt();

		return packageCommand(buffer, headerSize, type, length);

	}

	/**
	 * @param buffer
	 * @param headerSize
	 * @param type
	 * @param length
	 * @return
	 */
	private Command packageCommand(ChannelBuffer buffer, int headerSize, long type, int length) {

		// 计算payLoad长度
		int payLoadSize = length - headerSize;

		log.info("payLoadSize:" + payLoadSize);

		ByteBuffer payLoad = ByteBuffer.allocate(payLoadSize);
		readBytes(buffer, payLoad);

		if (payLoad.hasRemaining()) {
			tempCommand = new Command(type, payLoad);
			return null;
		} else {
			payLoad.flip();
			return new Command(type, payLoad);
		}
	}

	/**
	 * @param buffer
	 * @param payLoad
	 */
	private void readBytes(ChannelBuffer buffer, ByteBuffer payLoad) {
		int length = payLoad.remaining();
		buffer.getBytes(buffer.readerIndex(), payLoad);
		buffer.readerIndex(Math.min(buffer.readerIndex()+length,buffer.writerIndex()));
	}
}
