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
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Personal extends Command {
	public Personal(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Personal() {super();}
	
	@Override
	public void init() {
		name = "Personal Overview";
		cmd = "gpersonal";
		desc = "See your guesses, grades and points for all events. only in my dms";
		
		
		arg_str = "";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gp", "gradepersonal",
		}));
		
		options = new HashMap<String, String>();
	}

	@Override
	public String parse() {
		if (!this.e.isFromType(ChannelType.PRIVATE)) {
			return "This command can only be used in Private Channels!";
		}
		if (args.length > 1)
			return "Too many arguments!";
		return "OK";
	}
	
	User user;
	ArrayList<String> events;
	ArrayList<Boolean> ev_current;
	ArrayList<Boolean> ev_confirmed;
	ArrayList<String[]> ev_subjects;
	ArrayList<Double[]> ev_guesses;
	ArrayList<Double[]> ev_grades;
	
	@Override
	public void main() {
		events = new ArrayList<String>();
		ev_subjects = new ArrayList<String[]>();
		ev_guesses = new ArrayList<Double[]>();
		ev_grades = new ArrayList<Double[]>();
		ev_current = new ArrayList<Boolean>();
		ev_confirmed = new ArrayList<Boolean>();
		user = e.getAuthor();
		
		try {
			ResultSet set = Grades.connect().createStatement().executeQuery(
				"SELECT *, (datetime('now', 'localtime') BETWEEN start AND end) FROM grades JOIN events ON grades.event_id = events.id WHERE user_id = " + user.getIdLong() + ";"
			);
			
			while (set.next()) {
				
				events.add(set.getString("name"));
				
				ev_subjects.add(new String[] {
					set.getString("sub1"), set.getString("sub2"),
					set.getString("sub3"), set.getString("sub4")
				});
				
				Double[] guesses = new Double[] {
					set.getDouble("guess1"), set.getDouble("guess2"),
					set.getDouble("guess3"), set.getDouble("guess4")
				};
				
				Double[] grades = new Double[] {
					set.getDouble("grade1"), set.getDouble("grade2"),
					set.getDouble("grade3"), set.getDouble("grade4")
				};
				
				for (int j = 0; j < 4; j++) {
					if (guesses[j].equals(0.0))
						guesses[j] = null;
					if (grades[j].equals(0.0))
						grades[j] = null;
				}
				
				ev_guesses.add(guesses);
				ev_grades.add(grades);
				
				ev_current.add(set.getBoolean("(datetime('now', 'localtime') BETWEEN start AND end)"));
				ev_confirmed.add(set.getBoolean("confirmed"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void answer() {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("Grade Guessing Personal Info");
		embed.setAuthor(user.getName(), null, user.getAvatarUrl());
		embed.setColor(Tomo.COLOR);
		
		int max_sub_len = Integer.MIN_VALUE;
		for (String[] subs : ev_subjects) {
			for (String s : subs) {
				if (s.length() > max_sub_len)
					max_sub_len = s.length();
			}
		}
		
		for (int i = 0; i < events.size(); i++) {
			String[] subs = ev_subjects.get(i);
			Double[] guesses = ev_guesses.get(i);
			Double[] grades = ev_grades.get(i);
			
			String content = "```md\n";
			
			content += "#";
			for (int k = 0; k < max_sub_len + 2; k++)
				content += " ";
			
			content += "| guess";
			if (!ev_current.get(i))
				content += " | grade";
			if (ev_confirmed.get(i))
				content += " | diff  | points";
			content += "\n";
			
			for (int j = 0; j < 4; j++) {
				content += "<" + subs[j] + ">";
				for (int k = 0; k < max_sub_len - subs[j].length(); k++)
					content += " ";
				content += " | ";
				content += guesses[j] == null ? "-   " : String.format("%04.2f", guesses[j]);
				if (ev_current.get(i)) {
					content += "\n";
					continue;
				}
				
				content += "  | ";
				content += grades[j] == null ? "-   " : String.format("%04.2f", grades[j]);
				if (!ev_confirmed.get(i)) {
					content += "\n";
					continue;
				}
					
				content += "  | ";
				
				Double diff = (guesses[j] != null && grades[j] != null) ? guesses[j] - grades[j] : null;
				
				if (diff != null && diff >= 0) content += "+";
				else if (diff != null && diff < 0) content += "-";
				
				content += diff == null ? "-    " : String.format("%04.2f", Math.abs(diff));
				
				content += " | ";
				
				content += (int)(diff != null ? 500 - Math.abs(diff) * 100 : 0);
				
				content += "\n";
			}
			content += "```";
			
			embed.addField(events.get(i), content, false);
		}
		
		if (events.size() == 0) {
			embed.setDescription("You haven't partaken in any Guessing Event.");
		}
		
		c.sendMessage(embed.build()).queue();
	}

	@Override
	public String example(String alias) {
		return "";
	}

}
