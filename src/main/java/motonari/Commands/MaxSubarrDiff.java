package motonari.Commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MaxSubarrDiff {
	private static final String NAME = "Maximum Subarray Difference";
	private static final String MAIN_CMD = "msd";
	private static final String DESC = "Computes the maximum subarray difference of some array.";
	
	private static final String ARGSTR = "arr_elem{2,}";
	private static final HashSet<String> ALIASES = new HashSet<String>( Arrays.asList(new String[] {
			MAIN_CMD, "maxsubarrdiff"
	}));
	
	private static final HashMap<String, String> OPTIONS = new HashMap<String, String>();
	static {
		OPTIONS.put("show", "s");
		//OPTIONS.put("emotes", "e");
		//OPTIONS.put("path", "p");
		OPTIONS.put("reaction", "r");
	}
	
	private static int MAXLEN = 100;
	
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
	
	String[] args;
	int[] arr;
	int[] add;
	int[] sub;
	int max;
	HashSet<String> myOpts;
	MessageReceivedEvent e;
	MessageChannel c;

	MaxSubarrDiff(MessageReceivedEvent e, String[] args) {
		this.args = args;
		this.e = e;
		this.c = e.getChannel();
	}
	
	public void run() {
		String err = parse();
		if (!err.equals("OK")) {
			Helper.error(c, args[0], err);
			return;
		};
		
		main();
		
		if (myOpts.contains("s"))
			show(add, sub, max);
		else if (myOpts.contains("r"))
			Helper.reactNumber(c, e.getMessageId(), max);
		else
			sendAnswer(max);
			
	}
	
	private void sendAnswer(int answer) {
		c.sendMessage("Maximum Subarray Difference of `" + printArr(arr) + "` is **" + answer + "**.").queue();
	}
	
	public static String printArr(int[] arr) {
		String s = "[";
		for (int i = 0; i < arr.length - 1; i++) {
			s += arr[i] + " ";
		}
		s += arr[arr.length - 1] + "]";
		return s;
	}
	
	private void show(int[] add, int[] sub, int answer) {
		c.sendMessage("Maximum Subarray Difference of `" + printArr(arr) + 
				"` is \n`sum(" + printArr(add) + ") - sum(" + printArr(sub) + ")`, or **" + answer + "**.").queue();
	}
	
	private void main() {
		int[] maxSubArr = maxSubArr();
		int[] minSubArr = minSubArr();
		int[] maxSubArrLen = maxSubArrLen(maxSubArr);
		int[] minSubArrLen = minSubArrLen(minSubArr);
		
		max = Integer.MIN_VALUE;
		add = null;
		sub = null;
		for (int i = 0; i < arr.length - 1; i++) {
			if (maxSubArr[i] - minSubArr[i+1] > max) {
				max = maxSubArr[i] - minSubArr[i+1];
				add = new int[maxSubArrLen[i]];
				for (int j = 0; j < add.length; j++) {
					add[j] = arr[i - (add.length - 1) + j];
				}
				
				sub = new int[minSubArrLen[i+1]];
				for (int j = 0; j < sub.length; j++) {
					sub[j] = arr[i + 1 + j];
				}
			}
		}

	}
	
	private int[] maxSubArr() {
		int[] res = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			if (i > 0 && res[i-1] > 0) {
				res[i] = res[i-1] + arr[i];
			} else {
				res[i] = arr[i];
			}
		}
		return res;
	}
	
	private int[] maxSubArrLen(int[] maxSubArr) {
		int[] res = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			if (i > 0 && maxSubArr[i] == maxSubArr[i-1] + arr[i]) {
				res[i] = res[i-1] + 1;
			} else {
				res[i] = 1;
			}
		}
		return res;
	}
	
	private int[] minSubArr() {
		int[] res = new int[arr.length];
		for (int i = arr.length - 1; i >= 0; i--) {
			if (i < arr.length - 1 && res[i+1] < 0) {
				res[i] = res[i+1] + arr[i];
			} else {
				res[i] = arr[i];
			}
		}
		return res;
	}
	
	private int[] minSubArrLen(int[] minSubArr) {
		int[] res = new int[arr.length];
		for (int i = arr.length - 1; i >= 0; i--) {
			if (i < arr.length - 1 && minSubArr[i] == minSubArr[i+1] + arr[i]) {
				res[i] = res[i+1] + 1;
			} else {
				res[i] = 1;
			}
		}
		return res;
	}
	
	private String parse() {
		if (args.length < 3) {
			return "Not enough array elements!";
		}
		int l = 0;
		for (int i = 1; i < args.length; i++) {
			if (args[i].matches("-?\\d+")) l++;
			else break;
		}
		if (l < 2) {
			return "Not enough array elements!";
		} else if (l > MAXLEN) {
			return "Array length (" + l + ") exceeds max length of " + MAXLEN + "!";
		}
		arr = new int[l];
		for (int i = 0; i < l; i++) {
			arr[i] = Integer.valueOf(args[i+1]);
		}
		
		
		String err = Helper.checkOptions(args, l+1, OPTIONS);
		if (!err.equals("OK")) {
			return err;
		}
		
		myOpts = Helper.options(args, l+1, OPTIONS);
		
		if (myOpts.contains("r") && myOpts.size() > 1) {
			return "Option `reaction` is incompatible with other options!";
		}
		
		return "OK";
	}
	
	public static MaxSubarrDiff random(MessageReceivedEvent e, String cmd) {
		String argStr = cmd;
		Random rand = new Random();
		int lim = 0;
		while (lim == 0) lim = (int)(rand.nextGaussian() * 1000);
		if (lim < 0) lim = -lim;
		int n = rand.nextInt(15) + 2;
		for (int i = 0; i < n; i++) {
			int elem = rand.nextInt(2*lim) - lim;
			argStr += " " + elem;
		}
		
		if (rand.nextDouble() < 0.3) {
			if (rand.nextDouble() < 0.5) {
				argStr += " --reaction";
			} else {
				argStr += " -r";
			}
		} else if (rand.nextDouble() < 0.5) {
			if (rand.nextDouble() < 0.5) {
				argStr += " --show";
			} else {
				argStr += " -s";
			}
		}
		
		e.getChannel().sendMessage("Example usage of " + cmd + ": `" + Tomo.prefix + argStr + "`").queue();
		
		return new MaxSubarrDiff(e, argStr.split(" "));
	}
}
