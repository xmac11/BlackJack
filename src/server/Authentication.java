package server;

import java.sql.*;

public class Authentication {

    public static boolean logjn(String username, String password){
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
    public static void newAccount(String username, String password){

        String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
        String user = "group21";
        String pass = "tb2ij946i6";
        int passwordHash = password.hashCode();

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String newEntry = "INSERT INTO User_Info (username, password_hash) VALUES (?,?);";
            PreparedStatement statement = connection.prepareStatement(newEntry);
            statement.setString(1, username);
            statement.setInt(2, passwordHash);
            statement.executeUpdate();


        } catch (SQLException e) {
            System.out.println("Username already exists");
        }

    }

}
