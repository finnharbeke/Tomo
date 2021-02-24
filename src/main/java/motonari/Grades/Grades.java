package motonari.Grades;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import motonari.Commands.Command;
import motonari.Tomo.Helper;
import motonari.Tomo.Tomo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Grades extends Command {
	public Grades(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Grades() {super();}
	
	@Override
	public void init() {
		name = "Grades Info";
		cmd = "grades";
		desc = "Info about Grades and Guessing Events";
		
		
		arg_str = "";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "g"
		}) );
		
		options = new HashMap<String, String>();
		
	}
	
	EmbedBuilder embed;

	public void main() {
		
		embed = new EmbedBuilder();
		embed.setColor(Tomo.COLOR);
		//embed.setAuthor("the grade guessing game");
		
		int event_id = currentEvent();
		if (event_id != -1) {
			String ev_name;
			String end;
			String[] subs;
			try {
				ResultSet set = connect().createStatement().executeQuery("SELECT * FROM events WHERE id = " + event_id + ";");
				set.next();
				ev_name = set.getString("name");
				end = set.getString("end");
				subs = new String[] {set.getString("sub1"), set.getString("sub2"), set.getString("sub3"), set.getString("sub4")};
			} catch (SQLException e) {
				e.printStackTrace();
				// impossible?
				return;
			}
			
			embed.setTitle("Current Grade Guessing Event: **" + ev_name + "**");
			String desc = "Guess until `" + end + "`!\n"
					+ "Subjects: `" + String.join("`, `", subs) + "`.";
			embed.setDescription(desc);
			
			String footer = this.e.isFromType(ChannelType.PRIVATE) ? "" : "Slide in my DMs and ";
			
			footer += "type \"&gg <subject> <grade>\" to start guessing!";
			
			embed.setFooter(footer);
			
		} else {
			embed.setTitle("Grades Info");
			
			embed.setDescription("Here you see current Guessing Events, "
					+ "the timespan in which one can guess for them, "
					+ "if they're closed, active or to be opened and their subjects.");
			
			String events = "";
			
			try {
				ResultSet set = conn.createStatement().executeQuery("SELECT * FROM events ORDER BY start DESC;");
				while (set.next()) {
					int id = set.getInt("id");
					String name = set.getString("name");
					String start = set.getString("start");
					String end = set.getString("end");
					String[] subs = {set.getString("sub1"), set.getString("sub2"), set.getString("sub3"), set.getString("sub4")};
					
					if (byAdmin()) {
						events += id + " ";
					}
					
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
					LocalDateTime stdt = LocalDateTime.parse(start, formatter);
					LocalDateTime endt = LocalDateTime.parse(end, formatter);
					
					events += "`" + start + " - " + end + "` ";
					if (LocalDateTime.now().isBefore(stdt)) {
						events += "*" + name + "*: yet to come";
					} else if (LocalDateTime.now().isAfter(stdt) && LocalDateTime.now().isBefore(endt)) {
						events +=  "**" + name + "**: active";
					} else {
						events += "*" + name + "*: closed";
					}
					events += "; " + String.join(", ", subs) + ".\n";
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			embed.addField("Events", events, false);
			
			String commands = "type `" + Tomo.prefix + "help <cmd>` for more info:\n\n";
			
			for (Class<? extends Command> clazz : Tomo.commands.get("Grades")) {
				try {
					Command g_cmd = clazz.getConstructor().newInstance();
					if (!g_cmd.admin)
						commands += "`" + Tomo.prefix + g_cmd.cmd + "`: " + g_cmd.desc + "\n";
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}
			
			embed.addField("Commands", commands, false);
		}
		
		
	}

	public void answer() {
		c.sendMessage(embed.build()).queue();
		embed.clear();
	}
	
	public String parse() {
		
		if (args.length > 1) {
			return "Too many arguments";
		}
		
		String err = Helper.checkOptions(args, 1, options);
		if (!err.equals("OK")) {
			return err;
		}
		
		myOpts = Helper.options(args, 1, options);
		
		return "OK";
		
	}

	@Override
	public String example(String alias) {
		return null;
	}
	
	
	
	static final double min_grade = 1.0;
	static final double max_grade = 6.0;
	
	static final long Eth_id = 747752542741725244L;
	static final long sem1_id = 773543051011555398L;
	static final long sem2_id = 772552854832807938L;
	
	private static Connection conn = null;
	
	public static Connection connect() throws SQLException {
		
		if (conn != null)
			return conn;
		
		// SQLite connection string
        String url = "jdbc:sqlite:grades.db";
        
        conn = DriverManager.getConnection(url);
        
        return conn;
	}
	
	public static void buildDB() {
		String sql1 = "CREATE TABLE IF NOT EXISTS stats (\n"
                + "	user_id BIGINT,\n"
                + "	event_id INTEGER,\n"
                + "	messages_sent INTEGER,\n"
                + "	words_sent INTEGER,\n"
                + "	msgs_during_lecture INTEGER,\n"
                + "	reactions_added INTEGER,\n"
                + "	reactions_received INTEGER,\n"
                + " PRIMARY KEY (event_id, user_id),\n"
                + " FOREIGN KEY(event_id) REFERENCES events(id)"
                + ");";
		String sql2 = "CREATE TABLE IF NOT EXISTS events (\n"
				+ " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
				+ "	name TEXT UNIQUE,\n"
				+ "	start DATETIME,\n"
				+ "	end DATETIME,\n"
				+ "	sub1 VARCHAR(16),\n"
				+ "	sub2 VARCHAR(16),\n"
				+ "	sub3 VARCHAR(16),\n"
				+ "	sub4 VARCHAR(16)\n"
				+ ");";
		String sql3 = "CREATE TABLE IF NOT EXISTS grades (\n"
				+ " event_id INTEGER NON NULL,\n"
                + "	user_id BIGINT NON NULL,\n"
                + "	guess1 FLOAT,\n"
                + "	guess2 FLOAT,\n"
                + "	guess3 FLOAT,\n"
                + "	guess4 FLOAT,\n"
                + "	grade1 FLOAT,\n"
                + "	grade2 FLOAT,\n"
                + "	grade3 FLOAT,\n"
                + "	grade4 FLOAT,\n"
                + " tags TEXT,\n"
                + " confirmed INTEGER NON NULL DEFAULT 0,\n"
                + " points INTEGER,\n"
                + " PRIMARY KEY (event_id, user_id),\n"
                + " FOREIGN KEY(event_id) REFERENCES events(id)"
                + ");";
		try {
			Connection conn = connect();
			
			Statement stmt = conn.createStatement();
	        // create table
	        stmt.execute(sql1);
	        stmt.execute(sql2);
	        stmt.execute(sql3);
		} catch (SQLException e) {
			e.printStackTrace(System.err); 
		}
	}
	
	
	public static boolean newEvent(String name, String start, String end, String[] subs) {
		
		String sql = "INSERT INTO events(name, start, end, sub1, sub2, sub3, sub4) VALUES ( ?, ?, ?, ?, ?, ?, ?);";
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, start);
			pstmt.setString(3, end);
			pstmt.setString(4, subs[0]);
			pstmt.setString(5, subs[1]);
			pstmt.setString(6, subs[2]);
			pstmt.setString(7, subs[3]);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean editEvent(String name, String start, String end, String[] subs) {
		
		String sql = "UPDATE events set start = ?, end = ?, sub1 = ?, sub2 = ?, sub3 = ?, sub4 = ? WHERE name = ?;";
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, start);
			pstmt.setString(2, end);
			pstmt.setString(3, subs[0]);
			pstmt.setString(4, subs[1]);
			pstmt.setString(5, subs[2]);
			pstmt.setString(6, subs[3]);
			pstmt.setString(7, name);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static int currentEvent() {
		int event_id = -1;
		
		String sql = "SELECT id FROM events WHERE datetime('now', 'localtime') BETWEEN start AND end;";
		try {
			Statement stmt = conn.createStatement();
			ResultSet set = stmt.executeQuery(sql);
			
			if (!set.next()) {
				return -1;
			}
			
			event_id = set.getInt("id");
			
			if (set.next()) {
				System.out.println("Multiple events exist!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		return event_id;
	}
}
