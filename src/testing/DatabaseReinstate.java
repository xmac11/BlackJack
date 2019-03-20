package testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseReinstate {

    public static void databaseReinstate(){
        String url;
        String username;
        String password;

        try(FileInputStream input = new FileInputStream(new File("db.properties"))){
            Properties props = new Properties();
            props.load(input);
            username = (String) props.getProperty("username");
            password = (String) props.getProperty("password");
            url = (String) props.getProperty("URL");


            try (Connection connection = DriverManager.getConnection(url, username, password)) {


                Statement statement = connection.createStatement();
                String drop = "DROP table match_history, Session, User_Info;";
                statement.executeUpdate(drop);
                String query1 = "CREATE TABLE IF NOT EXISTS User_Info (username VARCHAR(50) PRIMARY KEY," +
                        "password_hash VARCHAR(64) NOT NULL," +
                        "salt VARCHAR(32) NOT NULL);";
                statement.executeUpdate(query1);
                String query2 = "CREATE TABLE IF NOT EXISTS Session (session_id serial," +
                        " username VARCHAR(50) references User_Info(username)," +
                        " win BOOLEAN," +
                        " time_start TIMESTAMP NOT NULL ," +
                        " time_end TIMESTAMP," +
                        " winnings INT NOT NULL," +
                        " PRIMARY KEY(session_id, username));";
                statement.executeUpdate(query2);
                String query4 = "CREATE TABLE IF NOT EXISTS match_history (" +
                        "username VARCHAR(50) references user_info(username) PRIMARY KEY," +
                        " games_played INTEGER NOT NULL," +
                        " games_won INTEGER not null," +
                        " funds INTEGER NOT NULL);";
                statement.executeUpdate(query4);
                System.out.println("Connection established");


            } catch (SQLException e) {
                System.out.println("Connection not successful");
            }
        }catch (IOException e){
            System.out.println("No properties found");
        }

    }
}
