package com.springboot.FlomadAIplanner.changelogs;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.springboot.FlomadAIplanner.model.entities.login.Role;
import com.springboot.FlomadAIplanner.model.repository.login.RoleRepository;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@ChangeUnit(id = "V1_1_LoadData", order = "001", author = "author", systemVersion = "1")
public class V1_1_LoadDataChangeUnit {

    private final RoleRepository roleRepository;

    public V1_1_LoadDataChangeUnit(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Execution
    public void loadRoles() throws IOException, CsvException {
        Resource resource = new ClassPathResource("migrations/initial/roles.csv");
        try (InputStream is = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             CSVReader reader = new CSVReader(isr)) {

            List<String[]> records = reader.readAll();

            // Skip header
            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                String id = record[0];
                String name = record[1];
                String description = record[2];

                // Check if role already exists
                if (!roleRepository.existsById(id)) {
                    Role role = new Role(id, name, description);
                    roleRepository.save(role);
                }
            }
        }
    }

    @RollbackExecution
    public void rollbackLoadRoles() {
        // Implement rollback logic if necessary
        // For example, remove the inserted roles
        roleRepository.deleteAll();
    }
}
