package motonari.Tomo;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import motonari.Algorithms.BinarySearchTree;
import motonari.Algorithms.MaxSubarrDiff;
import motonari.Algorithms.MinEditDistance;
import motonari.Algorithms.UnboundedKnapsack;
import motonari.Algorithms.ZeroOneKnapsack;
import motonari.Ascii.Draw;
import motonari.Ascii.Graph;
import motonari.Ascii.Line;
import motonari.Ascii.Point;
import motonari.Commands.Command;
import motonari.Commands.Example;
import motonari.Commands.Help;
import motonari.Grades.Broadcast;
import motonari.Grades.Confirm;
import motonari.Grades.Event;
import motonari.Grades.Grades;
import motonari.Grades.Guess;
import motonari.Grades.Guessers;
import motonari.Grades.Personal;
import motonari.Grades.PointsRanking;
import motonari.Grades.ProcessStats;
import motonari.Grades.SQL;
import motonari.Grades.Stats;
import motonari.Grades.Submit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Tomo {
	public static final String prefix = "&";
	public static final boolean DEV = true;
	public static final String dev_pre = "d";

	public static final int COLOR = 0xED635D;
	public static final String SRC = "https://github.com/MoriMotonari/Tomo";
	public static final String ADMIN = "304014259975880704";
	
	public static JDA jda;
	
	public static final HashMap<String, ArrayList<Class<? extends Command>>> commands;
	static {
		commands = new HashMap<String, ArrayList<Class<? extends Command>>>();
		
		String sub = "Sub";
		commands.put(sub, new ArrayList<Class<? extends Command>>());
		commands.get(sub).add(Help.class);
		commands.get(sub).add(Example.class);
		
		String algos = "Algorithms";
		commands.put(algos, new ArrayList<Class<? extends Command>>());
		commands.get(algos).add(MaxSubarrDiff.class);
		commands.get(algos).add(UnboundedKnapsack.class);
		commands.get(algos).add(ZeroOneKnapsack.class);
		commands.get(algos).add(BinarySearchTree.class);
		commands.get(algos).add(MinEditDistance.class);
		
		String ascii = "Ascii";
		commands.put(ascii, new ArrayList<Class<? extends Command>>());
		commands.get(ascii).add(Graph.class);
		commands.get(ascii).add(Line.class);
		commands.get(ascii).add(Point.class);
		commands.get(ascii).add(Draw.class);
		
		String grades = "Grades";
		commands.put(grades, new ArrayList<Class<? extends Command>>());
		commands.get(grades).add(Grades.class);
		commands.get(grades).add(Guess.class);
		commands.get(grades).add(ProcessStats.class);
		commands.get(grades).add(Event.class);
		commands.get(grades).add(Stats.class);
		commands.get(grades).add(Guessers.class);
		commands.get(grades).add(SQL.class);
		commands.get(grades).add(Submit.class);
		commands.get(grades).add(Personal.class);
		commands.get(grades).add(Confirm.class);
		commands.get(grades).add(Broadcast.class);
		commands.get(grades).add(PointsRanking.class);
	}
	
	public static Class<? extends Command> random(boolean byAdmin) {
		Random r = new Random();
		ArrayList<Class<? extends Command>> sub = commands.get(commands.keySet().toArray()[r.nextInt(commands.keySet().size())]);
		return sub.get(r.nextInt(sub.size()));
	}
	
	
	
	public static Class<? extends Command> fromAlias(String alias) {
		for (ArrayList<Class<? extends Command>> list : commands.values()) {
			for (Class<? extends Command> clazz : list) {
				try {
					Command cmd = clazz.getConstructor().newInstance();
					if (cmd.isAlias(alias)) {
						return clazz;
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (e instanceof InvocationTargetException) e.getCause().printStackTrace();
				}
			}
		}
		return null;
	}
	
	
    public static void main(String[] args) throws LoginException, FileNotFoundException {
    	Scanner s = new Scanner(new File("token.txt"));
    	String token = s.next();
    	s.close();
    	jda = JDABuilder.createDefault(token).build();
    	jda.getPresence().setActivity(Activity.watching("Ueli's Lectures"));
    	jda.addEventListener(new TomoListener());
    }
    
    
    public static void hello() {
    	System.out.println("hello world");
    }
}
