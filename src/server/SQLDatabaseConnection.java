package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;


public class SQLDatabaseConnection {


	public static void main(String[] args) throws IOException {


		String url = "jdbc:postgresql://mod-msc-sw1.cs.bham.ac.uk/";
		String username = "group21";
		String password = "tb2ij946i6";

		try (Connection connection = DriverManager.getConnection(url, username, password)) {

			Statement statement = connection.createStatement();
			String query1 = "CREATE TABLE IF NOT EXISTS User_Info (username VARCHAR(50) PRIMARY KEY," +
					"password_hash INT NOT NULL);";
			statement.executeUpdate(query1);
			String query2 = "CREATE TABLE IF NOT EXISTS Session (session_id serial," +
					" username VARCHAR(50) references User_Info(username)," +
					" session_points INTEGER, " +
					"time_start TIMESTAMP NOT NULL ," +
					" time_end TIMESTAMP, " +
					"PRIMARY KEY(session_id, username));";
			statement.executeUpdate(query2);
			String query3 = "CREATE TABLE IF NOT EXISTS friend_list (" +
					"username VARCHAR(50) references user_info(username)," +
					" username_friend VARCHAR(50) references user_info(username) ," +
					"PRIMARY KEY(username, username_friend));";
			statement.executeUpdate(query3);
			String query4 = "CREATE TABLE IF NOT EXISTS match_history (" +
					"username VARCHAR(50) references user_info(username) PRIMARY KEY," +
					" games_played INTEGER NOT NULL," +
					" games_won INTEGER not null);";
			statement.executeUpdate(query4);
			System.out.println("Connection established");


		} catch (SQLException e) {
			System.out.println("Connection not successful");
		}
	}

}
