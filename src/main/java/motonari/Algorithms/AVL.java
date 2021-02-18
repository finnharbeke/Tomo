package motonari.Algorithms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AVL extends BinaryTree {
	public AVL(MessageReceivedEvent e, String[] args) {super(e, args);}
	public AVL() {super();}
	
	public void init() {
		name = "AVL Tree";
		cmd = "avl";
		desc = "Draws ascii AVL tree after row of commands";
		
		arg_str = "(i val | d val | q val ){1, }";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "avltree", "a"
		}) );
		
		options = new HashMap<String, String>();
	}

	@Override
	public String parse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String example(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

}
