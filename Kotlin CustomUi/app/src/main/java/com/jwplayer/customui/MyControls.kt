package com.jwplayer.customui

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.ui.viewmodels.NextUpViewModel

class MyControls(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
  ConstraintLayout(
    context!!, attrs, defStyleAttr, defStyleRes
  ) {
  private var nextUpView: MyNextUpView? = null

  @JvmOverloads
  constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : this(
    context,
    attrs,
    defStyleAttr,
    0
  )

  init {
    initView(context)
  }

  private fun initView(context: Context?) {
    inflate(context, R.layout.view_my_controls, this)
    nextUpView = findViewById(R.id.my_next_up)
  }

  fun bind(player: JWPlayer, lifecycleOwner: LifecycleOwner?) {
    // Bind Views
    val nextUpVM = player.getViewModelForUiGroup(UiGroup.NEXT_UP) as NextUpViewModel
    nextUpView!!.bind(nextUpVM, lifecycleOwner)
  }
}
