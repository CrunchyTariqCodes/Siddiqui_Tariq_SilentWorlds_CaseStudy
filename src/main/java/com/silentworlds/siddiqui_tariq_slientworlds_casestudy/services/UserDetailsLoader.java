package com.silentworlds.siddiqui_tariq_slientworlds_casestudy.services;

import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.User;
import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.UserWithRoles;
import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.repositories.UserRepo;
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
