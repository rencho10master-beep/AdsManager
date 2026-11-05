package com.ads.manager.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ads.manager.AdsConfig

object AdLoadingDialog {
    private var dialog: Dialog? = null

    fun show(activity: Activity, config: AdsConfig) {
        if (activity.isFinishing || activity.isDestroyed) return
        dismiss()

        try {
            dialog = Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar).apply {
                setCancelable(false)
                val textView = TextView(activity).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    gravity = Gravity.CENTER
                    text = config.dialogLoadingText
                    setTextColor(config.dialogTextColor)
                    setBackgroundColor(config.dialogBackgroundColor)
                    textSize = 16f
                    setTypeface(null, Typeface.BOLD)
                }
                setContentView(textView)

                window?.let { window ->
                    window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                    window.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        window.attributes = window.attributes.apply {
                            layoutInDisplayCutoutMode =
                                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        }
                    }

                    WindowCompat.setDecorFitsSystemWindows(window, false)

                    val controller = WindowInsetsControllerCompat(window, window.decorView)
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }

            if (!activity.isFinishing && !activity.isDestroyed) {
                dialog?.show()
            }
        } catch (_: Exception) {
            dialog = null
        }
    }

    fun dismiss() {
        try {
            dialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        } catch (_: Exception) {
        } finally {
            dialog = null
        }
    }
}
