package com.example.user.myapplication.Activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.user.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MenuActivity extends AppCompatActivity {

    //레이아웃 버튼, 텍스트 뷰들 선언
    private Button btn_notReceived, btn_received, btn_sendActivity;
    private TextView txt_userName, txt_userGroup;

    //데이터베이스 접근용이를 위한 변수선언
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference chkDatabaseReference;
    private ChildEventListener mChildEventListener;

    //사용자 uid를 담을 변수
    private String uid;

    //미수신상황 갯수, 전체상황 갯수 를 저장할 변수 선언
    private int countReceived, countNotReceived;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //버튼 객체 가져오기
        btn_notReceived = findViewById(R.id.btn_notReceived);
        btn_received = findViewById(R.id.btn_received);
        btn_sendActivity = findViewById(R.id.btn_sendActivity);

        //버튼들 클릭시 각자 액티비티로 전환, 버튼들 색깔 지정
        btn_notReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, NotReceivedActivity.class));
                finish();
            }
        });
        btn_received.getBackground().setColorFilter(0xFF33b5e5, PorterDuff.Mode.MULTIPLY);
        btn_received.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, MainActivity.class));
                finish();
            }
        });
        btn_sendActivity.getBackground().setColorFilter(0xFFcc0000, PorterDuff.Mode.MULTIPLY);
        btn_sendActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, SendActivity.class));
                finish();
            }
        });

        //미수신갯수,전체갯수 일단 0으로 쵝화
        countReceived = 0;
        countNotReceived = 0;

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("messages");
        chkDatabaseReference = mFirebaseDatabase.getReference("users").child(uid).child("received");
        //messages 하위항목에서 사용할 리스너 생성
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //messages 하위항목에서 상황데이터를 발견했을 시
                //users - uid - received 항목 하위에 지금 발견한 상황데이터의 키 값이 들어있는지 확인
                chkDatabaseReference.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //들어있지 않다면, 수신확인하지 않은 상황이므로 미수신상황갯수에 1 더함
                        if(!dataSnapshot.exists()) countNotReceived++;
                        //들어있으면 수신된 상황. 전체상황갯수에 1 더함
                        else countReceived++;

                        //각자 버튼에 숫자 표시
                        btn_received.setText("상  황  목  록\n["+Integer.toString(countReceived)+"]");
                        if(countNotReceived == 0) {
                            btn_notReceived.setText("미수신 상황\n없음");
                        }else {
                            btn_notReceived.setText("미수신 상황\n접수\n[" + Integer.toString(countNotReceived) + "]");
                            //미수신상황이 있으면 버튼색깔 빨간색으로
                            btn_notReceived.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        //messages 하위항목 리스너로 방금 생성한 리스너 지정
        mDatabaseReference.addChildEventListener(mChildEventListener);

        //users - uid 에서 유저계급성명, 유저소속을 받아와서 텍스트뷰에 입력
        FirebaseDatabase.getInstance().getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txt_userName = findViewById(R.id.txt_menuUserName);
                txt_userName.setText(": "+dataSnapshot.child("userName").getValue(String.class).trim());
                txt_userGroup = findViewById(R.id.txt_menuUserGroup);
                txt_userGroup.setText(": "+dataSnapshot.child("userGroup").getValue(String.class).trim());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //버튼 정보 최신화
        btn_received.setText("상  황  목  록\n["+Integer.toString(countReceived)+"]");
        if(countNotReceived == 0) {
            btn_notReceived.setText("미수신 상황\n없음");
        }else {
            btn_notReceived.setText("미수신 상황\n접수\n[" + Integer.toString(countNotReceived) + "]");
            btn_notReceived.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        }
    }

    //액티비티 종료 시 리스너제거
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseReference.removeEventListener(mChildEventListener);
    }
}
