package com.livinglifetechway.quickpermissions.util

import java.lang.reflect.Method

data class QuickPermissionsRequest(
        private var target: PermissionCheckerFragment,
        var permissions: Array<String> = emptyArray(),
        var handleRationale: Boolean = true,
        var rationaleMessage: String = "",
        var handlePermanentlyDenied: Boolean = true,
        var permanentlyDeniedMessage: String = "",
        var rationaleMethod: Method? = null,
        var permanentDeniedMethod: Method? = null,
        var permanentlyDeniedPermissions: Array<String> = emptyArray()
) {
    /**
     * Proceed with requesting permissions again with user request
     */
    fun proceed() = target.requestPermissionsFromUser()

    /**
     * Cancels the current permissions request flow
     */
    fun cancel() = target.clean()

    /**
     * In case of permissions permanently denied, request user to enable from app settings
     */
    fun openAppSettings() = target.openAppSettings()
}