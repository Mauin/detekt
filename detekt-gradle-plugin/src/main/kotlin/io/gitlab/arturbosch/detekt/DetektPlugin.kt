package io.gitlab.arturbosch.detekt

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.io.File

/**
 * @author Marvin Ramin
 */
class DetektPlugin : Plugin<Project> {


	override fun apply(project: Project) {

		val extension = project.extensions.create(DETEKT, DetektExtension::class.java, project)

		configurePluginDependencies(project, extension)

		createAndConfigureDetektTask(project, extension)
		createAndConfigureCreateBaselineTask(project, extension)
		createAndConfigureGenerateConfigTask(project, extension)

		createAndConfigureIdeaTasks(project, extension)
	}

	private fun createAndConfigureDetektTask(project: Project, extension: DetektExtension) {
		val detektTask = project.tasks.createLater(DETEKT, Detekt::class.java) {
			debug = extension.debug
			parallel = extension.parallel
			disableDefaultRuleSets = extension.disableDefaultRuleSets
			filters = extension.filters
			config = extension.config
			baseline = extension.baseline
			plugins = extension.plugins
			input = determineInput(extension)
			reports.all {
				extension.reports.withName(name)?.let {
					val reportExtension = it
					isEnabled = reportExtension.enabled
					val fileSuffix = name
					val reportsDir = extension.reportsDir ?: extension.defaultReportsDir
					val customDestination = reportExtension.destination
					destination = customDestination ?: File(reportsDir, "${DETEKT}.$fileSuffix")
				}
			}
		}

		project.tasks.findByName(LifecycleBasePlugin.CHECK_TASK_NAME)?.dependsOn(detektTask)
	}

	private fun createAndConfigureCreateBaselineTask(project: Project, extension: DetektExtension) =
			project.tasks.createLater(BASELINE, DetektCreateBaselineTask::class.java) {
				baseline = extension.baseline
				debugOrDefault = extension.debug
				parallelOrDefault = extension.parallel
				disableDefaultRuleSetsOrDefault = extension.disableDefaultRuleSets
				filters = extension.filters
				config = extension.config
				plugins = extension.plugins
				input = determineInput(extension)
			}

	private fun createAndConfigureGenerateConfigTask(project: Project, extension: DetektExtension) =
			project.tasks.createLater(GENERATE_CONFIG, DetektGenerateConfigTask::class.java) {
				input = determineInput(extension)
			}

	private fun createAndConfigureIdeaTasks(project: Project, extension: DetektExtension) {
		project.tasks.createLater(IDEA_FORMAT, DetektIdeaFormatTask::class.java) {
			debugOrDefault = extension.debug
			input = determineInput(extension)
			ideaExtension = extension.idea
		}

		project.tasks.createLater(IDEA_INSPECT, DetektIdeaInspectionTask::class.java) {
			debugOrDefault = extension.debug
			input = determineInput(extension)
			ideaExtension = extension.idea
		}
	}

	private fun determineInput(extension: DetektExtension) = extension.input.filter { it.exists() }

	private fun configurePluginDependencies(project: Project, extension: DetektExtension) =
			project.configurations.create(DETEKT) {
				isVisible = false
				isTransitive = true
				description = "The $DETEKT libraries to be used for this project."
				defaultDependencies {
					val version = extension.toolVersion ?: DEFAULT_DETEKT_VERSION
					add(project.dependencies.create("io.gitlab.arturbosch.detekt:detekt-cli:$version"))
				}
			}


	companion object {
		private const val DEFAULT_DETEKT_VERSION = "1.0.0-GRADLE"
		private const val DETEKT = "detekt"
		private const val IDEA_FORMAT = "detektIdeaFormat"
		private const val IDEA_INSPECT = "detektIdeaInspect"
		private const val GENERATE_CONFIG = "detektGenerateConfig"
		private const val BASELINE = "detektBaseline"
	}
}
