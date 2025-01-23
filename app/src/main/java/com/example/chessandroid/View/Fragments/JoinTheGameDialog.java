package com.example.chessandroid.View.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.chessandroid.Model.Room;
import com.example.chessandroid.Model.User;
import com.example.chessandroid.View.GameActivity;
import com.example.chessandroid.databinding.JoinTheGameDialogBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinTheGameDialog extends DialogFragment {
    private String gameID;
    private final User user;
    private final String TAG = "MainActivity";
    private  JoinTheGameDialogBinding binding;

    public JoinTheGameDialog(User user) {
        this.user = user;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = JoinTheGameDialogBinding.inflate(inflater, container, false);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://test-f992b-default-rtdb.europe-west1.firebasedatabase.app");


        EditText gameIDEt = binding.gameIDEt;
        gameIDEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s!=null&&s.toString().length()==4){
                    gameID = s.toString();
                    Log.d(TAG, "gameID: "+gameID);
                    DatabaseReference reference = database.getReference("Rooms").child(gameID).child("Room");
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Room room = snapshot.getValue(Room.class);
                            if (room != null) {
                                User player_1 = room.getPlayer_1();

                                if (!room.isRoomFull()&&player_1!=null) {
                                    Room roomFull = new Room(player_1, user, true, room.getWho_plays_white());
                                    reference.setValue(roomFull);

                                    Intent intent = new Intent(getContext(), GameActivity.class);
                                    intent.putExtra("gameID", gameID);
                                    intent.putExtra("player_2_nick", player_1.getNickname());
                                    switch (room.getWho_plays_white()) {
                                        case PLAYER_1:
                                            intent.putExtra("isInvertedBoard", true);
                                            intent.putExtra("myColorOfPieces", "Black");
                                            break;
                                        case PLAYER_2:
                                            intent.putExtra("isInvertedBoard", false);
                                            intent.putExtra("myColorOfPieces", "White");
                                            break;
                                    }

                                    startActivity(intent);
                                    dismissAllowingStateLoss();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }


                    });
                }
            }
        });






        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog!=null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}