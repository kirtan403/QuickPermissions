package com.livinglifetechway.quickpermissions.aspect;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.livinglifetechway.quickpermissions.annotations.RequiresPermissions;
import com.livinglifetechway.quickpermissions.util.PermissionCheckerFragment;
import com.livinglifetechway.quickpermissions.util.PermissionUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * Injects code to ask for permissions before executing any code that requires permissions
 * defined in the annotation
 */

@Aspect
public class PermissionsManager {

    private static final String TAG = PermissionsManager.class.getSimpleName();

    private static final String POINTCUT_METHOD =
            "execution(@com.livinglifetechway.quickpermissions.annotations.RequiresPermissions * *(..))";

    @Pointcut(POINTCUT_METHOD)
    public void methodAnnotatedRequiresPermissions() {
    }

    @Around("methodAnnotatedRequiresPermissions()")
    public Object weaveJoinPoint(final ProceedingJoinPoint joinPoint) throws Throwable {
        Log.d(TAG, "weaveJoinPoint: inside weavejointpoint");
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        Method method = methodSignature.getMethod();
        RequiresPermissions annotation = method.getAnnotation(RequiresPermissions.class);
        String[] permissions = annotation.permissions();

        Log.d(TAG, "weaveJoinPoint: " + permissions[0]);

        Object target = joinPoint.getTarget();
        AppCompatActivity context = null;
        if (target instanceof AppCompatActivity) {
            context = (AppCompatActivity) target;
        }

        if (context != null) {
            Log.d(TAG, "weaveJoinPoint: context found");
            if (PermissionUtil.INSTANCE.hasSelfPermission(context, permissions)) {
                Log.d(TAG, "weaveJoinPoint: has permissions");
                joinPoint.proceed();
            } else {
                Log.d(TAG, "weaveJoinPoint: doesn't has permissions");
                PermissionCheckerFragment fragmentByTag = (PermissionCheckerFragment) context.getSupportFragmentManager().findFragmentByTag(PermissionCheckerFragment.class.getSimpleName());

                if (fragmentByTag == null) {
                    Log.d(TAG, "weaveJoinPoint: adding headless fragment");
                    fragmentByTag = PermissionCheckerFragment.Companion.newInstance(permissions);
                    FragmentManager supportFragmentManager = context.getSupportFragmentManager();

                    supportFragmentManager.beginTransaction().add(fragmentByTag, PermissionCheckerFragment.class.getSimpleName()).commit();
                }

                fragmentByTag.setListener(new PermissionCheckerFragment.PermissionCallback() {
                    @Override
                    public void onPermissionResult() {
                        Log.d(TAG, "weaveJoinPoint: got permissions");
                        try {
                            joinPoint.proceed();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                });

            }
        }
        return null;
    }

}
