package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class Session {

    public static void startSession(String username, int id){
        String url;
        String user;
        String pass;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = (String) props.getProperty("username");
            pass = (String) props.getProperty("password");
            url = (String) props.getProperty("URL");


            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newPoints = "INSERT INTO session(username, session_points, time_start, session_id) VALUES(?,0,?,?);";

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
    public static void setSessionPoints(int sessionID, String username, boolean points){
        String url;
        String user;
        String pass;
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = (String) props.getProperty("username");
            pass = (String) props.getProperty("password");
            url = (String) props.getProperty("URL");

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newPoints = "UPDATE session SET session_points = ? WHERE username = ? and session_id = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setBoolean(1,points);
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

    public static boolean getSessionPoints(String username){
        boolean points = false;
        String url;
        String user;
        String pass;
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = (String) props.getProperty("username");
            pass = (String) props.getProperty("password");
            url = (String) props.getProperty("URL");

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String getPoints = "SELECT session_points FROM session WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(getPoints);
            statement.setString(1,username);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                points = rs.getBoolean(1);
            }

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        }catch (IOException e){
            System.out.println("No properties found");
        }
        return points;
    }

    public static void setSessionend(String username, int sessionID){
        String url;
        String user;
        String pass;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = (String) props.getProperty("username");
            pass = (String) props.getProperty("password");
            url = (String) props.getProperty("URL");

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
            user = (String) props.getProperty("username");
            pass = (String) props.getProperty("password");
            url = (String) props.getProperty("URL");
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
