package motonari.Grades;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import motonari.Commands.Command;
import motonari.Tomo.Helper;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class Guess extends Command {
	public Guess(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Guess() {super();}
	
	@Override
	public void init() {
		name = "Guess your Grades";
		cmd = "gguess";
		desc = "Guess your Grades for current Event in a direct message to me.";
		
		
		arg_str = "(<subject> <grade>){0,4}";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gg", "gradeguess"
		}) );
		
		options = new HashMap<String, String>();
		
		
	}
	
	int event_id;
	String[] subs = {null, null, null, null};
	Double[] grades = {null, null, null, null};

	public void main() {
		
		long id = e.getAuthor().getIdLong();
		
		Guild eth = Tomo.jda.getGuildById(Grades.Eth_id);
		
		Member mem;
		try {
			mem = eth.retrieveMember(e.getAuthor()).complete();
		}
		catch (ErrorResponseException err) {
			err.printStackTrace(); // should not be possible
			return;
		}
		
		String tags = "";
		for (Role r : mem.getRoles()) {
			tags += r.getId() + ";";
		}
		tags = tags.substring(0, tags.length()-1);
		
		try {
			Grades.put(Grades.connect(), event_id, id, grades[0], grades[1], grades[2], grades[3], null, null, null, null, tags);
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		}
		
	}

	public void answer() {
		
		long id = e.getAuthor().getIdLong();
		
		Double guess1, guess2, guess3, guess4;
		
		String event;
		try {
			
			String sql = "SELECT * FROM grades WHERE user_id = ? AND event_id = ?;";
					
			PreparedStatement pstmt = Grades.connect().prepareStatement(sql);
			pstmt.setLong(1, id);
			pstmt.setInt(2, event_id);
			
			ResultSet query = pstmt.executeQuery();
			
			query.next();
			
			int ev = query.getInt("event_id");
			guess1 = query.getDouble("guess1");
			guess2 = query.getDouble("guess2");
			guess3 = query.getDouble("guess3");
			guess4 = query.getDouble("guess4");
			
			pstmt = Grades.connect().prepareStatement("SELECT name FROM events WHERE id = ?;");
			pstmt.setInt(1, ev);
			
			query = pstmt.executeQuery();
			
			query.next();
			event = query.getString("name");
			
		} catch (SQLException e) {
			e.printStackTrace(System.err);
			return;
		}
		
		String msg = "";
		String last = "";
		if (guess1 != 0.0) {
			last = "**" + subs[0] + ":** *" + guess1 + "*";
		}
		if (guess2 != 0.0) {
			if (!last.equals(""))
				msg += (msg.equals("") ? "" : ", ") + last;
			last = "**" + subs[1] + ":** *" + guess2 + "*";
		}
		if (guess3 != 0.0) {
			if (!last.equals(""))
				msg += (msg.equals("") ? "" : ", ") + last;
			last = "**" + subs[2] + ":** *" + guess3 + "*";
		}
		if (guess4 != 0.0) {
			if (!last.equals(""))
				msg += (msg.equals("") ? "" : ", ") + last;
			last = "**" + subs[3] + ":** *" + guess4 + "*";
		}
		msg += (msg.equals("") ? "" : " and ") + last + ".";
		
		if (!msg.equals("."))
			msg = "Your current grade guesses for **" + event + "** are: " + msg;
		else
			msg = "You have no current grade guesses for **" + event + "** yet!";
		
		c.sendMessage(msg).queue();
		
	}
	
	public String parse() {
		
		if (!this.e.isFromType(ChannelType.PRIVATE)) {
			return "This command can only be used in Private Channels!";
		}
		
		Guild eth = Tomo.jda.getGuildById(Grades.Eth_id);
		
		Member mem;
		try {
			mem = eth.retrieveMember(e.getAuthor()).complete();
		}
		catch (ErrorResponseException err) {
			return "I'm sorry you're not in the Guild required for this command!";
		}
		
		//System.out.println(eth + " " + e.getAuthor() + " " + mem);
		
		if (!(mem.getRoles().contains(eth.getRoleById(Grades.sem1_id)) ||
			mem.getRoles().contains(eth.getRoleById(Grades.sem2_id)))) {
			return "I'm sorry you need 1. or 2. semester role for using this command!";
		}
		
		
		event_id = Grades.currentEvent();
		if (event_id == -1) {
			return "No current Guessing event!";
		}
		
		try {
			ResultSet set = Grades.connect().createStatement().executeQuery("SELECT * FROM events WHERE id = " + event_id + ";");
			set.next();
			subs[0] = set.getString("sub1");
			subs[1] = set.getString("sub2");
			subs[2] = set.getString("sub3");
			subs[3] = set.getString("sub4");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		HashSet<String> left = new HashSet<String>(Arrays.asList(subs));
		HashSet<String> exams = new HashSet<String>(Arrays.asList(subs));
		
		int i = 1;
		while (i < args.length) {
			if (args[i].startsWith("-")) {
				break;
			}
			
			if (args.length < i + 2) {
				return "Not enough arguments!";
			}
			
			String ex = args[i++];
			if (!exams.contains(ex)) {
				return "Invalid Exam Abbreviation!";
			} else if (!left.contains(ex)) {
				return "You're guessing twice for '" + ex + "'!";
			} else {
				left.remove(ex);
			}
			String gr_s = args[i++];
			if (!gr_s.matches("\\d(\\.\\d{1,2})?")) {
				return "A grade ('" + ex + "': " + gr_s + ") must be a one-digit number with at most two decimals!";
			}
			double grade = Double.valueOf(gr_s);
			if (!(Grades.min_grade <= grade && Grades.max_grade >= grade)) {
				return "Your '" + ex + "' grade (" + grade + ") is out of bounds!";
			}
			
			for (int j = 0; j < subs.length; j++) {
				if (ex.equals(subs[j])) {
					grades[j] = grade;
					break;
				}
			}
		}
		
		String err = Helper.checkOptions(args, i, options);
		if (!err.equals("OK")) {
			return err;
		}
		
		myOpts = Helper.options(args, i, options);
		
		return "OK";
		
	}

	@Override
	public String example(String alias) {
		return alias + "dm 6.0";
	}

}
