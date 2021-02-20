package motonari.Grades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import motonari.Commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Event extends Command {
	public Event(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Event() {super();}
	
	@Override
	public void init() {
		admin = true;
		
		name = "set/edit a Guessing Event";
		cmd = "gevent";
		desc = "set and edit a event for the guessing stuff.";
		
		
		arg_str = "(new | edit) <name> <start> <end> <sub1> <sub2> <sub3> <sub4>";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "ge", "guessevent"
		}) );
		
		options = new HashMap<String, String>();
	}
	
	boolean edit;
	String event_name;
	String start;
	String end;
	String[] subs;
	boolean success;

	@Override
	public String parse() {
		if (args.length < 9)
			return "Not enough arguments!";
		else if(args.length > 9)
			return "Too many arguments!";
		
		if (args[1].equals("edit")) {
			edit = true;
		} else if (args[1].equals("new")) {
			edit = false;
		} else {
			return "Invalid second argument (" + args[1] + ")!";
		}
		
		event_name = args[2];
		
		if (edit) {
			try {
				ResultSet set = Grades.connect().createStatement().executeQuery("SELECT * from events WHERE name = \"" + event_name + "\";");
				if (!set.next())
					return "No Event called " + event_name + " found!";
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		String datetime = "(\\d{4}-[01]\\d-[0123]\\dT[012]\\d:[0-5]\\d)";
		if (!args[3].matches(datetime+"|now")) {
			return "Invalid start datetime (" + args[3] + ")!";
		} else {
			start = args[3];
			if (start.equals("now")) {
				start = LocalDateTime.now()
			       .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
			}
		}
		if (!args[4].matches(datetime)) {
			return "Invalid end datetime (" + args[4] + ")!";
		} else {
			end = args[4];
		}
		
		start = start.replace('T', ' ');
		end = end.replace('T', ' ');
		
		subs = new String[] {args[5], args[6], args[7], args[8]};
		
		return "OK";
	}

	@Override
	public void main() {
		if (edit) {
			System.out.println(String.join(", ", subs));
			success = Grades.editEvent(event_name, start, end, subs);
		} else {
			success = Grades.newEvent(event_name, start, end, subs);
		}

	}

	@Override
	public void answer() {
		String msg;
		if (success) {
			msg = "Successfully " + (edit ? "edited" : "added" ) + " Event!\n";
			try {
				ResultSet set = Grades.connect().createStatement().executeQuery("SELECT * from events WHERE name = \"" + event_name + "\";");
				set.next();
				msg += set.getString("id") + "; " + set.getString("name") + ": from " + set.getString("start") + " until " + set.getString("end");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			msg = "Couldn't " + (edit ? "edit" : "add" ) + " Event!";
		}
		c.sendMessage(msg).queue();

	}

	@Override
	public String example(String alias) {
		return "Nope";
	}

}
