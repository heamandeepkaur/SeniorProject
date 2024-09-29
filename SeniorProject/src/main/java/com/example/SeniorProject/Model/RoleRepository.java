package com.example.SeniorProject.Model;

import com.example.SeniorProject.Model.Role;
import com.example.SeniorProject.Model.RoleEnum;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {
        Optional<Role> findByName(RoleEnum name);
}