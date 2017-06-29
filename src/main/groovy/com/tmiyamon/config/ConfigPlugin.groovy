package com.tmiyamon.config

import com.android.build.gradle.LibraryExtension
import com.tmiyamon.config.task.GenerateSettingsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.AppExtension
import org.gradle.api.Task

class ConfigPlugin implements Plugin<Project> {

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
        ProjectType.values().each {
            createCodeGenerationTasks(project, it)
        }
    }

    private createCodeGenerationTasks(Project project, ProjectType projectType) {
        project.plugins.withId(projectType.pluginId) {
            def android = project.extensions.getByType(projectType.extensionType)
            android.libraryVariants.all { BaseVariant variant ->

                def task = createCodeGenerationTask(project, variant)

                variant.registerJavaGeneratingTask(task, task.outputDir())
                android.sourceSets[variant.name].java.srcDirs += [task.outputDir()]
            }
        }
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

