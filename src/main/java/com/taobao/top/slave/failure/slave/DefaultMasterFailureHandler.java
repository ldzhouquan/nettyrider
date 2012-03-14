package com.taobao.top.slave.failure.slave;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.master.MasterState;
import com.taobao.top.node.SlaveNode;

public class DefaultMasterFailureHandler implements MasterFailureHandler {

	private static final Log logger = LogFactory.getLog(DefaultMasterFailureHandler.class);
	
	private SlaveNode slave;
	
	public DefaultMasterFailureHandler(SlaveNode slave) {
		this.slave = slave;
	}
	
	@Override
	public void handle(List<MasterState> masterStateList) {
		logger.warn("Master down");
		dump(masterStateList);
	}
	
	private void dump(List<MasterState> masterStateList) {
		logger.debug("Start to dump masterStateList in MasterFailureMonitor:");
		for(MasterState masterState : masterStateList) {
			logger.debug(masterState);
		}
	}

}
