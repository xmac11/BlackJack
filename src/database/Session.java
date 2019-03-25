package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * Class that allows the application to manipulate the session table in the database
 *
 * @author Group21
 *
 */
public class Session {

    /**
     * Creates a new session for the user, which has the same session id, as the other users in the same game
     * @param username username of the user
     * @param id id of the session
     */
    public static void startSession(String username, int id){
        String url;
        String user;
        String pass;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = props.getProperty("username");
            pass = props.getProperty("password");
            url = props.getProperty("URL");


            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newPoints = "INSERT INTO session(username, win, time_start, session_id, winnings) VALUES(?,false,?,?,0);";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setString(1,username);
            statement.setTimestamp(2,timestamp);
            statement.setInt(3, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        }catch (IOException e){
            System.out.println("No properties found");
        }
    }

    /**
     * Sets whether the user has won or lost the session
     * @param sessionID id of the session
     * @param username username of the user
     * @param result whether the user has won or lost the game
     */
    public static void setSessionResult(int sessionID, String username, boolean result){
        String url;
        String user;
        String pass;
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = props.getProperty("username");
            pass = props.getProperty("password");
            url = props.getProperty("URL");

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newPoints = "UPDATE session SET win = ? WHERE username = ? and session_id = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setBoolean(1,result);
            statement.setString(2,username);
            statement.setInt(3,sessionID);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        }catch (IOException e){
            System.out.println("No properties found");
        }
    }

    /**
     * Gets, whether the user won or lost a game
     * @param username username of the user
     * @param sessionID id of the session
     * @return whether the user won the session or not
     */
    public static boolean getResult(String username, int sessionID){
        boolean result = false;
        String url;
        String user;
        String pass;
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = props.getProperty("username");
            pass = props.getProperty("password");
            url = props.getProperty("URL");

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String getPoints = "SELECT win FROM session WHERE username = ? and session_id = ?;";

            PreparedStatement statement = connection.prepareStatement(getPoints);
            statement.setString(1,username);
            statement.setInt(2, sessionID);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                result = rs.getBoolean(1);
            }

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        }catch (IOException e){
            System.out.println("No properties found");
        }
        return result;
    }

    /**
     * Allows the application to set the amount of money won or lost during a session
     * @param sessionID id of the session
     * @param username username of the user
     * @param bet amount of money won or lost
     */
    public static void setWinnings(int sessionID, String username, int winnings){
        String url;
        String user;
        String pass;
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = props.getProperty("username");
            pass = props.getProperty("password");
            url = props.getProperty("URL");

            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                String newPoints = "UPDATE session SET winnings = ? WHERE username = ? and session_id = ?;";

                PreparedStatement statement = connection.prepareStatement(newPoints);
                statement.setInt(1,winnings);
                statement.setString(2,username);
                statement.setInt(3,sessionID);
                statement.executeUpdate();

            } catch (SQLException e) {
                System.out.println("Username does not exist");
            }
        }catch (IOException e){
            System.out.println("No properties found");
        }
    }


    /**
     * Allows the application to get the amount of money won or lost in a given session of a certain user
     * @param username username of the user
     * @param sessionID id of the session
     * @return funds won or lost
     */
    public static int getWinnings(String username, int sessionID){
        int winnings = 0;
        String url;
        String user;
        String pass;
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = props.getProperty("username");
            pass = props.getProperty("password");
            url = props.getProperty("URL");

            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                String getPoints = "SELECT winnings FROM session WHERE username = ? and session_id = ?;";

                PreparedStatement statement = connection.prepareStatement(getPoints);
                statement.setString(1,username);
                statement.setInt(2, sessionID);
                ResultSet rs = statement.executeQuery();
                while(rs.next()){
                    winnings = rs.getInt(1);
                }

            } catch (SQLException e) {
                System.out.println("Username does not exist");
            }
        }catch (IOException e){
            System.out.println("No properties found");
        }
        return winnings;
    }

    /**
     * Allows the application to close started session, by adding timestamp of game's end
     * @param username username of the user
     * @param sessionID session to be ended
     */
    public static void setSessionEnd(String username, int sessionID){
        String url;
        String user;
        String pass;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = props.getProperty("username");
            pass = props.getProperty("password");
            url = props.getProperty("URL");

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newPoints = "UPDATE session SET time_end = ? WHERE username = ? and session_id = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setTimestamp(1,timestamp);
            statement.setString(2,username);
            statement.setInt(3, sessionID);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        }catch (IOException e){
            System.out.println("No properties found");
        }
    }

    /**
     * This method gets the maximum session ID from the session table in the database
     * @return max session ID
     */
    
    public static int getMaxSessionID(){
        String url;
        String user;
        String pass;
        int maxID = 0;
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = props.getProperty("username");
            pass = props.getProperty("password");
            url = props.getProperty("URL");
        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String getPoints = "SELECT MAX(session_id) from session;";

            PreparedStatement statement = connection.prepareStatement(getPoints);

            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                maxID = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        }catch (IOException e){
            System.out.println("No properties found");
        }
        return maxID;
    }

}
