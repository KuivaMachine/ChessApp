package com.example.chessandroid.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.chessandroid.R;
import com.example.chessandroid.View.Fragments.LoginFragment;
import com.example.chessandroid.View.Fragments.SighUpFragment;
import com.example.chessandroid.ViewModel.ChessViewModel;
import com.example.chessandroid.databinding.ActivityAuthBinding;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {
    final private String TAG = "MainActivity";
    private ActivityAuthBinding binding;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_FIRST_RUN = "isFirstRun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean(KEY_FIRST_RUN, true);


        if (isFirstRun) {
            runSighUp();
            sharedPreferences.edit().putBoolean(KEY_FIRST_RUN, false).apply();
        } else {
            runLogIn();
        }


        binding.sighUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runSighUp();
            }
        });

        binding.logInLink.setOnClickListener(view -> {
            runLogIn();
        });

    }

    public void runLogIn() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new LoginFragment()).commit();
        binding.switchToSighUpFragText.setVisibility(View.VISIBLE);
        binding.switchToLogInFragText.setVisibility(View.INVISIBLE);
    }

    public void runSighUp() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new SighUpFragment()).commit();
        binding.switchToSighUpFragText.setVisibility(View.INVISIBLE);
        binding.switchToLogInFragText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}