package motonari.Commands;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Random;

import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TomoListener extends ListenerAdapter {
	
	JDA jda;
	public TomoListener(JDA jda) {
		super();
		this.jda = jda;
	}
	
	public void onMessageReceived(MessageReceivedEvent event) {
		Message msg = event.getMessage();
		String raw = msg.getContentRaw();
		if (!raw.startsWith(Tomo.prefix)) return;
		raw = raw.substring(Tomo.prefix.length());
		String[] args = raw.split("\\s+");
		//System.out.println(String.join(" ", args));
		if (args.length == 0) return;
		// abort if bot that's not me
		if (event.getAuthor().isBot() && !event.getAuthor().getId().equals(this.jda.getSelfUser().getId())) return;
		
		ArrayList<Class<? extends Command>> commands = new ArrayList<Class<? extends Command>>();
		commands.add(MinEditDistance.class);
		commands.add(MaxSubarrDiff.class);
		commands.add(UnboundedKnapsack.class);
		commands.add(ZeroOneKnapsack.class);
		commands.add(Graph.class);
		commands.add(Line.class);
		commands.add(Point.class);
		commands.add(Draw.class);
		commands.add(BinarySearchTree.class);
		
		if (args[0].equals("help")) { // HELP
			if (args.length == 1) {
				Help.help(event, jda, commands);
			} else if (args.length == 2) {
				for (Class<? extends Command> clazz : commands) {
					try {
						Command cmd = clazz.getConstructor().newInstance();
						if (cmd.isAlias(args[1])) {					
							cmd.help(event.getChannel(), args[1]);
							break;
						}
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}
				}
			}
		} else if (args[0].equals("ex")) {  
			if (args.length == 1) {
				try {
					Random rand = new Random();
					Class<? extends Command> clazz = commands.get(rand.nextInt(commands.size()));
					Command cmd = clazz.getConstructor().newInstance();
					Object[] aliases = cmd.aliases.toArray();
					String argStr = cmd.example((String)aliases[rand.nextInt(aliases.length)]);
					event.getChannel().sendMessage(Tomo.prefix + argStr).queue();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			} else if (args.length == 2) {
				for (Class<? extends Command> clazz : commands) {
					try {
						Command cmd = clazz.getConstructor().newInstance();
						if (cmd.isAlias(args[1])) {					
							String argStr = cmd.example(args[1]);
							event.getChannel().sendMessage(Tomo.prefix + argStr).queue();
							break;
						}
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}
				}
			}
			
		}
		else if (args[0].equals("full")) {
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
		} else if (args[0].equals("source")) {
			source(event);
		} else if (args[0].equals("8ball")) {
			eight(event, args);
		} else {
			for (Class<? extends Command> clazz : commands) {
				try {
					Command cmd = clazz.getConstructor().newInstance();
					if (cmd.isAlias(args[0])) {					
						cmd = clazz.getConstructor(MessageReceivedEvent.class, String[].class).newInstance(event, args);
						cmd.run();
						break;
					}
				} catch (Exception e) {
					e.printStackTrace(System.err);
					if (e instanceof InvocationTargetException) e.getCause().printStackTrace(System.err);
				}
			}
		}
	}

	private static void source(MessageReceivedEvent e) {
		EmbedBuilder info = new EmbedBuilder();
		info.setTitle("Tomo Source");
		info.setDescription(Tomo.SRC);
		info.setFooter("Summoned by " + e.getAuthor().getName(), e.getAuthor().getAvatarUrl());
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
	
	private static void iconUrl(MessageReceivedEvent e) {
		String iconUrl = e.getJDA().getSelfUser().getAvatarUrl();
		e.getChannel().sendMessage(iconUrl).queue();
	}
	
	private static void file(MessageReceivedEvent e) {
		e.getChannel().sendFile(new File("IMG_7087.jpeg")).queue();
	}
	
	private static void kekw(MessageReceivedEvent e) {
		e.getMessage().addReaction(":kekw:781893007900540928").queue();
	}
}
