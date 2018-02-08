package com.livinglifetechway.quickpermissions.util


import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.fromParts
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import org.jetbrains.anko.alert


/**
 * A simple [Fragment] subclass.
 */
class PermissionCheckerFragment : Fragment() {

    var quickPermissionsRequest: QuickPermissionsRequest? = null
//    private var permissions: Array<String> = arrayOf()

    interface QuickPermissionsCallback {
        fun shouldShowRequestPermissionsRationale(quickPermissionsRequest: QuickPermissionsRequest?)
        fun onPermissionsGranted(quickPermissionsRequest: QuickPermissionsRequest?)
        fun onPermissionsPermanentlyDenied(quickPermissionsRequest: QuickPermissionsRequest?)
    }

    var mListener: QuickPermissionsCallback? = null

    companion object {
        private val TAG = Companion::class.java.simpleName
        fun newInstance(): PermissionCheckerFragment = PermissionCheckerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: permission fragment created")
    }

    fun setListener(listener: QuickPermissionsCallback) {
        mListener = listener
        Log.d(TAG, "onCreate: listeners set")
    }

    fun requestPermissionsFromUser(quickPermissionsRequest: QuickPermissionsRequest?) {
        this.quickPermissionsRequest = quickPermissionsRequest
        Log.d(TAG, "requestPermissionsFromUser: requesting permissions")
        requestPermissions(quickPermissionsRequest?.permissions.orEmpty(), 101)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "passing callback")

        // check if permissions granted
        handlePermissionResult(permissions, grantResults)

    }

    /**
     * Checks and takes the action based on permission results retrieved from onRequestPermissionsResult
     * and from the settings activity
     *
     * @param permissions List of Permissions
     * @param grantResults A list of permission result <b>Granted</b> or <b>Denied</b>
     */
    private fun handlePermissionResult(permissions: Array<String>, grantResults: IntArray) {
        if (PermissionUtil.hasSelfPermission(context, permissions)) {
            // we are good to go!
            mListener?.onPermissionsGranted(quickPermissionsRequest)
        } else {
            // we are still missing permissions
            val deniedPermissions = PermissionUtil.getDeniedPermissions(permissions, grantResults)

            // check if rationale dialog should be shown or not
            var shouldShowRationale = true
            var isPermenentlyDenied = false
            for (i in 0 until deniedPermissions.size) {
                val deniedPermission = deniedPermissions[i]
                val rationale = shouldShowRequestPermissionRationale(deniedPermission)
                if (!rationale) {
                    shouldShowRationale = false
                    isPermenentlyDenied = true
                    break
                }
            }

            if (quickPermissionsRequest?.handlePermanentlyDenied == true && isPermenentlyDenied) {

                quickPermissionsRequest?.permanentDeniedMethod?.let {
                    mListener?.onPermissionsPermanentlyDenied(quickPermissionsRequest)
                    return
                }

                activity?.alert {
                    message = quickPermissionsRequest?.permanentlyDeniedMessage.orEmpty()
                    positiveButton("SETTINGS") {
                        val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS,
                                fromParts("package", activity?.packageName, null))
                        //                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, 101)
                    }
                    negativeButton("CANCEL") { }
                }?.apply { isCancelable = false }?.show()
                return
            }

            // if should show rationale dialog
            if (quickPermissionsRequest?.handleRationale == true && shouldShowRationale) {

                quickPermissionsRequest?.rationaleMethod?.let {
                    mListener?.shouldShowRequestPermissionsRationale(quickPermissionsRequest)
                    return
                }

                activity?.alert {
                    message = quickPermissionsRequest?.rationaleMessage.orEmpty()
                    positiveButton("TRY AGAIN") {
                        requestPermissionsFromUser(quickPermissionsRequest)
                    }
                    negativeButton("CANCEL") { }
                }?.apply { isCancelable = false }?.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            val permissions = quickPermissionsRequest?.permissions ?: emptyArray()
            val grantResults = IntArray(permissions.size)
            permissions.forEachIndexed { index, s ->
                grantResults[index] = context?.let { ActivityCompat.checkSelfPermission(it, s) } ?: PackageManager.PERMISSION_DENIED
            }

            handlePermissionResult(permissions, grantResults)
        }
    }
}
