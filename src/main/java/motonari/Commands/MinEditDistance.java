package motonari.Commands;

import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MinEditDistance {
	private static int maxLen = 100;
	
	public static List<String> aliases = Arrays.asList(new String[] {"med", "m"});
	public static List<String> options = Arrays.asList(new String[] {"table", "how", "code"});
	
	public static String desc = "Minimum Edit Distance";
	public static String long_desc = "Computes the minimum edit distance between two variable-like strings.";
	
	public static void run(MessageReceivedEvent e, String[] args) {
		MessageChannel c = e.getChannel();
		if (args.length < 3) {
			System.out.println("1");
			help(c, args);
			return;
		}
		String A = args[1];
		String B = args[2];
		if (!B.matches("\\w+") || !A.matches("\\w+")) {
			System.out.println("2");
			help(c, args);
			return;
		}
		B = B.toLowerCase();
		A = A.toLowerCase();
		
		if (B.length() > maxLen || A.length() > maxLen) {
			System.out.println("3");
			help(c, args);
			return;
		}
		boolean table = false;
		for (int i = 3; i < args.length; i++) {
			System.out.println(args[i] + " " + !args[i].startsWith("--") +  " " + (args[i].length() < 2) + " " + !options.contains(args[i].substring(2)) + " " + args[i].substring(2) + " [" + String.join(" ", options) + "]");
			if (!args[i].startsWith("--") || args[i].length() < 2 || !options.contains(args[i].substring(2))) {
				help(c, args);
				return;
			}
			if (args[i].equals("--table")) table = true;
		}
		main(c, A, B, table);
	}
	
	private static void main(MessageChannel c, String A, String B, boolean table) {
		int[][] dp = new int[B.length() + 1][A.length() + 1];
		// BASECASES
		for (int i = 0; i <= B.length(); i++) dp[i][0] = i;
		for (int j = 0; j <= A.length(); j++) dp[0][j] = j;
		// BOTTOM UP
		for (int i = 1; i <= B.length(); i++) {
			for (int j = 1; j <= A.length(); j++) {
				int topleft = dp[i-1][j-1] + (B.charAt(i-1) == A.charAt(j-1) ? 0 : 1);
				int top = dp[i-1][j] + 1;
				int left = dp[i][j-1] + 1;
				int min = Math.min(topleft, Math.min(top, left));
				dp[i][j] = min;
			}
		}
		String tableOut = "";
		if (table) tableOut = printTable(dp, A, B, true);
		String rest = "Minimum Edit Distance between `" + A + "` and `" + B + "` is " + dp[B.length()][A.length()];
		System.out.println(tableOut.length());
		if ((tableOut + rest).length() > 2000) tableOut = printTable(dp, A, B, false);
		String msg = (tableOut + rest).length() > 2000 ? rest : tableOut + rest;
		System.out.println(tableOut.length());
		c.sendMessage(msg).queue();

	}
	
	private static String printTable(int[][] dp, String A, String B, boolean pretty) {
		String s = pretty ? "" : "```\n";
		for (int j = 0; j < A.length() + 3; j++) {
			if (j == 0 || j == 2) s += pretty ? ":black_large_square:" : "-";
			else if (j == 1) s += pretty ? ":orange_square:" : " ";
			else s += pretty ? toEmote(A.charAt(j-3)) : A.substring(j-3, j-2).toUpperCase();
		}
		s += "\n";
		for (int j = 0; j < A.length() + 3; j++) s += pretty ? ":orange_square:" : " ";
		s += "\n";
		for (int i = 0; i <= B.length(); i++) {
			if (i == 0) s += pretty ? ":black_large_square:" : "-";
			else s += pretty ? toEmote(B.charAt(i-1)) : B.substring(i-1, i).toUpperCase();
			s += pretty ? ":orange_square:" : " ";
			for (int j = 0; j <= A.length(); j++) {
				if (dp[i][j] < 10) {
					s += pretty ? toEmote((char)('0' + dp[i][j])) : String.valueOf(dp[i][j]);
				} else {
					s += pretty ? toEmote((char)('a' + dp[i][j] - 10)) : (char)('A' + dp[i][j] - 10);
				}
			}
			s += "\n";
		}
		return pretty ? s : s + "```\n";
	}
	
	private static String toEmote(char c) {
		if ('a' <= c && c <= 'z') {
			return ":regional_indicator_" + c + ":";
		} else if (c == '0') {
			return ":zero:";
		} else if (c == '1') {
			return ":one:";
		} else if (c == '2') {
			return ":two:";
		} else if (c == '3') {
			return ":three:";
		} else if (c == '4') {
			return ":four:";
		} else if (c == '5') {
			return ":five:";
		} else if (c == '6') {
			return ":six:";
		} else if (c == '7') {
			return ":seven:";
		} else if (c == '8') {
			return ":eight:";
		} else if (c == '9') {
			return ":nine:";
		} else if (c == '_') {
			return ":stop_button:";
		}
		
		return null;
	}

	public static void help(MessageChannel c, String[] args) {
		Helper.commandHelp(c, "med", long_desc, aliases, options);
	}

}
