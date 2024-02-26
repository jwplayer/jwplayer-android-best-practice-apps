package com.jwplayer.drmdemo.kotlin

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import androidx.media3.exoplayer.drm.ExoMediaDrm
import com.jwplayer.drmdemo.utils.Util
import com.jwplayer.pub.api.media.drm.MediaDrmCallback
import java.io.IOException
import java.util.UUID

class WidevineCallbackKt : MediaDrmCallback {
  private lateinit var defaultUri: String
  private lateinit var requestProperties: Map<String, String>

  constructor(drmAuthUrl: String, properties: Map<String, String>) {
    defaultUri = drmAuthUrl
    requestProperties = properties
  }

  protected constructor(parcel: Parcel) {
    defaultUri = parcel.readString() ?: ""
  }

  @Throws(IOException::class)
  override fun executeProvisionRequest(
    uuid: UUID,
    request: ExoMediaDrm.ProvisionRequest
  ): ByteArray {
    val url = request.defaultUrl + "&signedRequest=" + String(request.data)
    return Util.executePost(url, null, requestProperties)
  }

  @Throws(IOException::class)
  override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
    var url: String? = request.licenseServerUrl
    if (TextUtils.isEmpty(url)) {
      url = defaultUri
    }
    return Util.executePost(url, request.data, requestProperties)
  }

  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(defaultUri)
  }

  companion object CREATOR : Parcelable.Creator<WidevineCallbackKt> {
    override fun createFromParcel(parcel: Parcel): WidevineCallbackKt {
      return WidevineCallbackKt(parcel)
    }

    override fun newArray(size: Int): Array<WidevineCallbackKt?> {
      return arrayOfNulls(size)
    }
  }
}