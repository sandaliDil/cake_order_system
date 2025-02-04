package com.example.foodordersystem.migration;

import com.example.foodordersystem.database.DatabaseConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration {

    public static void runMigration() {
        DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
        Connection connection = null;

        try {
            connection = databaseConnection.getConnection();

            // Execute the SQL scripts
            executeScript(connection, "migration.sql");
            // Add more scripts as needed

            System.out.println("Database migration completed successfully.");
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace(); // Optional: Print the stack trace for debugging
        } finally {
            if (connection != null) {
                try {
                    connection.close(); // Close the connection if it's not null
                } catch (SQLException e) {
                    System.err.println("Failed to close the connection: " + e.getMessage());
                }
            }
        }
    }

    private static void executeScript(Connection connection, String scriptPath) {
        try (InputStream inputStream = DatabaseMigration.class.getClassLoader().getResourceAsStream(scriptPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder scriptBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                scriptBuilder.append(line).append("\n");
            }

            String script = scriptBuilder.toString();
            Statement statement = connection.createStatement();
            statement.execute(script);
            statement.close();
        } catch (IOException | SQLException e) {
            //e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
