package motonari.Commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import motonari.Tomo.Helper;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {
	public String name;
	public String cmd;
	public String desc;
	
	public boolean admin = false;
	
	
	public String arg_str = "";
	public HashSet<String> aliases;
	
	public HashMap<String, String> options;
	
	public boolean isAlias(String alias) {
		return aliases.contains(alias);
	}
	
	public String randomAlias() {
		Random rand = new Random();
		String[] arr = aliases.toArray(new String[aliases.size()]);
		return arr[rand.nextInt(arr.length)];
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
	
	public String permissions() {
		if (this.admin && !e.getAuthor().getId().equals(Tomo.ADMIN)) {
			return "You don't have permission to use this command!";
		}
		return "OK";
	}
	
	public boolean byAdmin() {
		return Tomo.ADMIN.equals(e.getAuthor().getId());
	}
	
	abstract public String parse();	
	abstract public void main();
	abstract public void answer();
	
	public void run() {
		String per = permissions();
		if (!per.equals("OK")) {
			Helper.error(e, c, args[0], per, 7);
			return;
		}
		String err = parse();
		if (!err.equals("OK")) {
			Helper.error(e, c, args[0], err, 15);
			return;
		};
		
		main();
		answer();
	}
	
	public abstract String example(String alias);
	
	public String example() {
		return example(this.randomAlias());
	}
	
	public EmbedBuilder help(String alias) {
		EmbedBuilder help = new EmbedBuilder();
		help.setTitle(name);
		String in = "`" + Tomo.prefix + alias + " " + arg_str;
		if (!options.isEmpty())
			in += " [-o | --option]";
		in += "`";
		
		help.addField(in, desc, false);
		if (aliases.size() > 1) help.addField("Aliases", mdList(aliases), true);
		
		if (!options.isEmpty()) {
			LinkedList<String> optStrs = new LinkedList<String>();
			Object[] keys = options.keySet().toArray();
			Arrays.sort(keys);
			for (Object opt : keys) optStrs.addLast("-" + options.get(opt) + " | --" + opt);
			help.addField("Options", mdList(optStrs), true);
		}
		
		return help;
	}
	
	private static String mdList(Iterable<? extends CharSequence> list) {
		return "```md\n- " + String.join("\n- ", list) + "\n```";
	}
}
