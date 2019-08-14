package com.sam.lib.mobilepay.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginImpl implements Plugin<Project> {

    void apply(Project project) {


        def android = project.extensions.getByType(AppExtension)

        //注册一个Transform
        def classTransform = new MobilePayTransform(project)
        android.registerTransform(classTransform)

        project.extensions.create("mobilePay", PayExtension, project)


        android.applicationVariants.all { variant ->

            println(project.mobilePay.aliExtension.appid)
            println(project.mobilePay.aliExtension.notify)
            println(project.mobilePay.aliExtension.partner)
            println(project.mobilePay.aliExtension.rsaprivate)
            println(project.mobilePay.aliExtension.seller)
            println(project.mobilePay.wechatExtension.appid)
            println(project.mobilePay.wechatExtension.mchid)

        }


    }

}