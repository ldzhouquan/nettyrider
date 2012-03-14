package com.taobao.top.command;


/**
 * 命令发生器管理类
 * 
 * @author <a href="mailto:fangwu.zq@taobao.com">fangwu.zq</a>
 * @version 1.0
 * @since 2012-3-12
 */
public interface CommandProviderMannager<T> {
	
	/**
	 * 添加一个命令发生器
	 * @param commandProvider
	 */
	void addCommandProvider(CommandProvider commandProvider);
	
	/**
	 * Mannager初始化
	 * @return
	 */
	boolean init();
	
	/**
	 * 启动命令发生器线程，开始生产命令
	 * @return
	 */
	boolean start();
	
	/**
	 * 停止生产命令
	 * @return
	 */
	boolean stop();
	
	/**
	 * 重启线程
	 * @return
	 */
	boolean restart();
	
	/**
	 * 设置Channel
	 * @param t
	 */
	void withChannel(T t);
}
