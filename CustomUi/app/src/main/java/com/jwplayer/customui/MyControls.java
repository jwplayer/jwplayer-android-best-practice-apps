package com.jwplayer.customui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.UiGroup;
import com.jwplayer.pub.ui.viewmodels.NextUpViewModel;

public class MyControls extends ConstraintLayout {

    private MyNextUpView nextUpView;

    public MyControls(Context context) {
        this(context, null);
    }

    public MyControls(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyControls(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyControls(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.view_my_controls, this);
        nextUpView = findViewById(R.id.my_next_up);
    }

    public void bind(JWPlayer player, LifecycleOwner lifecycleOwner) {
        // Bind Views
        NextUpViewModel nextUpVM = (NextUpViewModel) player.getViewModelForUiGroup(UiGroup.NEXT_UP);
        nextUpView.bind(nextUpVM, lifecycleOwner);
    }

}
