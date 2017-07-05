package io.gitlab.arturbosch.detekt.rules.performance

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtNamedFunction


class ForEachOnRange(config: Config = Config.empty) : Rule(config) {
	override val issue = Issue("ForEachOnRange",
			Severity.Performance,
			"Using the forEach method on ranges has a heavy performance cost. Prefer using simple for loops")

	fun test() {
		(1..10).forEach {
			println(it)
		}
	}

	override fun visitNamedFunction(function: KtNamedFunction) {
		super.visitNamedFunction(function)
		
		println(function.text)
		println(function.name)


		if (function.text != "forEach") {
			return
		}

		report(CodeSmell(issue, Entity.from(function)))
	}
}