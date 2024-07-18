package com.springboot.FlomadAIplanner.model.repository.login;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);

}
