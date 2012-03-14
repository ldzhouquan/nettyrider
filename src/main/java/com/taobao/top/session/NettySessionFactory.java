package com.taobao.top.session;

import org.jboss.netty.channel.Channel;

import com.taobao.top.command.CommandDispatcher;

/**
 * NettySessionFactory
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-24
 */
public class NettySessionFactory {

	public static NettySession newSession(Long id, Channel channel, int commandQueueSize,
			CommandDispatcher commandDispatcher) {
		NettySession session = new NettySession(id, commandQueueSize, channel);
		session.withCommandDispatcher(commandDispatcher);
		return session;
	}

}
