package com.livinglifetechway.quickpermissions.util


import android.content.Intent
import android.net.Uri.fromParts
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import org.jetbrains.anko.alert


data class PermissionsRequestHolder(
        var permissions: Array<String>,
        var shouldShowRationale: Boolean = false,
        var rationaleMessage: String = "",
        var shouldShowPermenentlyDeniedDialog: Boolean = false,
        var permenetlyDeniedMessage: String = ""
)

/**
 * A simple [Fragment] subclass.
 */
class PermissionCheckerFragment : Fragment() {

    var permissions: Array<String> = arrayOf()

    interface PermissionCallback {
        fun onPermissionResult()
    }

    var mListener: PermissionCallback? = null

    companion object {
        private val TAG = Companion::class.java.simpleName
        fun newInstance(): PermissionCheckerFragment = PermissionCheckerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: permission fragment created")
    }

    fun setListener(listener: PermissionCallback) {
        mListener = listener
        Log.d(TAG, "onCreate: listeners set")
    }

    fun requestPermissions(permissions: Array<String>) {
        this.permissions = permissions
        Log.d(TAG, "requestPermissions: requesting permissions")
        requestPermissions(permissions, 101)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "passing callback")

        // check if permissions granted
        handlePermissionResult(grantResults, permissions)

    }

    private fun handlePermissionResult(grantResults: IntArray, permissions: Array<String>) {
        if (PermissionUtil.hasSelfPermission(context, permissions)) {
            // we are good to go!
            mListener?.onPermissionResult()
        } else {
            // we are still missing permissions
            val deniedPermissions = PermissionUtil.getDeniedPermissions(permissions, grantResults)

            // check if rationale dialog should be shown or not
            var shouldShowRationale = true
            var isPermenentlyDenied = false
            for (i in 0 until permissions.size) {
                val deniedPermission = deniedPermissions[i]
                val rationale = shouldShowRequestPermissionRationale(deniedPermission)
                if (!rationale) {
                    shouldShowRationale = false
                    isPermenentlyDenied = true
                    break
                }
            }

            if (isPermenentlyDenied) {
                activity.alert {
                    message = "We found you have permenently denied some permissions. To continue using our app, please allow it from settings."
                    positiveButton("SETTINGS") {
                        val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS,
                                fromParts("package", activity.packageName, null))
                        //                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, 101)
                    }
                    negativeButton("CANCEL") { }
                }.apply { isCancelable = false }.show()
                return
            }

            // if should show rationale dialog
            if (shouldShowRationale) {
                activity.alert {
                    message = "Permissions are required to run this function. Please allow us to move ahead."
                    positiveButton("TRY AGAIN") {
                        requestPermissions(permissions)
                    }
                    negativeButton("CANCEL") { }
                }.apply { isCancelable = false }.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            val grantResults = IntArray(permissions.size)
            permissions.forEachIndexed { index, s ->
                grantResults[index] = ActivityCompat.checkSelfPermission(context, s)
            }

            handlePermissionResult(grantResults, permissions)
        }
    }
}
