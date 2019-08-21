package com.sam.lib.mobilepay.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class MobilePayTransform extends Transform {

    Project project

    MobilePayTransform(Project project) {
        println "----- 注入 transform 了 ----"
        this.project = project
    }
    /**
     * 设置自定义的Transform对应的Task名称
     * @return
     */
    @Override
    String getName() {
        return "mobilePayTransform"
    }
    /**
     * 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
     * @return 这里只处理Class文件
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }
    /**
     * 指定Transform的作用范围
     * @return 所有文件下去寻找
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }
    /**
     * 是否支持增量编译
     * @return false-不支持
     */
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        def outputProvider = transformInvocation.getOutputProvider()
        def inputs = transformInvocation.inputs

        def payExtension = project.extensions.getByType(PayExtension)
        println("pay extension " + payExtension)

        println "-- start transform ---"

        inputs.each { TransformInput input ->

            input.directoryInputs.each { DirectoryInput dirInput ->
                // 获取output目录
                def dest = outputProvider.getContentLocation(dirInput.name,
                        dirInput.contentTypes, dirInput.scopes,
                        Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(dirInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->

                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                if (jarInput.name.startsWith("com.github.whxceg:MobilePay")) {
                    println("找到要修改的jar")
                    def file = ModifyPayConfigUtils.modifyJar(jarInput.file, transformInvocation.context.temporaryDir, true, payExtension)
                    FileUtils.copyFile(file, dest)
                } else {
                    FileUtils.copyFile(jarInput.file, dest)
                }
            }
        }

    }

}