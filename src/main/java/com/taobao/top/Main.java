package com.taobao.top;

import com.taobao.top.config.NettyriderConfig;
import com.taobao.top.node.NettyMasterNode;
import com.taobao.top.node.NettySlaveNode;
import com.taobao.top.node.Node;

/**
 * start class java nettyrider Main -mode master java nettyrider Main -mode slave
 * 
 * @author raoqiang
 */
public class Main {

	private static final String MASTER_MODE = "master";
	private static final String SLAVE_MODE = "slave";
	private static final String ARG_MODE_KEY = "-mode";
	private static final int MIN_ARGS_LENGTH = 2;

	private static NettyriderConfig config = new NettyriderConfig();
	private static Node node;

	public static void showUsage() {
		System.out.println("nettyrider");
		System.out.println("	Usage:");
		System.out.println("			Run as master mode:");
		System.out.println("				java -jar nettyrider.jar Main -mode master");
		System.out.println("			Run as slave mode:");
		System.out.println("				java -jar nettyrider.jar Main -mode slave");
	}

	public static void main(String[] args) {
		if (args.length < MIN_ARGS_LENGTH) {
			showUsage();
			return;
		}

		if (!ARG_MODE_KEY.equals(args[0])) {
			showUsage();
			return;
		}

		if (MASTER_MODE.equals(args[1])) {
			runAsMaster();
		} else if (SLAVE_MODE.equals(args[1])) {
			runAsSlave();
		} else {
			showUsage();
			return;
		}

		try {
			Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void runAsMaster() {
		System.out.println("Run as master model ......");
		node = new NettyMasterNode(config);
		node.init();
		node.start();
	}

	public static void runAsSlave() {
		System.out.println("Run as slave model ......");
		config.setMasterAddress("127.0.0.1");
		node = new NettySlaveNode(config);
		node.init();
		node.start();
	}
}
