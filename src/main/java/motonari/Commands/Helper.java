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
import net.dv8tion.jda.api.requests.restaction.MessageAction;

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
	
	public static void commandHelp(MessageChannel channel, String name, String cmd, String desc, String[] args, HashSet<String> aliases, HashMap<String, String> options) {
		EmbedBuilder help = new EmbedBuilder();
		help.setTitle(name);
		String in = "`" + Tomo.prefix + cmd;
		for (String arg : args) in += " <" + arg + ">";
		if (!options.isEmpty()) {
			in += " [-o | --option]";
		}
		in += "`";
		help.addField(in, desc, false);
		if (aliases.size() > 1) help.addField("Aliases", mdList(aliases), true);
		
		if (!options.isEmpty()) {
			LinkedList<String> optStrs = new LinkedList<String>();
			Object[] keys = options.keySet().toArray();
			Arrays.sort(keys);
			for (Object opt : keys) optStrs.addLast("-" + options.get(opt) + " | --" + opt);
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
		for (int i = 0; i < digits.length(); i++) {
			int d = Integer.valueOf(digits.substring(i, i+1));
			int zero = 0x0030;
			String emoji = (char)(zero + d) + "\ufe0f\u20e3";
			c.addReactionById(messageId, emoji).queue();
		}
		
	}
}
