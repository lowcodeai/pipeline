package com.lowcode.pipeline.service;

import com.lowcode.pipeline.dto.UserDTO;
import com.lowcode.pipeline.model.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
//    boolean isUserExists(String email);
    void registerUser(UserDTO userDTO);
    User getUser(String email);
}
