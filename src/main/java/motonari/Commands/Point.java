package motonari.Commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Point extends Canvas {
	public Point(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Point() {super();}
	
	public void init() {
		name = "Point";
		cmd = "point";
		desc = "Draws a single point on an ascii canvas.";
		arg_str = "x y [ch]";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "p", "asciipoint", "drawpoint"
		}) );
		
		options = new HashMap<String, String>();
		
	}
	
	int x;
	int y;
	char ch;
	
	public void main() {
		super.main();
		set(x, y, ch);
	}
	
	public String parse() {
		if (args.length < 3) return "Not enough arguments!";
		if (args.length > 4) return "Too many arguments!";
		if (!args[1].matches("-?\\d+")) return "x (" + args[1] + ") must be an integer!";
		x = Integer.valueOf(args[1]);
		if (x < 0 || x >= W) return "x (" + x + ") must be 0 <= x < " + W + "!";
		
		if (!args[2].matches("-?\\d+")) return "y (" + args[2] + ") must be an integer!";
		y = Integer.valueOf(args[2]);
		if (y < 0 || y >= H) return "y (" + y + ") must be 0 <= y < " + H + "!";

		if (args.length == 4) {
			if (args[3].length() != 1) return "ch (\"" + args[3] + "\") must be a character!";
			ch = args[3].charAt(0);
		} else {
			ch = '\u2588';
		}
		
		myOpts = Helper.options(args, 3, options);
		
		return "OK";
	}

	public String example(String alias) {
		String argStr = alias;
		
		Random rand = new Random();
		
		argStr += " " + rand.nextInt(W);
		argStr += " " + rand.nextInt(H);
		if (rand.nextDouble() < 0.7) {
			argStr += " " + (char)(rand.nextInt(93) + 33);
		}
		
		return argStr;
	}

}
