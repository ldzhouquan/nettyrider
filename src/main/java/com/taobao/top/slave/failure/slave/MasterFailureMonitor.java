package com.taobao.top.slave.failure.slave;

import com.taobao.top.common.LifeCycle;
import com.taobao.top.config.NettyriderConfig;
import com.taobao.top.master.MasterState;

/**
 * Slave端监控Master故障，并做响应的处理
 * 
 * @author raoqiang
 *
 */
public interface MasterFailureMonitor extends LifeCycle {
	
	int 	DEFAULT_STORE_MASTER_STATE_SIZE = 10;
	long 	DEFAULT_FAILURE_MONITOR_INTERVAL = NettyriderConfig.NETTYRIDER_DEFAULT_HEART_BEAT_INTERVAL * 4;
	long 	DEFAULT_FAILURE_MONITOR_WAIT_MASTER_STATE_TIME_OUT = NettyriderConfig.NETTYRIDER_DEFAULT_HEART_BEAT_INTERVAL * 10;
	
	/**
	 * 
	 * @param masterState
	 */
	void process(MasterState masterState);
}
