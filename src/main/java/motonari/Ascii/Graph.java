package motonari.Ascii;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Graph extends Canvas {
	public Graph(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Graph() { super(); }
	
	public void init() {
		name = "ascii Graph";
		cmd = "graph";
		desc = "Draws ascii graph.";
		
		arg_str = "n m (u v){m}";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "asciigraph", "drawgraph"
		}) );
		
		options = new HashMap<String, String>();
	}
	
	int n;
	int m;
	int[] us;
	int[] vs;
	
	public void main() {
		super.main();
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
		
		for (int i = 0; i < m; i++) {
			line(xs[us[i]], ys[us[i]], xs[vs[i]], ys[vs[i]]);
		}
		
		for (int i = 0; i < n; i++) {
			if (xs[i] > 0) set(xs[i]-1, ys[i],'<');
			set(xs[i], ys[i], (char)(i + '0'));
			if (xs[i] < W-1) set(xs[i]+1, ys[i],'>');
		}
	}
	
	private int transformXToMap(int x) {
		return x + (int)(W / 2);
	}
	
	private int transformYToMap(int y) {
		return -y + (int)(H / 2);
	}
	
	private static final int nMax = 36;
	
	public String parse() {
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

	public String example(String alias) {
		String argStr = alias;
		Random rand = new Random();
		
		final int N_LOWER_LIM = 2;
		final int N_UPPER_LIM = 24;
		
		int n = rand.nextInt(N_UPPER_LIM - N_LOWER_LIM) + N_LOWER_LIM;
		
		final int M_UPPER_LIM = n * (n - 1) / 2;
		
		int m = rand.nextInt(M_UPPER_LIM);
		
		argStr += " " + n + " " + m;
		
		HashSet<Integer> used = new HashSet<Integer>();
		
		for (int i = 0; i < m; i++) {
			int comb = 0;
			int u = 0;
			int v = 0;
			while (comb == 0 || used.contains(comb)) {
				u = rand.nextInt(n);
				v = u;
				while (v == u) {
					v = rand.nextInt(n);
				}
				if (v < u) {
					int tmp = u;
					u = v;
					v = tmp;
				}
				comb = m * u + v;
			}
			used.add(comb);
			
			argStr += " " + u + " " + v;
		}
		
		return argStr;
	}
}
