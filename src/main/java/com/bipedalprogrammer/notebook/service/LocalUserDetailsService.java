package com.bipedalprogrammer.notebook.service;

import com.bipedalprogrammer.notebook.sbthyme.repository.UserPersistence;
import com.bipedalprogrammer.notebook.sbthyme.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LocalUserDetailsService implements UserDetailsService {
    private UserPersistence userPersistence;

    @Autowired
    public LocalUserDetailsService(UserPersistence userPersistence) {
        this.userPersistence = userPersistence;
    }

    @Override
    public User loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userPersistence.findByUsername(username);
        if (user != null) {
            return user;
        }
        throw new UsernameNotFoundException(
                "User '" + username + "' not found");
    }
}
