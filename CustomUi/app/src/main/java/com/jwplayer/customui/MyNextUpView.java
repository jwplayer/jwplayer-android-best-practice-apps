package com.jwplayer.customui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.imageview.ShapeableImageView;
import com.jwplayer.pub.ui.viewmodels.NextUpViewModel;
import com.squareup.picasso.Picasso;

public class MyNextUpView extends ConstraintLayout {

    private TextView title;
    private TextView countdown;
    private ShapeableImageView poster;
    private View close;

    public MyNextUpView(Context context) {
        this(context, null);
    }

    public MyNextUpView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyNextUpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyNextUpView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.view_my_next_up, this);
        title = findViewById(R.id.next_title);
        countdown = findViewById(R.id.next_count);
        poster = findViewById(R.id.next_poster);
        close = findViewById(R.id.next_close);
    }

    public void bind(NextUpViewModel nextUpViewModel, LifecycleOwner lifecycleOwner) {
        nextUpViewModel.getThumbnailUrl().observe(lifecycleOwner, url -> {
            Picasso.get().load(url).into(poster);
        });
        nextUpViewModel.getTitle().observe(lifecycleOwner, string -> {
            title.setText(string);
        });
        nextUpViewModel.isUiLayerVisible().observe(lifecycleOwner, visible -> {
            setVisibility(visible ? VISIBLE : GONE);
        });
        nextUpViewModel.getNextUpTimeRemaining().observe(lifecycleOwner, remaining -> {
            countdown.setText("Next in: " + remaining);
        });
        close.setOnClickListener(v -> {
            nextUpViewModel.closeNextUpView();
        });
        setOnClickListener(v -> {
            nextUpViewModel.playNextPlaylistItem();
        });
    }

}
