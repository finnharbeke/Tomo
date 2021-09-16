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

	private static int MIN_N = 5;

	@Override
	public void init() {
		name = "Stats about Grade Guessing";
		cmd = "gstats";
		desc = "Returns basic stats about the given Event.";
		
		
		arg_str = "<event_name> [<subject>] [-r<role_id>]";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gst", "gradestats",
		}));
		
		options = new HashMap<String, String>();
		
	}
	
	Integer event_id = null;
	String event_name;
	Long role_id = null;
	String role_name = null;
	Integer subject = null;

	@Override
	public String parse() {
		String[] subs = new String[4];
		if (args.length > 4) {
			return "Too many arguments";
		} else if (args.length < 2) {	
			return "Not enough arguments!";
		} else {
			event_name = args[1];
			try {
				ResultSet set = Grades.connect().createStatement()
					.executeQuery("SELECT * from events WHERE name = \"" + event_name + "\";");
				if (!set.next())
					return "No Event called " + event_name + " found!";
				else {
					event_id = set.getInt("id");
					subs[0] = set.getString("sub1");
					subs[1] = set.getString("sub2");
					subs[2] = set.getString("sub3");
					subs[3] = set.getString("sub4");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return "SQLException!";
			}
			if (args.length >= 3) {
				int i = 2;
				String next = args[i];
				for (int j = 0; j < 4; j++) {
					if (subs[j].equals(next)) {
						subject = j;
					}
				}
				if (subject != null)
					i++;
				if (i < args.length) {
					next = args[i];
					if (!next.startsWith("-r"))
						return "Argument '" + next + "' needs to start with \"-r\"!";
					next = next.substring(2);
					try {
						role_id = Long.valueOf(next);
					} catch (NumberFormatException e) {
						return "<role_id> needs to be integer format!";
					}
					
					Guild eth = Tomo.jda.getGuildById(Grades.Eth_id);
					Role role = eth.getRoleById(role_id);
					if (role == null) {
						return "No such role in the server!";
					}
					role_name = role.getName();
				} else if (subject == null) {
					return "Argument '" + next + "' is invalid. Valid are '<subject>' or '-r<role_id>'.";
				}
			}
		}
		return "OK";
	}
	
	double[] means = new double[8];
	int[] counts = new int[8];
	double[] stds = new double[8];
	double guessMean;
	double gradeMean;
	int guessCount = 0;
	int gradeCount = 0;
	
	int n;
	String[] subs;

	@Override
	public void main() {
		
		try {
			ResultSet set = Grades.connect().createStatement()
				.executeQuery("SELECT count(*) as c FROM grades WHERE event_id = "
				+ event_id + (role_id != null ? " AND tags LIKE '%" + role_id + "%'" : "") + ";"
			);
			
			n = set.getInt("c");

			Double[][] all = new Double[8][n];
			double[] sums = new double[8];
			double guessSum = 0;
			double gradeSum = 0;
			
			set = Grades.connect().createStatement()
				.executeQuery("SELECT * FROM grades WHERE event_id = "
				+ event_id + (role_id != null ? " AND tags LIKE '%" + role_id + "%'" : "") + ";"
			);

			for (int i = 0; set.next(); i++) {
				all[0][i] = set.getDouble("guess1");
				all[1][i] = set.getDouble("guess2");
				all[2][i] = set.getDouble("guess3");
				all[3][i] = set.getDouble("guess4");
				all[4][i] = set.getDouble("grade1");
				all[5][i] = set.getDouble("grade2");
				all[6][i] = set.getDouble("grade3");
				all[7][i] = set.getDouble("grade4");
				
				for (int s = 0; s < 8; s++) {
					if (all[s][i] != 0.0) {
						sums[s] += all[s][i];
						counts[s]++;
						if (s < 4) {
							guessCount++;
							guessSum += all[s][i];
						} else {
							gradeCount++;
							gradeSum += all[s][i];
						}
					}
				}
			}

			// MEANS
			guessMean = guessSum / guessCount;
			gradeMean = gradeSum / gradeCount;
			for (int s = 0; s < 8; s++) {
				means[s] = sums[s] / counts[s];
			}
			// STANDARD DEVIATION
			for (int s = 0; s < 8; s++) {
				double var = 0;
				for (int i = 0; i < n; i++) {
					if (all[s][i] == 0.0) continue;
					double t = means[s] - all[s][i];
					var += t*t;
				}
				var /= counts[s];
				stds[s] = Math.sqrt(var);
			}
			// SUBJECTS
			set = Grades.connect().createStatement()
				.executeQuery("SELECT sub1, sub2, sub3, sub4 FROM events WHERE id = " + event_id + ";");
			
			set.next();
			
			subs = new String[] {set.getString("sub1"), set.getString("sub2"), set.getString("sub3"), set.getString("sub4")};
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void subjectFields(EmbedBuilder embed, int s, boolean sayname) {
		String content;
		if (counts[s] == 0)
			content = "Zero guesses.";
		else
			content = "mean: `" + String.format("%.2f", means[s]) + "`\n"
				+ "std: `" + String.format("%.2f", stds[s]) + "`\n"
				+ counts[s] + " guesses.";
		
		embed.addField("**" + (sayname ? subs[s] + " " : "") + "Guess**", content, true);
		content = "";
		if (counts[4+s] == 0)
			content = "Zero submitted grades.";
		else
			content = "mean: `" + String.format("%.2f", means[4+s]) + "`\n"
				+ "std: `" + String.format("%.2f", stds[4+s]) + "`\n"
				+ counts[4+s] + " submitted grades.";
		
		embed.addBlankField(true);
		embed.addField("**" + (sayname ? subs[s] + " " : "") + "Grade**", content, true);
	}

	@Override
	public void answer() {
		
		if (n < MIN_N) {
			Helper.error(e, c, args[0], 
				"Event " + event_name
				+ ": I won't return grades stats when querying less than "
				+ MIN_N + " people.", 20
			); return;
		}
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(Tomo.COLOR);
		embed.setTitle("Guess Stats for **"
			+ (subject != null ? subs[subject] + " in " : "")
			+ event_name + "**"
			+ (role_name != null ? ": *" + role_name + "*" : "")
		);
		
		if (subject == null) {
			embed.setDescription("In total " + n + " people have guessed.\n");
			for (int s = 0; s < 4; s++)
				subjectFields(embed, s, true);
			embed.addField("**All Guesses**", "mean: `" + String.format("%.2f", guessMean) + "`\n"
				+ guessCount + " total guesses.", true);
			embed.addField("**All Grades**", "mean: `" + String.format("%.2f", gradeMean) + "`\n"
				+ gradeCount + " total submitted grades.", true);
		} else {
			subjectFields(embed, subject, false);
		}

		c.sendMessage(embed.build()).queue();
		
	}

	@Override
	public String example(String alias) {
		return alias + " BP1";
	}

}
