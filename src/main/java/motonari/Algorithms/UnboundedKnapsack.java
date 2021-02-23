package motonari.Algorithms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import motonari.Commands.Command;
import motonari.Tomo.Helper;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UnboundedKnapsack extends Command {
	public UnboundedKnapsack(MessageReceivedEvent e, String[] args) {super(e, args);}
	public UnboundedKnapsack() {super();}
	public void init() {
		name = "Unbounded Knapsack Problem";
		cmd = "ukp";
		desc = "Computes the Optimum of a Unbounded Knapsack Problem.";
		
		arg_str = "W n (wi vi){n}";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "u", "unboundedknapsack", "unboundedknapsackproblem"
		}) );
		
		options = new HashMap<String, String>();
		options.put("table", "t");
		options.put("what", "w");
		options.put("reaction", "r");
	}
	private static int MAX = 100;
	private static int TABLE_W = 20;
	
	int W;
	int n;
	int[] w;
	int[] v;
	
	int V;
	int[][] dp;
	
	public void answer() {
		if (myOpts.contains("t"))
			table();
		
		if (myOpts.contains("w"))
			what();
		
		if (myOpts.contains("r"))
			Helper.reactNumber(c, e.getMessageId(), V);
		else
			sendAnswer();
	}
	
	public void main() {
		dp = new int[n+1][W+1];
		for (int i = 1; i <= n; i++) {
			for (int j = 0; j <= W; j++) {
				if (j - w[i-1] >= 0 && dp[i][j - w[i-1]] + v[i-1] > dp[i-1][j]) {
					dp[i][j] = dp[i][j - w[i-1]] + v[i-1];
				} else {
					dp[i][j] = dp[i-1][j];
				}
			}
		}
		V = dp[n][W];
	}
	
	public void sendAnswer() {
		c.sendMessage("The maximal value you can pack in your knapsack is **" + V + "**.").queue();
	}
	
	public void table() {
		int len = String.valueOf(Math.max(V, Math.max(n, W))).length();
		String format = "%1$"+(len+1)+"s";
		
		String t = "```md\n";
		for (int off = 0; off < W; off += TABLE_W) {
			t += "#";
			for (int s = 0; s < len; s++) t += " ";
			t += "|";
			for (int j = off; j <= W && j < off + TABLE_W; j++) t += String.format(format, j);
			t += "\n";
			for (int i = 0; i <= n; i++) {
				if (i == 0)
					for (int s = 0; s < len+1; s++) t += " ";
				else
					t += String.format(format, i-1);
				t += "|";
				for (int j = off; j <= W && j < off + TABLE_W; j++) {
					t += String.format(format, dp[i][j]);
					if (t.length() > Message.MAX_CONTENT_LENGTH) break;
				}
				if (t.length() > Message.MAX_CONTENT_LENGTH) break;
				t += "\n";
			}
			t += "\n";
		}
		t += "```";
		if (t.length() > Message.MAX_CONTENT_LENGTH)
			c.sendMessage("Couldn't print table, because of the " + Message.MAX_CONTENT_LENGTH + " char Message Limit.").queue();
		else
			c.sendMessage(t).queue();
	}
	
	private void what() {
		int i = n;
		int j = W;
		
		int count = 0;
		
		while (j > 0 && i > 0) {
			if (i > 0 && dp[i][j] == dp[i-1][j]) {
				i--;
			} else {
				count++;
				j -= w[i-1];
			}
		}
		
		i = n;
		j = W;
		
		int[] indx = new int[count];
		
		while (j > 0 && i > 0) {
			if (i > 0 && dp[i][j] == dp[i-1][j]) {
				i--;
			} else {
				indx[--count] = i-1;
				j -= w[i-1];
			}
		}
		

		int k = 0;
		int totalW = 0;
		String msg = "Take ";
		boolean first = true;
		while (k < indx.length) {
			//System.out.println(k);
			int ind = indx[k];
			int num = 0;
			while (k < indx.length) {
				if (ind == indx[k]) {
					num++;
					k++;
				} else break;
			}
			//System.out.println(k);
			if (!first) {
				if (k != indx.length) msg += ", ";
				else msg += " and ";
			}
			msg += num + " piece";
			if (num != 1) msg += "s";
			msg += " of item " + ind + " (" + w[ind] + "kg; " + v[ind] + ".- chf)";
			
			totalW += num * w[ind];
			
			first = false;
		}
		msg += ".\nThat's " + indx.length + " item";
		if (indx.length != 1) msg += "s";
		msg += " for a total weight of " + totalW + "kg.";
		
		
		c.sendMessage(msg).queue();
	}
	
	public String parse() {
		if (args.length < 3) return "Not enough arguments!";
		if (!args[1].matches("-?\\d+")) return "W (" + args[1] + ") must be an integer!";
		W = Integer.valueOf(args[1]);
		if (W <= 0) return "W (" + W + ") must be positive!";
		if (!args[2].matches("-?\\d+")) return "n (" + args[2] + ") must be an integer!";
		n = Integer.valueOf(args[2]);
		if (n <= 0) return "n (" + n + ") must be positive!";
		if (n > MAX) return "n (" + n + ") exceeds max of " + MAX + "!";
		if (args.length < 3 + 2 * n) return "Not enough arguments! I need n weights and values each.";
		w = new int[n];
		v = new int[n];
		
		for (int i = 0; i < n; i++) {
			String wStr = args[3 + 2*i];
			if (!wStr.matches("-?\\d+")) return "w[" + i + "] (" + wStr + ") must be an integer!";
			w[i] = Integer.valueOf(wStr);
			if (w[i] <= 0) return "w[" + i + "] (" + wStr + ") must be positive!";
			String vStr = args[3 + 2*i + 1];
			if (!vStr.matches("-?\\d+")) return "v[" + i + "] (" + vStr + ") must be an integer!";
			v[i] = Integer.valueOf(vStr);
			if (v[i] <= 0) return "v[" + i + "] (" + vStr + ") must be positive!";
		}
		
		int start = 3 + 2 * n;
		String err = Helper.checkOptions(args, start, options);
		if (!err.equals("OK")) return err;
		
		myOpts = Helper.options(args, start, options);
		
		if (myOpts.contains("r") && myOpts.size() > 1) {
			return "Option `reaction` is incompatible with other options!";
		}
		
		return "OK";
	}
	
	public String example(String cmd) {
		final int Wi_LOWER_LIM = 1;
		final int Wi_UPPER_LIM = 10;
		final int Vi_LOWER_LIM = 3;
		final int Vi_UPPER_LIM = 30;
		final int N_LOWER_LIM = 2;
		final int N_UPPER_LIM = 8;
		final int W_LOWER_LIM = 1;
		final int W_UPPER_LIM = 60;
		
		String argStr = cmd;
		Random rand = new Random();
		
		int n = rand.nextInt(N_UPPER_LIM - N_LOWER_LIM) + N_LOWER_LIM;
		int w = rand.nextInt(W_UPPER_LIM - W_LOWER_LIM) + W_LOWER_LIM;
		
		argStr += " " + w + " " + n;
		
		for (int i = 0; i < n; i++) {
			int wi = rand.nextInt(Wi_UPPER_LIM - Wi_LOWER_LIM) + Wi_LOWER_LIM;
			int vi = rand.nextInt(Vi_UPPER_LIM - Vi_LOWER_LIM) + Vi_LOWER_LIM;
			argStr += " " + wi + " " + vi;
		}
		
		if (rand.nextDouble() < 0.3) {
			if (rand.nextDouble() < 0.5) {
				argStr += " --reaction";
			} else {
				argStr += " -r";
			}
		} else {
			if (rand.nextDouble() < 0.7) {
				if (rand.nextDouble() < 0.5) {
					argStr += " --table";
				} else {
					argStr += " -t";
				}
			}
			if (rand.nextDouble() < 0.7) {
				if (rand.nextDouble() < 0.5) {
					argStr += " --what";
				} else {
					argStr += " -w";
				}	
			}
		}
		
		
		return argStr;
	}
}
