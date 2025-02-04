package com.example.foodordersystem.service;

import com.example.foodordersystem.model.User;
import com.example.foodordersystem.repository.UserRepository;

import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public boolean saveUser(String username, String password) {
        User user = new User(0, username, password);
        return userRepository.saveUser(user);
    }

    public boolean login(String username, String password) {
        User user = userRepository.validateUser(username, password);
        return user != null; // Returns true if user exists, otherwise false
    }

    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    public User findUserByUsernameAndPassword(String username, String password) {
        return userRepository.findUserByUsernameAndPassword(username, password);
    }

    public int getUserCount() {
        return userRepository.getUserCount();
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    // Delete user
    public boolean deleteUser(int id) {
        return userRepository.deleteUser(id);
    }

    // Update user
    public boolean updateUser(User user) {
        return userRepository.updateUser(user);
    }

}
