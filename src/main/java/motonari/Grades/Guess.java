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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class Guess extends Command {
	public Guess(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Guess() {super();}
	
	@Override
	public void init() {
		name = "Guess your Grades";
		cmd = "gguess";
		desc = "Guess your Grades of AnD, EProg, DM and LinAlg";
		
		
		arg_str = "((ad | dm | ep | la) <grade>){1,4}";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gg", "gradeguess"
		}) );
		
		options = new HashMap<String, String>();
		
		
	}
	
	Double ad = null;
	Double dm = null;
	Double ep = null;
	Double la = null;
	int event_id;

	public void main() {
		
		
		long id = e.getAuthor().getIdLong();
		try {
			
			Grades.put(Grades.conn, event_id, id, ad, dm, ep, la, null, null, null, null, "");
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		}
		
	}

	public void answer() {
		
		long id = e.getAuthor().getIdLong();
		
		Double ad_guess, dm_guess, ep_guess, la_guess;
		
		User u;
		try {
			
			String sql = "SELECT * FROM grades WHERE id = ?;";
					
			PreparedStatement pstmt = Grades.conn.prepareStatement(sql);
			pstmt.setLong(1, id);
			
			ResultSet query = pstmt.executeQuery();
			
			query.next();
			
			u = User.fromId(query.getLong("id"));
			ad_guess = query.getDouble("ad_guess");
			dm_guess = query.getDouble("dm_guess");
			ep_guess = query.getDouble("ep_guess");
			la_guess = query.getDouble("la_guess");
			
		} catch (SQLException e) {
			e.printStackTrace(System.err);
			return;
		}
		
		String msg = u.getAsMention() + "Your current grade guesses are: ";
		String last = "";
		if (ad_guess != 0.0) {
			last = "**AnD:** *" + ad_guess + "*";
		}
		if (dm_guess != 0.0) {
			if (!last.equals(""))
				msg += last + ", ";
			last = "**DiskMath:** *" + dm_guess + "*";
		}
		if (ep_guess != 0.0) {
			if (!last.equals(""))
				msg += last + ", ";
			last = "**EProg:** *" + ep_guess + "*";
		}
		if (la_guess != 0.0) {
			if (!last.equals(""))
				msg += last + ", ";
			last = "**LinAlg:** *" + la_guess + "*";
		}
		msg += " and " + last + ".";
		
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
		
		HashSet<String> exams = new HashSet<String>(Arrays.asList(Grades.exams));
		HashSet<String> left = new HashSet<String>(Arrays.asList(Grades.exams));
		
		event_id = Grades.currentEvent();
		if (event_id == -1) {
			return "No current Guessing event!";
		}
		
		if (args.length < 3) {
			return "Not enough arguments!";
		}
		
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
			
			if (ex.equals(Grades.ad)) {
				ad = grade;
			} else if (ex.equals(Grades.dm)) {
				dm = grade;
			} else if (ex.equals(Grades.ep)) {
				ep = grade;
			} else if (ex.equals(Grades.la)) {
				la = grade;
			} else {
				return "Invalid Exam String, should be impossible";
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
