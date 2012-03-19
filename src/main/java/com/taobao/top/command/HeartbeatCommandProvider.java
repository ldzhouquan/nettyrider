package com.taobao.top.command;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class HeartbeatCommandProvider implements CommandProvider {
	
	private static final String HELLO = "Hello Slave!";
	
	@Override
	public Command produce() {
		return makeHeartbeatCommand();
	}

	@Override
	public List<Command> produce(long count) {
		List<Command> commandList = new LinkedList<Command>();
		for(int i = 0; i < count; i++){
			commandList.add(makeHeartbeatCommand());
		}
		
		return commandList;
	}
	
	private Command makeHeartbeatCommand() {
		ByteBuffer buffer = ByteBuffer.allocate(HELLO.getBytes().length);
		buffer.put(HELLO.getBytes());
		buffer.flip();
		
		Command command = new Command(0L, buffer);
		
		return command;
	}
	
	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}

}
