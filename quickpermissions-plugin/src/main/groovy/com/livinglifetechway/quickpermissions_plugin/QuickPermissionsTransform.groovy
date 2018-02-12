package com.livinglifetechway.quickpermissions_plugin

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.google.common.base.Joiner
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger

class QuickPermissionsTransform extends Transform {

    private Project project

    QuickPermissionsTransform(Project project) {
        this.project = project
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        List<File> files = Lists.newArrayList()
        List<File> classpathFiles = Lists.newArrayList()
        Logger logger = project.logger

        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()

        // clean
        outputProvider.deleteAll()

        //  Referenced Inputs to classpath
        for (TransformInput input : transformInvocation.getReferencedInputs()) {
            input.directoryInputs.each {
                classpathFiles.add(it.file)
            }

            input.jarInputs.each {
                classpathFiles.add(it.file)
            }
        }

        // Scope inputs
        for (TransformInput input : transformInvocation.getInputs()) {

            for (DirectoryInput folder : input.directoryInputs) {
                files.add(folder.file)
            }

            for (JarInput jar : input.jarInputs) {
                files.add(jar.file)
            }
        }

        // Evaluate class paths
        final String inpath = Joiner.on(File.pathSeparator).join(files)
        final String classpath = Joiner.on(File.pathSeparator).join(
                classpathFiles.collect { it.absolutePath })
        final String bootpath = Joiner.on(File.pathSeparator).join(project.android.bootClasspath)
        final File output = outputProvider.getContentLocation("main", outputTypes, Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT), Format.DIRECTORY)

        // Weaving args
        def args = [
                "-verbose",
                "-source", "1.7",
                "-target", "1.7",
                "-showWeaveInfo",
                "-inpath", inpath,
                "-d", output.absolutePath,
                "-bootclasspath", bootpath]

        // Append classpath argument if any
        if (!Strings.isNullOrEmpty(classpath)) {
            args << '-classpath'
            args << classpath
        }

        // run aspectj
        MessageHandler handler = new MessageHandler(true)
        new Main().run(args as String[], handler)

        for (IMessage message : handler.getMessages(null, true)) {

            if (IMessage.ERROR.isSameOrLessThan(message.kind)) {
                logger.error(message.message, message.thrown)
                throw new GradleException(message.message, message.thrown)
            } else if (IMessage.WARNING.isSameOrLessThan(message.kind)) {
                logger.warn message.message
            } else if (IMessage.DEBUG.isSameOrLessThan(message.kind)) {
                logger.info message.message
            } else {
                logger.debug message.message
            }
        }

    }

    @NonNull
    @Override
    String getName() {
        "QuickPermissions"
    }

    @NonNull
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return Sets.immutableEnumSet(QualifiedContent.DefaultContentType.CLASSES)
    }

    @NonNull
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT)
    }

    @Override
    Set<QualifiedContent.Scope> getReferencedScopes() {
        return Sets.immutableEnumSet(
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES,
                QualifiedContent.Scope.PROVIDED_ONLY
        )
    }

    @Override
    boolean isIncremental() {
        false
    }

}