package motonari.Grades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import motonari.Commands.Command;
import motonari.Tomo.Helper;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Plot extends Command {
    public Plot(MessageReceivedEvent e, String[] args) {super(e, args);}
	public Plot() {super();}

    private static int MIN_N = 5;
    private static String API =
        "https://quickchart.io/chart/render/zm-cb5b84ca-6e73-47f4-b686-b0dedf2750d4";

	@Override
	public void init() {
		name = "Plot for Grade Guessing";
		cmd = "gplot";
		desc = "plots a barplot with the guessing / grade distribution.";
		
		
		arg_str = "<event_name> <subject>";
		aliases = new HashSet<String>( Arrays.asList(new String[] {
				cmd, "gradeplot"
		}));
		
		options = new HashMap<String, String>();
		
	}

    private String event_name;
    private String subject;
    private int sub_i;
    private int event_id;
    private String query;
    private int n;

    public String parse() {
        if (args.length > 3) return "Too many arguments!";
        else if (args.length < 3) return "Not enough arguments!";
        event_name = args[1];
        subject = args[2];
		try {
            ResultSet set = Grades.connect().createStatement()
            .executeQuery("SELECT id, sub1, sub2, sub3, sub4 from events WHERE name = \""
                + event_name + "\";");
			if (!set.next())
                return "No Event called " + event_name + " found!";
			else {
                event_id = set.getInt("id");
                for (int i = 0; i < 4; i++) {
                    if (subject.equals(set.getString("sub"+(i+1)))) {
                        sub_i = i+1;
                        break;
                    }
                }
                if (sub_i == 0)
                    return event_name + " event has no subject '" + subject + "'!";
			}
		} catch (SQLException e) {
            e.printStackTrace();
			return "SQLException!";
		}
        return "OK";
    }

    public void main() {
        ArrayList<Double> guesses = new ArrayList<Double>();
        ArrayList<Double> grades = new ArrayList<Double>();

        try {
            ResultSet set = Grades.connect().createStatement()
            .executeQuery("SELECT guess" + sub_i + ", grade" + sub_i
                + " from grades where event_id = " + event_id + ";");

            while (set.next()) {
                guesses.add(set.getDouble("guess" + sub_i));
                grades.add(set.getDouble("grade" + sub_i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
		}

        Collections.sort(guesses);
        Collections.sort(grades);

        double step = 1.5;
        int gu = 0;
        int gr = 0;
        String data1 = "data1=";
        String data2 = "data2=";
        for (double min = 1; min < 6; min += step) {
            int c1 = 0;
            int c2 = 0;
            for (; gu < guesses.size() && guesses.get(gu) <= min + step; gu++)
                c1++;
            for (; gr < guesses.size() && grades.get(gr) <= min + step; gr++)
                c2++;
            data1 += c1 + ",";
            data2 += c2 + ",";
            n += c2;
            if (min == 1) {
                min = 2.25;
                step = 0.25;
            }
        }

        query = "?" + data1 + "&" + data2 + "&title=" + subject.toUpperCase() + "%20Distribution";
    }

    @Override
    public void answer() {
        if (n < MIN_N) {
            Helper.error(e, c, args[0], "Won't plot with less than " + MIN_N + " datapoints!", 30);
        } else {
            c.sendMessage(API + query).queue();
        }

    }

    @Override
    public String example(String alias) {
        return alias + " BP1 dm";
    }
}
