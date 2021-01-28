package motonari.Commands;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

public class Helper {
	public static void fullEmbed(MessageChannel channel) {
		EmbedBuilder help = new EmbedBuilder();
		help.setTitle("title", "https://www.ecosia.org/images?q=image");
		help.setAuthor("Author", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "https://spielverlagerung.de/wp-content/uploads/2014/11/football-ball.jpg");
		help.setColor(0xe305e3);
		help.setDescription("description");
		help.setFooter("footer", "https://tse2.mm.bing.net/th?id=OIP.akMbDipCNVyOxnlVWdXVPgHaGL&o=6&pid=Api");
		help.setImage("https://tse1.mm.bing.net/th?id=OIP.wx64GmJDu2nd32eO_tieDgHaEK&o=6&pid=Api");
		help.setThumbnail("https://tse4.mm.bing.net/th?id=OIP.dyG9YY1J0V58F-e3hUH8rwHaFj&o=6&pid=Api");
		help.setTimestamp(Instant.now());
		help.addField("Field1", "field", false);
		help.addField("Field2", "field", true);
		help.addField("Field3", "field", true);
		channel.sendMessage(help.build()).queue();
		help.clear();
	}
	
	public static void commandHelp(MessageChannel channel, Command cmd, String alias) {
		EmbedBuilder help = new EmbedBuilder();
		help.setTitle(cmd.name);
		String in = "`" + Tomo.prefix + alias + " " + cmd.arg_str;
		if (!cmd.options.isEmpty())
			in += " [-o | --option]";
		in += "`";
		
		help.addField(in, cmd.desc, false);
		if (cmd.aliases.size() > 1) help.addField("Aliases", mdList(cmd.aliases), true);
		
		if (!cmd.options.isEmpty()) {
			LinkedList<String> optStrs = new LinkedList<String>();
			Object[] keys = cmd.options.keySet().toArray();
			Arrays.sort(keys);
			for (Object opt : keys) optStrs.addLast("-" + cmd.options.get(opt) + " | --" + opt);
			help.addField("Options", mdList(optStrs), true);
		}
		
		channel.sendMessage(help.build()).queue();
		help.clear();
		
	}
	
	public static String mdList(Iterable<? extends CharSequence> list) {
		return "```md\n- " + String.join("\n- ", list) + "\n```";
	}

	public static void error(MessageChannel c, String cmd, String err) {
		EmbedBuilder error = new EmbedBuilder();
		error.setColor(0xfc2003);
		error.setDescription("**Error:**\t" + err);
		error.setFooter("type \"" + Tomo.prefix + "help " + cmd + "\" for more info");
		c.sendMessage(error.build()).queue((m) -> {
			m.delete().queueAfter(10, TimeUnit.SECONDS);
		});
		error.clear();
	}

	public static void reactNumber(MessageChannel c, String messageId, int answer) {
		String digits = String.valueOf(answer);
		boolean had_one = false;
		for (int i = 0; i < digits.length(); i++) {
			int d = Integer.valueOf(digits.substring(i, i+1));
			int zero = 0x0030;
			String emoji = (char)(zero + d) + "\ufe0f\u20e3";
			if (d == 1 && had_one) emoji = "U+1F502";
			c.addReactionById(messageId, emoji).queue();
			if (d == 1) had_one = true;
		}	
	}
	
	public static String checkOptions(String[] args, int start, HashMap<String, String> OPTIONS) {
		for (int i = start; i < args.length; i++) {
			String arg = args[i];
			//System.out.println(args[i] + " " + !args[i].startsWith("--") +  " " + (args[i].length() < 2) + " " + !options.contains(args[i].substring(2)) + " " + args[i].substring(2) + " [" + String.join(" ", options) + "]");
			if (!arg.startsWith("-")) {
				return "Invalid Argument: `" + arg + "`!";
			}
			if (args[i].startsWith("--")) {
				if (!OPTIONS.containsKey(arg.substring(2))) {
					return "Invalid Option: `" + arg.substring(2) + "`!";
				}
			} else {
				// -
				if (!OPTIONS.containsValue(arg.substring(1))) {
					return "Invalid Option: `" + arg.substring(1) + "`!";
				}
			}
		}
		return "OK";
	}
	
	public static HashSet<String> options(String[] args, int start, HashMap<String, String> OPTIONS) {
		HashSet<String> res = new HashSet<String>();
		for (int i = start; i < args.length; i++) {
			String arg = args[i];
			//System.out.println(args[i] + " " + !args[i].startsWith("--") +  " " + (args[i].length() < 2) + " " + !options.contains(args[i].substring(2)) + " " + args[i].substring(2) + " [" + String.join(" ", options) + "]");
			if (args[i].startsWith("--")) {
				res.add(OPTIONS.get(arg.substring(2)));
			} else {
				// -
				res.add(arg.substring(1));
			}
		}
		return res;
	}
}
