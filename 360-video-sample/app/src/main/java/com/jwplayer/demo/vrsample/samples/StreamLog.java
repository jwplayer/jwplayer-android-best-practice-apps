package com.jwplayer.demo.vrsample.samples;

import com.longtailvideo.jwplayer.vr.JWVrVideoView;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Keeps track of the Played Streams.
 */
public class StreamLog {

	private static StreamLog mInstance;

	private Map<String, Integer> mUrls = new TreeMap<>();

	private StreamLog() {}

	public static StreamLog getInstance() {
		if (mInstance == null) {
			mInstance = new StreamLog();
		}
		return mInstance;
	}

	public void append(String url, @JWVrVideoView.StereoMode int stereoMode) {
		mUrls.put(url, stereoMode);
	}

	public Set<Map.Entry<String, Integer>> getStreams() {
		return mUrls.entrySet();
	}

}
