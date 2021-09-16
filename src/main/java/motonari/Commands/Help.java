package motonari.Commands;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Help extends Command {
	public Help(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Help() {super();}

	@Override
	public void init() {
		name = "Help about commands";
		cmd = "help";
		desc = "List commands or get help for a specific command.";
		
		
		arg_str = "[cmd]";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "h",
		}) );
		
		options = new HashMap<String, String>();
		
	}
	
	Command help_cmd = null;
	EmbedBuilder embed;

	@Override
	public String parse() {
		if (args.length == 2) {
			try {
				help_cmd = Tomo.fromAlias(args[1]).getConstructor().newInstance();
				if (help_cmd.admin && !byAdmin()) {
					return "This is an admin command!";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (args.length > 2) {
			return "Too many arguments!";
		}
		return "OK";
	}

	@Override
	public void main() {
		
		if (help_cmd != null) {
			embed = help_cmd.help(args[1]);
		} else {
			embed = new EmbedBuilder();
			embed.setColor(Tomo.COLOR);
			embed.setTitle(Tomo.jda.getSelfUser().getName() + "'s Help");
			embed.setDescription("For more info on a particular command:\n```yaml\n&help cmd```For example uses:\n```yaml\n&ex [cmd]```");
			embed.setFooter(e.getAuthor().getName() + " called for help!", e.getAuthor().getAvatarUrl());
			embed.setTimestamp(Instant.now());
			int i = 0;
			for (String group : Tomo.commands.keySet()) {
				if (group.equals("Sub")) continue;
				String content = "";
				for (Class<? extends Command> clazz : Tomo.commands.get(group)) {
					try {
						Command cmd = clazz.getConstructor().newInstance();
						if (cmd.admin && !byAdmin()) {
							continue;
						}
						//help.addField(cmd.name, cmd.desc + "\n```yaml\n" + Tomo.prefix + cmd.cmd + " " + cmd.arg_str + "```", true);
						content += "# " + cmd.name + "\n";
						content += Tomo.prefix + cmd.cmd + " " + cmd.arg_str + "\n";
					} catch (Exception ex) {
						ex.printStackTrace(System.err);
						if (ex instanceof InvocationTargetException) ex.getCause().printStackTrace(System.err);
					}
				}
				if (content.equals(""))
					continue;
				content = "```yaml\n" + content + "```";
				embed.addField(group, content, true);
				
				
				if (i % 2 == 1) {
					embed.addBlankField(true);
				} else if (i == Tomo.commands.keySet().size() - 1) {
					embed.addBlankField(true);
					embed.addBlankField(true);
				}
				i++;
			}
			
		}
		
	}

	@Override
	public void answer() {
		c.sendMessage(embed.build()).queue();
		embed.clear();
	}

	@Override
	public String example(String alias) {
		Random rand = new Random();
		String res = alias;
		if (rand.nextDouble() < 0.4) {
			return res;
		} else {
			try {
				Command cmd = Tomo.random(byAdmin()).getConstructor().newInstance();
				res += " " + cmd.randomAlias();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}
}
