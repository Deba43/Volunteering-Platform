package com.cvp.frontend.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String website;
    private String location;
    private String email;
    private String phoneNumber;
    private String password;

}
