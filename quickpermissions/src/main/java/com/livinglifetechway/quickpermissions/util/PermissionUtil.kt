package com.livinglifetechway.quickpermissions.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Utility class that wraps access to the runtime permissions API in M and provides basic helper
 * methods.
 */
object PermissionUtil {

    val isMNC: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value [PackageManager.PERMISSION_GRANTED].
     *
     * @see Activity.onRequestPermissionsResult
     */
    fun verifyPermissions(grantResults: IntArray): Boolean {
        // Verify that each required permission has been granted, otherwise return false.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * Returns true if the Activity has access to all given permissions.
     * Always returns true on platforms below M.
     *
     * @see Activity.checkSelfPermission
     */
    fun hasSelfPermission(activity: Context, permissions: Array<String>): Boolean {
        // Below Android M all permissions are granted at install time and are already available.
        if (!isMNC) {
            return true
        }

        // Verify that all required permissions have been granted
        for (permission in permissions) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    /**
     * Returns true if the Activity has access to a given permission.
     * Always returns true on platforms below M.
     *
     * @see Activity.checkSelfPermission
     */
    fun hasSelfPermission(activity: Context, permission: String): Boolean {
        // Below Android M all permissions are granted at install time and are already available.
        return if (!isMNC) {
            true
        } else activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

}