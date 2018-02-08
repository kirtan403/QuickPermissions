package com.livinglifetechway.quickpermissions.annotations


/**
 * Permission Required annotation to safely execute code block requiring defined permissions
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class RequiresPermissions(
        val permissions: Array<String>,
        val rationaleMessage: String = "",
        val permanentlyDeniedMessage: String = "",
        val handleRationale: Boolean = true,
        val handlePermanentlyDenied: Boolean = true)

/**
 * Permission Required annotation to safely execute code block requiring defined permissions
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class OnShowRationale

/**
 * Permission Required annotation to safely execute code block requiring defined permissions
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class OnPermissionPermanentlyDenied

/**
 * Permission Required annotation to safely execute code block requiring defined permissions
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class OnPermissionsDenied