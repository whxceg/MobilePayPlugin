package com.sam.lib.mobilepay.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginImpl implements Plugin<Project> {

    void apply(Project project) {

        project.extensions.create("mobilePay", PayExtension, project)

        def android = project.extensions.getByType(AppExtension)
        //注册一个Transform
        def classTransform = new MobilePayTransform(project)
        android.registerTransform(classTransform)
        project.extensions.create("mobilePay", PayExtension, project)

    }

}