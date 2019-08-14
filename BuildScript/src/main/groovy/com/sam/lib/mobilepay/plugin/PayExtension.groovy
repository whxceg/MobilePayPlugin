package com.sam.lib.mobilepay.plugin


import org.gradle.api.Project

class PayExtension {


    Project project

    AliExtension aliExtension
    WeChatExtension wechatExtension

    PayExtension(Project project) {
        this.project = project
    }

    void ali(Closure closure) {
        def extension = new AliExtension(project)
        project.configure(extension, closure)
        println " -- > $extension"
        aliExtension = extension
    }

    void wechat(Closure closure) {
        def extension = new WeChatExtension(project)
        project.configure(extension, closure)
        println " -- > $extension"
        wechatExtension = extension
    }

    @Override
    String toString() {
        return "aliExtension : " + aliExtension.toString() + " \n wechatExtension : " + wechatExtension.toString()
    }

    static class AliExtension {

        Project project

        String appid
        String notify
        String partner
        String rsaprivate
        String seller

        AliExtension(Project project) {
            this.project = project
        }

        @Override
        String toString() {
            return "appid ->" + appid + " , notify -> " + notify + " , partner -> " + partner + " , rsaprivate -> " + rsaprivate + " , seller -> " + seller
        }

    }

    static class WeChatExtension {
        Project project

        String appid
        String mchid

        WeChatExtension(Project project) {
            this.project = project
        }

        @Override
        String toString() {
            return "appid ->" + appid + " , machid -> " + mchid
        }
    }


}