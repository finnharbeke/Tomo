package motonari.Commands;

import java.time.Instant;
import java.util.List;

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
	
	public static void commandHelp(MessageChannel channel, String cmd, String desc, List<String> aliases, List<String> options) {
		EmbedBuilder help = new EmbedBuilder();
		help.setTitle(cmd);
		help.addField("`med <str1> <str2> `", desc, false);
		help.addField("Aliases", mdList(aliases), true);
		help.addField("Options", mdList(options), true);
		channel.sendMessage(help.build()).queue();
		help.clear();
		
	}
	
	public static String mdList(Iterable<? extends CharSequence> list) {
		return "```md\n- " + String.join("\n- ", list) + "\n```";
	}
}
