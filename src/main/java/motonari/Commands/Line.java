package motonari.Commands;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Line extends Canvas {
	private static final String NAME = "ascii Line";
	private static final String MAIN_CMD = "l";
	private static final String DESC = "Draws ascii line";
	
	private static final String ARGSTR = "x1 y1 x2 y2";
	private static final HashSet<String> ALIASES = new HashSet<String>( Arrays.asList(new String[] {
			MAIN_CMD, "line", "asciiline", "drawline"
	}) );
	
	private static final HashMap<String, String> OPTIONS = new HashMap<String, String>();
	
	public static boolean isAlias(String alias) {
		return ALIASES.contains(alias);
	}
	
	public static String name() {
		return NAME;
	}
	
	public static String desc() {
		return DESC;
	}
	
	public static void help(MessageChannel c) {
		Helper.commandHelp(c, NAME, MAIN_CMD, DESC, ARGSTR, ALIASES, OPTIONS);
	}
	
	MessageReceivedEvent e;
	MessageChannel c;
	HashSet<String> myOpts;
	String[] args;
	int x1;
	int y1;
	int x2;
	int y2;
	
	Line(MessageReceivedEvent e, String[] args) {
		super(64, 25);
		this.args = args;
		this.e = e;
		this.c = e.getChannel();
	}
	
	public void run() {
		System.out.println("====");
		String err = parse();
		if (!err.equals("OK")) {
			Helper.error(c, args[0], err);
			return;
		};
		
		main();
		send();
	}
	
	private void main() {
		line(x1, y1, x2, y2);
	}
	
	public void send() {		
		String msg = "```\n";
		for (int i = 0; i < H; i++) {
			msg += map[i];
		}
		msg += "```";
		c.sendMessage(msg).queue();
	}
	
	private String parse() {
		if (args.length < 5) return "Not enough arguments!";
		if (!args[1].matches("-?\\d+")) return "x1 (" + args[1] + ") must be an integer!";
		x1 = Integer.valueOf(args[1]);
		if (x1 < 0 || x1 >= W) return "x1 (" + x1 + ") must be 0 <= x1 < " + W + "!";
		
		if (!args[2].matches("-?\\d+")) return "y1 (" + args[2] + ") must be an integer!";
		y1 = Integer.valueOf(args[2]);
		if (y1 < 0 || y1 >= H) return "y1 (" + y1 + ") must be 0 <= y1 < " + H + "!";
		
		if (!args[3].matches("-?\\d+")) return "x2 (" + args[3] + ") must be an integer!";
		x2 = Integer.valueOf(args[3]);
		if (x2 < 0 || x2 >= W) return "x2 (" + x2 + ") must be 0 <= x2 < " + W + "!";
		
		if (!args[4].matches("-?\\d+")) return "y2 (" + args[4] + ") must be an integer!";
		y2 = Integer.valueOf(args[4]);
		if (y2 < 0 || y2 >= H) return "y2 (" + y2 + ") must be 0 <= y2 < " + H + "!";
		
		String err = Helper.checkOptions(args, 5, OPTIONS);
		if (!err.equals("OK")) return err;
		
		myOpts = Helper.options(args, 5, OPTIONS);
		
		return "OK";
	}
}
