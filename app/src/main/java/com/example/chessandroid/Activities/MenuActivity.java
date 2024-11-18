package com.example.chessandroid.Activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.chessandroid.R;
import com.example.chessandroid.databinding.ActivityMenuBinding;
import com.google.android.material.navigation.NavigationView;

public class MenuActivity extends AppCompatActivity {
private AppBarConfiguration appBarConfiguration;
private NavController navController;
    static String gameID = "";
    Button play;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMenuBinding binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavigationView navigationView = binding.navView;
        DrawerLayout drawer = binding.drawerLayoutActivityMenu;

setSupportActionBar(binding.abm.toolbar);
       appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.profile, R.id.settings,R.id.users)
                .setOpenableLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.fragmentContainerView);
       NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


    }

   @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

}