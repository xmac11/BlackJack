package database;

import java.sql.*;

public class Session {

    public static void startSession(String username, int id){
        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

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
    }
    public static void setSessionPoints(String username, int points){
        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newPoints = "UPDATE session SET session_points = session_points + ? WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setInt(1,points);
            statement.setString(2,username);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
    }

    public static int getSessionPoints(String username){
        int points = 0;
        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String getPoints = "SELECT session_points FROM session WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(getPoints);
            statement.setString(1,username);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                points = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        return points;
    }
    public static void setSessionend(String username){
        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newPoints = "UPDATE session SET time_end = ? WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setTimestamp(1,timestamp);
            statement.setString(2,username);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
    }

    /**
     * This method gets the maximum session ID from the session table in the database
     * @return max session ID
     */
    
    public static int getMaxSessionID(){
        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";
        int maxID = 0;
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
        return maxID;
    }

}
