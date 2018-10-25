package com.example.user.myapplication.Activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class loginActivity extends AppCompatActivity {

    //레이아웃 Button 과 EditText 관리를 위한 변수 선언
    private Button login, auth;
    private EditText id, pw;

    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //처음 로그인화면에서는 앱 로고와 이름을 레이아웃에 표시해두었으니 액션바 숨김
        getSupportActionBar().hide();

        //각각 객체를 불러온다
        login = ((Button)findViewById(R.id.loginActivity_button_login));
        auth = ((Button)findViewById(R.id.loginActivity_button_auth));
        id = (EditText) findViewById(R.id.loginActivity_editText_id);
        pw = (EditText) findViewById(R.id.loginActivity_editText_password);

        //회원가입 버튼 눌렀을 시 회원가입 액티비티로 전환
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(loginActivity.this, authActivity.class));
            }
        });
        //회원가입 버튼 색 변경 (하늘색)
        auth.getBackground().setColorFilter(0xFF0099cc, PorterDuff.Mode.MULTIPLY);

        //로그인 버튼 클릭시 동작 지정
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 아이디 비밀번호 입력되지 않은 상황 예외처리
                if (id.getText().toString().isEmpty() || pw.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_LONG).show();
                    return;
                }

                //파이어베이스 함수로 로그인 시도
                FirebaseAuth.getInstance().signInWithEmailAndPassword(id.getText().toString(), pw.getText().toString())
                        .addOnCompleteListener(loginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //로그인 성공 시 메뉴 액티비티로 전환하고 액티비티 종료
                                    startActivity(new Intent(loginActivity.this, MenuActivity.class));
                                    finish();
                                } else {
                                    //실패 시 메시지 띄움
                                    Toast.makeText(getApplicationContext(), "실패하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        //로그인 버튼 색 하늘색으로 지정
        login.getBackground().setColorFilter(0xFF0099cc, PorterDuff.Mode.MULTIPLY);
    }
}
