package com.scheduler.scheduler.data;

import java.io.Serializable;

public class UserResponse implements Serializable {
    private String fullname;
    private String email;
    private boolean time1;
    private boolean time2;
    private boolean time3;
    private boolean time4;
    private boolean time5;


    public UserResponse() {
    }

    public UserResponse(String fullname, String email) {
        this.fullname = fullname;
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isTime1() {
        return time1;
    }

    public void setTime1(boolean time1) {
        this.time1 = time1;
    }

    public boolean isTime2() {
        return time2;
    }

    public void setTime2(boolean time2) {
        this.time2 = time2;
    }

    public boolean isTime3() {
        return time3;
    }

    public void setTime3(boolean time3) {
        this.time3 = time3;
    }

    public boolean isTime4() {
        return time4;
    }

    public void setTime4(boolean time4) {
        this.time4 = time4;
    }

    public boolean isTime5() {
        return time5;
    }

    public void setTime5(boolean time5) {
        this.time5 = time5;
    }
}
