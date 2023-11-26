package com.winnie.app.userbean;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

public class LoginForm implements Serializable {
    private String emailPlaceHolder="email";
   /* private String usernamePlaceHolder="username";*/
    private String passwordPlaceHolder="password";

    private String timeToLogin;



   /* public String getUsernamePlaceHolder() {
        return usernamePlaceHolder;
    }*/

    public String getEmailPlaceHolder() {
        return emailPlaceHolder;
    }
/*
    public void setUsernamePlaceHolder(String usernamePlaceHolder) {
        this.usernamePlaceHolder = usernamePlaceHolder;
    }*/

    public void setEmailPlaceHolder(String emailPlaceHolder) {
        this.emailPlaceHolder = emailPlaceHolder;
    }

    public String getPasswordPlaceHolder() {
        return passwordPlaceHolder;
    }

    public void setPasswordPlaceHolder(String passwordPlaceHolder) {
        this.passwordPlaceHolder = passwordPlaceHolder;
    }
    public String getTimeToLogin() {
        if(timeToLogin==null)
             timeToLogin= DateFormat.getDateTimeInstance().format(new Date());
        return timeToLogin;
    }

    public void setTimeToLogin(String timeToLogin) {
        this.timeToLogin = timeToLogin;
    }
}
