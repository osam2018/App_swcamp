package com.example.user.myapplication;

public class ChatData {
    public String firebaseKey; // Firebase Realtime Database 에 등록된 Key 값
    public String userName; // 사용자 이름
    public String userGroup; // 사용자 소속
    public String messageExtra; //메시지 보고시기, 중요도
    public String message; // 작성한 메시지
    public long time; // 작성한 시간
}