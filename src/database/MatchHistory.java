package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * Class that allows the application to add and retrieve information from the match_history table in the
 * database
 *
 * @author Group21
 *
 */
public class MatchHistory {

    /**
     * Allows the application to set number of games the user has won
     * @param username username of the user
     * @param points number of games the user won after last update
     */
    public static void setGamesWon(String username, int points){
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
            String newPoints = "UPDATE match_history SET games_won = games_won + ? WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setInt(1,points);
            statement.setString(2,username);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        }catch (IOException e){
            System.out.println("No properties found");
        }
    }

    /**
     * Allows the application to get number of games won by the user
     * @param username username of the user
     * @return number of games won by the user
     */
    public static int getGamesWon(String username){
        int points = 0;
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
            String getPoints = "SELECT games_won FROM match_history WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(getPoints);
            statement.setString(1,username);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                points = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Username does not exist");
            return -1;
        }
        }catch (IOException e){
            System.out.println("No properties found");
            return -1;
        }
        return points;
    }

    /**
     * Allows to the add to the games played counter
     * @param username username of the user
     * @param points games played since last update
     */
    public static void setGamesPlayed(String username, int points){
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
            String newPoints = "UPDATE match_history SET games_played = games_played + ? WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setInt(1,points);
            statement.setString(2,username);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        }catch (IOException e){
            System.out.println("No properties found");
        }
    }

    /**
     * Allows to get the amount of games played by the user
     * @param username username of the user
     * @return number of games played
     */
    public static int getGamesPlayed(String username){
        int points = 0;
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
            String getPoints = "SELECT games_played FROM match_history WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(getPoints);
            statement.setString(1,username);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                points = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Username does not exist");
            return -1;
        }
        }catch (IOException e){
            System.out.println("No properties found");
            return -1;
        }
        return points;
    }

    /**
     * Allows to get the amount of funds the user has in their wallet
     * @param username username of the user
     * @return amount of funds available
     */
    public static int getAmount(String username){
        int funds = 0;
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
            String getPoints = "SELECT funds FROM match_history WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(getPoints);
            statement.setString(1,username);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                funds = rs.getInt(1);
            }

        } catch (SQLException e) {
        	System.out.println(username);
            System.out.println("Username does not exist");
            return -1;
        }
        }catch (IOException e){
            System.out.println("No properties found");
            return -1;
        }
        return funds;
    }

    /**
     * Allows the application to reduce the amount of funds available to the user
     * @param username username of the user
     * @param amount amount of money taken away from the funds
     */
    public static void reduceAmount(String username, int amount){
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
            String newPoints = "UPDATE match_history SET funds = funds - ? WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setInt(1,amount);
            statement.setString(2,username);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        }catch (IOException e){
            System.out.println("No properties found");
        }
    }

    /**
     * Allows the application to increase the amount of funds available to the user
     * @param username username of the user
     * @param amount amount to be added to available funds
     */
    public static void increaseAmount(String username, int amount){
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
            String newPoints = "UPDATE match_history SET funds = funds + ? WHERE username = ?;";

            PreparedStatement statement = connection.prepareStatement(newPoints);
            statement.setInt(1,amount);
            statement.setString(2,username);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Username does not exist");
        }
        }catch (IOException e){
            System.out.println("No properties found");
        }
    }
    
}
