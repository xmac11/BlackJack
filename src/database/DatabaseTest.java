package database;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseTest {
    private String jack, jackpass, peter, peterpass, mary, marypass, nick, nickpass;

    @Before
    public void setUp(){
        jack = "jack";
        jackpass = "password";
        peter = "peter";
        peterpass = "password1";
        mary = "mary";
        marypass = "marypass";
        nick = "nick";
        nickpass = "";

    }

    @Test
    //testing logging in and signing up

    public void test1(){
        //can't log in without signing up first
        assertTrue(Authentication.login(jack,jackpass));
        Authentication.newAccount(jack,jackpass);
        assertTrue(Authentication.login(jack,jackpass));

        //can't login in with another password
        Authentication.newAccount(peter,peterpass);
        assertFalse(Authentication.login(jack,peterpass));

        //can't signup with empty password
        Authentication.newAccount(nick, nickpass);
        assertFalse(Authentication.login(nick,nickpass));


    }

    @Test
    //Testing match history table functionality
    public  void test2(){
        //Testing default values after setting up new account
        Authentication.newAccount(mary,marypass);
        assertEquals(0, MatchHistory.getGamesWon(mary));
        assertEquals(0, MatchHistory.getGamesPlayed(mary));
        assertEquals(500, MatchHistory.getAmount(mary));

        //Testing changing values for a user
        MatchHistory.setGamesPlayed(mary,2);
        MatchHistory.setGamesWon(mary, 2);
        MatchHistory.increaseAmount(mary, 50);

        assertEquals(2, MatchHistory.getGamesPlayed(mary));
        assertEquals(2, MatchHistory.getGamesWon(mary));
        assertEquals(550, MatchHistory.getAmount(mary));

        MatchHistory.reduceAmount(mary, 50);
        assertEquals(500, MatchHistory.getAmount(mary));
    }

    @Test
    //Testing Session table functionality
    public void test3(){
        Session.startSession(jack,1);
        assertFalse(Session.getWin(jack,1));
        assertEquals(0, Session.getBet(jack,1));

        Session.setSessionPoints(1, jack,true);
        assertTrue(Session.getWin(jack,1));

        Session.setBet(1,jack, -10);
        assertEquals(-10, Session.getBet(jack, 1));

        assertEquals(1, Session.getMaxSessionID());
    }
}
