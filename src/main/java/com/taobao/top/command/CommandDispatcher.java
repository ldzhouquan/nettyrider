package com.taobao.top.command;

import java.util.Map;

/**
 * 
 * @author raoqiang
 *
 */
public interface CommandDispatcher {
	
	/**
	 * 
	 * @param command
	 * @param handler
	 */
	CommandDispatcher addCommandHandler(Long command, CommandHandler handler);
	
	/**
	 * 
	 * @param commandRoutingTable
	 */
	void setCommandRoutingTable(Map<Long, CommandHandler> commandRoutingTable);
	
	/**
	 * 
	 * @param command
	 * @return
	 */
	Command dispatch(Command command);
}
