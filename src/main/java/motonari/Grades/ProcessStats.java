package motonari.Grades;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import motonari.Commands.Command;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ProcessStats extends Command {
	public ProcessStats(MessageReceivedEvent e, String[] args) {super(e, args);}
	public ProcessStats() {super();}

	@Override
	public void init() {
		admin = true;
		
		name = "Process a statistics.json";
		cmd = "gprocess";
		desc = "Process a statistics.json from Mark and his Lecturfier.";
		
		
		arg_str = "<event_id> <append statistics.json>";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gradeprocess"
		}) );
		
		options = new HashMap<String, String>();

	}

	String file = null;
	JSONObject EthStatsJson = null;
	
	int inserted = 0;
	int event_id;
	
	HashMap<String, HashMap<String, Long>> EthStats;

	@Override
	public void main() {
		
		EthStats = new HashMap<String, HashMap<String, Long>>();
		
		for (Object obj : EthStatsJson.keySet()) {
			String key;
			try {
				key = (String)obj;
			} catch (ClassCastException e) {
				System.out.println(obj);
				e.printStackTrace();
				continue;
			}
			
			EthStats.put(key, new HashMap<String, Long>());
			
			JSONObject data;
			try {
				data = (JSONObject)EthStatsJson.get(key);
			} catch (ClassCastException e) {
				e.printStackTrace();
				continue;
			}
			
			for (Object obj2 : data.keySet()) {
				String id;
				try {
					id = (String)obj2;
				} catch (ClassCastException e) {
					System.out.println(obj2);
					e.printStackTrace();
					continue;
				}
				
				long num;
				try {
					num = (Long)data.get(id);
				} catch (ClassCastException e) {
					System.out.println(data.get(id));
					e.printStackTrace();
					continue;
				}
				
				EthStats.get(key).put(id, num);
			}
			
		}
		
		
		HashMap<String, HashMap<String, Long>> StatsByUser = new HashMap <String, HashMap<String, Long>>();
		
		for (String stat : EthStats.keySet()) {
			
			for (String user_id : EthStats.get(stat).keySet()) {
				
				if (!StatsByUser.containsKey(user_id)) {
					
					StatsByUser.put(user_id, new HashMap<String, Long>());
					
				}
				
				StatsByUser.get(user_id).put(stat, EthStats.get(stat).get(user_id));
				
			}
			
		}
		PreparedStatement pstmt;
		try {
			pstmt = Grades.connect().prepareStatement(
				"INSERT INTO stats(user_id, event_id, messages_sent, "
				+ "words_sent, msgs_during_lecture, "
				+ "reactions_added, reactions_received) "
				+ "VALUES(?, ?, ?, ?, ?, ?, ?);"
				);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		for (String user_id : StatsByUser.keySet()) {
			
			try {
				pstmt.setLong(1, Long.valueOf(user_id));
				pstmt.setInt(2, event_id);
				pstmt.setLong(3, StatsByUser.get(user_id).getOrDefault("messages_sent", -1L));
				pstmt.setLong(4, StatsByUser.get(user_id).getOrDefault("words_sent", -1L));
				pstmt.setLong(5, StatsByUser.get(user_id).getOrDefault("msgs_during_lecture", -1L));
				pstmt.setLong(6, StatsByUser.get(user_id).getOrDefault("reactions_added", -1L));
				pstmt.setLong(7, StatsByUser.get(user_id).getOrDefault("reactions_received", -1L));
				
				pstmt.executeUpdate();
				
				inserted++;
				
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}

	}

	@Override
	public void answer() {
		c.sendMessage("Inserted " + inserted + " stats entries for event " + event_id + "!").queue();
	}
	
	
	public String parse() {
		if (args.length < 2) {
			return "Not enough arguments!";
		} else if (args.length > 2) {
			return "Too many arguments!";
		}
		
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
		
		List<Attachment> l = e.getMessage().getAttachments();
		
		if (l.size() < 1) {
			return "Message is missing Attachment!";
		} else if (l.size() > 1) {
			return "Message has too many Attachments!";
		}
		
		Attachment att = l.get(0);
		
		if (!att.getFileExtension().equals("json")) {
			return "Attachment must be a .json file!";
		}
		
		att.retrieveInputStream().thenAccept(in -> {
			Scanner s = new Scanner(in).useDelimiter("\\A");
			file = s.hasNext() ? s.next() : "";
			s.close();
	    });
		
		while (file == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace(System.err);
			}
		}
		
		JSONParser parser = new JSONParser();
		JSONObject data;
		try {
			Object obj = parser.parse(file);
			data = (JSONObject)obj;
		} catch (ParseException | ClassCastException e) {
			e.printStackTrace();
			return "Couldn't parse the JSON file!";
		}
		
		boolean found = false;
		for (Object obj : data.keySet()) {
			//System.out.println(obj);
			String key;
			try {
				key = (String)obj;
			} catch (ClassCastException e) {
				e.printStackTrace();
				continue;
			}
			//System.out.println(key);
			if (key.equals(String.valueOf(Grades.Eth_id))) {
				found = true;
				break;
			}
		}
		if (!found) {
			return "JSON must have a key \"" + Grades.Eth_id + "\" (ETH D-INFK Guild ID)!";
		}
		EthStatsJson = (JSONObject) data.get(String.valueOf(Grades.Eth_id));
		
		return "OK";
	}

	@Override
	public String example(String alias) {
		return null;
	}

}
