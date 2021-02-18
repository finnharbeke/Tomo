package motonari.Commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Example extends Command {
	public Example(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Example() {super();}
	
	@Override
	public void init() {
		name = "Example command usages";
		cmd = "ex";
		desc = "Let the bot show you the usage of a (random) command.";
		
		
		arg_str = "[cmd]";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "x",
		}) );
		
		options = new HashMap<String, String>();
	}
	
	Command ex_cmd = null;
	String argStr;

	@Override
	public String parse() {
		if (args.length == 1) {
			
		} else if (args.length == 2) {
			if (Tomo.fromAlias(args[1]) == null) {
				return "Unknown Command (" + args[1] +  ")!";
			}
			try {
				ex_cmd = Tomo.fromAlias(args[1]).getConstructor().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return "Too many arguments";
		}
		return "OK";
	}

	@Override
	public void main() {
		if (ex_cmd == null) {
			try {
				Command ex = Tomo.random(byAdmin()).getConstructor().newInstance();
				argStr = ex.example();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			argStr = ex_cmd.example(args[1]);
		}

	}

	@Override
	public void answer() {
		c.sendMessage((Tomo.DEV ? Tomo.dev_pre : "") + Tomo.prefix + argStr).queue();
	}

	@Override
	public String example(String alias) {
		Random rand = new Random();
		String res = alias;
		if (rand.nextDouble() < 0.4) {
			return res;
		} else {
			try {
				Command cmd = Tomo.random(byAdmin()).getConstructor().newInstance();
				res += " " + cmd.randomAlias();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}

}
