package com.jwplayer.demo.vrsample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jwplayer.demo.vrsample.samples.Samples;
import com.jwplayer.demo.vrsample.samples.StreamLog;
import com.longtailvideo.jwplayer.vr.JWVrVideoView;
import com.longtailvideo.jwplayer.vr.utils.MediaUrlUtil;

import java.util.Map;

public class SamplePickerActivity extends AppCompatActivity {

	private CoordinatorLayout mCoordinatorLayout;

	private EditText mStreamInput;
	private LinearLayout mStreamLog;

	private RadioGroup mStereoModeRadio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sample_picker);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
		mStreamInput = (EditText) findViewById(R.id.stream_url_input);
		mStreamLog = (LinearLayout) findViewById(R.id.stream_log);
		mStereoModeRadio = (RadioGroup) findViewById(R.id.stereo_mode_radio_group);

		// Only show the video selection spinner in debug builds.
		mStreamInput.setText(Samples.SAMPLE_STREAM.mUri);
		checkStereoButton(Samples.SAMPLE_STREAM.mStereoMode);

		Button watchButton = (Button) findViewById(R.id.watch_button);
		assert watchButton != null;
		watchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSampleSelected();
			}
		});

		initializeLog();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void onSampleSelected() {
		String streamUrl = mStreamInput.getText().toString();
		if (streamUrl.length() == 0) {
			Snackbar.make(mCoordinatorLayout, R.string.no_video_selected, Snackbar.LENGTH_SHORT).show();
			return;
		}

		Uri streamURI = Uri.parse(streamUrl);
		Intent intent = new Intent(this, VideoActivity.class);
		intent.setData(streamURI);

		int contentType = MediaUrlUtil.TYPE_UNKNOWN;
		@JWVrVideoView.StereoMode int stereoMode;
		switch (mStereoModeRadio.getCheckedRadioButtonId()) {
			case R.id.radio_stereo_over_under:
				stereoMode = JWVrVideoView.STEREO_MODE_TOP_BOTTOM;
				break;
			case R.id.radio_stereo_side_by_side:
				stereoMode = JWVrVideoView.STEREO_MODE_LEFT_RIGHT;
				break;
			case R.id.radio_mono:
			default:
				stereoMode = JWVrVideoView.STEREO_MODE_MONO;
				break;
		}

		intent.putExtra(VideoActivity.STEREO_MODE_EXTRA, stereoMode);
		intent.putExtra(VideoActivity.CONTENT_TYPE_EXTRA, contentType);
		startActivity(intent);

		StreamLog.getInstance().append(streamUrl, stereoMode);
		addToLog(streamUrl, stereoMode);
	}

	private void initializeLog() {
		for (Map.Entry<String, Integer> url : StreamLog.getInstance().getStreams()) {
			addToLog(url.getKey(), url.getValue());
		}
	}

	private void addToLog(String url, final int stereoMode) {
		final TextView textView = new TextView(this);
		textView.setText(url);
		textView.setLines(1);
		textView.setEllipsize(TextUtils.TruncateAt.END);
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mStreamInput.setText(textView.getText());
				checkStereoButton(stereoMode);
			}
		});
		mStreamLog.addView(textView, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	private void checkStereoButton(@JWVrVideoView.StereoMode int stereoMode) {
		switch (stereoMode) {
			case JWVrVideoView.STEREO_MODE_MONO:
				mStereoModeRadio.check(R.id.radio_mono);
				break;
			case JWVrVideoView.STEREO_MODE_LEFT_RIGHT:
				mStereoModeRadio.check(R.id.radio_stereo_side_by_side);
				break;
			case JWVrVideoView.STEREO_MODE_TOP_BOTTOM:
				mStereoModeRadio.check(R.id.radio_stereo_over_under);
				break;
		}
	}


}
