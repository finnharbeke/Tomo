package motonari.Commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Canvas extends Command {
	public Canvas(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Canvas() {super();}

	String[] map;
	final int W = 64;
	final int H = 25;
	
	public void main() {
		initMap();
	}
	
	abstract public void answer();
	
	private void initMap() {
		map = new String[H];
		for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) map[i] = j == 0 ? " " : map[i] + " ";
			map[i] += "\n";
		}
	}
	
	public void set(int x, int y, char ch) {
		map[y] = map[y].substring(0, x) + ch + map[y].substring(x + 1);
	}
	
	public void line(int x1, int y1, int x2, int y2) {
		LineCont lineC = createLine(x1, y1, x2, y2);
		setLine(lineC.chars, lineC.xs, lineC.ys);
	}
	
	private static LineCont createLine(int x1, int y1, int x2, int y2) {
		char ch1, ch2;
		char[] tmp = symbols(x1, y1, x2, y2);
		ch1 = tmp[0]; ch2 = tmp[1];
		
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		
		if (y2 > y1) {
			int temp = x2; x2 = x1; x1 = temp;
			temp = y2; y2 = y1; y1 = temp;
		}
		
		int xDir, yDir;
		if (x2 > x1) xDir = 1;
		else xDir = -1;
		if (y2 > y1) yDir = 1;
		else yDir = -1;
		
		int x = x1;
		int y = y1;
		boolean steep = false;
		if (dy > dx) {
			steep = true;
			int temp = dx; dx = dy; dy = temp;
			temp = xDir; xDir = yDir; yDir = temp;
			y = x1;
			x = y1;
		}
		
		char[] chars = new char[dx+1];
		int[] xs = new int[dx+1];
		int[] ys = new int[dx+1];
		
		double delta = dy / (double)dx;
		double err = 0;
		
		for (int i = 0; i < dx; i++) {
			chars[i] = ch1;
			xs[i] = x;
			ys[i] = y;
			err += delta;
			//System.out.println(err);
			if (err >= 0.5) {
				chars[i] = ch2;
	            y += yDir;
	            err--;
			}
			x += xDir;
		}
		chars[dx] = err >= 0.5 ? ch1 : ch2;
		xs[dx] = x;
		ys[dx] = y;
		
		if (steep)
			return new LineCont(dx+1, chars, ys, xs);
		else
			return new LineCont(dx+1, chars, xs, ys);
	}
	
	private void setLine(char[] chars, int[] xs, int[] ys) {
		assert chars.length == xs.length;
		assert xs.length == ys.length;
		for (int i = 0; i < chars.length; i++) {
			System.out.println(xs[i] + " " + ys[i] + " " + chars[i]);
			set(xs[i], ys[i], chars[i]);
		}
	}
	
	private static char[] symbols(int x1, int y1, int x2, int y2) {
		
		// make p1 left of p2
		if (x2 < x1) {
			int temp = x2;
			x2 = x1;
			x1 = temp;
			temp = y2;
			y2 = y1;
			y1 = temp;
		}
		
		// set drawing chars		
		char ch1;
		char ch2;
		double slope = x1 == x2 ? 1e99 : (double)(y2 - y1) / (double)(x2 - x1);
		if (slope > 0) {
			if (slope > 1) {
				ch1 = '|'; ch2 = '\\';
			} else {
				ch1 = '_'; ch2 = '\\';
			}
		} else {
			if (slope < -1) {
				ch1 = '|'; ch2 = '/';
			} else {
				ch1 = '_'; ch2 = '/';
			}
		}
		
		return new char[] {ch1, ch2};
	}
	
}

class LineCont {
	int n;
	char[] chars;
	int[] xs;
	int[] ys;
	
	LineCont(int n, char[] chars, int[] xs, int[] ys) {
		this.n = n;
		assert chars.length == n;
		assert xs.length == n;
		assert ys.length == n;
		this.chars = chars;
		this.xs = xs;
		this.ys = ys;
	}
}
