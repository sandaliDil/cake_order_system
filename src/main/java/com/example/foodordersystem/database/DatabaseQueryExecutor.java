package com.example.foodordersystem.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseQueryExecutor {

    private Connection connection;

    public DatabaseQueryExecutor() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        setStatementParameters(statement, params);
        return statement.executeQuery();
    }

    public int executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query,PreparedStatement.RETURN_GENERATED_KEYS);
        setStatementParameters(statement, params);
        int affectedRows = statement.executeUpdate();
        int rowId = 0;
        if (affectedRows > 0) {
            // Retrieve the generated keys
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                // Get the generated row ID
                rowId = generatedKeys.getInt(1);
            }
        }
        return rowId;
    }

    private void setStatementParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
