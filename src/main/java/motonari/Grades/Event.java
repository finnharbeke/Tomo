package motonari.Grades;

import java.sql.ResultSet;
import java.sql.SQLException;
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
		
		
		arg_str = "(new <name> <start> <end>) | (edit <name> <start> <end>)";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "ge", "guessevent"
		}) );
		
		options = new HashMap<String, String>();
	}
	
	boolean edit;
	String name;
	String start;
	String end;
	boolean success;

	@Override
	public String parse() {
		if (args.length < 5)
			return "Not enough arguments!";
		else if(args.length > 5)
			return "Too Many argument!";
		
		if (args[1].equals("edit")) {
			edit = true;
		} else if (args[1].equals("new")) {
			edit = false;
		} else {
			return "Invalid second argument (" + args[1] + ")!";
		}
		
		name = args[2];
		
		if (edit) {
			try {
				ResultSet set = Grades.conn.createStatement().executeQuery("SELECT * from events WHERE name = " + name + ";");
				if (!set.next())
					return "No Event called " + name + " found!";
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		String datetime = "(\\d{4}-[01]\\d-[0123]\\dT[012]\\d:[0-5]\\d)|now";
		if (!args[3].matches(datetime)) {
			return "Invalid start datetime (" + args[3] + ")!";
		} else {
			start = args[3];
		}
		if (!args[4].matches(datetime)) {
			return "Invalid end datetime (" + args[4] + ")!";
		} else {
			end = args[4];
		}
			
		return "OK";
	}

	@Override
	public void main() {
		if (edit) {
			success = Grades.editEvent(name, start, end);
		} else {
			success = Grades.newEvent(name, start, end);
		}

	}

	@Override
	public void answer() {
		String msg;
		if (success) {
			msg = "Successfully " + (edit ? "edited" : "added" ) + " Event!\n";
			try {
				ResultSet set = Grades.conn.createStatement().executeQuery("SELECT * from events WHERE name = " + name + ";");
				set.next();
				msg += set.getString("id") + "; " + set.getString("name") + ": from " + set.getString("start") + " until " + set.getString("end");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			msg = "Couldn't " + (edit ? "edit" : "add" ) + " Event!";
		}
		c.sendMessage(msg);

	}

	@Override
	public String example(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

}
