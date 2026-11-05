package com.ads.manager.dialog

import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
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

                val rootLayout = LinearLayout(activity).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setBackgroundColor(config.dialogBackgroundColor)

                    if (config.showDialogProgressBar) {
                        val progressBar = ProgressBar(activity).apply {
                            val sizePx = (44 * activity.resources.displayMetrics.density).toInt()
                            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                                bottomMargin = (14 * activity.resources.displayMetrics.density).toInt()
                            }
                            isIndeterminate = true
                            indeterminateTintList = ColorStateList.valueOf(config.dialogProgressColor)
                        }
                        addView(progressBar)
                    }

                    if (config.dialogLoadingText.isNotBlank()) {
                        val textView = TextView(activity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            gravity = Gravity.CENTER
                            text = config.dialogLoadingText
                            setTextColor(config.dialogTextColor)
                            textSize = 15f
                            setTypeface(null, Typeface.BOLD)
                        }
                        addView(textView)
                    }
                }

                setContentView(rootLayout)

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
