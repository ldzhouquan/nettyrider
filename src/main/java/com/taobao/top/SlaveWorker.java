/**
 * Nettyrider
 *  
 */

package com.taobao.top;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * <p>
 * Slave信息
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class SlaveWorker implements Serializable {
	
	private static final long serialVersionUID = 3802995283987134205L;
	
	private long 	id;		// ID
	private String 	name;	// 名称
	private String  ip;		// IP
	private int     port;	// Port

	public SlaveWorker(long id, String name, String ip, int port) {
		this.id = id;
		this.name = name;
		this.ip = ip;
	}
	
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public static SlaveWorker fromChannel(Long id, String name, SocketChannel channel) {
		InetSocketAddress remoteAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
		SlaveWorker worker = new SlaveWorker(id, name, remoteAddress.getHostName(), remoteAddress.getPort());
		return worker;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		sb.append(",");
		sb.append(name);
		sb.append(",");
		sb.append(ip);
		sb.append(",");
		sb.append(port);
		return sb.toString();
	}
}
