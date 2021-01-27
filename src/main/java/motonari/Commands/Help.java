package motonari.Commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Help {
	public static void help(MessageReceivedEvent e, JDA jda, ArrayList<Class<? extends Command>> commands) {
		EmbedBuilder help = new EmbedBuilder();
		help.setColor(Tomo.COLOR);
		help.setThumbnail(jda.getSelfUser().getAvatarUrl());
		help.setTitle(jda.getSelfUser().getName() + "'s Help");
		help.setDescription("For more info on a particular command:\n```yaml\n&help cmd```");
		help.setFooter(e.getAuthor().getName() + "called for help!", e.getAuthor().getAvatarUrl());
		for (Class<? extends Command> clazz : commands) {
			try {
				Command cmd = clazz.getConstructor().newInstance();
				help.addField(cmd.name, cmd.desc + "\n```yaml\n" + Tomo.prefix + cmd.cmd + " " + cmd.arg_str + "```", false);
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				if (ex instanceof InvocationTargetException) ex.getCause().printStackTrace(System.err);
			}
		}
		e.getChannel().sendMessage(help.build()).queue();
		help.clear();
	}
}
