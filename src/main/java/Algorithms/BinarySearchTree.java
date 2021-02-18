package Algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import motonari.Tomo.Helper;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class BinarySearchTree extends BinaryTree {
	public BinarySearchTree(MessageReceivedEvent e, String[] args) {super(e, args);}
	public BinarySearchTree() {super();}
	
	public void init() {
		name = "Binary Search Tree";
		cmd = "bst";
		desc = "Draws ascii BST after row of commands.";
		
		arg_str = "(i val | d val | q val ){1, }";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "bstree", "binarysearchtree"
		}));
		
		options = new HashMap<String, String>();
		options.put("log", "l");
	}
	
	ArrayList<Character> acts;
	ArrayList<Integer> vals;
	ArrayList<String> log;
	ArrayList<String> full_log;
	
	public void main() {
		super.main();

		log = new ArrayList<String>();
		full_log = new ArrayList<String>();
		
		for (int i = 0; i < acts.size(); i++) {
			char act = acts.get(i);
			int val = vals.get(i);
			if (act == 'i') {
				if (origin == null) {
					origin = new Node(this.getId(), val);
				} else {
					insert(origin, val);
				}
				full_log.add("insert " + val);
			} else if (act == 'd') {
				full_log.add("delete " + (String.valueOf(val).length() == 1 ? " " : "") + val + ": ");
				Integer id = del(origin, null, val);
				if (id == null) {
					full_log.set(i, full_log.get(i) + " no node with value " + val + " found");
					log.add(full_log.get(i));
				} else {
					full_log.set(i, full_log.get(i) + " deleted node " + id);
					updateHeights(origin);
				}
			} else if (act == 'q') {
				full_log.add("query " + (String.valueOf(val).length() == 1 ? " " : "") + val);
				Node node = query(origin, val);
				if (node != null && node.getVal() == val) {
					full_log.set(i, full_log.get(i) + ": id = " + node.getId());
				} else {
					full_log.set(i, full_log.get(i) + ": no node with value " + val + " found");
				}
				log.add(full_log.get(i));
			} else {
				throw new IllegalArgumentException("Invalid Command in acts: '" + act + "'!");
			}
		}
		
		draw();
	}
	
	public void answer() {
		String msg = "";
		if (myOpts.contains("l") && full_log.size() > 0)
			msg = "**Log:**\n```\n" + String.join("\n", full_log) + "```";
		else if (log.size() > 0)
			msg = "**Log:**\n```\n" + String.join("\n", log) + "```";
		
		if (!msg.equals("")) {
			if (msg.length() < Tomo.msgLim)
				c.sendMessage(msg).queue();
			else
				c.sendMessage("Couldn't print Log, because of the " + Tomo.msgLim + " char Message Limit.");
		}
		
		super.answer();
	}
	
	private int insert(Node node, Integer value) {
		if (value < node.getVal()) {
			if (node.left() != null) {
				int h = insert(node.left(), value);
				if (h == node.getH()) {
					node.incH();
				}
			} else {
				node.setLeft(new Node(getId(), value));
				if (node.getH() == 1) {
					node.incH();
				}
			}
		} else {
			if (node.right() != null) {
				int h = insert(node.right(), value);
				if (h == node.getH()) {
					node.incH();
				}
			} else {
				node.setRight(new Node(getId(), value));
				if (node.getH() == 1) {
					node.incH();
				}
			}
		}
		return node.getH();
	}
	
	private Integer del(Node node, Node parent, Integer value) {
		if (value < node.getVal()) {
			if (node.left() != null)
				return del(node.left(), node, value);
			else
				return null;
		} else {
			if (value > node.getVal()) {
				if (node.right() != null)
					return del(node.right(), node, value);
				else
					return null;
			} else {
				if (node.right() != null && node.right().getVal() == value)
					return del(node.right(), node, value);
				
				if (node.left() == null && node.right() == null) {
					// no child
					if (parent == null) {
						origin = null;
					} else if (parent.left() == node) {
						parent.setLeft(null);
					} else {
						parent.setRight(null);
					}
				} else if (node.left() == null || node.right() == null) {
					// one child
					Node child;
					if (node.left() != null) child = node.left();
					else child = node.right();
					
					if (parent == null) {
						origin = child;
					} else if (parent.left() == node) {
						parent.setLeft(child);
					} else {
						parent.setRight(child);
					}
				} else {
					// two childs
					Node replace = pre_and_del(node);
					if (replace == null) throw new IllegalArgumentException("predecessor is null, impossible!");
					
					replace.setLeft(node.left());
					replace.setRight(node.right());
					
					if (parent == null) {
						origin = replace;
					} else if (parent.left() == node) {
						parent.setLeft(replace);
					} else {
						parent.setRight(replace);
					}
				}
				return node.getId();
			}
		}
	}
	
	private Node pre_and_del(Node node) {
		if (node.left() == null)
			return null;
		Node parent = node;
		node = node.left();
		while (node.right() != null) {
			parent = node;
			node = node.right();
		}
		if (parent.left() == node) {
			if (node.left() != null)
				parent.setLeft(node.left());
			else
				parent.setLeft(null);
		} else {
			if (node.left() != null)
				parent.setRight(node.left());
			else
				parent.setRight(null);
		}
		return node;
	}
	
	void updateHeights(Node node) {
		if (node == null) return;
		if (node.left() != null) {
			updateHeights(node.left());
		}
		if (node.right() != null) {
			updateHeights(node.right());
		}
		int ch = 0;
		if (node.left() != null) ch = node.left().getH();
		if (node.right() != null && node.right().getH() > ch) ch = node.right().getH();
		node.setH(ch+1);
	}
	
	private Node query(Node node, int value) {
		if (node == null) return null;
		if (value < node.getVal()) {
			if (node.left() != null) {
				return query(node.left(), value);
			} else 
				return node;
		} else if (value > node.getVal()) {
			if (node.right() != null) {
				return query(node.right(), value);
			} else 
				return node;
		} else {
			return node;
		}
	}
	
	public String parse() {
		acts = new ArrayList<Character>();
		vals = new ArrayList<Integer>();
		
		int i = 1;
		
		while (i < args.length) {
			if (args[i].startsWith("-")) break;
			if (args.length < i + 2) return "Not enough arguments!";
			if (args[i].equals("i")) {
				acts.add('i');
			} else if (args[i].equals("d")) {
				acts.add('d');
			} else if (args[i].equals("q")) {
				acts.add('q');
			} else {
				return "Invalid cmd: \"" + args[i] + "\"!";
			}
			i++;
			if (!args[i].matches("-?\\d+")) return "val (" + args[i] + ") must be an integer!";
			int val = Integer.valueOf(args[i]);
			if (val < 0 || val >= max_Val) return "val (" + val + ") must be 0 <= val < " + max_Val + "!";
			vals.add(val);
			i++;
		}
		
		String err = Helper.checkOptions(args, i, options);
		if (!err.equals("OK")) {
			return err;
		}
		
		myOpts = Helper.options(args, i, options);
		
		return "OK";
	}

	public String example(String alias) {
		final int M_UPPER_LIM = 30;
		final int M_LOWER_LIM = 1;
		final int N_LIM = max_Val - 1;

		String argStr = alias;
		
		Random rand = new Random();
		int m = rand.nextInt(M_UPPER_LIM - M_LOWER_LIM) + M_LOWER_LIM;
		
		int[] used = new int[m];
		int count = 0;

		for (int i = 0; i < m; i++) {
			double r = rand.nextDouble();
			if (r < 0.7) {
				int n = rand.nextInt(N_LIM) + 1;
				argStr += " i " + n;
				used[i] = n;
				count++;
			} else if (r < 0.9 && count > 0) {
				argStr += " d ";
				int n = 0;
				int j = 0;
				while (n == 0) {
					j = rand.nextInt(m);
					n = used[j];
				}
				argStr += n;
				used[j] = 0;
				count--;
			} else {
				argStr += " q ";
				int n = 0;
				int j = 0;
				r = rand.nextDouble();
				while (n == 0 && !(r < 0.1)) {
					j = rand.nextInt(m);
					n = used[j];
					r = rand.nextDouble();
				}
				if (r < 0.3) n = rand.nextInt(N_LIM) + 1;
				argStr += n;
			}
		}
		if (rand.nextDouble() < 0.3) {
			if (rand.nextDouble() < 0.5) {
				argStr += " -l";
			} else {
				argStr += " --log";
			}
		}
		
		return argStr;
	}
}
