package motonari.Grades;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import motonari.Commands.Command;
import motonari.Tomo.Helper;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class Submit extends Command {
	public Submit(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Submit() {super();}
	
	@Override
	public void init() {
		name = "Submit your Grades";
		cmd = "gsubmit";
		desc = "Submit your actual Grades for an Event in a direct message to me.";
		
		
		arg_str = "<event_name> (<subject> <grade>){0,4}";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gs", "gradesubmit",
		}));
		
		options = new HashMap<String, String>();
	}
	
	int event_id;
	String event_name;
	long user_id;
	String[] subs = {null, null, null, null};
	Double[] grades = {null, null, null, null};

	@Override
	public String parse() {
		user_id = e.getAuthor().getIdLong();
		
		if (!this.e.isFromType(ChannelType.PRIVATE)) {
			return "This command can only be used in Private Channels!";
		}
		
		Guild eth = Tomo.jda.getGuildById(Grades.Eth_id);
		
		try {
			eth.retrieveMember(e.getAuthor()).complete();
		}
		catch (ErrorResponseException err) {
			return "I'm sorry you're not in the Guild required for this command!";
		}
		
		if (args.length < 2) return "Not enough arguments!";
		
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
				subs[0] = set.getString("sub1");
				subs[1] = set.getString("sub2");
				subs[2] = set.getString("sub3");
				subs[3] = set.getString("sub4");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "SQLException!";
		}
		
		try {
			ResultSet set = Grades.connect().createStatement().executeQuery(
				"SELECT confirmed FROM grades WHERE event_id = " + event_id + " AND user_id = " + user_id + ";"
			);
			if (!set.next()) {
				return "You didn't guess for this event!";
			}
			if (set.getBoolean("confirmed")) {
				return "You already confirmed your submission, now you can't change it anymore!";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "SQLException!";
		}
		
		HashSet<String> left = new HashSet<String>(Arrays.asList(subs));
		HashSet<String> exams = new HashSet<String>(Arrays.asList(subs));
		
		int i = 2;
		while (i < args.length) {
			if (args[i].startsWith("-")) {
				break;
			}
			
			if (args.length < i + 2) {
				return "Not enough arguments!";
			}
			
			String ex = args[i++];
			if (!exams.contains(ex)) {
				return "Invalid Exam Abbreviation '" + ex + "'!";
			} else if (!left.contains(ex)) {
				return "You're submitting twice for '" + ex + "'!";
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
	public void main() {
		String sql = "UPDATE grades SET ";
		
		boolean grade_before = false;
		for (int i = 0; i < grades.length; i++) {
			if (grades[i] != null) {
				if (grade_before)
					sql += ", ";
				sql += "grade" + (i+1) + " = ?";
				grade_before = true;
			}
		}
		sql += " WHERE event_id = ? AND user_id = ?;";
		
		// Nothing to update
		if (!grade_before)
			return;
		
		
		try {
			PreparedStatement pstmt = Grades.connect().prepareStatement(sql);
	        
	        int i = 1;
	        if (grades[0] != null)
	        	pstmt.setDouble(i++, grades[0]);
	        if (grades[1] != null)
	        	pstmt.setDouble(i++, grades[1]);
	        if (grades[2] != null)
	        	pstmt.setDouble(i++, grades[2]);
	        if (grades[3] != null)
	        	pstmt.setDouble(i++, grades[3]);
	        
	        pstmt.setInt(i++, event_id);
	        pstmt.setLong(i++, user_id);
	        pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
	}

	@Override
	public void answer() {
		Double grade1, grade2, grade3, grade4;
		
		String event;
		try {
			
			String sql = "SELECT * FROM grades WHERE user_id = ? AND event_id = ?;";
					
			PreparedStatement pstmt = Grades.connect().prepareStatement(sql);
			pstmt.setLong(1, user_id);
			pstmt.setInt(2, event_id);
			
			ResultSet query = pstmt.executeQuery();
			
			query.next();
			
			int ev = query.getInt("event_id");
			grade1 = query.getDouble("grade1");
			grade2 = query.getDouble("grade2");
			grade3 = query.getDouble("grade3");
			grade4 = query.getDouble("grade4");
			
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
		if (grade1 != 0.0) {
			last = "**" + subs[0] + ":** *" + grade1 + "*";
		}
		if (grade2 != 0.0) {
			if (!last.equals(""))
				msg += (msg.equals("") ? "" : ", ") + last;
			last = "**" + subs[1] + ":** *" + grade2 + "*";
		}
		if (grade3 != 0.0) {
			if (!last.equals(""))
				msg += (msg.equals("") ? "" : ", ") + last;
			last = "**" + subs[2] + ":** *" + grade3 + "*";
		}
		if (grade4 != 0.0) {
			if (!last.equals(""))
				msg += (msg.equals("") ? "" : ", ") + last;
			last = "**" + subs[3] + ":** *" + grade4 + "*";
		}
		msg += (msg.equals("") ? "" : " and ") + last + ".";
		
		if (!msg.equals("."))
			msg = "Your currently submitted grades for **" + event + "** are: " + msg;
		else
			msg = "You have no grades submitted for **" + event + "** yet!";
		
		c.sendMessage(msg).queue();

	}

	@Override
	public String example(String alias) {
		return "BP1 dm 4.0";
	}

}
