package com.jwplayer.demo.nativecontrols;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.PlayerState;
import com.jwplayer.pub.api.events.CompleteEvent;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.PauseEvent;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.TimeEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.view.JWPlayerView;

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
    JWPlayer        mPlayer;
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

    public void resetVideoPlaybackUI(){
        mPlayPauseButton.setImageResource(R.drawable.exo_legacy_controls_play);
        mSeekBar.setProgress(0);
        mSeekBarText.setText("00:00 / 00:00");
    }

    public void addControlsInteractionListener(OnControlsInteraction ocl){
        mControlChangeListeners.add(ocl);
    }

    public boolean removeControlsInteractionListener(OnControlsInteraction ocl){
        return mControlChangeListeners.remove(ocl);
    }

    public void setJWView(JWPlayer player) {
        mPlayer = player;
        //Sign us up for player related events so we can appopriately update UI
        //Play and pause will be used to toggle the play/pause button and it's functionality
        mPlayer.addListener(EventType.PLAY, this);
        mPlayer.addListener(EventType.PAUSE, this);
        //We'll use time event to update the seek bar time
        mPlayer.addListener(EventType.TIME, mVideoProgressBar);
        //Listen to the complete event to update the play pause UI
        mPlayer.addListener(EventType.COMPLETE, this);
    }

    public void unSubscribeFromJWEvents(){
        mPlayer.removeListener(EventType.PLAY, this);
        mPlayer.removeListener(EventType.PAUSE, this);
        mPlayer.removeListener(EventType.TIME, mVideoProgressBar);
        mPlayer.removeListener(EventType.COMPLETE, this);
    }

    @Override
    public void onClick(View v) {
        boolean stoppedPlayback = false;
        switch (v.getId()) {
            case R.id.play_and_pause_button:
                //If we're playing stop us, if we're not playing play us
                if(mPlayer.getState() == PlayerState.PLAYING){
                    mPlayer.pause();
                    stoppedPlayback = true;
                } else if(mPlayer.getState() != PlayerState.PLAYING){
                    mPlayer.play();
                }
                break;
            case R.id.stop_button:
                //Cancel playback ASAP and reset the UI back to the pre video starts
                stoppedPlayback = true;
                mPlayer.stop();
                resetVideoPlaybackUI();
                break;
            case R.id.fullscreen_button:
                if(mPlayer.getFullscreen()){
                    mPlayer.setFullscreen(false,true);
                } else {
                    mPlayer.setFullscreen(true,true);
                }
                break;
            case R.id.fast_forward_button:
                mPlayer.seek(mPlayer.getPosition() + 10);
                break;
            case R.id.rewind_button:
                double newPosition = mPlayer.getPosition() - 10;
                //We clamp to 0 because JWPlayer would use negative numbers to jump to <end of video> - seekPos
                //which would make -10, 10 seconds from the end of the video
                if(newPosition < 0){
                    newPosition = 0;
                }
                mPlayer.seek(newPosition);
                break;
            case R.id.mute_button:
                if(mPlayer.getState() == PlayerState.PLAYING){
                    boolean muted = mPlayer.getMute();
                    if(muted){ //we're gonna unmute it
                        mMuteButton.setText("Mute");
                    } else { //We're gonna mute it
                        mMuteButton.setText("UnMute");
                    }
                    mPlayer.setMute(!muted);
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
        mPlayPauseButton.setImageResource(R.drawable.exo_legacy_controls_pause);
    }

    @Override
    public void onPause(PauseEvent pauseEvent) {
        mPlayPauseButton.setImageResource(R.drawable.exo_legacy_controls_play);
    }

    @Override
    public void onComplete(CompleteEvent completeEvent) {
        mPlayPauseButton.setImageResource(R.mipmap.ic_replay);
    }

    private class JWProgressBar implements VideoPlayerEvents.OnTimeListener,
            SeekBar.OnSeekBarChangeListener {
        //Ignore time events when the user is using the scrub to navigate the video otherwise the scrubber will jump erratically
        private boolean ignoreTimeEvents = false;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Convert from progress to a time stamp to display to the user
            double videoDuration = mPlayer.getDuration();
            String seekbarMessage = "";
            if(videoDuration > 0){
                double currentTime = progress;
                int currentMinutes = (int)currentTime / 60;
                int currentSeconds = (int)currentTime % 60;
                int finalMinutes = (int)videoDuration / 60;
                int finalSeconds = (int)videoDuration % 60;


                seekbarMessage = String.format("%02d", currentMinutes) +
                                ":" +
                                String.format("%02d", currentSeconds) +
                                " / " +
                                String.format("%02d", finalMinutes) +
                                ":" +
                                String.format("%02d", finalSeconds);
            } else { // if videoDuration is < 0 we have a live stream so disable the scrubber
                //seekbarMessage = "Streaming";
                double currentTime = Math.abs(videoDuration + progress);
                if(progress > 0 && videoDuration == 0){
                    seekbarMessage = "LIVE";
                } else {
                    int currentMinutes = (int)currentTime / 60;
                    int currentSeconds = (int)currentTime % 60;

                    if(videoDuration < 0){
                        seekbarMessage += "-";
                    }
                    //Manually add the negative sign so this plays nice with -00:02s
                    seekbarMessage +=
                            String.format("%02d", currentMinutes) +
                                    ":" +
                                    String.format("%02d", currentSeconds);
                }
            }
            mSeekBarText.setText(seekbarMessage);
            //If the user moved the scrubber jump to that timestamp in the video
            //Convert that to seconds into the video and seek that far
            if(fromUser){
                if(videoDuration < 0){
                    //To get the bar to look right, if we jump to live we set the seek to -1 instead of 0
                    double seekValue = videoDuration + progress;
                    if(seekValue == 0){
                        seekValue = -1;
                    }
                    mPlayer.seek(seekValue);
                } else {
                    mPlayer.seek(progress);
                }
            }
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
            if(ignoreTimeEvents){
                return;
            }
            double position = timeEvent.getPosition();
            double duration = timeEvent.getDuration();
            //Time events will have a negative position if we're streaming
            //and if we're streaming we should disable several UI elements like rewind, seek forward, and scrubber
            int UIVisibility = VISIBLE;
            if(timeEvent.getDuration() < 0){
                UIVisibility = INVISIBLE;
            }
            mRewindButton.setVisibility(UIVisibility);
            mFastForwardButton.setVisibility(UIVisibility);
            mSeekBar.setVisibility(VISIBLE);

            //if theres a negative duration then it's a stream
            //if theres a positive position then it's a live stream you can't scrub through
            //if theres a negative position it's a stream you CAN scrub through
            int absDuration = (int)Math.abs(duration);
            mSeekBar.setMax(absDuration);
            if(duration < 0){
                if(position < 0){
                    position = absDuration - (int)Math.abs(position);
                } else {
                    mSeekBarText.setText("LIVE");
                }
            }
            //Whenever we get an time update from the player move the seek bar forward in lock step
            mSeekBar.setProgress((int)position);
        }
    }
}
