package com.taobao.top.network.netty.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.taobao.top.command.Command;
import com.taobao.top.session.NettySession;
import com.taobao.top.session.NettySessionManager;

/**
 * CommandServerChannelHandler
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-2-16
 */
public class CommandServerChannelHandler extends SimpleChannelHandler {

	private static final Log logger = LogFactory.getLog(CommandServerChannelHandler.class);

	private NettySessionManager nettySessionManager;
	
	/**
	 * Constructor
	 * @param nettySessionManager
	 */
	public CommandServerChannelHandler(NettySessionManager nettySessionManager) {
		super();
		this.nettySessionManager = nettySessionManager;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		ctx.setAttachment(nettySessionManager.newSession(e.getChannel(), true));
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (!(e.getMessage() instanceof Command)) {
			return;
		}
		Command command = (Command) e.getMessage();
		NettySession session = (NettySession) ctx.getAttachment();
		command.setSession(session);
		session.addCommand(command);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		e.getCause().printStackTrace();
		logger.error(e.getCause());
	}
}
