package com.upgrad.technical.service.business;


import com.upgrad.technical.service.dao.ImageDao;
import com.upgrad.technical.service.dao.UserDao;
import com.upgrad.technical.service.entity.ImageEntity;
import com.upgrad.technical.service.entity.UserAuthTokenEntity;
import com.upgrad.technical.service.exception.ImageNotFoundException;
import com.upgrad.technical.service.exception.UnauthorizedException;
import com.upgrad.technical.service.exception.UserNotSignedInException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@Service
public class AdminService {

    @Autowired
    private ImageDao imageDao;

    public ImageEntity getImage(final String imageUuid, final String authorization) throws ImageNotFoundException, UnauthorizedException, UserNotSignedInException {

        UserAuthTokenEntity userAuthTokenEntity = imageDao.getUserAuthToken(authorization);

        if(userAuthTokenEntity == null)
            throw new UserNotSignedInException("USR-001", "For image detail, You need to be signed in.");

        String role = userAuthTokenEntity.getUser().getRole();

        if(!role.equals("admin"))
            throw new UnauthorizedException("ATH-001", "UNAUTHORIZED Access, User is not an admin.");

        ImageEntity image = imageDao.getImage(imageUuid);

        if(image == null)
            throw new ImageNotFoundException("IMG-001", "No image found with this Uuid");

        return image;

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ImageEntity updateImage(final ImageEntity imageEntity, final String authorization) throws ImageNotFoundException, UnauthorizedException, UserNotSignedInException {
        UserAuthTokenEntity userAuthTokenEntity = imageDao.getUserAuthToken(authorization);

        if(userAuthTokenEntity == null)
            throw new UserNotSignedInException("USR-001", "Be a signed user to get the details of image");

        String role = userAuthTokenEntity.getUser().getRole();

        if(!role.equals("admin"))
            throw new UnauthorizedException("ATH-001", "UNAUTHERISED Acess, User is not admin.");

        ImageEntity updationImage = imageDao.getImageById(imageEntity.getId());

        if(updationImage == null)
            throw new ImageNotFoundException("IMG-001", "Image with id not found");

        updationImage.setImage(imageEntity.getImage());
        updationImage.setStatus(imageEntity.getStatus());
        updationImage.setDescription(imageEntity.getDescription());
        updationImage.setName(imageEntity.getName());

        imageDao.updateImage(updationImage);
        return updationImage;
    }
}
