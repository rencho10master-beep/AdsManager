package com.ads.manager.dialog

import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import java.lang.ref.WeakReference

object AdLoadingDialog {
    private var dialog: Dialog? = null
    private var activityRef: WeakReference<Activity>? = null

    fun show(activity: Activity, config: AdsConfig) {
        // 1. Thread check: Always run on Main (UI) thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post { show(activity, config) }
            return
        }

        // 2. Activity lifecycle safety: isFinishing or isDestroyed check
        if (activity.isFinishing || activity.isDestroyed) return

        dismiss()

        try {
            activityRef = WeakReference(activity)
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

            // 3. Double-check right before show()
            val act = activityRef?.get()
            if (act != null && !act.isFinishing && !act.isDestroyed) {
                dialog?.show()
            } else {
                dialog = null
            }
        } catch (_: Exception) {
            dialog = null
        }
    }

    fun dismiss() {
        // Always run dismiss on Main UI thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post { dismiss() }
            return
        }

        try {
            val act = activityRef?.get()
            // If activity is already finishing or destroyed, skip dismiss call to avoid WindowManager BadTokenException
            if (act != null && (act.isFinishing || act.isDestroyed)) {
                dialog = null
                activityRef = null
                return
            }

            dialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        } catch (_: Exception) {
            // Silently catch any WindowLeaked or BadTokenException
        } finally {
            dialog = null
            activityRef = null
        }
    }
}
