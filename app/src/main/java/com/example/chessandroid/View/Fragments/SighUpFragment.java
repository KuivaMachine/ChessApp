package com.example.chessandroid.View.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.chessandroid.Model.User;
import com.example.chessandroid.View.MenuActivity;
import com.example.chessandroid.ViewModel.ChessViewModel;
import com.example.chessandroid.databinding.FragmentSighUpBinding;
import com.google.firebase.auth.FirebaseAuth;


public class SighUpFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FragmentSighUpBinding binding;
    final private String TAG = "MainActivity";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSighUpBinding.inflate(inflater, container, false);


        ChessViewModel viewModel = new ViewModelProvider(this).get(ChessViewModel.class);
        Intent intent = new Intent(getContext(), MenuActivity.class);


        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.emailSighupEt.getText().toString().isEmpty() || binding.passwordSighupEt.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Поля должны быть заполнены", Toast.LENGTH_SHORT).show();
                } else {
                    User user = new User(binding.usernameSighupEt.getText().toString(), binding.emailSighupEt.getText().toString(), binding.passwordSighupEt.getText().toString(), "User");
                    if(binding.adminCheckBox.isChecked()){
                        user.setMode("Admin");
                    }
                    intent.putExtra("nickname", user.getNickname());
                    viewModel.sighUp(user, binding.emailSighupEt.getText().toString(), binding.passwordSighupEt.getText().toString());
                }
            }
        });

        viewModel.sighUpResult()
                .observe(getViewLifecycleOwner(), task -> {
                    if (task.isSuccessful()) {
                        startActivity(intent);
                    }
                });


        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}