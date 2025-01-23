package com.example.chessandroid.View.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chessandroid.View.GameActivity;
import com.example.chessandroid.Model.Room;
import com.example.chessandroid.Model.User;
import com.example.chessandroid.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {
    private final String TAG = "MainActivity";
    private FragmentProfileBinding binding;
    private User user;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance( "https://test-f992b-default-rtdb.europe-west1.firebasedatabase.app");
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference reference = database.getReference("User").child(currentUser.getUid());

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    user = snapshot.getValue(User.class);
                    Log.d(TAG, "GET USER "+user.toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //TODO:
                }
            });
        }

        binding = FragmentProfileBinding.inflate(inflater, container, false);



        binding.createGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "USER: "+user);
                CreateNewGameDialog createNewGameDialog = new CreateNewGameDialog(user);
                createNewGameDialog.show(getChildFragmentManager(), "CREATENEWGAME");


            }
        });


        binding.joinGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JoinTheGameDialog dialog = new JoinTheGameDialog(user);
                dialog.show(getChildFragmentManager(), "JOINTHEGAME");

            }
        });

        binding.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GameActivity.class);
                intent.putExtra("gameID", "start");
                intent.putExtra("isInvertedBoard", false);
                intent.putExtra("myColorOfPieces", "White");
                intent.putExtra("player_2_nick", "TEST");
                startActivity(intent);

            }
        });


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


