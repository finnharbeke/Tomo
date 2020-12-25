package motonari.Commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ZeroOneKnapsack extends UnboundedKnapsack {
	private static final String NAME = "0-1 Knapsack Problem";
	private static final String MAIN_CMD = "kp";
	private static final String DESC = "Computes the Optimum of a 0-1 Knapsack Problem.";
	
	private static final String ARGSTR = "W n (wi vi){n}";
	private static final HashSet<String> ALIASES = new HashSet<String>( Arrays.asList(new String[] {
			MAIN_CMD, "01", "01kp", "01knapsack", "01knapsackproblem"
	}) );
	
	private static final HashMap<String, String> OPTIONS = new HashMap<String, String>();
	static {
		OPTIONS.put("table", "t");
		OPTIONS.put("what", "w");
		OPTIONS.put("reaction", "r");
	}
	
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
	
	ZeroOneKnapsack(MessageReceivedEvent e, String[] args) {
		super(e, args);
	}
	
	public void run() {
		String err = super.parse();
		if (!err.equals("OK")) {
			Helper.error(c, args[0], err);
			return;
		};
		
		main();
		
		if (myOpts.contains("t"))
			super.table();
		
		if (myOpts.contains("w"))
			what();
		
		if (myOpts.contains("r"))
			Helper.reactNumber(c, e.getMessageId(), V);
		else
			super.sendAnswer();
			
	}
	
	private void main() {
		dp = new int[n+1][W+1];
		for (int i = 1; i <= n; i++) {
			for (int j = 0; j <= W; j++) {
				if (j - w[i-1] >= 0 && dp[i-1][j - w[i-1]] + v[i-1] > dp[i-1][j]) {
					dp[i][j] = dp[i-1][j - w[i-1]] + v[i-1];
				} else {
					dp[i][j] = dp[i-1][j];
				}
			}
		}
		V = dp[n][W];
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
				i--;
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
				i--;
			}
		}
		
		int k = 0;
		int totalW = 0;
		String msg = "Take";
		boolean first = true;
		while (k < indx.length) {
			int ind = indx[k++];
			if (!first) {
				if (k != indx.length) msg += ",";
				else msg += " and";
			}
			msg += " item " + ind + " (" + w[ind] + "kg; " + v[ind] + ".- chf)";
			
			totalW += w[ind];
			
			first = false;
		}
		msg += ".\nThat's " + indx.length + " item";
		if (indx.length != 1) msg += "s";
		msg += " for a total weight of " + totalW + "kg.";
		
		
		c.sendMessage(msg).queue();
	}
}
