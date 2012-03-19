/**
 * nettyrider
 *  
 */

package com.taobao.top.command;

import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Master端处理Heartbeat Command处理器
 * </p>
 * 
 * @author fangwu.zq
 */
public class MasterHeartbeatCommandHandler implements CommandHandler {

	private static final Log logger = LogFactory.getLog(MasterHeartbeatCommandHandler.class);

	@Override
	public Command handle(Command command) {
		
		logger.info(new StringBuilder("Master receive heartbeat from slave : ").append(command.getSession()
				.getSlaveWorker().getIp()));

		// 读取payLoad
		ByteBuffer payLoad = command.getPayLoad();
		byte[] msg = payLoad.array();
		
		logger.info("Recieve message: " + new String(msg));
		
		return null;
		
	}
}
