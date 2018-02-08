package com.livinglifetechway.quickpermissions.util

import java.lang.reflect.Method

data class QuickPermissionsRequest(
        var target: PermissionCheckerFragment,
        var permissions: Array<String> = arrayOf(),
        var handleRationale: Boolean = true,
        var rationaleMessage: String = "",
        var handlePermanentlyDenied: Boolean = true,
        var permanentlyDeniedMessage: String = "",
        var rationaleMethod: Method? = null,
        var permanentDeniedMethod: Method? = null
) {
    fun proceed() {
        target.requestPermissionsFromUser(this)
    }

    fun cancel() {
    }
}