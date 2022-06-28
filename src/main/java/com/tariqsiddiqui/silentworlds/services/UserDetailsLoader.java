package com.tariqsiddiqui.silentworlds.services;

import com.tariqsiddiqui.silentworlds.repositories.UserRepo;
import com.tariqsiddiqui.silentworlds.models.User;
import com.tariqsiddiqui.silentworlds.models.UserWithRoles;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsLoader implements UserDetailsService {
    private final UserRepo usersDao;

    public UserDetailsLoader(UserRepo users) {
        this.usersDao = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = usersDao.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user found for " + username);
        }

        return new UserWithRoles(user);
    }

}
