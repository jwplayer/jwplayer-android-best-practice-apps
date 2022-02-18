package com.jwplayer.customfragment.callbacks;

import android.os.Bundle;
import android.view.View;

import com.jwplayer.customfragment.R;
import com.jwplayer.pub.api.JWPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CallbackFragment extends Fragment implements
                                                     JWPlayer.PlayerInitializationListener {

    private CallbackView mCallbackView;
    private JWPlayer mJWPlayer;

    public CallbackFragment() {
        super(R.layout.fragment_callbacks);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCallbackView = view.findViewById(R.id.callback_fragment);
        if (mJWPlayer != null){
            mCallbackView.registerListeners(mJWPlayer);
        }
    }

    @Override
    public void onPlayerInitialized(JWPlayer jwPlayer) {
        mJWPlayer = jwPlayer;
        if (mCallbackView != null) {
            mCallbackView.registerListeners(mJWPlayer);
        }
    }
}
