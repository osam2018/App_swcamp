package com.example.user.myapplication;

//유저 계급이름 과 소속 저장을 위한 클래스
public class UserInfo {
    public String userName;
    public String userGroup;

    public UserInfo() {
    }

    public UserInfo(String name, String group) {
        userName = name;
        userGroup = group;
    }
}