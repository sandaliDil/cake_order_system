package com.example.foodordersystem.service;

import com.example.foodordersystem.model.Branch;
import com.example.foodordersystem.repository.BranchRepository;

import java.util.List;

public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService() {
        this.branchRepository = new BranchRepository();
    }

    // Fetch all branches
    public List<Branch> getAllBranches() {
        return branchRepository.getAllBranches();
    }

    public List<Branch> searchBranchesByName(String branchName) {
        return branchRepository.searchBranchesByName(branchName);
    }

    public int getBranchCount() {
        return branchRepository.getBranchCount();
    }

    // Add a new branch
    public boolean addBranch(Branch branch) {
        // Example business logic: Ensure the branch code is unique
        List<Branch> branches = branchRepository.getAllBranches();
        for (Branch existingBranch : branches) {
            if (existingBranch.getBranchCode().equals(branch.getBranchCode())) {
                throw new IllegalArgumentException("Branch code must be unique.");
            }
        }
        return branchRepository.addBranch(branch);
    }

    // Update an existing branch
    public boolean updateBranch(Branch branch) {
        // Business logic: Ensure the branch exists before updating
        Branch existingBranch = branchRepository.findBranchById(branch.getId());
        if (existingBranch == null) {
            throw new IllegalArgumentException("Branch with ID " + branch.getId() + " does not exist.");
        }
        return branchRepository.updateBranch(branch);
    }

    // Delete a branch by ID
    public boolean deleteBranch(int branchId) {
        // Business logic: Ensure the branch exists before deleting
        Branch existingBranch = branchRepository.findBranchById(branchId);
        if (existingBranch == null) {
            throw new IllegalArgumentException("Branch with ID " + branchId + " does not exist.");
        }
        return branchRepository.deleteBranch(branchId);
    }

    // Find a branch by its ID
    public Branch findBranchById(int branchId) {
        Branch branch = branchRepository.findBranchById(branchId);
        if (branch == null) {
            throw new IllegalArgumentException("Branch with ID " + branchId + " not found.");
        }
        return branch;
    }

}
