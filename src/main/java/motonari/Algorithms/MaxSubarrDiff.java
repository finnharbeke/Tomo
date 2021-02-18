package motonari.Algorithms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import motonari.Commands.Command;
import motonari.Tomo.Helper;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MaxSubarrDiff extends Command {
	public MaxSubarrDiff(MessageReceivedEvent e, String[] args) {super(e, args);}
	public MaxSubarrDiff() {super();}
	public void init() {
		name = "Maximum Subarray Difference";
		cmd = "msd";
		desc = "Computes the maximum subarray difference of some array.";
		
		arg_str = "arr_elem{2,}";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "maxsubarrdiff"
		}));
		
		options = new HashMap<String, String>();
		options.put("show", "s");
		options.put("reaction", "r");
	
	}
	private static int MAXLEN = 100;
	
	int[] arr;
	int[] add;
	int[] sub;
	int max;
	
	public void answer() {
		if (myOpts.contains("s"))
			show(add, sub, max);
		else if (myOpts.contains("r"))
			Helper.reactNumber(c, e.getMessageId(), max);
		else
			sendAnswer();
	}
	
	private void sendAnswer() {
		c.sendMessage("Maximum Subarray Difference of `" + printArr(arr) + "` is **" + max + "**.").queue();
	}
	
	private static String printArr(int[] arr) {
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
	
	public void main() {
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
	
	public String parse() {
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
		
		
		String err = Helper.checkOptions(args, l+1, options);
		if (!err.equals("OK")) {
			return err;
		}
		
		myOpts = Helper.options(args, l+1, options);
		
		if (myOpts.contains("r") && myOpts.size() > 1) {
			return "Option `reaction` is incompatible with other options!";
		}
		
		return "OK";
	}
	
	public String example(String alias) {
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
		
		return argStr;
	}
}
