package com.example.user.myapplication;

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

public class authActivity extends AppCompatActivity {
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
        name = (EditText) findViewById(R.id.authActivity_editText_name);
        group = (EditText) findViewById(R.id.authActivity_editText_group);
        email = (EditText) findViewById(R.id.authActivity_editText_id);
        password = (EditText) findViewById(R.id.authActivity_editText_password);
        auth = (Button) findViewById(R.id.authActivity_button_auth);
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password.getText().toString().length() < 6) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 6자리 이상으로 입력해주십시오.", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(authActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                final String uid = task.getResult().getUser().getUid();

                                databaseReference = FirebaseDatabase.getInstance().getReference();
                                databaseReference.child("users").child(uid).setValue(new UserInfo(name.getText().toString(), group.getText().toString()));
                                Toast.makeText(getApplicationContext(), "회원가입을 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
            }
        });
    }
}
