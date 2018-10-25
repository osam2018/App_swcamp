package com.example.user.myapplication;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MenuActivity extends AppCompatActivity {
    private Button btn_notReceived, btn_received, btn_sendActivity;
    private TextView txt_userName, txt_userGroup;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference chkDatabaseReference;
    private ChildEventListener mChildEventListener;

    private String uid;

    private int countReceived, countNotReceived;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btn_notReceived = findViewById(R.id.btn_notReceived);
        btn_received = findViewById(R.id.btn_received);
        btn_sendActivity = findViewById(R.id.btn_sendActivity);

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
        countReceived = 0;
        countNotReceived = 0;

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("ㅅㅂ",uid);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("messages");
        chkDatabaseReference = mFirebaseDatabase.getReference("users").child(uid).child("received");
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                chkDatabaseReference.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()) countNotReceived++;
                        else countReceived++;
                        btn_received.setText("상  황  목  록\n["+Integer.toString(countReceived)+"]");
                        if(countNotReceived == 0) {
                            btn_notReceived.setText("미수신 상황\n없음");
                        }else {
                            btn_notReceived.setText("미수신 상황\n접수\n[" + Integer.toString(countNotReceived) + "]");
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

                if(dataSnapshot.getValue(int.class) == 1) {
                    countReceived--;
                    chkDatabaseReference.child(dataSnapshot.getKey()).removeValue();
                }
                else countNotReceived--;
                btn_received.setText("상  황  목  록\n["+Integer.toString(countReceived)+"]");
                if(countNotReceived == 0) {
                    btn_notReceived.setText("미수신 상황\n없음");
                }else {
                    btn_notReceived.setText("미수신 상황\n접수\n[" + Integer.toString(countNotReceived) + "]");
                    btn_notReceived.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);

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

        btn_received.setText("상  황  목  록\n["+Integer.toString(countReceived)+"]");
        if(countNotReceived == 0) {
            btn_notReceived.setText("미수신 상황\n없음");
        }else {
            btn_notReceived.setText("미수신 상황\n접수\n[" + Integer.toString(countNotReceived) + "]");
            btn_notReceived.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        }
    }@Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseReference.removeEventListener(mChildEventListener);
    }
}
