package Ascii;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import motonari.Tomo.Helper;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Draw extends Canvas {
	public Draw(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Draw() {super();}
	
	public void init() {
		name = "Draw";
		cmd = "draw";
		desc = "Draws points and lines on an ascii canvas.";
		arg_str = "(p x y ch | l x1 y1 x2 y2){1,}";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "d", "asciidraw"
		}) );
		
		options = new HashMap<String, String>();
		
	}
	
	ArrayList<Action> actions;
	
	public void main() {
		super.main();
		for (Action act : actions) {
			act.exec(this);
		}
	}
	
	public String parse() {
		int i = 1;
		actions = new ArrayList<Action>();
		
		while (i < args.length) {
			if (args[i].equals("p")) {
				if (args.length < i + 4) return "Not enough arguments!";
				i++;
				int x, y;
				char ch;
				if (!args[i].matches("-?\\d+")) return "x (" + args[i] + ") must be an integer!";
				x = Integer.valueOf(args[i]);
				if (x < 0 || x >= W) return "x (" + x + ") must be 0 <= x < " + W + "!";
				i++;
				if (!args[i].matches("-?\\d+")) return "y (" + args[i] + ") must be an integer!";
				y = Integer.valueOf(args[i]);
				if (y < 0 || y >= H) return "y (" + y + ") must be 0 <= y < " + H + "!";
				i++;
				if (args[i].length() != 1) return "ch (\"" + args[i] + "\") must be a character!";
				ch = args[i].charAt(0);
				actions.add(new Action(x, y, ch));
				
			} else if (args[i].equals("l")) {
				if (args.length < i + 5) return "Not enough arguments!";
				i++;
				int x1, y1, x2, y2;
				if (!args[i].matches("-?\\d+")) return "x1 (" + args[i] + ") must be an integer!";
				x1 = Integer.valueOf(args[i]);
				if (x1 < 0 || x1 >= W) return "x1 (" + x1 + ") must be 0 <= x1 < " + W + "!";
				i++;
				if (!args[i].matches("-?\\d+")) return "y1 (" + args[i] + ") must be an integer!";
				y1 = Integer.valueOf(args[i]);
				if (y1 < 0 || y1 >= H) return "y1 (" + y1 + ") must be 0 <= y1 < " + H + "!";
				i++;
				if (!args[i].matches("-?\\d+")) return "x2 (" + args[i] + ") must be an integer!";
				x2 = Integer.valueOf(args[i]);
				if (x2 < 0 || x2 >= W) return "x2 (" + x2 + ") must be 0 <= x2 < " + W + "!";
				i++;
				if (!args[i].matches("-?\\d+")) return "y2 (" + args[i] + ") must be an integer!";
				y2 = Integer.valueOf(args[i]);
				if (y2 < 0 || y2 >= H) return "y2 (" + y2 + ") must be 0 <= y2 < " + H + "!";
				
				actions.add(new Action(x1, y1, x2, y2));
			} else {
				return "Invalid cmd: \"" + args[i] + "\"!";
			}
			i++;
		}

		myOpts = Helper.options(args, 3, options);
		
		return "OK";
	}

	
	public String example(String alias) {
		final int M_UPPER_LIM = 20;
		final int M_LOWER_LIM = 1;
		
		String argStr = alias;
		
		Random rand = new Random();
		int m = rand.nextInt(M_UPPER_LIM - M_LOWER_LIM) + M_LOWER_LIM;
		
		for (int i = 0; i < m; i++) {
			if (rand.nextDouble() < 0.5) {
				argStr += " p";
				argStr += " " + rand.nextInt(W);
				argStr += " " + rand.nextInt(H);
				argStr += " " + (char)(rand.nextInt(93) + 33);
			} else {
				argStr += " l";
				argStr += " " + rand.nextInt(W);
				argStr += " " + rand.nextInt(H);
				argStr += " " + rand.nextInt(W);
				argStr += " " + rand.nextInt(H);
			}
		}
		
		return argStr;
	}

}

class Action {
	boolean point;
	int x;
	int y;
	char ch;
	Action(int x, int y, char ch) {
		point = true;
		this.x = x;
		this.y = y;
		this.ch = ch;
	}
	
	int x1;
	int y1;
	int x2;
	int y2;
	Action(int x1, int y1, int x2, int y2) {
		point = false;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	void exec(Draw canvas) {
		if (point) {
			canvas.set(x, y, ch);
		} else {
			canvas.line(x1, y1, x2, y2);
		}
	}
}