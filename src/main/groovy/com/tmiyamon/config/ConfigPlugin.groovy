package com.tmiyamon.config

import com.android.build.gradle.LibraryExtension
import com.tmiyamon.config.task.GenerateSettingsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.AppExtension
import org.gradle.api.Task
import org.gradle.api.logging.Logging

class ConfigPlugin implements Plugin<Project> {

    public static final LOG = Logging.getLogger(ConfigPlugin)

    enum ProjectType {

        ANDROID_APPLICATION('com.android.application', AppExtension),
        ANDROID_LIBRARY('com.android.library', LibraryExtension)

        final String pluginId
        final Class extensionType

        ProjectType(String pluginId, Class extensionType) {
            this.pluginId = pluginId
            this.extensionType = extensionType
        }
    }

    @Override
    void apply(Project project) {
        ProjectType projectType = evaluateProjectType(project)
        if (projectType) {
            registerSettingsCodeGenerationTask(project, projectType)
        } else {
            LOG.warn("Config plugin supports only android.application and android.library projects.")
        }
    }

    private ProjectType evaluateProjectType(Project project) {
        ProjectType.values().find { project.plugins.hasPlugin(it.pluginId) }
    }

    private def registerSettingsCodeGenerationTask(Project project, ProjectType projectType) {
        project.plugins.withId(projectType.pluginId) {
            project.extensions
                    .getByType(projectType.extensionType)
                    .libraryVariants.all {
                        registerSettingsCodeGenerationTask(project, it)
                    }
        }
    }

    private def registerSettingsCodeGenerationTask(Project project, BaseVariant variant) {
        def task = createCodeGenerationTask(project, variant)

        variant.registerJavaGeneratingTask(task, task.outputDir())
        android.sourceSets[variant.name].java.srcDirs += [task.outputDir()]
    }


    private Task createCodeGenerationTask(Project project, BaseVariant variant) {
        project.tasks.create(
                name: "generate${variant.name.capitalize()}Settings",
                type: GenerateSettingsTask) {
            packageName variant.generateBuildConfig.buildConfigPackageName
            flavorName variant.flavorName
            buildTypeName variant.buildType.name
            variantDirName variant.dirName
        }
    }
}

