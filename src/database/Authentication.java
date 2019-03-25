package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Class that provides methods to Authenticate the user into the system or create a new account with the use of the database
 *
 * @author Group21
 *
 */
public class Authentication {

    /**
     * Logs in the user using their credentials and matching them against the one in the database
     * @param username username of the user
     * @param password password from the user account
     * @return true, if the credentials match, false otherwise
     */
    public static boolean login(String username, String password){
        boolean login = false;
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

        String loginQuery = "SELECT password_hash, salt FROM User_Info WHERE username = ?"; 

        PreparedStatement statement = connection.prepareStatement(loginQuery);
        statement.setString(1, username);

        ResultSet rs = statement.executeQuery();

        while(rs.next()){
        	String actualHash = rs.getString(1); 
        	String salt = rs.getString(2);
        	
        	String passwordHash = null;
        	try {
				passwordHash = UserPassword.getEncryptedPassword(password, salt);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				e.printStackTrace();
			}           
            if(actualHash.equals(passwordHash)){
                login = true;
            }
        }

        } catch (SQLException e) {
            System.out.println("Connection not successful");
            return false;
        }
        }catch (IOException e){
            System.out.println("No properties found");
            return false;
        }
        return login;
    }

    /**
     * Allows the user to create a new account, which is then recorded in the database
     * @param username new username
     * @param password new password
     * @return true, if the username is unique and account is created, false if such username already exists
     */
    public static boolean newAccount(String username, String password){

        String url;
        String user;
        String pass;
        
        boolean success;

        // generate salt
        byte[] saltArray = null;
        try {
        	saltArray = UserPassword.generateSalt();
        } catch (NoSuchAlgorithmException e1) {
        	e1.printStackTrace();
        }
        String salt = UserPassword.bytesToHex(saltArray);

        // compute hash of password
        String passwordHash = null;
        try {
        	passwordHash = UserPassword.getEncryptedPassword(password, salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
        	e1.printStackTrace();
        }       
        
        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            user = props.getProperty("username");
            pass = props.getProperty("password");
            url = props.getProperty("URL");  
            
        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
        	String newEntry = "INSERT INTO User_Info (username, password_hash, salt) VALUES (?,?,?);"; 
            String newHistory = "INSERT INTO match_history (username, games_played, games_won, funds) VALUES (?, 0, 0, 500);";

            PreparedStatement statement = connection.prepareStatement(newEntry);
            statement.setString(1, username);
            statement.setString(2, passwordHash); 
            statement.setString(3, salt); 
            statement.executeUpdate();
            PreparedStatement statement1 = connection.prepareStatement(newHistory);
            statement1.setString(1, username);
            statement1.executeUpdate();
            success = true;
        } catch (SQLException e) {
            System.out.println("Username already exists");
            success = false;
        }
        }catch (IOException e){
            System.out.println("No properties found");
            success = false;
        }
        return success;
    }

}
