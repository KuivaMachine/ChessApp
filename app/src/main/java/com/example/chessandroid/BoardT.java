package com.example.chessandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BoardT extends View {
    Paint paint = new Paint();
    int defX = 0;
    int defY = 0;
    int squareSide = 80;
    int x = 0;
    int y= 0;
    int z =120;
    public BoardT(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.MAGENTA);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j <8 ; j++) {
            canvas.drawRect(x,y,x+z,y+z,paint);
            x+=z;
            }
            x=0;
            y+=z;
        }





        canvas.drawARGB(80, 102, 404, 455);

       /* paint.setColor(Color.GREEN);
        paint.setStrokeWidth(20);
        boolean colorWB = true;
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++) {
                if (colorWB) {
                    paint.setColor(Color.GRAY);
                } else {
                    paint.setColor(Color.GREEN);
                }
                colorWB = !colorWB;
                canvas.drawRect(defX + (squareSide * i), defY, defX + (squareSide * (i + 1)), (defY + squareSide), paint);
            }
            defY += squareSide;
            colorWB = !colorWB;
        }*/
    }
}
