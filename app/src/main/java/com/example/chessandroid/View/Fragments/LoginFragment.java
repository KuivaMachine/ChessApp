package com.example.chessandroid.View.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chessandroid.R;
import com.example.chessandroid.View.AuthActivity;
import com.example.chessandroid.View.MenuActivity;
import com.example.chessandroid.ViewModel.ChessViewModel;
import com.example.chessandroid.databinding.FragmentLoginBinding;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment {

    FragmentLoginBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);

        ChessViewModel viewModel = new ViewModelProvider(this).get(ChessViewModel.class);
        FirebaseUser currentUser = viewModel.getCurrentUser();
        Intent intent = new Intent(getContext(), MenuActivity.class);
        if (currentUser != null) {
            startActivity(intent);
        }


        binding.loginBtn.setOnClickListener(view -> {
            if (binding.emailLoginEt.getText().toString().isEmpty() || binding.passwordLoginEt.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.logIn(binding.emailLoginEt.getText().toString(),binding.passwordLoginEt.getText().toString());
            }
        });
        viewModel.logInResult().observe(getViewLifecycleOwner(), task -> {
            if (task.isSuccessful()) {
                startActivity(intent);
            }
        });



        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding=null;
    }
}