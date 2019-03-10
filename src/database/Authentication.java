package database;

import java.sql.*;

public class Authentication {

    public static boolean login(String username, String password){
        boolean login = false;
        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";
        int passwordHash = password.hashCode();

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

        String loginQuery = "SELECT password_hash FROM User_Info WHERE username =?";

        PreparedStatement statement = connection.prepareStatement(loginQuery);
        statement.setString(1, username);

        ResultSet rs = statement.executeQuery();

        while(rs.next()){
            int actualHash = rs.getInt(1);
            if(actualHash == passwordHash){
                login = true;
            }
        }


        } catch (SQLException e) {
            System.out.println("Connection not successfull");
        }
        return login;
    }
    public static boolean newAccount(String username, String password){

        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";
        int passwordHash = password.hashCode();

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newEntry = "INSERT INTO User_Info (username, password_hash) VALUES (?,?);";
            String newHistory = "INSERT INTO match_history (username, games_played, games_won) VALUES (?,0,0);";
            PreparedStatement statement = connection.prepareStatement(newEntry);
            statement.setString(1, username);
            statement.setInt(2, passwordHash);
            statement.executeUpdate();
            PreparedStatement statement1 = connection.prepareStatement(newHistory);
            statement1.setString(1,username);
            statement1.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Username already exists");
            return false;
        }

    }

    public static void main(String[] args){
       // newAccount("nxk828","password");
        System.out.println(login("nxk828","password"));
        //MatchHistory.setGamesWon("nxk827",2);
        //System.out.println(MatchHistory.getGamesWon("nxk827"));
        //MatchHistory.setGamesPlayed("nxk827",4);
        //System.out.println(MatchHistory.getGamesPlayed("nxk827"));
    }

}
