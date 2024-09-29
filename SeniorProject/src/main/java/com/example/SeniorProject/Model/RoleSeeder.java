package com.example.SeniorProject.Model;

import com.example.SeniorProject.Model.Role;
import com.example.SeniorProject.Model.RoleEnum;
import com.example.SeniorProject.Model.RoleRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {
        private final RoleRepository roleRepository;


        public RoleSeeder(RoleRepository roleRepository) {
                this.roleRepository = roleRepository;
        }

        @Override
        public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
                this.loadRoles();
        }

        private void loadRoles() {
                RoleEnum[] roleNames = new RoleEnum[] { RoleEnum.USER, RoleEnum.ADMIN};
                Map<RoleEnum, String> roleDescriptionMap = Map.of(
                        RoleEnum.USER, "Default user role",
                        RoleEnum.ADMIN, "Administrator role"
                );

                Arrays.stream(roleNames).forEach((roleName) -> {
                        Optional<Role> optionalRole = roleRepository.findByName(roleName);

                        optionalRole.ifPresentOrElse(System.out::println, () -> {
                                Role roleToCreate = new Role();

                                roleToCreate.setName(roleName);
                                roleToCreate.setDescription(roleDescriptionMap.get(roleName));

                                roleRepository.save(roleToCreate);
                        });
                });
        }
}