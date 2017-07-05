package io.gitlab.arturbosch.detekt.rules.performance

import io.gitlab.arturbosch.detekt.rules.bugs.LateinitUsage
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class ForEachOnRangeSpec : Spek({

	given("a kt file with using a forEach on a range") {
		val code = """
			package foo

			fun test() {
				(1..10).forEach {
					println(it)
				}
			}
		"""

		it("should report the forEach usage") {
			val findings = ForEachOnRange().lint(code)
			Assertions.assertThat(findings).hasSize(1)
		}
	}
})
