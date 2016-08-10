package com.jwplayer.demo.vrsample.samples;

import com.longtailvideo.jwplayer.vr.JWVrVideoView;
import com.longtailvideo.jwplayer.vr.utils.MediaUrlUtil;

/**
 * 360 Video Samples.
 */
public class Samples {

	public static final Sample SAMPLE_STREAM = new Sample("Caminandes VR",
			"http://content.jwplatform.com/manifests/G3ADNSa0.m3u8",
			JWVrVideoView.STEREO_MODE_TOP_BOTTOM, MediaUrlUtil.TYPE_HLS);

	private Samples() {

	}

	public static class Sample {
		public final String mName;
		public final String mUri;
		public final @JWVrVideoView.StereoMode int mStereoMode;
		public final int mContentType;

		public Sample(String name, String uri, @JWVrVideoView.StereoMode int stereoMode, int type) {
			mName = name;
			mUri = uri;
			mStereoMode = stereoMode;
			mContentType = type;
		}
	}
}
