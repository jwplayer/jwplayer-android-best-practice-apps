package com.jwplayer.demo.convivaintegration;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.conviva.api.AndroidSystemInterfaceFactory;
import com.conviva.api.Client;
import com.conviva.api.ClientSettings;
import com.conviva.api.ContentMetadata;
import com.conviva.api.SystemFactory;
import com.conviva.api.SystemSettings;
import com.conviva.api.player.PlayerStateManager;
import com.conviva.api.system.SystemInterface;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that manages Conviva Client, Session, PlayerStateManager and integration API calls.
 */
public class ConvivaSessionManager {

    private static final String TAG = ConvivaSessionManager.class.getSimpleName();

    private static boolean initialized;

    private static PlayerStateManager mPlayerStateManager;

    private static SystemInterface mAndroidSystemInterface;
    private static SystemFactory mAndroidSystemFactory;
    private static SystemSettings mSystemSettings;

    private static ClientSettings mClientSettings;
    private static Client mClient;

    private static int mSessionKey = -1;

    /**
     * Should be called first
     */
    public static Client initClient(Context context, String gatewayUrl, String customerKey) {
        try {
            if (!initialized) {
                mAndroidSystemInterface = AndroidSystemInterfaceFactory.build(context);

                mSystemSettings = new SystemSettings();

                // Do not use DEBUG for production app
                mSystemSettings.logLevel = SystemSettings.LogLevel.DEBUG;
                mSystemSettings.allowUncaughtExceptions = false;

                mAndroidSystemFactory = new SystemFactory(mAndroidSystemInterface, mSystemSettings);

                mClientSettings = new ClientSettings(customerKey);
                mClientSettings.gatewayUrl = gatewayUrl;

                mClient = new Client(mClientSettings, mAndroidSystemFactory);
                initialized = true;
            }

        } catch (Exception ex) {
            Log.e(TAG, "Failed to initialize LivePass");
            ex.printStackTrace();
        }
        return mClient;
    }

    public static PlayerStateManager getPlayerStateManager() {

        if (mPlayerStateManager == null) {
            mPlayerStateManager = new PlayerStateManager(mAndroidSystemFactory);
        }
        return mPlayerStateManager;
    }

    public static void releasePlayerStateManager() {
        try {
            if (mPlayerStateManager != null) {
                mPlayerStateManager.release();
                mPlayerStateManager = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to release mPlayerStateManager");
        }
    }

    public static void deinitClient() {

        if (!initialized) {
            return;
        }

        if (mClient == null) {
            Log.w(TAG, "Unable to deinit since client has not been initialized");
            return;
        }

        if (mAndroidSystemFactory != null)
            mAndroidSystemFactory.release();
        try {
            releasePlayerStateManager();
            mClient.release();
        } catch (Exception e) {
            Log.e(TAG, "Failed to release client");
        }

        mAndroidSystemFactory = null;
        mClient = null;
        initialized = false;
    }

    /**
     * Called when player has been created and the media url is known.
     * Note that:
     * This function may be called multiple times by the same player and
     * for different sessions,
     */
    public static void createConvivaSession(@NonNull PlaylistItem playlistItem) {

        if (!initialized || mClient == null) {
            Log.e(TAG, "Unable to create session since client not initialized");
            return;
        }

        try {
            if (mSessionKey != -1) {
                cleanupConvivaSession();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to cleanup session: " + e.toString());
        }

        try {
            Log.d("Player", "Creating a session");

            final ContentMetadata metadata = createContentMetadata(playlistItem);

            mSessionKey = mClient.createSession(metadata);

            Log.d("Player", "Session created attaching streamer" + mPlayerStateManager);

            mClient.attachPlayer(mSessionKey, mPlayerStateManager);

        } catch (Exception ex) {
            Log.e(TAG, "Failed to create session");
            ex.printStackTrace();
        }
    }

    private static ContentMetadata createContentMetadata(@NonNull PlaylistItem playlistItem) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("key", "value");

        final ContentMetadata metadata = new ContentMetadata();

        metadata.assetName = playlistItem.getTitle();
        metadata.streamUrl = playlistItem.getSources().get(0).getFile();

        metadata.custom = tags;
        metadata.defaultBitrateKbps = -1;
        metadata.defaultResource = "AKAMAI";
        metadata.viewerId = "Test Viewer";
        metadata.applicationName = "ConvivaAndroidSDK_JWPlayerDemo";

        metadata.streamType = ContentMetadata.StreamType.VOD;
        metadata.duration = 0;
        metadata.encodedFrameRate = -1;

        return metadata;
    }

    /**
     * Called after video session has completed
     */
    public static void cleanupConvivaSession() {

        if (!initialized || mClient == null) {
            Log.w(TAG, "Unable to clean session since client not initialized");
            return;
        }

        if (mSessionKey != -1) {
            Log.d(TAG, "cleanup session: " + mSessionKey);
            try {
                mClient.cleanupSession(mSessionKey);

            } catch (Exception ex) {
                Log.e(TAG, "Failed to cleanup");
                ex.printStackTrace();
            }
            mSessionKey = -1;
        }
    }

    public static void reportError(String err, boolean fatal) {

        if (!initialized || mClient == null) {
            Log.e(TAG, "Unable to report error since client not initialized");
            return;
        }

        final Client.ErrorSeverity severity = fatal
                ? Client.ErrorSeverity.FATAL
                : Client.ErrorSeverity.WARNING;

        try {
            mClient.reportError(mSessionKey, err, severity);

        } catch (Exception ex) {
            Log.e(TAG, "Failed to report error");
            ex.printStackTrace();
        }
    }

    public static void adStart() {

        if (!initialized || mClient == null) {
            Log.e(TAG, "Unable to start Ad since client not initialized");
            return;
        }

        if (mSessionKey == -1) {
            Log.e(TAG, "adStart() requires a session");
            return;
        }
        try {
            mClient.adStart(mSessionKey, Client.AdStream.SEPARATE,
                    Client.AdPlayer.SEPARATE,
                    Client.AdPosition.PREROLL);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to start Ad");
            ex.printStackTrace();
        }
    }

    public static void adEnd() {

        if (!initialized || mClient == null) {
            Log.e(TAG, "Unable to stop Ad since client not initialized");
            return;
        }

        if (mSessionKey == -1) {
            Log.e(TAG, "adEnd() requires a session");
            return;
        }
        try {
            mClient.adEnd(mSessionKey);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to end Ad");
            ex.printStackTrace();
        }
    }

    public static void seek(int newpos) {

        try {
            PlayerStateManager mCurrPlayerStateManager = getPlayerStateManager();

            if (mCurrPlayerStateManager != null) {
                getPlayerStateManager().setPlayerSeekStart(newpos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
