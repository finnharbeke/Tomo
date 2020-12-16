package motonari.Commands;

import java.util.Random;

import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TomoListener extends ListenerAdapter {
	public void onMessageReceived(MessageReceivedEvent event) {
		Message msg = event.getMessage();
		String raw = msg.getContentRaw();
		if (!raw.startsWith(Tomo.prefix)) return;
		raw = raw.substring(Tomo.prefix.length());
		String[] args = raw.split("\\s+");
		//System.out.println(String.join(" ", args));
		if (args.length == 0) return;
		if (args[0].equals("help")) { // HELP
			if (args.length == 1) {
				// main help
			} else if (args.length == 2) {
				if (MinEditDistance.aliases.contains(args[1])) {
					MinEditDistance.help(event.getChannel(), args);
				}
			}
		} else if (MinEditDistance.aliases.contains(args[0])) {
			MinEditDistance.run(event, args);
		}
	}

	private static void info(MessageReceivedEvent e) {
		EmbedBuilder info = new EmbedBuilder();
		info.setTitle("Tomo Info");
		
		info.setFooter("Summoned by " + e.getAuthor().getName(), e.getAuthor().getAvatarUrl());
		System.out.println(e.getAuthor().getAvatarUrl());
		info.setColor(0xED635E);
		e.getChannel().sendMessage(info.build()).queue();
		info.clear();
	}

	private static void eight(MessageChannel c, String[] args) {
		if (args.length < 2) {
			c.sendMessage("Ask me a yes/no question!").queue();
		} else {
			String[] answers = {"Definitely!", "100%!", "Probably", "Why not?", "You know that yourself!", "Nope.", "Mmmh... no?"};
			Random r = new Random();
			c.sendMessage(answers[r.nextInt(answers.length)]).queue();
		}
	}

	private static void ping(MessageChannel c) {
		long time = System.currentTimeMillis();
		c.sendMessage("Pong!").queue(response -> 
			response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue()
		);
	}
}
