package motonari.Grades;

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
		
		
		arg_str = "<append statistics.json>";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gp", "gradeprocess"
		}) );
		
		options = new HashMap<String, String>();

	}

	String file = null;
	JSONObject EthStatsJson = null;
	
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

	}

	@Override
	public void answer() {
		
		String msg = "";
		
		for (String key : EthStats.keySet()) {
			msg += key + ": " + EthStats.get(key).getOrDefault(e.getAuthor().getId(), -1L) + "\n";
		}
		
		c.sendMessage(msg).queue();
		
		
		
	}
	
	
	public String parse() {
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
		} catch (ParseException e) {
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
		// TODO Auto-generated method stub
		return null;
	}

}
