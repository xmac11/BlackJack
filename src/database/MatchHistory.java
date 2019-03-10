package database;

import java.sql.*;

public class MatchHistory {

    public static void setGamesWon(String username, int points){
        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newPoints = "UPDATE match_history SET games_won = games_won + ? WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setInt(1,points);
            statement.setString(2,username);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
    }
    public static int getGamesWon(String username){
        int points = 0;
        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String getPoints = "SELECT games_won FROM match_history WHERE username = ?;";

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

    public static void setGamesPlayed(String username, int points){
        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newPoints = "UPDATE match_history SET games_played = games_played + ? WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setInt(1,points);
            statement.setString(2,username);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
    }

    public static int getGamesPlayed(String username){
        int points = 0;
        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String getPoints = "SELECT games_played FROM match_history WHERE username = ?;";

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

}
