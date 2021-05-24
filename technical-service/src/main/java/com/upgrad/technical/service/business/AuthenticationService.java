package com.upgrad.technical.service.business;


import com.upgrad.technical.service.dao.UserDao;
import com.upgrad.technical.service.entity.UserAuthTokenEntity;
import com.upgrad.technical.service.entity.UserEntity;
import com.upgrad.technical.service.exception.AuthenticationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class AuthenticationService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider CryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity authenticate(final String username, final String password) throws AuthenticationFailedException {
        UserEntity userEntity = userDao.getUserByEmail(username);

        if(userEntity == null)
            throw new AuthenticationFailedException("ATH-001", "User with email not found.");

        final String encryptPassword = CryptographyProvider.encrypt(password, userEntity.getSalt());

        if(encryptPassword.equals(userEntity.getPassword())){

            JwtTokenProvider jwtTokenGenerator = new JwtTokenProvider(encryptPassword);
            UserAuthTokenEntity userAuthToken = new UserAuthTokenEntity();
            userAuthToken.setUser(userEntity);

            final ZonedDateTime currentTime = ZonedDateTime.now();
            final ZonedDateTime validTill = currentTime.plusHours(18);

            userAuthToken.setAccessToken(jwtTokenGenerator.generateToken(userEntity.getUuid(), currentTime , validTill));
            userAuthToken.setExpiresAt(validTill);
            userAuthToken.setLoginAt(currentTime);

            userDao.createAuthToken(userAuthToken);
            userEntity.setLastLoginAt(currentTime);
            userDao.updateUser(userEntity);

            return userAuthToken;
        }
        else {
            throw new AuthenticationFailedException("ATH-002", "Password failure");
        }
    }
}


