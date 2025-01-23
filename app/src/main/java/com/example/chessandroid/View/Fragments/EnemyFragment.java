package com.example.chessandroid.View.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chessandroid.R;
import com.example.chessandroid.databinding.FragmentEnemyBinding;
import com.example.chessandroid.databinding.FragmentProfileBinding;


public class EnemyFragment extends Fragment {

    final private String TAG = "MainActivity";
    private final String nickname;
private FragmentEnemyBinding binding;
    public EnemyFragment(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding = FragmentEnemyBinding.inflate(inflater, container, false);

        if(nickname!=null){
            binding.playerNickname.setText(nickname);
        }
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}