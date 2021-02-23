package motonari.Grades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import motonari.Commands.Command;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Guessers extends Command {
	public Guessers(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Guessers() {super();}

	@Override
	public void init() {
		admin = true;
		
		name = "Grade Guessers";
		cmd = "gguessers";
		desc = "Sends you participating people";
		
		
		arg_str = "<event_id>";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gradeguessers",
		}));
		
		options = new HashMap<String, String>();
		
	}
	
	int event_id;
	
	@Override
	public String parse() {
		if (!this.e.isFromType(ChannelType.PRIVATE)) {
			return "This command can only be used in Private Channels!";
		}
		
		if (args.length > 2)
			return "Too many arguments";
		else if (args.length < 2)
			return "Not enough arguments";

		try {
			event_id = Integer.valueOf(args[1]);
			ResultSet set = Grades.connect().createStatement().executeQuery("SELECT * FROM events WHERE id = " + event_id + ";");
			if (!set.next()) {
				return "No event with id " + event_id + "!";
			}
		} catch (NumberFormatException e) {
			return "<event_id> must be of Integer format!";
		} catch (SQLException e) {
			e.printStackTrace();
			return "SQLError!";
		}
		return "OK";
	}
	
	ArrayList<User> users;
	
	@Override
	public void main() {
		users = new ArrayList<User>();
		try {
			ResultSet set = Grades.connect().createStatement().executeQuery("SELECT user_id FROM grades WHERE event_id = " + event_id + ";");
			while (set.next()) {
				long user_id = set.getLong("user_id");
				users.add(Tomo.jda.retrieveUserById(user_id).complete());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	@Override
	public void answer() {
		
		String msg = users.size() + " participators:\n";
		for (User u : users) {
			msg += ((u == null) ? "null" : u.getAsMention()) + "\n";
		}
		
		c.sendMessage(msg).queue();
		
	}
	@Override
	public String example(String alias) {
		return "1";
	}
}
