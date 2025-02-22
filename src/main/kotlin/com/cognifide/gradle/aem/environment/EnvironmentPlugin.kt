package com.cognifide.gradle.aem.environment

import com.cognifide.gradle.aem.AemPlugin
import com.cognifide.gradle.aem.common.CommonPlugin
import com.cognifide.gradle.aem.common.tasks.lifecycle.*
import com.cognifide.gradle.aem.environment.tasks.*
import com.cognifide.gradle.aem.instance.InstancePlugin
import com.cognifide.gradle.aem.instance.tasks.InstanceUp
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * Separate plugin which provides tasks for managing local development environment additional to AEM, like:
 * Dispatcher, Solr, Knot.X, etc.
 *
 * Most often should be applied only to one project in build.
 */
class EnvironmentPlugin : AemPlugin() {

    override fun Project.configure() {
        setupDependentPlugins()
        setupTasks()
    }

    private fun Project.setupDependentPlugins() {
        plugins.apply(CommonPlugin::class.java)
    }

    private fun Project.setupTasks() {
        tasks {
            // Plugin tasks

            register<EnvironmentDown>(EnvironmentDown.NAME)
            register<EnvironmentUp>(EnvironmentUp.NAME) {
                mustRunAfter(EnvironmentResolve.NAME, EnvironmentDown.NAME, EnvironmentDestroy.NAME)
                plugins.withId(InstancePlugin.ID) { mustRunAfter(InstanceUp.NAME) }
            }
            register<EnvironmentRestart>(EnvironmentRestart.NAME) {
                dependsOn(EnvironmentDown.NAME, EnvironmentUp.NAME)
            }
            register<EnvironmentDestroy>(EnvironmentDestroy.NAME) {
                dependsOn(EnvironmentDown.NAME)
            }
            register<EnvironmentResetup>(EnvironmentResetup.NAME) {
                dependsOn(EnvironmentDestroy.NAME, EnvironmentUp.NAME)
            }

            register<EnvironmentDev>(EnvironmentDev.NAME) {
                mustRunAfter(EnvironmentUp.NAME)
            }
            register<EnvironmentAwait>(EnvironmentAwait.NAME) {
                mustRunAfter(EnvironmentUp.NAME)
            }
            register<EnvironmentClean>(EnvironmentClean.NAME) {
                mustRunAfter(EnvironmentUp.NAME)
            }
            register<EnvironmentHosts>(EnvironmentHosts.NAME)
            register<EnvironmentResolve>(EnvironmentResolve.NAME)

            // Common lifecycle

            registerOrConfigure<Up>(Up.NAME) {
                dependsOn(EnvironmentUp.NAME)
            }
            registerOrConfigure<Down>(Down.NAME) {
                dependsOn(EnvironmentDown.NAME)
            }
            registerOrConfigure<Destroy>(Destroy.NAME) {
                dependsOn(EnvironmentDestroy.NAME)
            }
            registerOrConfigure<Restart>(Restart.NAME) {
                dependsOn(EnvironmentRestart.NAME)
            }
            registerOrConfigure<Setup>(Setup.NAME) {
                dependsOn(EnvironmentUp.NAME)
            }
            registerOrConfigure<Resetup>(Resetup.NAME) {
                dependsOn(EnvironmentResetup.NAME)
            }
            registerOrConfigure<Resolve>(Resolve.NAME) {
                dependsOn(EnvironmentResolve.NAME)
            }
            registerOrConfigure<Await>(Await.NAME) {
                dependsOn(EnvironmentAwait.NAME)
            }

            // Gradle lifecycle

            named<Task>(LifecycleBasePlugin.CLEAN_TASK_NAME) {
                dependsOn(EnvironmentClean.NAME)
            }
        }
    }

    companion object {
        const val ID = "com.cognifide.aem.environment"
    }
}