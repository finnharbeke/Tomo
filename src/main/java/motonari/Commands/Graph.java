package motonari.Commands;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Graph extends Canvas {
	private static final String NAME = "ascii Graph";
	private static final String MAIN_CMD = "g";
	private static final String DESC = "Draws ascii graph.";
	
	private static final String ARGSTR = "n m (u v){m}";
	private static final HashSet<String> ALIASES = new HashSet<String>( Arrays.asList(new String[] {
			MAIN_CMD, "graph", "asciigraph", "drawgraph"
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
	int n;
	int m;
	int[] us;
	int[] vs;
	
	Graph(MessageReceivedEvent e, String[] args) {
		super(64, 25);
		this.e = e;
		this.c = e.getChannel();
		this.args = args;
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
		int[] xs = new int[n];
		int[] ys = new int[n];
		
		int xRad = (W-2) / 2;
		int yRad = (H-2) / 2;
		double angle = Math.PI / 2;
		for (int i = 0; i < n; i++) {
			double x = xRad * Math.cos(angle);
			double y = yRad * Math.sin(angle);
			
			xs[i] = transformXToMap((int)x);
			ys[i] = transformYToMap((int)y);
			
			angle += (2 * Math.PI) / n;
		}
		
		for (int i = 0; i < n; i++) {
			System.out.print(xs[i] + " ");
		}
		System.out.println();
		for (int i = 0; i < n; i++) {
			System.out.print(ys[i] + " ");
		}
		System.out.println();
		
		for (int i = 0; i < m; i++) {
			line(xs[us[i]], ys[us[i]], xs[vs[i]], ys[vs[i]]);
		}
		
		for (int i = 0; i < n; i++) {
			if (xs[i] > 0) set(xs[i]-1, ys[i],'<');
			set(xs[i], ys[i], (char)(i + '0'));
			if (xs[i] < W-1) set(xs[i]+1, ys[i],'>');
		}
	}
	
	@Override
	public void send() {
		String msg = "```md\n";
		for (int i = 0; i < H; i++) {
			msg += map[i];
		}
		msg += "```";
		c.sendMessage(msg).queue();
	}
	
	private int transformXToMap(int x) {
		return x + (int)(W / 2);
	}
	
	private int transformYToMap(int y) {
		return -y + (int)(H / 2);
	}
	
	private static final int nMax = 36;
	
	private String parse() {
		if (args.length < 3) return "Not enough arguments!";
		if (!args[1].matches("-?\\d+")) return "n (" + args[1] + ") must be an integer!";
		n = Integer.valueOf(args[1]);
		if (n < 0 || n > nMax) return "n (" + n + ") must be 0 <= n <= " + nMax + "!";
		
		if (!args[2].matches("-?\\d+")) return "m (" + args[2] + ") must be an integer!";
		m = Integer.valueOf(args[2]);
		if (m < 0 || m > (n * (n-1)) / 2) return "m (" + m + ") must be 0 <= m <= " + ((n * (n-1)) / 2) + "!";
		
		us = new int[m];
		vs = new int[m];
		
		if (args.length < 3 + 2*m) return "Not enough arguments!";
		for (int i = 0; i < m; i++) {
			int u = 3 + 2 * i;
			int v = 3 + 2 * i + 1;
			if (!args[u].matches("-?\\d+")) return "u (" + args[u] + ") must be an integer!";
			if (!args[v].matches("-?\\d+")) return "v (" + args[v] + ") must be an integer!";
			u = Integer.valueOf(args[u]);
			if (u < 0 || u >= n) return "u (" + u + ") must be 0 <= u < " + n + "!";
			v = Integer.valueOf(args[v]);
			if (v < 0 || v >= n) return "v (" + v + ") must be 0 <= v < " + v + "!";
			if (u == v) return "you clever guy are building an edge from a node to itself! (" + u + ")";
			
			us[i] = u;
			vs[i] = v;
		}
		
		return "OK";
	}
}
