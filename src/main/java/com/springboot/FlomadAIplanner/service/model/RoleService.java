package com.springboot.FlomadAIplanner.service.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.springboot.FlomadAIplanner.model.entities.login.Role;
import com.springboot.FlomadAIplanner.model.repository.login.RoleRepository;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    // Find a role by its name
    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }

    // Create a new role
    public Role createRole(String roleName, String description) {
        // Check if the role already exists
        if (roleRepository.findByName(roleName) != null) {
            throw new IllegalArgumentException("Role with name " + roleName + " already exists.");
        }

        // Create a new role and save it
        Role newRole = new Role();
        newRole.setName(roleName);
        newRole.setDescription(description);
        return roleRepository.save(newRole);
    }

    // Update role description
    public Role updateRoleDescription(String roleName, String newDescription) {
        Role role = roleRepository.findByName(roleName);

        if (role == null) {
            throw new IllegalArgumentException("Role with name " + roleName + " does not exist.");
        }

        role.setDescription(newDescription);
        return roleRepository.save(role);
    }

    // Delete a role by name
    public void deleteRole(String roleName) {
        Role role = roleRepository.findByName(roleName);

        if (role == null) {
            throw new IllegalArgumentException("Role with name " + roleName + " does not exist.");
        }

        roleRepository.delete(role);
    }

    // Check if a role exists by name
    public boolean roleExists(String roleName) {
        return roleRepository.findByName(roleName) != null;
    }
}

