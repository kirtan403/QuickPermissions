package com.livinglifetechway.quickpermissions.util


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log


/**
 * A simple [Fragment] subclass.
 */
class PermissionCheckerFragment : Fragment() {

    interface PermissionCallback {
        fun onPermissionResult()
    }

    var mListener: PermissionCallback? = null

    companion object {

        private val TAG = Companion::class.java.simpleName
        fun newInstance(permisions: Array<String>): PermissionCheckerFragment {
            val fragment = PermissionCheckerFragment()

            val arguments = Bundle()
            arguments.putStringArray("permissions", permisions)

            fragment.arguments = arguments
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: requesting permissions")

        requestPermissions(arguments?.getStringArray("permissions").orEmpty(), 101)
    }

    fun setListener(listener: PermissionCallback) {
        mListener = listener
        Log.d(TAG, "onCreate: listeners set")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "passing callback")
        mListener?.onPermissionResult()
    }
}
