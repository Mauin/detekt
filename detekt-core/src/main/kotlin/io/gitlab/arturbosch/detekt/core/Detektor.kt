package io.gitlab.arturbosch.detekt.core

import io.gitlab.arturbosch.detekt.api.BaseRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.FileProcessListener
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import io.gitlab.arturbosch.detekt.api.toMergedMap
import org.jetbrains.kotlin.psi.KtFile
import java.util.concurrent.ExecutorService

/**
 * @author Artur Bosch
 */
class Detektor(settings: ProcessingSettings,
			   providers: List<RuleSetProvider>,
			   private val processors: List<FileProcessListener> = emptyList()) {

	private val ruleManager: RuleManager = RuleManager(providers, settings.config, settings.loadTestPattern())
	private val executor: ExecutorService = settings.executorService
	private val logger = settings.errorPrinter

	fun run(ktFiles: List<KtFile>): Map<String, List<Finding>> = withExecutor(executor) {

		val futures = ktFiles.map { file ->
			runAsync {
				processors.forEach { it.onProcess(file) }
				file.analyze().apply {
					processors.forEach { it.onProcessComplete(file, this) }
				}
			}.exceptionally { error ->
				logger.println("\n\nAnalyzing '${file.absolutePath()}' led to an exception.\n" +
						"Running detekt '${whichDetekt()}' on Java '${whichJava()}' on OS '${whichOS()}'.\n" +
						"Please create an issue and report this exception.")
				error.printStacktraceRecursively(logger)
				emptyMap()
			}
		}

		val result = HashMap<String, List<Finding>>()
		for (map in awaitAll(futures)) {
			result.mergeSmells(map)
		}

		result
	}

	private fun KtFile.analyze(): Map<String, List<Finding>> {
		return ruleManager.getApplicableRules(this).map {
			it.visit(this)
			val ruleSetId = ruleManager.ruleSetForRuleId(it.ruleId)
			ruleSetId to it.findings
		}.toMergedMap()
	}
}

class RuleManager(providers: List<RuleSetProvider>,
				  private val config: Config,
				  private val testPattern: TestPattern) {

	private val ruleSets = providers.asSequence()
			.mapNotNull { it.buildRuleset(config) }
			.sortedBy { it.id }
			.distinctBy { it.id }
			.toList()

	private val rules = ruleSets.flatMap { ruleSet ->
		val ruleSetConfig = config.subConfig(ruleSet.id)
		val rules = ruleSet.rules().filter { rule -> ruleSetConfig.subConfig(rule.ruleId).valueOrDefault("active", false) }
		rules
	}
	private val testPatternRules = ruleSets
			.filterNot { testPattern.matchesRuleSet(it.id) }
			.flatMap { ruleSet ->
				val ruleSetConfig = config.subConfig(ruleSet.id)
				val rules = ruleSet.rules(testPattern.excludingRules).filter { rule -> ruleSetConfig.subConfig(rule.ruleId).valueOrDefault("active", false) }
				rules
			}
	private val ruleSetLookup: Map<String, String>

	init {
		val lookup = mutableMapOf<String, String>()
		ruleSets.forEach { ruleSet ->
			lookup += ruleSet.rules().associate { rule -> rule.ruleId to ruleSet.id }
		}
		ruleSetLookup = lookup
	}

	fun ruleSetForRuleId(id: String): String {
		return ruleSetLookup[id]
				?: throw IllegalArgumentException("No Rule $id found in defined rules.")
	}

	fun getApplicableRules(file: KtFile): List<BaseRule> {
		return if (testPattern.isTestSource(file)) testPatternRules else rules
	}
}
