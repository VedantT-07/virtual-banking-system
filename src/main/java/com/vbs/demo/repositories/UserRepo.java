package com.vbs.demo.repositories;

import com.vbs.demo.models.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);//extra

    User findByUsername(String username);

    boolean existsByEmail(String newValue);//extra


    List<User> findAllByRole(String customer, Sort sort);

    List<User> findAllByUsernameContainingIgnoreCaseAndRole(String keyword, String customer);
}
