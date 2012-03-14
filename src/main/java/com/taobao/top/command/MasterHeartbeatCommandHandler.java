/**
 * nettyrider
 *  
 */

package com.taobao.top.command;

import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.master.MasterState;
import com.taobao.top.node.Node;
import com.taobao.top.slave.SlaveState;

/**
 * <p>
 * Master端处理Heartbeat Command处理器
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">fangwu</a>
 */
public class MasterHeartbeatCommandHandler implements CommandHandler {

	private static final Log logger = LogFactory.getLog(MasterGreetCommandHandler.class);

	private Node master;

	public MasterHeartbeatCommandHandler(Node master) {
		this.master = master;
	}

	@Override
	public Command handle(Command command) {
		logger.info(new StringBuilder("Master receive heartbeat from slave : ").append(command.getSession()
				.getSlaveWorker().getIp()));
		/*
		// 重置session状态
		command.getSession().alive();

		// 读取payLoad
		ByteBuffer payLoad = command.getPayLoad();
		byte[] msg = payLoad.array();
		
		logger.info("Recieve message: " + new String(msg));
		*/
		command.getSession().alive();
		master.acceptStatistics(SlaveState.fromByteBuffer(command.getPayLoad()));
		ByteBuffer buffer = ((MasterState)master.gatherStatistics()).toByteBuffer();
		logger.info("MasterState to bytebuffer size: " + buffer.limit());
		return CommandFactory.createHeartbeatCommand(buffer);
	}
}
