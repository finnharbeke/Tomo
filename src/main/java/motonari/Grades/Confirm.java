package motonari.Grades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import motonari.Commands.Command;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Confirm extends Command {
	public Confirm(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Confirm() {super();}

	@Override
	public void init() {
		name = "Confirm Grade Submission";
		cmd = "gconfirm";
		desc = "Confirm the submitted *actual* grades, this locks them and calculates points, includes you in rankings etc. only in my dms";
		
		
		arg_str = "<name>";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gc", "gradeconfirm"
		}));
		
		options = new HashMap<String, String>();

	}
	
	String event_name;
	int event_id;

	@Override
	public String parse() {
		if (!this.e.isFromType(ChannelType.PRIVATE)) {
			return "This command can only be used in Private Channels!";
		}
		if (args.length > 2)
			return "Too many arguments!";
		else if (args.length < 2)
			return "Not enough arguments!";
		
		event_name = args[1];
		try {
			ResultSet set = Grades.connect().createStatement()
				.executeQuery("SELECT * from events WHERE name = \"" + event_name + "\";");
			if (!set.next())
				return "No Event called " + event_name + " found!";
			else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				LocalDateTime end = LocalDateTime.parse(set.getString("end"), formatter);
				if (LocalDateTime.now().isBefore(end))
					return "Wait until the Event's Guessing Time has ended.";
				event_id = set.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "SQLException!";
		}
		
		try {
			ResultSet set = Grades.connect().createStatement()
				.executeQuery("SELECT confirmed from grades WHERE event_id = " + event_id + " AND user_id = " + e.getAuthor().getIdLong() + ";");
			if (!set.next())
				return "You missed participating in Event '" + event_name + "'!";
			else if (set.getBoolean("confirmed")) {
				return "Already confirmed!";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "SQLException!";
		}
		
		return "OK";
	}

	boolean success;
	
	@Override
	public void main() {
		try {
			ResultSet set = Grades.connect().createStatement().executeQuery(
				"SELECT * FROM grades WHERE user_id = " + e.getAuthor().getIdLong() + " AND event_id = " + event_id + ";"
			);
			int points = 0;
			for (int i = 1; i < 5; i++) {
				Double diff;
				double guess = set.getDouble("guess" + i);
				double grade = set.getDouble("grade" + i);
				if (guess == 0.0 || grade == 0.0) {
					diff = null;
				} else {
					diff = grade - guess;
				}
				points += (int)(diff != null ? 500 - Math.abs(diff) * 100 : 0);
			}
			
			Grades.connect().createStatement().execute(
				"UPDATE grades SET confirmed = 1, points = " + points + " WHERE user_id = " + e.getAuthor().getIdLong() + " AND event_id = " + event_id + ";"
			);
			
			success = true;
		} catch (SQLException e) {
			e.printStackTrace();
			success = false;
		}

	}

	@Override
	public void answer() {
		if (success) {
			c.sendMessage("Confirmed Grades for **" + event_name + "**, type `&gpersonal` to see you points!").queue();
		} else {
			c.sendMessage("SQLError! shouldn't happen?").queue();
		}

	}

	@Override
	public String example(String alias) {
		return "BP1";
	}

}
