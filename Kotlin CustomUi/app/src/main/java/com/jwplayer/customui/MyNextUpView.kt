package com.jwplayer.customui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.imageview.ShapeableImageView
import com.jwplayer.pub.ui.viewmodels.NextUpViewModel
import com.squareup.picasso.Picasso

class MyNextUpView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
  ConstraintLayout(
    context!!, attrs, defStyleAttr, defStyleRes
  ) {
  private var title: TextView? = null
  private var countdown: TextView? = null
  private var poster: ShapeableImageView? = null
  private var close: View? = null

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
    inflate(context, R.layout.view_my_next_up, this)
    title = findViewById(R.id.next_title)
    countdown = findViewById(R.id.next_count)
    poster = findViewById(R.id.next_poster)
    close = findViewById(R.id.next_close)
  }

  fun bind(nextUpViewModel: NextUpViewModel, lifecycleOwner: LifecycleOwner?) {
    nextUpViewModel.thumbnailUrl.observe(lifecycleOwner!!) { url: String? ->
      Picasso.get().load(url).into(poster)
    }
    nextUpViewModel.title.observe(lifecycleOwner) { string: String? -> title!!.text = string }
    nextUpViewModel.isUiLayerVisible()
      .observe(lifecycleOwner) { visible: Boolean -> visibility = if (visible) VISIBLE else GONE }
    nextUpViewModel.nextUpTimeRemaining.observe(lifecycleOwner) { remaining: Int ->
      countdown!!.text = "Next in: $remaining"
    }
    close!!.setOnClickListener { v: View? -> nextUpViewModel.closeNextUpView() }
    setOnClickListener { v: View? -> nextUpViewModel.playNextPlaylistItem() }
  }
}
