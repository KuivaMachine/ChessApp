package com.example.chessandroid.View.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.chessandroid.Model.Room;
import com.example.chessandroid.Model.User;
import com.example.chessandroid.R;
import com.example.chessandroid.View.GameActivity;
import com.example.chessandroid.databinding.FragmentCreateNewGameBinding;
import com.example.chessandroid.enums.Players;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;


public class CreateNewGameDialog extends DialogFragment {
    private final String TAG = "MainActivity";
    private String myColorOfPieces = "White";
    private final User user;
   private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://test-f992b-default-rtdb.europe-west1.firebasedatabase.app");
    private final String gameID = String.valueOf(new Random().nextInt(8999) + 1000);
    private final DatabaseReference reference = database.getReference("Rooms").child(gameID).child("Room");
    private FragmentCreateNewGameBinding binding;
    public CreateNewGameDialog(User user) {
        this.user = user;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateNewGameBinding.inflate(inflater, container, false);


        binding.gameID.setText(gameID);
        Log.d(TAG, "gameID: "+gameID);


        SpannableString colorText = new SpannableString("Случайно");
        colorText.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        colorText.setSpan(new ForegroundColorSpan(Color.BLACK), 4, colorText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.randomLLText.setText(colorText);

        ArrayList<LinearLayout> colorsLayoutList = new ArrayList<>();
        colorsLayoutList.add(binding.blackLL);
        colorsLayoutList.add(binding.whiteLL);
        colorsLayoutList.add(binding.randomLL);


        LottieAnimationView whiteAnim = binding.pawnWhiteAnim;
        whiteAnim.pauseAnimation();
        whiteAnim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseColorOfPiece(colorsLayoutList, binding.whiteLL, binding.pawnWhiteAnim);
                myColorOfPieces = "White";
            }
        });

        LottieAnimationView blackAnim = binding.pawnBlackAnim;
        blackAnim.pauseAnimation();
        blackAnim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseColorOfPiece(colorsLayoutList, binding.blackLL, binding.pawnBlackAnim);

                myColorOfPieces = "Black";
            }
        });

        LottieAnimationView randomAnim = binding.randomAnim;
        randomAnim.pauseAnimation();
        randomAnim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseColorOfPiece(colorsLayoutList, binding.randomLL, binding.randomAnim);
                int random = new Random().nextInt(2);
                if (random == 0) {
                    myColorOfPieces = "White";
                } else {
                    myColorOfPieces = "Black";
                }
            }
        });

        binding.startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.startGameBtn.setVisibility(View.INVISIBLE);
                binding.backToSettingsBtn.setVisibility(View.VISIBLE);
                binding.gameSettingsLayout.setVisibility(View.INVISIBLE);
                binding.instructionLayout.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getContext(), GameActivity.class);
                intent.putExtra("gameID", gameID);
                switch (myColorOfPieces) {
                    case "White":
                        intent.putExtra("isInvertedBoard", false);
                        break;
                    case "Black":
                        intent.putExtra("isInvertedBoard", true);
                        break;
                }
                intent.putExtra("myColorOfPieces", myColorOfPieces);

                Room room = new Room(user, new User(), false, Players.PLAYER_1);
                if (myColorOfPieces.equals("Black")) {
                    room.setWho_plays_white(Players.PLAYER_2);
                }
                reference.setValue(room);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Room room = snapshot.getValue(Room.class);
                        if (room != null && room.isRoomFull()) {
                            binding.backToSettingsBtn.setVisibility(View.INVISIBLE);
                            User player_2 = room.getPlayer_2();
                            if(player_2!=null){
                                intent.putExtra("player_2_nick", player_2.getNickname());
                                startActivity(intent);
                                dismissAllowingStateLoss();
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //TODO:
                    }
                });


            }
        });

        binding.backToSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.backToSettingsBtn.setVisibility(View.INVISIBLE);
                binding.instructionLayout.setVisibility(View.INVISIBLE);
                binding.gameSettingsLayout.setVisibility(View.VISIBLE);
                binding.startGameBtn.setVisibility(View.VISIBLE);
                reference.removeValue();
            }
        });

        return binding.getRoot();

    }

    public void chooseColorOfPiece(ArrayList<LinearLayout> list, LinearLayout window, LottieAnimationView anim) {
        for (LinearLayout layout : list) {
            layout.setBackgroundResource(0);
        }
        window.setBackgroundResource(R.drawable.selected_color_shape);
        anim.playAnimation();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        reference.removeValue();
    }
}