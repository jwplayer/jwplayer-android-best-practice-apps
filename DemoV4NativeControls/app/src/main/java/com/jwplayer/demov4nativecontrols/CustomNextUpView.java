package com.jwplayer.demov4nativecontrols;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.jwplayer.pub.ui.viewmodels.NextUpViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;

public class CustomNextUpView extends ConstraintLayout {

    private TextView mNextupTitleTextView;
    private String mNextUpTitle = "";
    private int mTimeUntilNextItem = 10;

    public CustomNextUpView(@NonNull Context context) {
        this(context,null);
    }

    public CustomNextUpView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomNextUpView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.custom_nextup_layout, this);
        mNextupTitleTextView = findViewById(R.id.next_up_title_textview);
    }

    public void setNextUpViewModel(NextUpViewModel model){
        model.getTitle().observeForever(new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mNextUpTitle = s;
                updateNextUpText();
            }
        });

        model.getNextUpTimeRemaining().observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                mTimeUntilNextItem = integer.intValue();
                updateNextUpText();
            }
        });
    }

    private void updateNextUpText(){
        mNextupTitleTextView.setText(mNextUpTitle + " in " + Integer.toString(mTimeUntilNextItem));
    }
}
