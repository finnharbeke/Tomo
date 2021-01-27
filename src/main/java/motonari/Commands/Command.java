package motonari.Commands;

import java.util.HashMap;
import java.util.HashSet;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {
	public String name;
	public String cmd;
	public String desc;
	
	
	public String arg_str = "";
	public HashSet<String> aliases;
	
	public HashMap<String, String> options;
	
	public boolean isAlias(String alias) {
		return aliases.contains(alias);
	}
	
	public void help(MessageChannel c) {
		Helper.commandHelp(c, this);
	}
	
	abstract public void init();
	
	public Command() {
		init();
	}
	
	public String[] args;
	public MessageReceivedEvent e;
	public MessageChannel c;
	public HashSet<String> myOpts;
	
	public Command(MessageReceivedEvent e, String[] args) {
		this.args = args;
		this.e = e;
		this.c = e.getChannel();
		init();
	}
	
	public String parse() {
		if (args.length > 1) return "Too many arguments!";
		return "OK";
	}
	
	abstract public void main();
	abstract public void answer();
	
	public void run() {
		String err = parse();
		if (!err.equals("OK")) {
			Helper.error(c, args[0], err);
			return;
		};
		
		main();
		answer();
	}
	
	public abstract String example(String alias);
}
