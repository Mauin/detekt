package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtWhenExpression

/**
 * Flags duplicate case statements in when expressions.
 *
 * If a when expression contains the same case statement multiple times they should be merged. Otherwise it might be
 * easy to miss one of the cases when reading the code, leading to unwanted side effects.
 *
 * @active since v1.0.0
 * @author Artur Bosch
 * @author Marvin Ramin
 */
class DuplicateCaseInWhenExpression(config: Config) : Rule(config) {

	override val issue = Issue("DuplicateCaseInWhenExpression",
			Severity.Warning,
			"Duplicated case statements in when expression. " +
					"Both cases should be merged.",
			Debt.TEN_MINS)

	override fun visitWhenExpression(expression: KtWhenExpression) {
		val distinctEntries = expression.entries
				.map { it.conditions }
				.fold(mutableListOf<String>(), { state, conditions ->
					state.apply { add(conditions.joinToString { it.text }) }
				})
				.distinct()
		val duplicateExpressions = expression.entries - distinctEntries

		if (duplicateExpressions.isNotEmpty()) {
			report(CodeSmell(issue, Entity.from(expression), "When expression has multiple case statements" +
					"for ${duplicateExpressions.joinToString { ", " }}."))
		}
	}
}
