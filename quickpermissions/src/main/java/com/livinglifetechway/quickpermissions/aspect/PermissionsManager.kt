package com.livinglifetechway.quickpermissions.aspect

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.livinglifetechway.k4kotlin.transact
import com.livinglifetechway.quickpermissions.annotations.OnPermissionPermanentlyDenied
import com.livinglifetechway.quickpermissions.annotations.OnShowRationalePermissionDialog
import com.livinglifetechway.quickpermissions.annotations.RequiresPermissions
import com.livinglifetechway.quickpermissions.util.PermissionCheckerFragment
import com.livinglifetechway.quickpermissions.util.PermissionUtil
import com.livinglifetechway.quickpermissions.util.QuickPermissionsRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import java.lang.reflect.Method

/**
 * Injects code to ask for permissions before executing any code that requires permissions
 * defined in the annotation
 */
@Aspect
class PermissionsManager {

    @Pointcut(POINTCUT_METHOD)
    fun methodAnnotatedRequiresPermissions() {
    }

    @Around("methodAnnotatedRequiresPermissions()")
    @Throws(Throwable::class)
    fun weaveJoinPoint(joinPoint: ProceedingJoinPoint): Any? {
        Log.d(TAG, "weaveJoinPoint: start")

        // get the permissions defined in annotation
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val annotation = method.getAnnotation(RequiresPermissions::class.java)
        val permissions = annotation.permissions

        Log.d(TAG, "permissions to check: " + permissions)

        val target = joinPoint.target
        var context: Context? = null
        if (target is Context) {
            context = target
        }

        if (context != null && (context is AppCompatActivity || context is Fragment)) {
            Log.d(TAG, "weaveJoinPoint: context found")

            // check if we have the permissions
            if (PermissionUtil.hasSelfPermission(context, permissions)) {
                Log.d(TAG, "weaveJoinPoint: already has required permissions. Proceed with the execution.")
                joinPoint.proceed()
            } else {
                // we don't have required permissions
                // begin the permission request flow

                Log.d(TAG, "weaveJoinPoint: doesn't have required permissions")

                // check if we have permission checker fragment already attached

                // support for AppCompatActivity and Activity
                var permissionCheckerFragment = when (context) {
                // for app compat activity
                    is AppCompatActivity -> context.supportFragmentManager.findFragmentByTag(PermissionCheckerFragment::class.java.canonicalName) as PermissionCheckerFragment?
                // for support fragment
                    is Fragment -> context.childFragmentManager.findFragmentByTag(PermissionCheckerFragment::class.java.canonicalName) as PermissionCheckerFragment?
                // else return null
                    else -> null
                }

                // check if permission check fragment is added or not
                // if not, add that fragment
                if (permissionCheckerFragment == null) {
                    Log.d(TAG, "weaveJoinPoint: adding headless fragment for asking permissions")
                    permissionCheckerFragment = PermissionCheckerFragment.newInstance()
                    when (context) {
                        is AppCompatActivity -> {
                            context.supportFragmentManager.transact {
                                add(permissionCheckerFragment, PermissionCheckerFragment::class.java.canonicalName)
                            }
                            // make sure fragment is added before we do any context based operations
                            context.supportFragmentManager.executePendingTransactions()
                        }
                        is Fragment -> {
                            // this does not work at the moment
                            context.childFragmentManager.transact {
                                add(permissionCheckerFragment, PermissionCheckerFragment::class.java.canonicalName)
                            }
                            // make sure fragment is added before we do any context based operations
                            context.childFragmentManager.executePendingTransactions()
                        }
                    }
                }

                // set callback to permission checker fragment
                permissionCheckerFragment.setListener(object : PermissionCheckerFragment.QuickPermissionsCallback {
                    override fun shouldShowRequestPermissionsRationale(quickPermissionsRequest: QuickPermissionsRequest?) {
                        quickPermissionsRequest?.rationaleMethod?.invoke(joinPoint.target, quickPermissionsRequest)
                    }

                    override fun onPermissionsGranted(quickPermissionsRequest: QuickPermissionsRequest?) {
                        Log.d(TAG, "weaveJoinPoint: got permissions")
                        try {
                            joinPoint.proceed()
                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                        }
                    }

                    override fun onPermissionsPermanentlyDenied(quickPermissionsRequest: QuickPermissionsRequest?) {
                        quickPermissionsRequest?.permanentDeniedMethod?.invoke(joinPoint.target, quickPermissionsRequest)
                    }
                })

                // create permission request instance
                var permissionRequest = QuickPermissionsRequest(permissionCheckerFragment, permissions)
                permissionRequest.handleRationale = annotation.handleRationale
                permissionRequest.handlePermanentlyDenied = annotation.handlePermanentlyDenied
                permissionRequest.rationaleMessage = if (annotation.rationaleMessage.isBlank()) "default rationale" else annotation.rationaleMessage
                permissionRequest.permanentlyDeniedMessage = if (annotation.permanentlyDeniedMessage.isBlank()) "default perm denied" else annotation.permanentlyDeniedMessage
                permissionRequest.rationaleMethod = getMethodWithAnnotation<OnShowRationalePermissionDialog>(joinPoint.target)
                permissionRequest.permanentDeniedMethod = getMethodWithAnnotation<OnPermissionPermanentlyDenied>(joinPoint.target)

                // begin the flow by requesting permissions
                permissionCheckerFragment.requestPermissionsFromUser(permissionRequest)
            }
        } else {
            // context is null
            // cannot handle the permission checking from the any class other than Activity/AppCompatActivity
            // crash the app RIGHT NOW!
        }
        return null
    }

    inline fun <reified T : Annotation> getMethodWithAnnotation(instance: Any): Method? {
        // returns first matched  method or null
        return instance::class.java.declaredMethods.firstOrNull {
            it.isAnnotationPresent(T::class.java) && it.parameterTypes.size == 1 && it.parameterTypes[0] == QuickPermissionsRequest::class.java
        }
    }

    companion object {

        private val TAG = PermissionsManager::class.java.simpleName

        private const val POINTCUT_METHOD = "execution(@com.livinglifetechway.quickpermissions.annotations.RequiresPermissions * *(..))"
    }

}
