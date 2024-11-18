package com.example.chessandroid.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chessandroid.R;
import com.example.chessandroid.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SighUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ImageButton back_to_login_btn;
    private Button sighup_btn;
    private EditText email_sighup, password_sighup, username_sighup;
    final private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sigh_up);


        sighup_btn = findViewById(R.id.sign_up_btn);
        email_sighup = findViewById(R.id.email_sighup_et);
        password_sighup = findViewById(R.id.password_sighup_et);
        username_sighup = findViewById(R.id.username_sighup_et);
        back_to_login_btn = findViewById(R.id.back_to_login_btn);

        /*FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(SighUpActivity.this, MainActivity.class));
        }
*/
        sighup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email_sighup.getText().toString().isEmpty() || password_sighup.getText().toString().isEmpty()) {
                    Toast.makeText(SighUpActivity.this, "Поля должны быть заполнены", Toast.LENGTH_SHORT).show();
                } else {

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email_sighup.getText().toString(), password_sighup.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                      //  Log.d(TAG, "Успешно");
                                        User user = new User(username_sighup.getText().toString(), email_sighup.getText().toString(), password_sighup.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
                                        startActivity(new Intent(SighUpActivity.this, MenuActivity.class));
                                    } else {
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(SighUpActivity.this, "Не удалось зарегистировать пользователя", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }
            }
        });

        back_to_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SighUpActivity.this, LogInActivity.class));
            }
        });
    }
}