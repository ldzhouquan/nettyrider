/**
 * Nettyrider
 *  
 */

package com.taobao.top.node;

import com.taobao.top.command.CommandProvider;

/**
 * <p>
 * 基于Master-Slave结构分布式命令执行框架Slave节点接口
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface SlaveNode extends Node {

	String SLAVE_COMMAND_PROVIDER_THREAD_NAME = "Waverider-Slave-Command-Provider-Thread";
	String SLAVE_HEART_BEAT_THREAD_NAME_PREFIX = "Waverider-Slave-Heartbeat";
	String SLAVE_COMMAND_DISPATCHE_THREAD_NAME = "Waverider-Slave-Command-Dispatcher-Thread";
	
	/**
	 * 添加命令产生器
	 * @param commandProvider
	 */
	void addCommandProvider(CommandProvider commandProvider);
	
	/**
	 * 
	 * @param commandProviderName
	 */
	//void startCommandProvider(String commandProviderName);
}
