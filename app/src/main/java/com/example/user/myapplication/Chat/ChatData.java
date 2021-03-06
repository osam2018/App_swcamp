package com.example.user.myapplication.Chat;

public class ChatData {
    public String firebaseKey; // Firebase Realtime Database 에 등록된 Key 값
    public String userName; // 사용자 이름
    public String userGroup; // 사용자 소속
    public String messageExtra; //메시지 보고시기, 중요도 문자열
    public String message; // 작성한 메시지
    public String picName; // 사진
    public float gpsLatitude; //gps 경도 위도 주소
    public float gpsLongitude;
    public String gpsAddress;
    public long time; // 작성한 시간

    public ChatData() {//초기화
        gpsLongitude = 0;
        gpsLatitude = 0;
        picName = "";
    }
}