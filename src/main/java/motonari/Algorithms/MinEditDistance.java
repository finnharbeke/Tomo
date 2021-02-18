package motonari.Algorithms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import motonari.Commands.Command;
import motonari.Tomo.Helper;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MinEditDistance extends Command {
	public MinEditDistance(MessageReceivedEvent e, String[] args) {super(e, args);}
	public MinEditDistance() {super();}
	
	public void init() {
		name = "Minimal Edit Distance";
		cmd = "med";
		desc = "Computes the minimal edit distance between two variable-like Strings.";
		
		
		arg_str = "<str1> <str2>";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "m", "mineditdistance"
		}) );
		
		options = new HashMap<String, String>();
		options.put("table", "t");
		options.put("emotes", "e");
		options.put("path", "p");
		options.put("reaction", "r");
	}
	
	private static int MAXLEN = 100;
	
	String str1;
	String str2;
	int[][] dp;
	
	public void answer() {
		if (myOpts.contains("t")) {
			if (myOpts.contains("e"))
				emoteTable();
			else
				table();
		}
		if (myOpts.contains("p")) path();
		
		if (myOpts.contains("r"))
			Helper.reactNumber(c, e.getMessageId(), dp[dp.length -1 ][dp[0].length - 1]);
		else
			sendAnswer();
	}

	public void main() {
		dp = new int[str2.length() + 1][str1.length() + 1];
		// BASECASES
		for (int i = 0; i <= str2.length(); i++) dp[i][0] = i;
		for (int j = 0; j <= str1.length(); j++) dp[0][j] = j;
		// BOTTOM UP
		for (int i = 1; i <= str2.length(); i++) {
			for (int j = 1; j <= str1.length(); j++) {
				int topleft = dp[i-1][j-1] + (str2.charAt(i-1) == str1.charAt(j-1) ? 0 : 1);
				int top = dp[i-1][j] + 1;
				int left = dp[i][j-1] + 1;
				int min = Math.min(topleft, Math.min(top, left));
				dp[i][j] = min;
			}
		}
	}

	public String parse() {
		if (args.length < 3) {
			return "Not enough arguments!";
		}
		str1 = args[1];
		str2 = args[2];
		if (!str1.matches("\\w+") || !str2.matches("\\w+")) {
			return "`str1` and `str2` must match the java regex `\\w+`!";
		}
		str1 = str1.toUpperCase();
		str2 = str2.toUpperCase();
		
		if (str1.length() > MAXLEN || str2.length() > MAXLEN) {
			return "`str1` and `str2` must be max " + MAXLEN + " characters long!";
		}
		
		String err = Helper.checkOptions(args, 3, options);
		if (!err.equals("OK")) {
			return err;
		}
		
		myOpts = Helper.options(args, 3, options);
		
		if (myOpts.contains("e") && !myOpts.contains("t")) {
			return "Option `emotes` requires option `table`!";
		}
		
		if (myOpts.contains("r") && myOpts.size() > 1) {
			return "Option `reaction` is incompatible with other options!";
		}
		
		return "OK";
	}

	private void sendAnswer() {
		String msg = "Minimal Edit Distance between `" + str1 + "` and `" + str2 + "` is **" + dp[dp.length - 1][dp[0].length - 1] + "**.";
		c.sendMessage(msg).queue();
	}

	private void path() {
		String[] ops = new String[dp[dp.length-1][dp[0].length-1]];
		
		int y = dp.length-1;
		int x = dp[0].length-1;
		
		for (int i = 0; i < ops.length; i++) {
			while (y > 0 && x > 0 && dp[y-1][x-1] == dp[y][x] && str1.charAt(x-1) == str2.charAt(y-1)) {
				y--; x--;
			}
			if (dp[y-1][x] + 1 == dp[y][x]) {
				ops[ops.length - 1 - i] = "+" + str2.charAt(y-1) + (y-1);
				y--;
			} else if (dp[y][x-1] + 1 == dp[y][x]) {
				ops[ops.length - 1 - i] = "-" + str1.charAt(x-1) + (y);
				x--;
			} else if (dp[y-1][x-1] + 1 == dp[y][x]) {
				ops[ops.length - 1 - i] = "/" + str1.charAt(x-1) + str2.charAt(y-1) + (y-1);
				y--;
				x--;
			} else {
				ops[ops.length - 1 - i] = "\"This shouldn't exist, maybe contact the creator.\"";
			}
		}
		
		String current = str1;
		String[] states = new String[ops.length + 1];
		//lines[0] = "Start: \"" + current + "\"";
		states[0] = "`" + current + "`";
		for (int i = 1; i <= ops.length; i++) {
			String op = ops[i-1];
			//System.out.println(current + "; " + op);
			if (op.charAt(0) == '+') {
				char ch = op.charAt(1);
				int ind = Integer.valueOf(op.substring(2));
				if (ind == current.length())
					current = current + ch;
				else
					current = current.substring(0, ind) + ch + current.substring(ind);
				states[i] = "`" + current + "`";
				//lines[i] = "Insert '" + ch + "' at position " + ind + ": \"" + current + "\"";
			} else if (op.charAt(0) == '-') {
				int ind = Integer.valueOf(op.substring(2));
				if (ind == current.length()-1)
					current = current.substring(0, ind);
				else
					current = current.substring(0, ind) + current.substring(ind + 1);
				states[i] = "`" + current + "`";
				//lines[i] = "Remove '" + ch + "' at position " + ind + ": \"" + current + "\"";
			} else if (op.charAt(0) == '/') {
				char newch = op.charAt(2);
				int ind = Integer.valueOf(op.substring(3));
				current = current.substring(0, ind) + newch + current.substring(ind + 1);
				states[i] = "`" + current + "`";
				//lines[i] = "Change '" + oldch + "' to '" + newch + "' at position " + ind + ": \"" + current + "\"";
			} else {
				states[i] = "`" + op + "`";
			}
		}
		
		c.sendMessage("Path: " + String.join(", ", states)).queue();
		
	}

	private boolean table() {
		String s = "```\n";
		for (int j = 0; j < str1.length() + 3; j++) {
			if (j == 0 || j == 2) s += "-";
			else if (j == 1) s += " ";
			else s += str1.substring(j-3, j-2);
		}
		s += "\n";
		for (int j = 0; j < str1.length() + 3; j++) s += " ";
		s += "\n";
		for (int i = 0; i <= str2.length(); i++) {
			if (i == 0) s += "-";
			else s += str2.substring(i-1, i);
			s += " ";
			for (int j = 0; j <= str1.length(); j++) {
				if (dp[i][j] < 10) {
					s += String.valueOf(dp[i][j]);
				} else {
					s += (char)('A' + dp[i][j] - 10);
				}
			}
			s += "\n";
		}
		s += "```\n";
		if (s.length() > Tomo.msgLim) {
			c.sendMessage("Can't print table because of the message size limit.").queue();
			return false;
		}
		else {
			c.sendMessage(s).queue();
			return true;
		}
		
	}

	private void emoteTable() {
		String s = "";
		for (int j = 0; j < str1.length() + 3; j++) {
			if (j == 0 || j == 2) s += ":black_large_square:";
			else if (j == 1) s += ":orange_square:";
			else s += toEmote(str1.toLowerCase().charAt(j-3));
		}
		s += "\n";
		for (int j = 0; j < str1.length() + 3; j++) s += ":orange_square:";
		s += "\n";
		for (int i = 0; i <= str2.length(); i++) {
			if (i == 0) s += ":black_large_square:";
			else s += toEmote(str2.toLowerCase().charAt(i-1));
			s += ":orange_square:";
			for (int j = 0; j <= str1.length(); j++) {
				if (dp[i][j] < 10) {
					s += toEmote((char)('0' + dp[i][j]));
				} else {
					s += toEmote((char)('a' + dp[i][j] - 10));
				}
			}
			s += "\n";
		}
		s = s.replace("::", "::");
		if (s.length() > 2000) {
			if (table()) {			
				c.sendMessage("Couldn't print emote table because of the message size limit.").queue();
			}
		} else {
			c.sendMessage(s).queue();
		}
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

	public String example(String alias) {
		String[] strs = new String[] {
			"ueli", "eth_dinfk_2020", "olga", "steurer", "pueschel",
			"janosch", "ana", "thomas", "best_discord", "another_example", "does_this_help",
			"advent_of_code", "karatsuba", "bonuspoints", "bp_passed", "bp_failed", 
			"bob_marley", "nasir_jones", "christopher_wallace", "phenomden"
		};
		String argStr = alias;
		Random rand = new Random();
		String str1 = strs[rand.nextInt(strs.length)];
		String str2 = strs[rand.nextInt(strs.length)];
		argStr += " " + str1 + " " + str2;
		
		if (rand.nextDouble() < 0.3) {
			if (rand.nextDouble() < 0.5) {
				argStr += " --reaction";
			} else {
				argStr += " -r";
			}
		} else {
			if (rand.nextDouble() < 0.6) {
				if (rand.nextDouble() < 0.5) {
					argStr += " --table";
				} else {
					argStr += " -t";
				}
				
				if (rand.nextDouble() < 0.6) {
					if (rand.nextDouble() < 0.5) {
						argStr += " --emotes";
					} else {
						argStr += " -e";
					}
				}
			}
			if (rand.nextDouble() < 0.3) {
				if (rand.nextDouble() < 0.5) {
					argStr += " --path";
				} else {
					argStr += " -p";
				}
			}
		}
		
		return argStr;
	}
}
