/**
 * nettyrider
 *  
 */

package com.taobao.top.network;

import com.taobao.top.common.LifeCycle;

/**
 * <p>
 * 网络客户端，运行在Slave节点
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface NetWorkClient extends LifeCycle {
	
	String NET_WORK_CLIENT_THREAD_NAME = "Nettyrider-NetWorkClient-Thread";
	
}
