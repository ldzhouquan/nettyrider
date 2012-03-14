package com.taobao.top.session;

import java.util.List;

import org.jboss.netty.channel.Channel;

import com.taobao.top.command.CommandDispatcher;
import com.taobao.top.common.LifeCycle;

/**
 * <p>
 * Master端Session管理器
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 2.0
 * @since 2012-2-24
 *
 */

public interface SessionManager extends LifeCycle {
	
	String SESSION_RECYCLE_THREAD_NAME_PREFIX = "Nettyrider-Sesion-Recycle";
	
	/**
	 * 设置Session的命令分发器
	 * @param commandDispatcher
	 */
	void setCommandDispatcher(CommandDispatcher commandDispatcher);
	
	/**
	 * 
	 * @param netWorkServer
	 * @param channel
	 * @param start
	 * @return
	 */
	Session newSession(Channel channel, boolean start);
	
	/**
	 * 
	 * @param session
	 */
	void freeSession(Session session);
	
	/**
	 * 
	 * @return
	 */
	List<SessionState> generateSessionState();
}
