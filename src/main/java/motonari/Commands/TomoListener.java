package motonari.Commands;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.Random;

import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
				if (MinEditDistance.isAlias(args[1])) {
					MinEditDistance.help(event.getChannel());
				} else if (MaxSubarrDiff.isAlias(args[1])) {
					MaxSubarrDiff.help(event.getChannel());
				} else if (UnboundedKnapsack.isAlias(args[1])) {
					UnboundedKnapsack.help(event.getChannel());
				} else if (ZeroOneKnapsack.isAlias(args[1])) {
					ZeroOneKnapsack.help(event.getChannel());
				}  else if (Line.isAlias(args[1])) {
					Line.help(event.getChannel());
				}
				
			}
		} else if (args[0].equals("ex")) {  
			if (args.length == 1) {
			
			} else if (args.length == 2) {
				if (MinEditDistance.isAlias(args[1])) {
					MinEditDistance cmd = MinEditDistance.random(event, args[1]);
					cmd.run();
				} else if (MaxSubarrDiff.isAlias(args[1])) {
					MaxSubarrDiff cmd = MaxSubarrDiff.random(event, args[1]);
					cmd.run();
				}
			}
			
			
		} else if (MinEditDistance.isAlias(args[0])) {
			MinEditDistance cmd = new MinEditDistance(event, args);
			cmd.run();
		} else if (MaxSubarrDiff.isAlias(args[0])) {
			MaxSubarrDiff cmd = new MaxSubarrDiff(event, args);
			cmd.run();
		} else if (UnboundedKnapsack.isAlias(args[0])) {
			UnboundedKnapsack cmd = new UnboundedKnapsack(event, args);
			cmd.run();
		} else if (ZeroOneKnapsack.isAlias(args[0])) {
			ZeroOneKnapsack cmd = new ZeroOneKnapsack(event, args);
			cmd.run();
		} else if (Line.isAlias(args[0])) {
			Line cmd = new Line(event, args);
			cmd.run();
		} else if (args[0].equals("full")) {
			Helper.fullEmbed(event.getChannel());
		} else if (args[0].equals("ping")) {
			ping(event);
		} else if (args[0].equals("pingdiy")) {
			pingDiy(event);
		} else if (args[0].equals("icon")) {
			iconUrl(event);
		} else if (args[0].equals("file")) {
			file(event);
		} else if (args[0].equals("kekw")) {
			kekw(event);
		} else if (args[0].equals("info")) {
			info(event);
		} else if (args[0].equals("8ball")) {
			eight(event, args);
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

	private static void eight(MessageReceivedEvent e, String[] args) {
		if (args.length < 2) {
			e.getChannel().sendMessage("Ask me a yes/no question!").queue();
		} else {
			String[] answers = {"Definitely!", "100%!", "Probably", "Why not?", "You know that yourself!", "Nope.", "Mmmh... no?"};
			Random r = new Random();
			e.getChannel().sendMessage(answers[r.nextInt(answers.length)]).queue();
		}
	}

	private static void pingDiy(MessageReceivedEvent e) {
		EmbedBuilder pong = new EmbedBuilder();
		pong.setTitle("Pong!:ping_pong:");
		pong.setColor(Tomo.COLOR);
		OffsetDateTime msgTime = e.getMessage().getTimeCreated();
		long msgMillis = msgTime.toEpochSecond()*1000 + (msgTime.getNano() / 1000000);
		long start = System.currentTimeMillis();
		long gateway = start - msgMillis;
		pong.setDescription(gateway + " ms");
		e.getChannel().sendMessage(pong.build()).queue(response -> {
			pong.setTitle("Pong!:ping_pong:");
			pong.setDescription((System.currentTimeMillis() - start) + " ms | " + gateway + " ms");
			pong.setColor(Tomo.COLOR);
			response.editMessage(pong.build()).queue();
		});
		pong.clear();
	}
	
	private static void ping(MessageReceivedEvent e) {
		EmbedBuilder pong = new EmbedBuilder();
		e.getJDA().getRestPing().queue( (time) -> {
			pong.setTitle("Pong!:ping_pong:");
			pong.setColor(Tomo.COLOR);
			pong.setDescription(time + " ms | " + e.getJDA().getGatewayPing() + " ms");
			e.getChannel().sendMessage(pong.build()).queue();
		});
		pong.clear();
	}
	
	private void iconUrl(MessageReceivedEvent e) {
		String iconUrl = e.getJDA().getSelfUser().getAvatarUrl();
		e.getChannel().sendMessage(iconUrl).queue();
	}
	
	private void file(MessageReceivedEvent e) {
		e.getChannel().sendFile(new File("IMG_7087.jpeg")).queue();
	}
	
	private void kekw(MessageReceivedEvent e) {
		e.getMessage().addReaction(":kekw:781893007900540928").queue();
	}
}
