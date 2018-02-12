package com.livinglifetechway.quickpermissions_plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class QuickPermissionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        makeSureProjectIsAndroidAppOrLib(project)

        project.android.registerTransform(new QuickPermissionsTransform(project))

        project.dependencies {
            implementation 'org.aspectj:aspectjrt:1.8.13'
            implementation 'com.github.kirtan403:quickpermissions:eb425cfbae'
        }
    }

    private void makeSureProjectIsAndroidAppOrLib(Project project) {
        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)

        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }
    }

}