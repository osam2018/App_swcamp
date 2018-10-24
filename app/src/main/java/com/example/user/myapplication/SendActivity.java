package com.example.user.myapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SendActivity extends AppCompatActivity implements View.OnClickListener {
    private UserInfo user;

    private EditText message;
    private RadioGroup sendTime;
    private RadioGroup sendUrgent;

    private String sendTimeString;
    private String sendUrgentString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        findViewById(R.id.sendButton).setOnClickListener(this);

        message = (EditText) findViewById(R.id.inputText);

        sendTime = (RadioGroup) findViewById(R.id.radioGroupTime);
        sendTime.setOnCheckedChangeListener(timeGroupChangeListener);

        sendUrgent = (RadioGroup) findViewById(R.id.radioGroupUrgent);
        sendUrgent.setOnCheckedChangeListener(urgentGroupChangeListener);

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(UserInfo.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    RadioGroup.OnCheckedChangeListener timeGroupChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.radio_first)
                sendTimeString = "최초";
            else if (checkedId == R.id.radio_mid)
                sendTimeString = "중간";
            else if (checkedId == R.id.radio_last)
                sendTimeString = "최종";
        }
    };
    RadioGroup.OnCheckedChangeListener urgentGroupChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.radio_high)
                sendUrgentString = "긴급";
            else if (checkedId == R.id.radio_soso)
                sendUrgentString = "보통";
            else if (checkedId == R.id.radio_last)
                sendUrgentString = "양호";
        }
    };

    @Override
    public void onClick(View v) {
        if (message.getText().toString().isEmpty() || sendTimeString.isEmpty() || sendUrgentString.isEmpty()) {
            Toast.makeText(getApplicationContext(), "내용을 모두 작성해주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }
        ChatData chatData = new ChatData();
        chatData.userGroup = "소속 : "+user.userGroup;
        chatData.userName = "사용자 : "+user.userName;
        chatData.messageExtra = "보고시기 : " + sendTimeString + " / 상황상태 : " +sendUrgentString;
        chatData.message = message.getText().toString();
        chatData.time = System.currentTimeMillis();
        FirebaseDatabase.getInstance().getReference().push().setValue(chatData);

        Toast.makeText(getApplicationContext(), "상황이 성공적으로 전파되었습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }
}