package io.gitlab.arturbosch.detekt.invoke

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.*
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/**
 * @author Marvin Ramin
 */
object DetektInvoker {
	fun check(detekt: Detekt) {
		val project = detekt.project
		val classpath = project.configurations.getAt("detekt")

		val extraArgs = mutableMapOf<String, String>()
		if (detekt.reports.html.isEnabled) extraArgs += REPORT_HTML_PARAMETER to detekt.reports.html.destination.absolutePath
		if (detekt.reports.xml.isEnabled) extraArgs += REPORT_XML_PARAMETER to detekt.reports.xml.destination.absolutePath

		val argumentList = baseDetektParameters(detekt, extraArgs)

		invokeCli(project, classpath, argumentList.toList(), detekt.debugOrDefault)
	}

	fun createBaseline(detekt: Detekt) {
		val project = detekt.project
		val classpath = project.configurations.getAt("detekt")

		val argumentList = baseDetektParameters(detekt)
		argumentList += CREATE_BASELINE_PARAMETER

		invokeCli(project, classpath, argumentList.toList(), detekt.debugOrDefault)
	}

	fun generateConfig(detekt: Detekt) {
		val project = detekt.project
		val classpath = project.configurations.getAt("detekt")

		val args = mapOf<String, String>(
				INPUT_PARAMETER to detekt.input.get().asPath
		)

		val argumentList = args.flatMapTo(ArrayList()) { listOf(it.key, it.value) }
		argumentList += GENERATE_CONFIG_PARAMETER

		invokeCli(project, classpath, argumentList.toList(), detekt.debugOrDefault)
	}

	private fun baseDetektParameters(detekt: Detekt, extraArgs: MutableMap<String, String> = mutableMapOf()): MutableList<String> {
		val args = extraArgs

		args += INPUT_PARAMETER to detekt.input.get().asPath

		detekt.config.orNull?.let { args += CONFIG_PARAMETER to it.absolutePath }
		detekt.filters.orNull?.let { args += FILTERS_PARAMETER to it }
		detekt.plugins.orNull?.let { args += PLUGINS_PARAMETER to it }
		detekt.baseline.orNull?.let { args += BASELINE_PARAMETER to it.absolutePath }

		val argumentList = args.flatMapTo(ArrayList()) { listOf(it.key, it.value) }
		if (detekt.debugOrDefault) argumentList += DEBUG_PARAMETER
		if (detekt.parallelOrDefault) argumentList += PARALLEL_PARAMETER
		if (detekt.disableDefaultRuleSetsOrDefault) argumentList += DISABLE_DEFAULT_RULESETS_PARAMETER

		return argumentList
	}

	private fun invokeCli(project: Project, classpath: Configuration, args: Iterable<String>, debug: Boolean = false) {
		if (debug) println(args)
		project.javaexec {
			main = "io.gitlab.arturbosch.detekt.cli.Main"
			classpath(classpath)
			args(args)
		}
	}
}
