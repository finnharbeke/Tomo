package motonari.Grades;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class Grades {
	
	static final double min_grade = 1.0;
	static final double max_grade = 6.0;
	static final String ad = "ad";
	static final String dm = "dm";
	static final String ep = "ep";
	static final String la = "la";
	static final String[] exams = {ad, dm, ep, la};
	
	static final long Eth_id = 747752542741725244L;
	static final long sem1_id = 773543051011555398L;
	static final long sem2_id = 772552854832807938L;
	
	static Connection conn = null;
	
	public static Connection connect() throws SQLException {
		
		if (conn != null)
			return conn;
		
		// SQLite connection string
        String url = "jdbc:sqlite:grades.db";
        
        conn = DriverManager.getConnection(url);
        
        return conn;
	}
	
	public static void buildDB() {
		String sql1 = "CREATE TABLE IF NOT EXISTS events (\n"
				+ " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
				+ "	name TEXT UNIQUE,\n"
				+ "	start DATETIME,\n"
				+ "	end DATETIME\n"
				+ ");";
		String sql2 = "CREATE TABLE IF NOT EXISTS grades (\n"
				+ " event_id INTEGER NON NULL,\n"
                + "	user_id BIGINT NON NULL,\n"
                + "	ad_guess FLOAT,\n"
                + "	dm_guess FLOAT,\n"
                + "	ep_guess FLOAT,\n"
                + "	la_guess FLOAT,\n"
                + "	ad_grade FLOAT,\n"
                + "	dm_grade FLOAT,\n"
                + "	ep_grade FLOAT,\n"
                + "	la_grade FLOAT,\n"
                + " tags TEXT,\n"
                + " PRIMARY KEY (event_id, user_id),\n"
                + " FOREIGN KEY(event_id) REFERENCES events(id)"
                + ");";
		try {
			Connection conn = connect();
			
			Statement stmt = conn.createStatement();
	        // create table
	        stmt.execute(sql1);
	        stmt.execute(sql2);
		} catch (SQLException e) {
			e.printStackTrace(System.err); 
		}
	}
	
	public static void put(Connection conn, int event_id, long id, 
			Double ad_guess, Double dm_guess, Double ep_guess, Double la_guess, 
			Double ad_grade, Double dm_grade, Double ep_grade, Double la_grade, String tags) throws SQLException {
		
		String sql = "INSERT INTO grades (event_id, user_id, ad_guess, dm_guess, ep_guess, la_guess, "
				+ "ad_grade, dm_grade, ep_grade, la_grade, tags)" 
				+ " VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\n"
				+ "  ON CONFLICT(id) DO UPDATE SET "
				+ (ad_guess != null ? "ad_guess = ?, " : "")
				+ (dm_guess != null ? "dm_guess = ?, " : "")
				+ (ep_guess != null ? "ep_guess = ?, " : "")
				+ (la_guess != null ? "la_guess = ?, " : "")
				+ (ad_grade != null ? "ad_grade = ?, " : "")
				+ (dm_grade != null ? "dm_grade = ?, " : "")
				+ (ep_grade != null ? "ep_grade = ?, " : "")
				+ (la_grade != null ? "la_grade = ?, " : "")
				+ "tags = ?;";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, event_id);
        pstmt.setLong(2, id);
        
        setDecimal(pstmt, 3, ad_guess);
        setDecimal(pstmt, 4, dm_guess);
        setDecimal(pstmt, 5, ep_guess);
        setDecimal(pstmt, 6, la_guess);
        setDecimal(pstmt, 7, ad_grade);
        setDecimal(pstmt, 8, dm_grade);
        setDecimal(pstmt, 9, ep_grade);
        setDecimal(pstmt, 10, la_grade);
        pstmt.setString(11, tags);
        
        int i = 12;
        
        if (ad_guess != null)
        	pstmt.setDouble(i++, ad_guess);
        if (dm_guess != null)
        	pstmt.setDouble(i++, dm_guess);
        if (ep_guess != null)
        	pstmt.setDouble(i++, ep_guess);
        if (la_guess != null)
        	pstmt.setDouble(i++, la_guess);
        if (ad_grade != null)
        	pstmt.setDouble(i++, ad_grade);
        if (dm_grade != null)
        	pstmt.setDouble(i++, dm_grade);
        if (ep_grade != null)
        	pstmt.setDouble(i++, ep_grade);
        if (la_grade != null)
        	pstmt.setDouble(i++, la_grade);
        
        pstmt.setString(i, tags);
        
        pstmt.executeUpdate();
        
	}
	
	public static boolean newEvent(String name, String start, String end) {
		
		String sql = "INSERT INTO events(name, start, end) VALUES ( ?, ?, ?);";
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, start);
			pstmt.setString(3, end);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean editEvent(String name, String start, String end) {
		
		String sql = "UPDATE events set start = ?, end = ? WHERE name = ?;";
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, start);
			pstmt.setString(2, end);
			pstmt.setString(3, name);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static int currentEvent() {
		int event_id = -1;
		
		String sql = "SELECT id FROM events WHERE datetime('now') BETWEEN start AND end;";
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
	
	public static void setDecimal(PreparedStatement pstmt, int ind, Double x) throws SQLException {
		if (x == null) {
			pstmt.setNull(ind, Types.DECIMAL);
		} else {
			pstmt.setDouble(ind, x);
		}
	}
	
	

}
