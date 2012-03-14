/**
* waverider 
 */

package com.taobao.top.network;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.command.Command;

/**
 * <p>
 * 网络数据报
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class Packet {
	
	private static final Log logger = LogFactory.getLog(Packet.class);
	
	private static final long PACKET_TYPE_DATA = 0;
	
	private static final AtomicLong sequenceGenerator = new AtomicLong(0);
	
	//=====================================================================
	//			Header
	//=====================================================================
	private String magic = NetWorkConstants.NETTYRIDER_MAGIC;	// 报文魔数
	private Long sequence;										// 报文Sequence
	private Long type;											// 报文类型
	private Integer length;										// 整个报文的长度
	
	//=====================================================================
	//			Body(负载)
	//=====================================================================
	private ByteBuffer payLoad;									// 报文负载
	
	/**
	 * 获取整个报文长度
	 * @return
	 */
	public int getSize() {
		int size = 0;
		size += getHeaderSize();
		size += payLoad.remaining();
		
		return size;
	}
	
	/**
	 * 获取报文类型
	 * @return
	 */
	public Long getType() {
		return type;
	}

	/**
	 * 设置报文类型
	 * @param type
	 */
	public void setType(Long type) {
		this.type = type;
	}

	/**
	 * 获取报文长度
	 * @return
	 */
	public Integer getLength() {
		return length;
	}

	/**
	 * 设置报文长度
	 * @param length
	 */
	public void setLength(Integer length) {
		this.length = length;
	}
	
	/**
	 * 获取报文魔数
	 * @return
	 */
	public String getMagic() {
		return magic;
	}

	/**
	 * 设置报文魔数
	 * @param magic
	 */
	public void setMagic(String magic) {
		this.magic = magic;
	}
	
	/**
	 * 
	 * @return
	 */
	public Long getSequence() {
		return sequence;
	}

	/**
	 * 
	 * @param sequence
	 */
	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}
	
	/**
	 * 获取报文负载
	 * @return
	 */
	public ByteBuffer getPayLoad() {
		return payLoad;
	}

	/**
	 * 设置报文负载
	 * @param payLoad
	 */
	public void setPayLoad(ByteBuffer payLoad) {
		this.payLoad = payLoad;
	}
	
	//=====================================================================
	//			工具方法
	//=====================================================================
	
	/**
	 * 报文头部长度
	 * @return
	 */
	public static int getHeaderSize() {
		int size = 0;
		size += NetWorkConstants.NETTYRIDER_MAGIC.getBytes().length;
		size += Long.SIZE / Byte.SIZE;
		size += Long.SIZE / Byte.SIZE;
		size += Integer.SIZE / Byte.SIZE;
		
		return size;
	}
	
	/**
	 * 获取报文长度字段的位置
	 * @return
	 */
	public static int getLengthPosition() {
		int size = 0;
		size += NetWorkConstants.NETTYRIDER_MAGIC.getBytes().length;
		size += Long.SIZE / Byte.SIZE;
		size += Long.SIZE / Byte.SIZE;
		
		return size;
	}
	
	/**
	 * 将报文写到ByteBuffer中
	 * @return
	 */
	public ByteBuffer marshall(){
		int size = getSize();
		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.put(magic.getBytes());
		buffer.putLong(sequence);
		buffer.putLong(type);
		buffer.putInt(size);
		buffer.put(payLoad);
		buffer.flip();
		return buffer;
	}
	
	/**
	 * 将ByteBuffer转换成报文
	 * @param buffer
	 * @return
	 */
	public static Packet unmarshall(ByteBuffer buffer) {
		if(buffer.remaining() < getHeaderSize()) {
			throw new RuntimeException("Wrong packet.");
		}
		
		Packet packet = new Packet();
		byte[] str = new byte[NetWorkConstants.NETTYRIDER_MAGIC.getBytes().length];
		buffer.get(str);
		packet.setMagic(new String(str));
		
		if(!NetWorkConstants.NETTYRIDER_MAGIC.equals(packet.getMagic())){
			throw new RuntimeException("Wrong packet.");
		}
		
		packet.setSequence(buffer.getLong());
		packet.setType(buffer.getLong());
		packet.setLength(buffer.getInt());
		packet.setPayLoad(buffer.slice());
		return packet;
	}
	
	public static Packet newDataPacket(Command command) {
		return newDataPacket(command, sequenceGenerator.getAndAdd(2));
	}
	/**
	 * 通过命令创建报文
	 * @param command
	 * @return
	 */
	public static Packet newDataPacket(Command command, Long sequence) {
		Packet packet = new Packet();
		packet.setSequence(sequence);
		packet.setType(PACKET_TYPE_DATA);
		ByteBuffer commandByteBuffer = command.marshall();
		packet.setLength(getHeaderSize() + commandByteBuffer.remaining());
		packet.setPayLoad(commandByteBuffer);
		//logger.info("New one packat");
		//packet.dump();
		return packet;
	}
	
	// For debug
	public void dump() {
		logger.info("==========================================================================dump packet==========================================================================");
		logger.info(new StringBuilder("== magic:").append(magic));
		logger.info(new StringBuilder("== sequence:").append(sequence));
		logger.info(new StringBuilder("== type:").append(type));
		logger.info(new StringBuilder("== length:").append(length));
		logger.info(new StringBuilder("== payLoad Size:").append(payLoad.remaining()));
		logger.info(new StringBuilder("== binary:"));
		byte[] buffer = this.copy().marshall().array();
		StringBuilder sb = new StringBuilder("== ");
		for(int i = 0; i < buffer.length; i++) {
			sb.append(buffer[i]);
			if(i % 80 == 0 && i > 0) {
				logger.info(sb);
				sb.delete(0, sb.length());
			}
		}
		logger.info("========================================================================dump packet end========================================================================");
	}
	
	private Packet copy() {
		Packet packet = new Packet();
		packet.magic = magic;
		packet.sequence = sequence;
		packet.type = type;
		packet.length = length;
		packet.payLoad = payLoad.duplicate();
		
		return packet;
	}
	
}
