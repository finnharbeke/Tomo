package motonari.Grades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
	
	ArrayList<String> msgs;

	@Override
	public void main() {
		msgs = new ArrayList<String>();
		try {
			Statement stmt = Grades.connect().createStatement();
			stmt.execute(sql);
			
			ResultSet set = stmt.getResultSet();
			int updatecount = stmt.getUpdateCount();
			if (set != null) {
				String pre = "```\n";
				String suf = "```";
				String msg = pre;
				while (set.next()) {
					int i = 1;
					String row = "";
					while (true) {
						try {
							row += set.getObject(i) + " | ";
						} catch (SQLException e) {
							break;
						}
						i++;
					}
					row += "\n";
					
					if (msg.length() + row.length() + suf.length() > Message.MAX_CONTENT_LENGTH) {
						msg += suf;
						msgs.add(msg);
						msg = pre + row;
					} else {
						msg += row;
					}
					
				}
				msgs.add(msg + suf);
			} else if (updatecount != -1) {
				msgs.add("Updated " + updatecount + " Rows!");
			} else {
				msgs.add("No return!");
			}
		} catch (SQLException e) {
			msgs.add(e.toString());
		}

	}

	@Override
	public void answer() {
		for (String m : msgs) {
			c.sendMessage(m).queue();
		}

	}

	@Override
	public String example(String alias) {
		return "";
	}

}
