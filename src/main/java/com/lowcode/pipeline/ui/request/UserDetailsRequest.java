package com.lowcode.pipeline.ui.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDetailsRequest {

    private String firstName;
    private String lastName;
    private String domain;
    private String userName;
    private String password;
    private String confirmPassword;
}
