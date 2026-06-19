package com.lowcode.pipeline.ui.controller;

import com.lowcode.pipeline.dto.UserDTO;
import com.lowcode.pipeline.service.UserService;
import com.lowcode.pipeline.ui.request.UserDetailsRequest;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user") // http://localhost:8080/user
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public void registerUser(@RequestBody UserDetailsRequest userDetailst) throws Exception{

        ModelMapper modelMapper = new ModelMapper();
        UserDTO userDTO = modelMapper.map(userDetailst, UserDTO.class);

        userService.registerUser(userDTO);

    }
}
