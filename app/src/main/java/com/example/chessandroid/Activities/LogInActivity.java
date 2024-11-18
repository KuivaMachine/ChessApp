package com.example.chessandroid.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.chessandroid.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button login_btn;
    private EditText email_login, password_login;
    private TextView sighup;
    final private String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        sighup = findViewById(R.id.go_to_register_activity_tv);
        email_login = findViewById(R.id.email_login_et);
        password_login = findViewById(R.id.password_login_et);
        login_btn = findViewById(R.id.login_btn);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LogInActivity.this, MenuActivity.class));
        }
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email_login.getText().toString().isEmpty() || password_login.getText().toString().isEmpty()) {
                    startActivity(new Intent(LogInActivity.this, MenuActivity.class));
                    Toast.makeText(LogInActivity.this, "Поля должны быть заполнены", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email_login.getText().toString(), password_login.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Log.d(TAG, user.toString());
                                        startActivity(new Intent(LogInActivity.this, MenuActivity.class));
                                    } else {
                                        Toast.makeText(LogInActivity.this, "Не удалось войти", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, task.getException().toString());
                                    }
                                }
                            });
                }
            }
        });

        sighup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogInActivity.this, SighUpActivity.class));
            }
        });

    }
}