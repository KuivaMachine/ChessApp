package com.example.chessandroid.View;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chessandroid.R;
import com.example.chessandroid.View.Fragments.ProfileFragment;
import com.example.chessandroid.databinding.ActivityMenuBinding;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class MenuActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMenuBinding binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigation bottomNavigation= binding.bottomNavigation;

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,  new ProfileFragment()).commit();
        bottomNavigation.setMenuItemSelectionListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(int i, int i1, boolean b) {
                Log.d(TAG, "i "+i);
                Log.d(TAG, "i1 "+i1);
                Log.d(TAG, "bool "+b);
            }

            @Override
            public void onMenuItemReselect(int i, int i1, boolean b) {
                Log.d(TAG, "i "+i);
                Log.d(TAG, "i1 "+i1);
                Log.d(TAG, "bool "+b);            }
        });



        /*ChessViewModel viewModel = new ViewModelProvider(this).get(ChessViewModel.class);
        viewModel.getUserData().observe(this, user -> {
        });*/


    }

}