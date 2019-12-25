package com.sjcqs.android.config

import com.android.build.gradle.LibraryExtension
import com.sjcqs.android.config.task.GenerateSettingsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.AppExtension
import org.gradle.api.Task
import org.gradle.api.logging.Logging

class ConfigPlugin implements Plugin<Project> {

    public static final LOG = Logging.getLogger(ConfigPlugin)

    enum ProjectType {

        ANDROID_APPLICATION('com.android.application',
                            AppExtension,
                {extension -> return extension.applicationVariants }),
        ANDROID_LIBRARY('com.android.library',
                LibraryExtension,
                {extension -> return extension.libraryVariants })

        final String pluginId
        final Class extensionType
        final Closure fetchVariants

        ProjectType(String pluginId, Class extensionType, Closure fetchVariants) {
            this.pluginId = pluginId
            this.extensionType = extensionType
            this.fetchVariants = fetchVariants
        }
    }

    @Override
    void apply(Project project) {
        ProjectType projectType = evaluateProjectType(project)
        if (projectType) {
            registerSettingsCodeGenerationTask(project, projectType)
        } else {
            LOG.warn("Config plugin supports only com.android.application and com.android.library projects.")
        }
    }

    private ProjectType evaluateProjectType(Project project) {
        ProjectType.values().find { project.plugins.hasPlugin(it.pluginId) }
    }

    private def registerSettingsCodeGenerationTask(Project project, ProjectType projectType) {
        def androidExtension = project.extensions.getByType(projectType.extensionType)
        def sourceSets = androidExtension.sourceSets
        projectType.fetchVariants(androidExtension)
                    .all { variant ->
                        def task = createCodeGenerationTask(project, variant)
                        variant.registerJavaGeneratingTask(task, task.outputDir())
                        sourceSets[variant.name].java.srcDirs += [task.outputDir()]
                    }
    }

    private Task createCodeGenerationTask(Project project, BaseVariant variant) {
        def dimensions = []
        variant.productFlavors.reverseEach {
            dimensions << it.name
        }
        project.tasks.create(
                name: "generate${variant.name.capitalize()}Settings",
                type: GenerateSettingsTask) {
            packageName variant.generateBuildConfigProvider.get().buildConfigPackageName
            flavorName variant.flavorName
            buildTypeName variant.buildType.name
            variantDirName variant.dirName
            dimensionNames dimensions
        }
    }
}

