package motonari.Tomo;

import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
	public void onMessageReceived(MessageReceivedEvent event) {
		Message msg = event.getMessage();
		String raw = msg.getContentRaw();
		if (!raw.startsWith(Tomo.prefix)) return;
		raw = raw.substring(Tomo.prefix.length());
		String[] args = raw.split("\\s+");
		if (raw.equals("ping")) { // PING
			ping(event.getChannel());
		} else if (args[0].equals("8ball")) { // 8-BALL
			eight(event.getChannel(), args);
		} else if (raw.equals("info")) {
			info(event);
		} else if (args[0].equals("med")) {
			minEditDistance(event.getChannel(), args);
		}
	}

	private static void minEditDistance(MessageChannel c, String[] args) {
		String A = args[1], B = args[2];
		int[][] DP = new int[A.length()+1][B.length()+1];
		for (int i = 0; i <= A.length(); i++) DP[i][0] = i;
		for (int j = 0; j <= B.length(); j++) DP[0][j] = j;
		for (int i = 1; i <= A.length(); i++) {
			for (int j = 1; j <= B.length(); j++) {
				DP[i][j] = Math.min(DP[i-1][j-1] + (A.charAt(i-1) == B.charAt(j-1) ? 0 : 1), Math.min(DP[i-1][j] + 1, DP[i][j-1] + 1));
			}
		}
		String table = "```";
		for (int i = -1; i <= A.length(); i++) {
			for (int j = -1; j <= B.length(); j++) {
				if (i == -1) table += j <= 0 ? "-" : B.substring(j-1, j);
				else if (j == -1) table += i <= 0 ? "-" : A.substring(i-1, i);
				else table += DP[i][j];
				table += " ";
			}
			table += "\n";
		}
		table += "```\n";
		c.sendMessage(table + "Minimum Edit Distance between `" + A + "` and `" + B + "` is " + DP[A.length()][B.length()]).queue();

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
