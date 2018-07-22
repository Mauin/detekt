package io.gitlab.arturbosch.detekt

import groovy.lang.Closure
import io.gitlab.arturbosch.detekt.invoke.DetektInvoker
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.*
import org.gradle.util.ConfigureUtil
import java.io.File

/**
 * @author Artur Bosch
 * @author Marvin Ramin
 */
@CacheableTask
open class Detekt : DefaultTask(), Reporting<DetektReports> {

	private val _reports: DetektReports = project.objects.newInstance(DetektReportsImpl::class.java, this)
	@Internal
	override fun getReports() = _reports

	override fun reports(closure: Closure<*>): DetektReports = ConfigureUtil.configure(closure, _reports)
	override fun reports(configureAction: Action<in DetektReports>): DetektReports = _reports.apply { configureAction.execute(this) }

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	@SkipWhenEmpty
	val input: Property<FileCollection> = project.objects.property(FileCollection::class.java)

	@Input
	@Optional
	var filters: Property<String> = project.objects.property(String::class.java)

	@InputFile
	@Optional
	@PathSensitive(PathSensitivity.ABSOLUTE)
	val baseline: Property<File> = project.objects.property(File::class.java)

	@InputFile
	@Optional
	@PathSensitive(PathSensitivity.ABSOLUTE)
	var config: Property<File> = project.objects.property(File::class.java)

	@Input
	@Optional
	val plugins: Property<String?> = project.objects.property(String::class.java)

	@Internal
	@Optional
	lateinit var debug: Property<java.lang.Boolean>
	val debugOrDefault: Boolean
		@Internal
		@Optional
		get() = debug.get().booleanValue()

	@Internal
	@Optional
	lateinit var parallel: Property<java.lang.Boolean>
	val parallelOrDefault: Boolean
		@Internal
		@Optional
		get() = parallel.get().booleanValue()

	@Internal
	@Optional
	lateinit var disableDefaultRuleSets: Property<java.lang.Boolean>
	val disableDefaultRuleSetsOrDefault: Boolean
		@Internal
		@Optional
		get() = disableDefaultRuleSets.get().booleanValue()

	@OutputFiles
	fun getOutputFiles(): Map<String, File> {
		val map = HashMap<String, File>()

		if (reports.xml.isEnabled) {
			map += "XML" to _reports.xml.destination
		}
		if (reports.html.isEnabled) {
			map += "HTML" to _reports.html.destination
		}
		return map
	}

	@TaskAction
	fun check() {
		DetektInvoker.check(this)
	}
}
