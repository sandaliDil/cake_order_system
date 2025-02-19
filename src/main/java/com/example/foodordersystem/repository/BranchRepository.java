package com.example.foodordersystem.repository;

import com.example.foodordersystem.database.DatabaseConnection;
import com.example.foodordersystem.model.Branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BranchRepository {

    // Method to get all branches from the database
    public List<Branch> getAllBranches() {
        List<Branch> branches = new ArrayList<>();
        String query = "SELECT id, branch_name, branch_code FROM branches";  // Ensure table and column names match your DB

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            // Process the result set
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String branchName = resultSet.getString("branch_name");
                String branchCode = resultSet.getString("branch_code");

                Branch branch = new Branch(id, branchName, branchCode);
                branches.add(branch);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle exception
        }

        return branches;
    }

    // Method to get the count of branches from the database
    public int getBranchCount() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM branches";  // Ensure the table name is correct

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                count = resultSet.getInt(1);  // Get the count from the first column
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle exception
        }

        return count;
    }

    // Method to add a new branch to the database
    public boolean addBranch(Branch branch) {
        String query = "INSERT INTO branches (id, branch_name, branch_code) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, branch.getId());
            preparedStatement.setString(2, branch.getBranchName());
            preparedStatement.setString(3, branch.getBranchCode());

            return preparedStatement.executeUpdate() > 0;  // Returns true if a row was inserted
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to update an existing branch in the database
    public boolean updateBranch(Branch branch) {
        String query = "UPDATE branches SET branch_name = ?, branch_code = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, branch.getBranchName());
            preparedStatement.setString(2, branch.getBranchCode());
            preparedStatement.setInt(3, branch.getId());

            return preparedStatement.executeUpdate() > 0;  // Returns true if a row was updated
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to delete a branch from the database
    public boolean deleteBranch(int branchId) {
        String query = "DELETE FROM branches WHERE id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, branchId);

            return preparedStatement.executeUpdate() > 0;  // Returns true if a row was deleted
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to find a branch by its ID
    public Branch findBranchById(int branchId) {
        String query = "SELECT id, branch_name, branch_code FROM branches WHERE id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, branchId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String branchName = resultSet.getString("branch_name");
                    String branchCode = resultSet.getString("branch_code");

                    return new Branch(id, branchName, branchCode);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Branch> findAllBranches() {
        List<Branch> branchList = new ArrayList<>();
        String query = "SELECT branch_name, branch_code FROM branches";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String branchName = resultSet.getString("branch_name");
                String branchCode = resultSet.getString("branch_code");

                branchList.add(new Branch(branchName, branchCode));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return branchList;
    }

    public List<Branch> searchBranchesByName(String branchName) {
        List<Branch> branches = new ArrayList<>();
        String query = "SELECT id, branch_name, branch_code FROM branches WHERE branch_name LIKE ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, "%" + branchName + "%"); // Wildcard search
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("branch_name");
                    String code = resultSet.getString("branch_code");

                    branches.add(new Branch(id, name, code));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return branches;
    }

    public Branch getBranchById(int branchId) {
        String query = "SELECT * FROM branches WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Branch(rs.getInt("id"), rs.getString("branch_name"), rs.getString("branch_code"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
