package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.preprocessor.typeReferenceName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty

class LateinitUsage(config: Config = Config.empty) : Rule(config) {

	override val issue = Issue(javaClass.simpleName,
			Severity.Style,
			"Usage of lateinit. Using lateinit for property initialization " +
					"is error prone, try using constructor injection or delegation.")

	private val excludeAnnotatedProperties = Excludes(valueOrDefault(EXCLUDE_ANNOTATED_PROPERTIES, ""))

	private var properties = mutableListOf<KtProperty>()

	override fun visitProperty(property: KtProperty) {
		if (isLateinitProperty(property)) {
			properties.add(property)
		}
	}

	override fun visit(root: KtFile) {
		properties = mutableListOf<KtProperty>()

		super.visit(root)

		val resolvedAnnotations = root.importList
				?.imports
				?.filterNot { it.isAllUnder }
				?.map { it.importedFqName?.asString() }
				?.filterNotNull()
				?.map { Pair(it.split(".").last(), it) }
				?.toMap()

		properties.filter { !isExcludedByAnnotation(it, resolvedAnnotations) }
				.forEach {
					report(CodeSmell(issue, Entity.from(it)))
				}
	}

	private fun isLateinitProperty(property: KtProperty)
			= property.modifierList?.hasModifier(KtTokens.LATEINIT_KEYWORD) ?: false

	private fun isExcludedByAnnotation(property: KtProperty, resolvedAnnotations: Map<String, String>?)
			= property.annotationEntries
					.map {
						val shortName = it.typeReferenceName
						resolvedAnnotations?.get(shortName) ?: shortName
					}
					.filterNotNull()
					.none { annotationFqn ->
						excludeAnnotatedProperties.none(annotationFqn)
					}

	companion object {
		const val EXCLUDE_ANNOTATED_PROPERTIES = "excludeAnnotatedProperties"
	}
}
