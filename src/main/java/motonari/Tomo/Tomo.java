package motonari.Tomo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import motonari.Commands.TomoListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Tomo {
	public static String prefix = "&";
	public static int msgLim = 2000;
	public static int COLOR = 0xED635D;
	public static String SRC = "https://github.com/MoriMotonari/Tomo";
	
    public static void main( String[] args ) throws LoginException, FileNotFoundException {
    	Scanner s = new Scanner(new File("token.txt"));
    	String token = s.next();
    	s.close();
    	JDA jda = JDABuilder.createDefault(token).build();
    	jda.getPresence().setActivity(Activity.watching("Ueli's Lectures"));
    	jda.addEventListener(new TomoListener(jda));
    }
    
    
    public static void hello() {
    	System.out.println("hello world");
    }
}
