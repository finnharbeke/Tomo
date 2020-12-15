package motonari.Tomo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Tomo {
	public static JDA jda;
	
    public static void main( String[] args ) throws LoginException, FileNotFoundException {
    	Scanner s = new Scanner(new File("token.txt"));
    	String token = s.next();
    	System.out.println(token);
    	s.close();
    	jda = JDABuilder.createDefault(token).build();
    	
    	
    }
}
