package testing;

import database.Authentication;
import database.MatchHistory;
import database.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseTest {
    private String username1, password1, username2, password2, username3, password3, username4, password4;

    @Before
    public void setUp(){
     
        username1 = createRandomUsername();
        password1 = "password1";
        
        username2 = createRandomUsername();
        password2 = "password2";
        
        username3 = createRandomUsername();
        password3 = "password3"; 
        
        username4 = createRandomUsername();
        password4 = "password4"; 
    }
    
    // create random username of 10 characters
    // probability to produce the same username is 1/26^10
    public static String createRandomUsername() {
		StringBuilder sb = new StringBuilder();
		int[] arr = new int[10];
		for(int i = 0; i < 10; i++) {
			arr[i] = (int) (26 * Math.random());
		}
		int offset = (int) 'A';
		
		for(int i = 0; i < 10; i++) {
			sb.append((char) (offset + arr[i]));
		}
		return sb.toString().toLowerCase();
	}

    @Test
    //testing logging in and signing up

    public void test1() {
        //can't log in without signing up first
        assertFalse(Authentication.login(username1, password1));
        //create account
        Authentication.newAccount(username1, password1);
        assertTrue(Authentication.login(username1, password1));

        //can't login in with another password
        Authentication.newAccount(username2, password2);
        assertFalse(Authentication.login(username1, password2));
    }

    @Test
    //Testing match history table functionality
    public  void test2() {
        //Testing default values after setting up new account
        Authentication.newAccount(username3,password3);
        Assert.assertEquals(0, MatchHistory.getGamesWon(username3));
        assertEquals(0, MatchHistory.getGamesPlayed(username3));
        assertEquals(500, MatchHistory.getAmount(username3));

        //Testing changing values for a user
        MatchHistory.setGamesPlayed(username3, 2);
        assertEquals(2, MatchHistory.getGamesPlayed(username3));
        
        MatchHistory.setGamesWon(username3, 2);
        assertEquals(2, MatchHistory.getGamesWon(username3));
        
        MatchHistory.increaseAmount(username3, 50);    
        assertEquals(550, MatchHistory.getAmount(username3));
        
        MatchHistory.reduceAmount(username3, 50);
        assertEquals(500, MatchHistory.getAmount(username3));
    }

    @Test
    //Testing Session table functionality
    public void test3() { 
        
        Authentication.newAccount(username4, password4);
        
        Session.startSession(username4, 1);
        assertFalse(Session.getWin(username4, 1));
        assertEquals(0, Session.getWinnings(username4, 1));

        Session.setSessionResult(1, username4, true);
        assertTrue(Session.getWin(username4, 1));

        Session.setWinnings(1, username4, 150);
        assertEquals(150, Session.getWinnings(username4, 1));

        assertEquals(1, Session.getMaxSessionID());
    }
}
