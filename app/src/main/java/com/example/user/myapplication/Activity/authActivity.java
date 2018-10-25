package com.example.user.myapplication.Activity;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.myapplication.R;
import com.example.user.myapplication.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class authActivity extends AppCompatActivity {

    //레이아웃 EditText 와 Button 변수들 선언
    private EditText email;
    private EditText name;
    private EditText group;
    private EditText password;
    private Button auth;

    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        //변수마다 각각 넣어주기
        name = (EditText) findViewById(R.id.authActivity_editText_name);
        group = (EditText) findViewById(R.id.authActivity_editText_group);
        email = (EditText) findViewById(R.id.authActivity_editText_id);
        password = (EditText) findViewById(R.id.authActivity_editText_password);
        auth = (Button) findViewById(R.id.authActivity_button_auth);
        //회원가입 버튼 클릭시 동작 지정
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //비밀번호 6자리 이상으로 강제
                if(password.getText().toString().length() < 6) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 6자리 이상으로 입력해주십시오.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //파이어베이스 함수로 회원가입
                FirebaseAuth.getInstance()
                        //입력 한 이메일과 비밀번호 스트링으로 유저생성함수 호출
                        .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(authActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    //회원가입 성공 시 데이터베이스 users - 생성한 유저 uid 에 UserInfo 객체 입력. (소속과 계급성명 저장 및 관리를 위해)
                                    final String uid = task.getResult().getUser().getUid();
                                    databaseReference = FirebaseDatabase.getInstance().getReference();
                                    databaseReference.child("users").child(uid).setValue(new UserInfo(name.getText().toString(), group.getText().toString()));
                                    Toast.makeText(getApplicationContext(), "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    //위에서 비밀번호 예외는 처리했으니 실패할 경우는 이메일 문제밖에 없음. 메시지를 띄운다
                                    email.setText("");
                                    Toast.makeText(getApplicationContext(), "이메일 형식이 맞지 않거나 사용 중입니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        //회원가입 버튼 색상 지정 (기본 버튼에서 색깔만 하늘색으로 지정)
        auth.getBackground().setColorFilter(0xFF0099cc, PorterDuff.Mode.MULTIPLY);
    }
}
