package com.sam.lib.mobilepay.plugin


import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * 查找并替换
 */
class ModifyPayConfigUtils {

    static File modifyJar(File jarFile, File tempDir, boolean nameHex, PayExtension payExtension) {
        /**
         * 读取原jar
         */
        def file = new JarFile(jarFile)
        /** 设置输出到的jar */
        def hexName = ""
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = file.getInputStream(jarEntry)

            String entryName = jarEntry.getName()
            String className

            ZipEntry zipEntry = new ZipEntry(entryName)

            jarOutputStream.putNextEntry(zipEntry)

            byte[] modifiedClassBytes = null
            byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
            if (entryName.endsWith(".class")) {
                className = entryName.replace(".class", "")
                println("遍历 class $className")
                modifiedClassBytes = modifyClasses(className, sourceClassBytes, payExtension)
            }
            if (modifiedClassBytes == null) {
                jarOutputStream.write(sourceClassBytes)
            } else {
                jarOutputStream.write(modifiedClassBytes)
            }
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    static byte[] modifyClasses(String className, byte[] srcByteCode, PayExtension payExtension) {
        byte[] classBytesCode = null
        if ("com/cnki/lib/mobilepay/PayConfig" == className) {
            try {
                println("--> 开始修改class start modifying---- ${className}")
                classBytesCode = modifyClass(srcByteCode, payExtension)
                return classBytesCode
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        if (classBytesCode == null) {
            classBytesCode = srcByteCode
        }
        return classBytesCode
    }

    static byte[] modifyClass(byte[] srcClass, PayExtension payExtension) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor adapter = new ClassFilterVisitor(classWriter, payExtension)
        ClassReader cr = new ClassReader(srcClass)
        cr.accept(adapter, 0)
        return classWriter.toByteArray()
    }


    static class ClassFilterVisitor extends ClassVisitor implements Opcodes {

        private PayExtension payExtension

        ClassFilterVisitor(
                final ClassVisitor cv, PayExtension payExtension) {
            super(org.objectweb.asm.Opcodes.ASM5, cv)
            this.payExtension = payExtension
        }

        @Override
        FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            println('* visitField *' + " , " + access + " , " + name + " , " + desc + " , " + signature + " , " + value)
            if (payExtension != null) {
                switch (name) {
                    case "AliPay_APPID":
                        return super.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, name, desc, signature, payExtension.aliExtension.appid)
                    case "AliPay_NOTIFY_URL":
                        return super.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, name, desc, signature, payExtension.aliExtension.notify)
                    case "AliPay_PARTNER":
                        return super.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, name, desc, signature, payExtension.aliExtension.partner)
                    case "AliPay_RSA_PRIVATE":
                        return super.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, name, desc, signature, payExtension.aliExtension.rsaprivate)
                    case "AliPay_SELLER":
                        return super.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, name, desc, signature, payExtension.aliExtension.seller)
                    case "WeiXin_APP_ID":
                        return super.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, name, desc, signature, payExtension.wechatExtension.appid)
                    case "WeiXin_MCH_ID":
                        return super.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, name, desc, signature, payExtension.wechatExtension.mchid)
                }
            }

            return super.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, name, desc, signature, value)
        }

        @Override
        void visitEnd() {
            super.visitEnd()
        }

        @Override
        void visitAttribute(Attribute attribute) {
            super.visitAttribute(attribute)
        }

        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return super.visitAnnotation(desc, visible)
        }

        @Override
        void visitInnerClass(String name, String outerName,
                             String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, access)
        }

        @Override
        void visitOuterClass(String owner, String name, String desc) {
            super.visitOuterClass(owner, name, desc)
        }

        @Override
        void visitSource(String source, String debug) {
            super.visitSource(source, debug)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return super.visitMethod(access, name, desc, signature, exceptions)
        }

    }


}