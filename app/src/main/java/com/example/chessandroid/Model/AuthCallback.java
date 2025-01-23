package com.example.chessandroid.Model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public interface AuthCallback {
    void returnAuthResult(Task<AuthResult> task);
}
