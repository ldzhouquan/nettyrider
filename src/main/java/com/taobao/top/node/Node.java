/**
 * Nettyrider
 * 
 */

package com.taobao.top.node;

import com.taobao.top.State;
import com.taobao.top.command.CommandHandler;
import com.taobao.top.common.LifeCycle;

/**
 * <p>
 * 分布式节点，Master节点MasterNodel和Slave节点SlaveNode实现本接口
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface Node extends LifeCycle {
	
	/**
	 * 添加命令处理器, command=0,系统保留给Heartbeat command
	 * @param command
	 * @param handler
	 */
	void addCommandHandler(Long command, CommandHandler handler);
	
	/**
	 * 收集统计信息
	 * 
	 * @return
	 */
	State gatherStatistics();
	
	/**
	 * 
	 * @param state
	 */
	void acceptStatistics(State state);

}
