package com.example.chessandroid.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chessandroid.ChessGame.GameActivity;
import com.example.chessandroid.classes.Room;
import com.example.chessandroid.classes.User;
import com.example.chessandroid.databinding.FragmentProfileBinding;
import com.example.chessandroid.enums.Players;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {
    final private String TAG = "MainActivity";
    FragmentProfileBinding binding;
    FirebaseDatabase database;
    DatabaseReference reference;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance("https://test-f992b-default-rtdb.europe-west1.firebasedatabase.app");
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        reference = database.getReference("User").child(currentUser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
               // Log.d(TAG, user.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //TODO:
            }
        });

        binding.createGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "START THE GAME");
                String gameID = "start";
                //TODO надо сделать выбор цвета первого хода
                Room room = new Room(user, new User(), false, Players.PLAYER_1);
                Intent intent = new Intent(getContext(), GameActivity.class);
                intent.putExtra("gameID", gameID);
                intent.putExtra("isInvertedBoard", false);

                reference = database.getReference(gameID).child("Room");
                Log.d(TAG, "OLD ROOM before SEND:");
                Log.d(TAG, room.toString());
                reference.setValue(room);

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Room room = snapshot.getValue(Room.class);
                        Log.d(TAG, "on data change");
                        Log.d(TAG, "OLD ROOM written");
                        Log.d(TAG, room.toString());
                       // Log.d(TAG, room.toString());
                        if (room.isRoomFull()) {
                            Log.d(TAG, "room is full");
                            Log.d(TAG, room.toString());
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //TODO:
                    }
                });
            }
        });


        binding.joinGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String gameID = "start";//TODO надо переделать на код из поля для ввода

                reference = database.getReference(gameID).child("Room");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Room room = snapshot.getValue(Room.class);
                        User player_1 = room.getPlayer_1();
                        Log.d(TAG, "JOIN THE GAME");
                        Log.d(TAG, "OLD ROOM:");
                        Log.d(TAG, room.toString());
                        if (!room.isRoomFull()&&player_1!=null) {
                            Room roomFull = new Room(player_1, user, true, room.getWho_is_white());
                            Log.d(TAG, "NEW ROOM:");
                            Log.d(TAG, roomFull.toString());
                            reference.setValue(roomFull);


                            Intent intent = new Intent(getContext(), GameActivity.class);
                            intent.putExtra("gameID", gameID);
                            switch (room.getWho_is_white()){
                                case PLAYER_1:intent.putExtra("isInvertedBoard", true);
                                case PLAYER_2:intent.putExtra("isInvertedBoard", false);
                            }

                            startActivity(intent);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }


                });
            }
        });

binding.roomBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        String gameID = "start";
        reference = database.getReference(gameID).child("Room");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, snapshot.getValue(Room.class).toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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


