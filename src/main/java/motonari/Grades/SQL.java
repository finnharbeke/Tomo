package motonari.Grades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import motonari.Commands.Command;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SQL extends Command {
	public SQL(MessageReceivedEvent e, String[] args) {super(e, args);}
	public SQL() {super();}

	@Override
	public void init() {
		admin = true;
		
		name = "grades.db SQL";
		cmd = "gsql";
		desc = "SQL into grades.db";
		
		
		arg_str = "{reply to ```sql\n<content>\n``` message}";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gradeSQL",
		}));
		
		options = new HashMap<String, String>();
	}
	
	String sql = null;

	@Override
	public String parse() {
		if (args.length > 1) {
			return "Too many arguments!";
		}
		Message ref = e.getMessage().getReferencedMessage();
		if (ref == null) {
			return "Command must be replying to a msg!";
		}
		String pre = "```sql\n";
		String suf = "\n```";
		String c = ref.getContentRaw();
		if (!c.matches(pre + "[\\d\\D]+" + suf)) {
			return "Referenced Message must be of format ```\\`\\`\\`sql\ncontent\n\\`\\`\\`\n```";
		}
		
		sql = c.substring(pre.length(), c.length()-suf.length()).strip();
		
		return "OK";
	}
	
	String msg;

	@Override
	public void main() {
		try {
			Statement stmt = Grades.connect().createStatement();
			stmt.execute(sql);
			
			ResultSet set = stmt.getResultSet();
			int updatecount = stmt.getUpdateCount();
			if (set != null) {
				msg = "```\n";
				while (set.next()) {
					int i = 1;
					while (true) {
						try {
							msg += set.getObject(i) + " | ";
						} catch (SQLException e) {
							break;
						}
						i++;
					}
					msg += "\n";
				}
				msg += "```";
			} else if (updatecount != -1) {
				msg = "Updated " + updatecount + " Rows!";
			} else {
				msg = "No return!";
			}
		} catch (SQLException e) {
			msg = e.getMessage();
		}

	}

	@Override
	public void answer() {
		c.sendMessage(msg).queue();

	}

	@Override
	public String example(String alias) {
		return "";
	}

}
