/**
 * Nettyrider
 * 
 */
package com.taobao.top.network;

import com.taobao.top.common.LifeCycle;
import com.taobao.top.session.SessionManager;

/**
 * <p>
 * 网络服务器，运行在Master节点
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface NetWorkServer extends LifeCycle {
	
	String NET_WORK_SERVER_THREAD_NAME = "Nettyrider-NetWorkServer-Thread";
	String NET_WORK_READER = "Nettyrider-NetworkServer-NetworkReader";
	String NET_WORK_WRITER = "Nettyrider-NetworkServer-NetworkWriter";
	
	int NETWORK_OPERATION_ACCEPT = 0;
	int NETWORK_OPERATION_READ = 1;
	int NETWORK_OPERATION_WRITE = 2;
	
	/**
	 * 
	 * @return
	 */
	String getIp();
	
	/**
	 * 
	 */
	int getPort();
	
	/**
	 * 
	 * @param sessionManager
	 */
	void setSessionManager(SessionManager sessionManager);
}
