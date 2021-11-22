package com.jwplayer.drmdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;

public class QrCodeController {

    private final Activity activity;
    private final EditText streamURL;
    private final EditText authURL;
    private final ViewGroup root;
    private int QR_TYPE;

    public QrCodeController(Activity activity, EditText streamURL, EditText authURL, ViewGroup root) {
        this.activity = activity;
        this.streamURL = streamURL;
        this.authURL = authURL;
        this.root = root;
    }

    public void readStreamQr() {
        maybeReadQrCode(Constants.QR_STREAM_URL);
    }

    public void readAuthQr() {
        maybeReadQrCode(Constants.QR_AUTH_URL);
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == Constants.REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readQrCode();
            } else {
                Snackbar.make(root, "Camera permission required to read QR Code",
                              Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String result = data.getStringExtra(Constants.ACTIVITY_RESULT);
            if (requestCode == Constants.QR_STREAM_URL) {
                streamURL.setText(result);
            } else if (requestCode == Constants.QR_AUTH_URL) {
                authURL.setText(result);
            }
        }
    }

    private void maybeReadQrCode(int type) {
        QR_TYPE = type;
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            readQrCode();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                                                Manifest.permission.CAMERA)) {
            Snackbar.make(root, "Camera permission required to read QR Code",
                          Snackbar.LENGTH_INDEFINITE).setAction("OK", view -> {
                ActivityCompat.requestPermissions(activity,
                                                  new String[]{Manifest.permission.CAMERA},
                                                  Constants.REQUEST_CAMERA
                );
            }).show();

        } else {
            ActivityCompat.requestPermissions(activity,
                                              new String[]{Manifest.permission.CAMERA},
                                              Constants.REQUEST_CAMERA
            );
        }
    }

    private void readQrCode() {
        activity.startActivityForResult(new Intent(activity, QrActivity.class), QR_TYPE);
    }
}
