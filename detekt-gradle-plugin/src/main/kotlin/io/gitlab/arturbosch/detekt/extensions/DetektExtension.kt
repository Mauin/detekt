package io.gitlab.arturbosch.detekt.extensions

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.provider.Property
import org.gradle.api.resources.TextResource
import org.gradle.kotlin.dsl.property
import java.io.File

/**
 * @author Artur Bosch
 * @author Said Tahsin Dane
 * @author Marvin Ramin
 */
open class DetektExtension
constructor(
		private val project: Project,
		projectLayout: ProjectLayout
) : CodeQualityExtension() {
	private val debugProperty: Property<Boolean?> = project.objects.property()
	private val parallelProperty: Property<Boolean?> = project.objects.property()
	private val disableDefaultRuleSetsProperty: Property<Boolean?> = project.objects.property()
	private val filtersProperty: Property<String?> = project.objects.property()
	private val baselineProperty: RegularFileProperty = projectLayout.fileProperty()
	private val pluginsProperty: Property<String?> = project.objects.property()
	private val configProperty: Property<TextResource?> = project.objects.property()
	private val configDirProperty: Property<Directory?> = project.objects.property()
	var ideaExtension: IdeaExtension = IdeaExtension()

	var debug: Boolean?
		get() = debugProperty.orNull
		set(value) = debugProperty.set(value)

	var parallel: Boolean?
		get() = parallelProperty.orNull
		set(value) = parallelProperty.set(value)

	var disableDefaultRuleSets: Boolean?
		get() = disableDefaultRuleSetsProperty.orNull
		set(value) = disableDefaultRuleSetsProperty.set(value)

	var filters: String?
		get() = filtersProperty.orNull
		set(value) {
			filtersProperty.set(value)
		}

	var plugins: String?
		get() = pluginsProperty.orNull
		set(value) = pluginsProperty.set(value)

	var baseline: RegularFile?
		get() = baselineProperty.orNull
		set(value) = baselineProperty.set(value)

	var configDir: Directory?
		get() = configDirProperty.orNull
		set(value) = configDirProperty.set(value)

	var config: TextResource?
		get() = configProperty.orNull
		set(value) = configProperty.set(value)

	fun getConfigFile(): File? {
		return configProperty.orNull?.asFile()
	}

	fun setConfigFile(file: File) {
		return configProperty.set(project.resources.text.fromFile(file))
	}

	fun idea(configuration: Action<in IdeaExtension>) {
		configuration.execute(ideaExtension)
	}
}
