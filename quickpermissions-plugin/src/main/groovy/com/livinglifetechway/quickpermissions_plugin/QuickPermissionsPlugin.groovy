package com.livinglifetechway.quickpermissions_plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.FeaturePlugin
import com.android.build.gradle.InstantAppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class QuickPermissionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        // check for the android plugins
        // app, lib, feature, instant app
        // it should be one of the above
        if (project.plugins.withType(AppPlugin)
                || project.plugins.withType(LibraryPlugin)
                || project.plugins.withType(FeaturePlugin)
                || project.plugins.withType(InstantAppPlugin)) {

            // register a transformer
            project.android.registerTransform(new QuickPermissionsTransform(project))

            // add necessary dependencies
            project.dependencies {
                implementation 'org.aspectj:aspectjrt:1.8.13'
                implementation 'com.github.kirtan403:quickpermissions:eb425cfbae'
            }

        } else {
            // throw exception it doesn't work on any other module
            throw new IllegalStateException("'com.android.application', 'com.android.library', " +
                    "'com.android.feature' or 'com.android.instantapp' plugin required.")
        }

    }

}