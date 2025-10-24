package com.lowcode.pipeline.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO implements Serializable {

//    TODO: Update the value with rightfulluy generated value or with IDE
    private static final long serialVersionUID = 9132968119495994264L;

    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String confirmPassword;
    private String domain;

}
