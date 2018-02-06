package com.livinglifetechway.quickpermissions.aspect

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.livinglifetechway.quickpermissions.annotations.RequiresPermissions
import com.livinglifetechway.quickpermissions.util.PermissionCheckerFragment
import com.livinglifetechway.quickpermissions.util.PermissionUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature

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

        if (context != null) {
            Log.d(TAG, "weaveJoinPoint: context found")
            if (PermissionUtil.hasSelfPermission(context, permissions)) {
                Log.d(TAG, "weaveJoinPoint: already has required permissions. Proceed with the execution.")
                joinPoint.proceed()
            } else {
                Log.d(TAG, "weaveJoinPoint: doesn't have required permissions")

                // support for AppCompatActivity and Activity
                var permissionCheckerFragment = when (context) {
                    is AppCompatActivity -> context.supportFragmentManager.findFragmentByTag(PermissionCheckerFragment::class.java.canonicalName) as PermissionCheckerFragment?
                    is Activity -> context.fragmentManager.findFragmentByTag(PermissionCheckerFragment::class.java.canonicalName) as PermissionCheckerFragment?
                    else -> null
                }

                // check if permission check fragment is added or not
                if (permissionCheckerFragment == null) {
                    Log.d(TAG, "weaveJoinPoint: adding headless fragment for asking permissions")
                    permissionCheckerFragment = PermissionCheckerFragment.newInstance()
                    when (context) {
                        is AppCompatActivity -> {
                            context.supportFragmentManager
                                    ?.beginTransaction()
                                    ?.add(permissionCheckerFragment, PermissionCheckerFragment::class.java.canonicalName)
                                    ?.commit()

                            // make sure fragment is added before we do any context based operations
                            context.supportFragmentManager.executePendingTransactions()
                        }
                        is Activity -> {
                            // this does not work at the moment
//                            context.fragmentManager
//                                    ?.beginTransaction()
//                                    ?.add(permissionCheckerFragment, PermissionCheckerFragment::class.java.canonicalName)
//                                    ?.commit()

                        }
                    }

                }

                permissionCheckerFragment.setListener(object : PermissionCheckerFragment.PermissionCallback {
                    override fun onPermissionResult() {
                        Log.d(TAG, "weaveJoinPoint: got permissions")
                        try {
                            joinPoint.proceed()
                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                        }

                    }
                })

                // now actually request permissions
                permissionCheckerFragment.requestPermissions(permissions)
            }
        } else {
            // context is null
            // cannot handle the permission checking from the any class other than Activity/AppCompatActivity
            // crash the app RIGHT NOW!
        }
        return null
    }

    companion object {

        private val TAG = PermissionsManager::class.java.simpleName

        private const val POINTCUT_METHOD = "execution(@com.livinglifetechway.quickpermissions.annotations.RequiresPermissions * *(..))"
    }

}
