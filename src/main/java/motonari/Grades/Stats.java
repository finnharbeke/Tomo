package motonari.Grades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import motonari.Commands.Command;
import motonari.Tomo.Helper;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Stats extends Command {
	public Stats(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Stats() {super();}

	@Override
	public void init() {
		name = "Stats about Grade Guessing";
		cmd = "gstats";
		desc = "Returns some stats about the current / given Event.";
		
		
		arg_str = "[<event_name>] [-r<role_id>]";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gs", "gradestats",
		}));
		
		options = new HashMap<String, String>();
		
	}
	
	Integer event_id = null;
	String event_name;
	Long role_id = null;
	String role_name = null;

	@Override
	public String parse() {
		if (args.length > 2) {
			return "Too many arguments";
		} else if (args.length >= 2) {
			event_name = args[1];
			int i = 1;
			if (!event_name.startsWith("-r")) {
				i++;
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
			}
			if (args.length >= i + 1) {
				String role_str = args[i];
				if (!role_str.startsWith("-r")) {
					return "Argumnent needs to start with \"-r\"!";
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
		}
		if (event_id == null) {
			event_id = Grades.currentEvent();
			if (event_id == -1) {
				return "No current event!";
			} else {
				try {
					ResultSet set = Grades.connect().createStatement()
						.executeQuery("SELECT name from events WHERE id = " + event_id + ";");
					set.next();
					event_name = set.getString("name");
				} catch (SQLException e) {
					e.printStackTrace();
					return "SQLException!";
				}
			}
		}
		
		return "OK";
	}
	
	double[] subavgs = new double[4];
	int[] subcnts = new int[4];
	double totalavg = 0.0;
	int totalcnt = 0;
	double avgavg = 0;
	int guessercnt = 0;
	String[] subs;
	
	

	@Override
	public void main() {
		
		try {
			ResultSet set = Grades.connect().createStatement()
				.executeQuery("SELECT guess1, guess2, guess3, guess4, tags FROM grades WHERE event_id = "
					+ event_id + (role_id != null ? " AND tags LIKE '%" + role_id + "%'" : "") + ";");
			
			while (set.next()) {
				guessercnt++;
				double[] guesses = {set.getDouble("guess1"), set.getDouble("guess2"), 
					set.getDouble("guess3"), set.getDouble("guess4")};
				
				double avg = 0;
				int cnt = 0;
				
				for (int i = 0; i < guesses.length; i++) {
					if (guesses[i] != 0.0) {
						totalcnt++;
						subcnts[i]++;
						totalavg += (guesses[i] - totalavg) / totalcnt;
						subavgs[i] += (guesses[i] - subavgs[i]) / subcnts[i];
						
						cnt++;
						avg += (guesses[i] - avg) / cnt;
					}
				}
				
				avgavg += (avg - avgavg) / guessercnt;
				
			}
			
			set = Grades.connect().createStatement()
				.executeQuery("SELECT sub1, sub2, sub3, sub4 FROM events WHERE id = " + event_id + ";");
			
			set.next();
			
			subs = new String[] {set.getString("sub1"), set.getString("sub2"), set.getString("sub3"), set.getString("sub4")};
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void answer() {
		
		if (guessercnt < 5) {
			Helper.error(e, c, args[0], "I won't return grades stats when querying less than 5 people.", 20);
			return;
		}
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(Tomo.COLOR);
		embed.setTitle("Guess Stats for **" + event_name + "**" + (role_name != null ? ": *" + role_name + "*" : ""));
		
		embed.setDescription("In total " + guessercnt + " people have guessed.\n");
		
		for (int i = 0; i < subs.length; i++) {
			String content;
			if (subcnts[i] == 0)
				content = "No Guesses.";
			else
				content = "average guess: `" + String.format("%.2f", subavgs[i]) + "`\n"
					+ subcnts[i] + " people have guessed.";
			
			embed.addField("**" + subs[i] + "**", content, true);
			if (i % 2 == 1)
				embed.addBlankField(true);
		}
		
		
		embed.addField("**Average personal avg**", "`" + String.format("%.2f", avgavg) + "`", true);
		
		double subavg = 0;
		int cnt = 0;
		for (int i = 0; i < subavgs.length; i++) {
			if (subcnts[i] == 0)
				continue;
			subavg += (subavgs[i] - subavg) / ++cnt;
		}
		embed.addField("**Average of subject avgs**", "`" + String.format("%.2f", subavg) + "`", true);
		embed.addBlankField(true);
		
		embed.addField("**Total**", "average guess: `" + String.format("%.2f", totalavg) + "`\n"
				+ totalcnt + " total guesses", false);
		
		c.sendMessage(embed.build()).queue();
		
	}

	@Override
	public String example(String alias) {
		return "";
	}

}
