package com.jwplayer.offlinedrmdemo.utils;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * This is a utility class to track the Network status and notify interested parties
 *
 * We use this to know if we should setup with regular or Offline DRM in the MainActivity
 * */
public class NetworkTracker {

    public interface NetworkStatusChangedListener {
        void onNetworkStatusChanged(boolean isOnline);
    }

    private final WeakReference<NetworkStatusChangedListener> mListener;
    private boolean mIsOnline;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final ConnectivityManager mConnectivityManager;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;

    public NetworkTracker(Context context, NetworkStatusChangedListener listener) {
        mListener = new WeakReference<>(listener);
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        mConnectivityManager = (ConnectivityManager) context.getSystemService(
                CONNECTIVITY_SERVICE);

        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (!mIsOnline) {
                    mIsOnline = true;
                    mMainHandler.post(() -> {
                        try {
                            mListener.get().onNetworkStatusChanged(mIsOnline);
                        } catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                if (mIsOnline) {
                    mIsOnline = false;
                    mMainHandler.post(() -> {
                        try {
                            mListener.get().onNetworkStatusChanged(mIsOnline);
                        } catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network,
                                              @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
            }
        };
        mConnectivityManager.requestNetwork(networkRequest, mNetworkCallback);
        mIsOnline = mConnectivityManager.getActiveNetworkInfo() != null;
        try {
            mListener.get().onNetworkStatusChanged(mIsOnline);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public boolean isOnline() {
        return mIsOnline;
    }

    public void destroy(){
        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
    }

}
