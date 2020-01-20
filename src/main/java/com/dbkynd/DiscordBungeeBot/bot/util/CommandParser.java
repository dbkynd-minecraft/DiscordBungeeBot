package com.dbkynd.DBBungeeBot.bot.util;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;

public class CommandParser {
	public CommandContainer parse(String rw, MessageReceivedEvent e, String prefix){
		ArrayList<String> split = new ArrayList<String>();
		String raw = rw;
		String fixed = raw.replaceFirst(prefix, "");
		String[] splitf = fixed.split(" ");
		for(String s: splitf){
			split.add(s);
		}
		String invoke = split.get(0);
		String[] args = new String[split.size() - 1];
		split.subList(1, split.size()).toArray(args);
		
		return new CommandContainer(raw, fixed, splitf, invoke, args, e);
	}
	
	public class CommandContainer{
		public final String raw;
		public final String fixed;
		public final String[] splitf;
		public final String invoke;
		public final String[] args;
		public final MessageReceivedEvent event;
		
		public CommandContainer(String rw, String fixed, String[] splitf, String invoke, String[] args, MessageReceivedEvent e){
			this.raw = rw;
			this.fixed = fixed;
			this.splitf = splitf;
			this.invoke = invoke;
			this.args = args;
			this.event = e;
			
		}
	}
}
