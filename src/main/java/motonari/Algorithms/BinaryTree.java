package motonari.Algorithms;

import motonari.Ascii.Canvas;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class BinaryTree extends Canvas {
	public BinaryTree(MessageReceivedEvent e, String[] args) {super(e, args);}
	public BinaryTree() {super();}
	
	private int id_counter = 0;
	protected Node origin = null;
	final int max_Val = 100;
	
	String long_draw_res;
	
	public void draw() {
		if (totalHeight() <= 5) {
			write(3, 1, "#format: [id|vl|h]");
			origin.draw(this, 1, 4, this.H / 2);
		} else {
			final int line = 96;
			
			long_draw_res = "```css\n";
			
			Node[] top = null;
			Node[] bot;

			for (int lev = 0; lev < totalHeight(); lev++) {
				long_draw_res += "#level #" + lev + "\n";
				
				if (lev == 0) {
					bot = new Node[1];
					bot[0] = origin;
				} else {
					bot = new Node[2 * top.length];
					for (int j = 0; j < top.length; j++) {
						if (top[j] != null) {
							bot[2 * j] = top[j].left();
							bot[2 * j + 1] = top[j].right();
						}
					}
				}
				
				int step = line / bot.length;
				
				if (step < 3)
					step = 3;
				
				for (int start = 0; start < bot.length; start += line / step) {
					for (int i = 0; i < line / step && start + i < bot.length; i++) {
						for (int j = 0; j < step / 2 - 1; j++)
							long_draw_res += " ";
						if (bot[start + i] == null)
							long_draw_res += "  ";
						else {
							String id_str = String.valueOf(bot[start + i].getId());
							if (id_str.length() == 1) id_str = "0" + id_str;
							long_draw_res += id_str;
						}
						if (i != line / step - 1 && start + i != bot.length - 1) {
							for (int j = 0; j < step / 2 - 1; j++)
								long_draw_res += " ";
							if (step == 3)
								long_draw_res += " ";
						}
					}
					long_draw_res += "\n";
					for (int i = 0; i < line / step && start + i < bot.length; i++) {
						for (int j = 0; j < step / 2 - 1; j++)
							long_draw_res += " ";
						if (bot[start + i] == null)
							long_draw_res += "  ";
						else {
							String val_str = String.valueOf(bot[start + i].getVal());
							if (val_str.length() == 1) val_str = " " + val_str;
							long_draw_res += val_str;
						}
						if (i != line / step - 1 && start + i != bot.length - 1) {
							for (int j = 0; j < step / 2 - 1; j++)
								long_draw_res += " ";
							if (step == 3)
								long_draw_res += " ";
						}
					}
					long_draw_res += "\n\n";
				}
				
				top = bot;
			}
			
			long_draw_res += "```";
			
		}
	}
	
	public void answer() {
		if (totalHeight() <= 5) {
			super.answer("css");
		} else {
			if (long_draw_res.length() < Tomo.msgLim) {				
				c.sendMessage(long_draw_res).queue();
			} else {
				c.sendMessage("Couldn't print Binary Tree, because of the " + Tomo.msgLim + " char Message Limit.").queue();
			}
		}
	}
	
	public int getId() {
		return id_counter++;
	}
	
	public void setOrigin(int value) {
		this.origin = new Node(this.getId(), value);
	}
	
	public int totalHeight() {
		if (origin == null)
			return 0;
		else
			return origin.getH();
	}
	
}

class Node {
	private int id;
	private int value;
	
	private Node left;
	private Node right;
	private int h;
	
	Node(int id, int value) {
		this.id = id;
		this.value = value;
		left = null;
		right = null;
		this.h = 1;
	}
	
	Node(int id, int value, Node left, Node right) {
		this.id = id;
		this.value = value;
		this.left = left;
		this.right = right;
		this.computeHeight();
	}
	
	int getId() { return this.id;}
	
	int getVal() { return this.value;}
	
	Node left() { return this.left;}
	
	Node right() { return this.right;}
	
	int getH() { return h;}
	void incH() { h++; }
	void decH() { h--; }
	void setH(int h) { this.h = h; }
	
	void setLeft(Node left) {
		this.left = left;
	}
	
	void setRight(Node right) {
		this.right = right;
	}
	
	void computeHeight() {
		int belowH = 0;
		if (this.left != null) belowH = left.h;
		if (this.right != null && right.h > belowH) belowH = right.h;
		
		this.h = belowH + 1;
	}
	
	void draw(BinaryTree canv, int level, int x, int y) {
		
		assert level <= 5;
		if (level == 5) {
			assert left == null;
			assert right == null;
		}
		
		int cx = x + 12;
		int ly;
		int ry;
		if (level == 1 || level == 3) {
			
			int off = level == 1 ? 6 : 2;
			ly = y + off;
			ry = y - off;
		} else if (level == 2) {
			if (y == 6) {
				ry = y - 4;
				ly = y + 3;
			} else {
				ry = y - 3;
				ly = y + 4;
			}
		} else { // level 4 (5 too, but doesn't use ly, ry)
			if (y == 0 || y == 7 || y == 13 || y == 20) {
				// down
				ry = y;
				ly = y + 1;
			} else {
				// up
				ry = y - 1;
				ly = y;
			}
		}
	
		// draw lines first
		
		if (left != null) {
			canv.line(x, y, cx, ly);
		}
		if (right != null) {
			canv.line(x, y, cx, ry);
		}
		
		// "draw" node
		// [id|vl|h]
		
		String id_str = String.valueOf(id);
		if (id_str.length() == 1) id_str = "0" + id_str;
		String val_str = String.valueOf(value);
		if (val_str.length() == 1) val_str = " " + val_str;
		
		canv.write(x-4, y, "[" + id_str + "|" + val_str + "|" + h + "]");
		
		if (right != null) {
			right.draw(canv, level + 1, cx, ry);
		}
		if (left != null) {
			left.draw(canv, level + 1, cx, ly);
		}
		
	}
	
}