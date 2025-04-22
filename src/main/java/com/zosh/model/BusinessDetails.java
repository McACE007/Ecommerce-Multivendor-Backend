package com.zosh.model;

import lombok.Data;

@Data
public class BusinessDetails {
    private String businessName;
    private String businessEmail;
    private String businessMobile;
    private String businessAddress;
    private String logo;
    private String banner;

    public BusinessDetails() {
        businessName = "";
        businessEmail = "";
        businessMobile = "";
        businessAddress = "";
        logo = "";
        banner = "";
    }
}
