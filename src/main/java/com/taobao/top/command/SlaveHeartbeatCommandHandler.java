/**
 * nettyrider
 *  
 */

package com.taobao.top.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.master.MasterState;
import com.taobao.top.node.Node;
import com.taobao.top.slave.SlaveState;

/**
 * <p>
 * Slave端处理Heartbeat Command处理器
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class SlaveHeartbeatCommandHandler implements CommandHandler {
	
	private static final Log logger = LogFactory.getLog(SlaveHeartbeatCommandHandler.class);
	
	private Node slave;
	
	public SlaveHeartbeatCommandHandler(Node slave) {
		this.slave = slave;
	}
	
	@Override
	public Command handle(Command command) {
		logger.info("Slave received heartbeat from master");
		slave.acceptStatistics(MasterState.fromByteBuffer(command.getPayLoad()));
		return CommandFactory.createHeartbeatCommand(((SlaveState)slave.gatherStatistics()).toByteBuffer());
	}
}
