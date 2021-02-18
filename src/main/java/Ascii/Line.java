package Ascii;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import motonari.Tomo.Helper;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Line extends Canvas {
	public Line(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Line() {super();}
	
	public void init() {
	
		name = "ascii Line";
		cmd = "line";
		desc = "Draws ascii line.";
		
		arg_str = "x1 y1 x2 y2";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "l", "asciiline", "drawline"
		}) );
		
		options = new HashMap<String, String>();
	}
	
	int x1;
	int y1;
	int x2;
	int y2;
	
	public void main() {
		super.main();
		line(x1, y1, x2, y2);
	}
	
	public String parse() {
		if (args.length < 5) return "Not enough arguments!";
		if (args.length > 5) return "Too many arguments!";
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
		
		String err = Helper.checkOptions(args, 5, options);
		if (!err.equals("OK")) return err;
		
		myOpts = Helper.options(args, 5, options);
		
		return "OK";
	}

	public String example(String alias) {
		String argStr = alias;
		
		Random rand = new Random();
		
		argStr += " " + rand.nextInt(W);
		argStr += " " + rand.nextInt(H);
		argStr += " " + rand.nextInt(W);
		argStr += " " + rand.nextInt(H);
		
		return argStr;
	}
}
