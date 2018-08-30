package ru.yourok.torrserve.dialog

import android.Manifest
import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.view.View
import ru.yourok.torrserve.R
import kotlin.concurrent.thread

object DialogPerm {
    fun requestPermissionWithRationale(activity: Activity) {
        thread {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(activity.findViewById<View>(R.id.content), R.string.permission_storage_msg, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.permission_btn, {
                            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                        })
                        .show()
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }
}