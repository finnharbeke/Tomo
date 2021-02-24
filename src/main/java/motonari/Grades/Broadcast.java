package motonari.Grades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import motonari.Commands.Command;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Broadcast extends Command {
	public Broadcast(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Broadcast() {super();}

	@Override
	public void init() {
		admin = true;
		
		name = "GradeGuess Broadcast";
		cmd = "gbc";
		desc = "SQL into grades.db";
		
		
		arg_str = "<event_name> {reply to any message}";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gradebroadcast",
		}));
		
		options = new HashMap<String, String>();
	}
	
	String msg;
	String event_name;
	int event_id;

	@Override
	public String parse() {
		if (args.length > 2)
			return "Too many arguments!";
		else if (args.length < 2)
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
		
		Message ref = e.getMessage().getReferencedMessage();
		if (ref == null) {
			return "Command must be replying to a msg!";
		} else {
			msg = ref.getContentRaw();
		}
		return "OK";
	}
	
	ArrayList<User> users;

	@Override
	public void main() {
		users = new ArrayList<User>();
		try {
			ResultSet set = Grades.connect().createStatement().executeQuery(
				"SELECT user_id FROM grades WHERE event_id = " + event_id + ";"
			);
			
			while (set.next()) {
				users.add(Tomo.jda.retrieveUserById(set.getLong("user_id")).complete());
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void answer() {
		
		for (User u : users) {
			u.openPrivateChannel().queue(channel -> {
				channel.sendMessage(msg).queue();
			});
		}

	}

	@Override
	public String example(String alias) {
		return "BP1";
	}

}
