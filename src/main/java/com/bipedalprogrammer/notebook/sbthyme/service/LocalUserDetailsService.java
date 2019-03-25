package com.bipedalprogrammer.notebook.sbthyme.service;

import com.bipedalprogrammer.notebook.sbthyme.repository.UserPersistence;
import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.UserObject;
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
    public UserObject loadUserByUsername(String username)
            throws UsernameNotFoundException {
        UserObject userObject = userPersistence.findByUsername(username);
        if (userObject != null) {
            return userObject;
        }
        throw new UsernameNotFoundException(
                "UserObject '" + username + "' not found");
    }
}
