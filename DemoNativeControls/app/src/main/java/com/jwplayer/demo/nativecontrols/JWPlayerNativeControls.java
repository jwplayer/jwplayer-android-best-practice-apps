package com.jwplayer.demo.nativecontrols;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.core.PlayerState;
import com.longtailvideo.jwplayer.events.CompleteEvent;
import com.longtailvideo.jwplayer.events.PauseEvent;
import com.longtailvideo.jwplayer.events.PlayEvent;
import com.longtailvideo.jwplayer.events.TimeEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

import java.util.ArrayList;

public class JWPlayerNativeControls extends RelativeLayout
        implements View.OnClickListener,
        VideoPlayerEvents.OnPlayListener,
        VideoPlayerEvents.OnPauseListener,
        VideoPlayerEvents.OnCompleteListener{

    public interface OnControlsInteraction {
        void OnControlsInteractedWith(boolean triggeredPlayback);
        void onProlongedInteractionBegan();
        void onProlongedInteractionEnded();
    }
    ArrayList<OnControlsInteraction> mControlChangeListeners = new ArrayList<>();

    JWPlayerView    mPlayerView;
    SeekBar         mSeekBar;
    TextView        mSeekBarText;
    JWProgressBar   mVideoProgressBar;
    Button          mMuteButton;
    ImageButton     mPlayPauseButton;
    ImageButton     mFullScreenButton;
    ImageButton     mFastForwardButton;
    ImageButton     mRewindButton;

    public JWPlayerNativeControls(Context context) {
        this(context, null, 0);
    }

    public JWPlayerNativeControls(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JWPlayerNativeControls(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View v = inflate(getContext(), R.layout.native_controls, this);

        mPlayPauseButton = v.findViewById(R.id.play_and_pause_button);
        v.findViewById(R.id.stop_button).setOnClickListener(this);
        mFastForwardButton = v.findViewById(R.id.fast_forward_button);
        mRewindButton = v.findViewById(R.id.rewind_button);
        mFullScreenButton = v.findViewById(R.id.fullscreen_button);
        mSeekBar = v.findViewById(R.id.seek_bar);
        mSeekBarText = v.findViewById(R.id.seekbar_textview);
        mMuteButton = v.findViewById(R.id.mute_button);

        mPlayPauseButton.setOnClickListener(this);
        mFastForwardButton.setOnClickListener(this);
        mRewindButton.setOnClickListener(this);
        mFullScreenButton.setOnClickListener(this);
        mMuteButton.setOnClickListener(this);

        mVideoProgressBar = new JWProgressBar();
        mSeekBar.setOnSeekBarChangeListener(mVideoProgressBar);
        resetVideoPlaybackUI();
    }

    private void resetVideoPlaybackUI(){
        mPlayPauseButton.setImageResource(R.drawable.exo_controls_play);
        mSeekBar.setProgress(0);
        mSeekBarText.setText("00:00 / 00:00");
    }

    public void addControlsInteractionListener(OnControlsInteraction ocl){
        mControlChangeListeners.add(ocl);
    }

    public boolean removeControlsInteractionListener(OnControlsInteraction ocl){
        return mControlChangeListeners.remove(ocl);
    }

    public void setJWView(JWPlayerView playerView) {
        mPlayerView = playerView;
        //Sign us up for player related events so we can appopriately update UI
        //Play and pause will be used to toggle the play/pause button and it's functionality
        mPlayerView.addOnPlayListener(this);
        mPlayerView.addOnPauseListener(this);
        //We'll use time event to update the seek bar time
        mPlayerView.addOnTimeListener(mVideoProgressBar);
        //Listen to the complete event to update the play pause UI
        mPlayerView.addOnCompleteListener(this);
    }

    public void unSubscribeFromJWEvents(){
        mPlayerView.removeOnPlayListener(this);
        mPlayerView.removeOnPauseListener(this);
        mPlayerView.removeOnTimeListener(mVideoProgressBar);
        mPlayerView.removeOnCompleteListener(this);
    }

    @Override
    public void onClick(View v) {
        boolean stoppedPlayback = false;
        switch (v.getId()) {
            case R.id.play_and_pause_button:
                //If we're playing stop us, if we're not playing play us
                if(mPlayerView.getState() == PlayerState.PLAYING){
                    mPlayerView.pause();
                    stoppedPlayback = true;
                } else if(mPlayerView.getState() != PlayerState.PLAYING){
                    mPlayerView.play();
                }
                break;
            case R.id.stop_button:
                //Cancel playback ASAP and reset the UI back to the pre video starts
                mPlayerView.stop();
                resetVideoPlaybackUI();
                break;
            case R.id.fullscreen_button:
                if(mPlayerView.getFullscreen()){
                    mPlayerView.setFullscreen(false,true);
                } else {
                    mPlayerView.setFullscreen(true,true);
                }
                break;
            case R.id.fast_forward_button:
                mPlayerView.seek(mPlayerView.getPosition() + 10);
                break;
            case R.id.rewind_button:
                double newPosition = mPlayerView.getPosition() - 10;
                //We clamp to 0 because JWPlayer would use negative numbers to jump to <end of video> - seekPos
                //so -10 is 10 seconds from the end of the video
                if(newPosition < 0){
                    newPosition = 0;
                }
                mPlayerView.seek(newPosition);
                break;
            case R.id.mute_button:
                if(mPlayerView.getState() == PlayerState.PLAYING){
                    boolean muted = mPlayerView.getMute();
                    if(muted){ //we're gonna unmute it
                        mMuteButton.setText("Mute");
                    } else { //We're gonna mute it
                        mMuteButton.setText("UnMute");
                    }
                    mPlayerView.setMute(!muted);
                }
                break;
        }

        //Notify all our listeners that one of the mNativePlayerControls has been interacted with
        for (OnControlsInteraction ocl: mControlChangeListeners) {
            ocl.OnControlsInteractedWith(stoppedPlayback);
        }
    }

    @Override
    public void onPlay(PlayEvent playEvent) {
        mPlayPauseButton.setImageResource(R.drawable.exo_controls_pause);
    }

    @Override
    public void onPause(PauseEvent pauseEvent) {
        mPlayPauseButton.setImageResource(R.drawable.exo_controls_play);
    }

    @Override
    public void onComplete(CompleteEvent completeEvent) {
        mPlayPauseButton.setImageResource(R.drawable.exo_controls_play);
    }

    private class JWProgressBar implements VideoPlayerEvents.OnTimeListener,
            SeekBar.OnSeekBarChangeListener {
        //Ignore time events when the user is using the scrub to navigate the video otherwise the scrubber will jump erratically
        private boolean ignoreTimeEvents = false;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Convert from progress to a time stamp to display to the user
            double videoDuration = mPlayerView.getDuration();
            double currentTime = mPlayerView.getDuration() * ((double)progress / 100);
            int finalMinutes = (int)videoDuration / 60;
            int finalSeconds = (int)videoDuration % 60;
            int currentMinutes = (int)currentTime / 60;
            int currentSeconds = (int)currentTime % 60;
            mSeekBarText.setText(
                    padNumber(currentMinutes) +
                    ":" +
                    padNumber(currentSeconds) +
                    " / " +
                    padNumber(finalMinutes) +
                    ":" +
                    padNumber(finalSeconds));

            //If the user moved the scrubber jump to that timestamp in the video
            //Convert that to seconds into the video and seek that far
            if(fromUser){
                //progress is a value between 1-100 for percentage of video
                double secondsToJumpTo = videoDuration * (progress / 100.0);
                //jump that deep in seconds into the video
                mPlayerView.seek(secondsToJumpTo);
            }
        }

        //Helper function to help format the numbers in the time bar
        private String padNumber(int number){
            String num = String.valueOf(number);
            if(number < 10){
                return "0" + num;
            }
            return num;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            ignoreTimeEvents = true;
            for (OnControlsInteraction ocl: mControlChangeListeners) {
                ocl.onProlongedInteractionBegan();
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            ignoreTimeEvents = false;
            for (OnControlsInteraction ocl: mControlChangeListeners) {
                ocl.onProlongedInteractionEnded();
            }
        }

        @Override
        public void onTime(TimeEvent timeEvent) {
            //Whenever we get an time update from the player move the seek bar forward in lock step
            if(ignoreTimeEvents){
                return;
            }
            double position = timeEvent.getPosition();
            mSeekBar.setProgress((int)position);
        }
    }
}
