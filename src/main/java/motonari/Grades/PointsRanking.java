package motonari.Grades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import motonari.Commands.Command;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PointsRanking extends Command {
	public PointsRanking(MessageReceivedEvent e, String[] args) {super(e, args);}
	public PointsRanking() {super();}

	@Override
	public void init() {
		name = "Ranking after Guessing Points";
		cmd = "grank";
		desc = "Returns a ranking after Guessing Points.";
		
		
		arg_str = "<event_name> [-r<role_id>]";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gr", "graderanking",
		}));
		
		options = new HashMap<String, String>();
		
	}
	
	String event_name;
	int event_id;
	String role_name;
	Long role_id = null;

	@Override
	public String parse() {
		if (args.length > 3) {
			return "Too many arguments!";
		} else if (args.length < 2)
			return "Not enough arguments!";
		
		event_name = args[1];
		try {
			ResultSet set = Grades.connect().createStatement()
				.executeQuery("SELECT id from events WHERE name = \"" + event_name + "\";");
			if (!set.next())
				return "No Event called " + event_name + " found!";
			else {
				event_id = set.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "SQLException!";
		}
		if (args.length >= 3) {
		String role_str = args[2];
			if (!role_str.startsWith("-r")) {
				return "Argument needs to start with \"-r\"!";
			}
			role_str = role_str.substring(2);
			try {
				role_id = Long.valueOf(role_str);
			} catch (NumberFormatException e) {
				return "<role_id> needs to be integer format!";
			}
			
			Guild eth = Tomo.jda.getGuildById(Grades.Eth_id);
			Role role = eth.getRoleById(role_id);
			if (role == null) {
				return "No such role in the server!";
			}
			role_name = role.getName();
		}
		return "OK";
	}
	
	ArrayList<Long> user_ids;
	ArrayList<Integer> points;

	@Override
	public void main() {
		user_ids = new ArrayList<Long>();
		points = new ArrayList<Integer>();
		try {
			ResultSet set = Grades.connect().createStatement().executeQuery("SELECT user_id, points FROM grades WHERE event_id = "
					+ event_id + " AND confirmed = 1" + (role_id != null ? " AND tags LIKE '%" + role_id + "%'" : "") + " ORDER BY points DESC;");
			
			while (set.next()) {
				user_ids.add(set.getLong("user_id"));
				points.add(set.getInt("points"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		

	}

	@Override
	public void answer() {
		if (user_ids.size() == 0) {
			c.sendMessage("No Participants found!").queue();
			return;
		}
		
		ArrayList<EmbedBuilder> embeds = new ArrayList<EmbedBuilder>();
		embeds.add(new EmbedBuilder());
		int ei = 0;
		embeds.get(ei).setTitle("Grade Guess Ranking **" + event_name + "**");
		embeds.get(ei).setColor(Tomo.COLOR);
		
		int i = 0;
		String content = "";
		while (i < user_ids.size()) {
			String line = "";
			if (i < 3) {
				switch (i) {
					case 0:
						line += ":first_place:";
						break;
					case 1:
						line += ":second_place:";
						break;
					case 2:
						line += ":third_place:";
						break;
				}
			} else {
				line += ":black_large_square:";
			}
			line += " **" + (i+1) + "**. ";
			line += "<@" + user_ids.get(i) + ">";
			line += " | ";
			line += points.get(i) + " pts\n";
			
			if (content.length() + line.length() < MessageEmbed.TEXT_MAX_LENGTH) {
				content += line;
			} else {
				embeds.get(ei).setDescription(content);
				content = "";
				embeds.add(new EmbedBuilder());
				ei++;
				embeds.get(ei).setTitle("Grade Guess Ranking **" + event_name + "**");
				embeds.get(ei).setColor(Tomo.COLOR);
			}
			
			i++;
		}
		
		for (EmbedBuilder e : embeds) {
			c.sendMessage(e.build()).queue();
		}
	}

	@Override
	public String example(String alias) {
		return "BP1";
	}

}
