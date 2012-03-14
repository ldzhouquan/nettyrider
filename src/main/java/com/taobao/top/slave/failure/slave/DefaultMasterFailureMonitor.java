package com.taobao.top.slave.failure.slave;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.common.NettyriderThreadFactory;
import com.taobao.top.master.MasterState;

public class DefaultMasterFailureMonitor implements MasterFailureMonitor {
	
	private static final Log logger = LogFactory.getLog(DefaultMasterFailureMonitor.class);
	
	private MasterFailureHandler masterFailureHandler;
	private ScheduledExecutorService monitorScheduler;
	
	private long	lastReceivedMasterStateTime;	// 
	private long    monitorInterval = DEFAULT_FAILURE_MONITOR_INTERVAL;
	private long    waitMasterStateTimeout = DEFAULT_FAILURE_MONITOR_WAIT_MASTER_STATE_TIME_OUT;

	/**
	 * 
	 */
	private List<MasterState> masterStateList = new ArrayList<MasterState>(MasterFailureMonitor.DEFAULT_STORE_MASTER_STATE_SIZE);
	
	public DefaultMasterFailureMonitor(MasterFailureHandler masterFailureHandler, long monitorInterval, long waitMasterStateTimeout) {
		this.masterFailureHandler = masterFailureHandler;
		this.monitorInterval = monitorInterval < DEFAULT_FAILURE_MONITOR_INTERVAL ? DEFAULT_FAILURE_MONITOR_INTERVAL : monitorInterval;
		this.waitMasterStateTimeout = waitMasterStateTimeout < DEFAULT_FAILURE_MONITOR_WAIT_MASTER_STATE_TIME_OUT ? DEFAULT_FAILURE_MONITOR_WAIT_MASTER_STATE_TIME_OUT : waitMasterStateTimeout;
	}
	
	@Override
	public boolean init() {
		// monitor scheduler
		monitorScheduler = Executors.newScheduledThreadPool(1, new NettyriderThreadFactory("Nettyrider-Monitor", null, false));
		return true;
	}

	@Override
	public boolean start() {
		monitorScheduler.scheduleAtFixedRate(new MonitorTask(), monitorInterval, monitorInterval, TimeUnit.SECONDS);
		return true;
	}

	@Override
	public boolean stop() {
		monitorScheduler.shutdownNow();
		return true;
	}
	
	@Override
	public boolean restart()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void process(MasterState masterState) {
		if(masterStateList.size() == MasterFailureMonitor.DEFAULT_STORE_MASTER_STATE_SIZE) {
			masterStateList.remove(MasterFailureMonitor.DEFAULT_STORE_MASTER_STATE_SIZE - 1);
		}
		masterStateList.add(masterState);
		lastReceivedMasterStateTime = System.currentTimeMillis();
		//logger.debug("Accept MasterState");
	}
	
	
	private void monitor() {
		long now = System.currentTimeMillis();
		if(now - lastReceivedMasterStateTime > waitMasterStateTimeout) {
			masterFailureHandler.handle(masterStateList);
		} else {
			lastReceivedMasterStateTime = now;
		}
	}
	
	private class MonitorTask implements Runnable {
		public void run() {
			try  {
				monitor();
			}
			catch(Exception e) {
				logger.error(e);
				throw new RuntimeException(e);
			}
		}
	} 
}
