package com.example.user.myapplication;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class loginActivity extends AppCompatActivity {
    private Button login, auth;
    private EditText id, pw;

    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        login = ((Button)findViewById(R.id.loginActivity_button_login));
        auth = ((Button)findViewById(R.id.loginActivity_button_auth));
        id = (EditText) findViewById(R.id.loginActivity_editText_id);
        pw = (EditText) findViewById(R.id.loginActivity_editText_password);

        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(loginActivity.this, authActivity.class));
            }
        });
        auth.getBackground().setColorFilter(0xFF0099cc, PorterDuff.Mode.MULTIPLY);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id.getText().toString().isEmpty() || pw.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_LONG).show();
                    return;
                }

                FirebaseAuth.getInstance().signInWithEmailAndPassword(id.getText().toString(), pw.getText().toString())
                        .addOnCompleteListener(loginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(loginActivity.this, MenuActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "실패하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        login.getBackground().setColorFilter(0xFF0099cc, PorterDuff.Mode.MULTIPLY);
    }
}
