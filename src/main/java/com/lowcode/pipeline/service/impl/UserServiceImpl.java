package com.lowcode.pipeline.service.impl;

import com.lowcode.pipeline.dto.UserDTO;
import com.lowcode.pipeline.exception.InvalidInputException;
import com.lowcode.pipeline.model.User;
import com.lowcode.pipeline.repository.UserRepository;
import com.lowcode.pipeline.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of the UserService interface.
 * This class provides concrete implementations for managing users,
 * including user registration and retrieval of user information.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//    private final String encodedSalt = BCrypt.gensalt();

    public UserServiceImpl(
            UserRepository userRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
//
//    @Override
//    public boolean isUserExists(String email) {
//        return userRepository.findByEmail(email) != null;
//    }
//
    @Override
    public void registerUser(UserDTO userDTO) {

        String error = "";

        if (userRepository.findByUserName(userDTO.getUserName()) != null) {
            error = "A user with this email already exists";
        } else if(userDTO.getFirstName() == null || userDTO.getFirstName().isEmpty()) {
            error = "First name cannot be empty";
        } else if (userDTO.getLastName() == null || userDTO.getLastName().isEmpty()) {
            error = "Last name cannot be empty";
        } else if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
            error = "Password cannot be empty";
        } else if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            error = "Passwords do not match";
        }

        if (!error.isEmpty()) {
            throw new InvalidInputException(error);
        }

        ModelMapper modelMapper = new ModelMapper();
        User user = modelMapper.map(userDTO, User.class);
        user.setPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
        userRepository.save(user);

        // TODO: Implement the custom exception handler
//        try {
//            userRepository.save(user);
//            return 1;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public User getUser(String email) {
        return userRepository.findByUserName(email);
    }
}
