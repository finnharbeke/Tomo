package motonari.Commands;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Line {
	private static final String NAME = "ascii Line";
	private static final String MAIN_CMD = "l";
	private static final String DESC = "Draws ascii line";
	
	private static final String ARGSTR = "x1 y1 x2 y2";
	private static final HashSet<String> ALIASES = new HashSet<String>( Arrays.asList(new String[] {
			MAIN_CMD, "line", "asciiline", "drawline"
	}) );
	
	private static final HashMap<String, String> OPTIONS = new HashMap<String, String>();
	
	private static int W = 64;
	private static int H = 25;
	
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
	String[] map;
	
	Line(MessageReceivedEvent e, String[] args) {
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
		
		draw();
	}
	
	private void main() {
		initMap();
		
		char ch1, ch2;
		char[] tmp = symbols(x1, y1, x2, y2);
		ch1 = tmp[0]; ch2 = tmp[1];
		
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		
		if (y2 > y1) {
			int temp = x2; x2 = x1; x1 = temp;
			temp = y2; y2 = y1; y1 = temp;
		}
		
		int xDir, yDir;
		if (x2 > x1) xDir = 1;
		else xDir = -1;
		if (y2 > y1) yDir = 1;
		else yDir = -1;
		
		System.out.println(xDir + " " + yDir);
		
		int x = x1;
		int y = y1;
		boolean steep = false;
		if (dy > dx) {
			System.out.println("steep");
			steep = true;
			int temp = dx; dx = dy; dy = temp;
			temp = xDir; xDir = yDir; yDir = temp;
			y = x1;
			x = y1;
		}
		
		char[] chars = new char[dx+1];
		int[] xs = new int[dx+1];
		int[] ys = new int[dx+1];
		
		double delta = dy / (double)dx;
		double err = 0;
		
		for (int i = 0; i < dx; i++) {
			chars[i] = ch1;
			xs[i] = x;
			ys[i] = y;
			err += delta;
			//System.out.println(err);
			if (err >= 0.5) {
				chars[i] = ch2;
	            y += yDir;
	            err--;
			}
			x += xDir;
		}
		chars[dx] = err >= 0.5 ? ch1 : ch2;
		xs[dx] = x;
		ys[dx] = y;
		
		for (int i = 0; i < chars.length; i++) {
			if (steep)
				set(ys[i], xs[i], chars[i]);
			else
				set(xs[i], ys[i], chars[i]);
		}
		
	}
	
	private static char[] symbols(int x1, int y1, int x2, int y2) {
		
		// make p1 left of p2
		if (x2 < x1) {
			System.out.println("symbol switch");
			int temp = x2;
			x2 = x1;
			x1 = temp;
			temp = y2;
			y2 = y1;
			y1 = temp;
		}
		
		// set drawing chars		
		char ch1;
		char ch2;
		double slope = x1 == x2 ? 1e99 : (double)(y2 - y1) / (double)(x2 - x1);
		if (slope > 0) {
			if (slope > 1) {
				ch1 = '|'; ch2 = '\\';
			} else {
				ch1 = '_'; ch2 = '\\';
			}
		} else {
			if (slope < -1) {
				ch1 = '|'; ch2 = '/';
			} else {
				ch1 = '_'; ch2 = '/';
			}
		}
		
		return new char[] {ch1, ch2};
	}
	
	private void draw() {		
		String msg = "```\n";
		for (int i = 0; i < H; i++) {
			msg += map[i];
		}
		msg += "```";
		c.sendMessage(msg).queue();
	}
	
	private void set(int x, int y, char ch) {
		map[y] = map[y].substring(0, x) + ch + map[y].substring(x + 1);
		
	}
	
	private void initMap() {
		map = new String[H];
		for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) map[i] = j == 0 ? " " : map[i] + " ";
			map[i] += "\n";
		}
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
