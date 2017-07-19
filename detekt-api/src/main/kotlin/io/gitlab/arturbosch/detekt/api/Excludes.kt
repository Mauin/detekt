package io.gitlab.arturbosch.detekt.api

class Excludes(excludeParameter: String) {
	private val excludes = excludeParameter
			.split(",")
			.map { it.trim() }
			.filter { it.isNotBlank() }
			.map { it.removeSuffix("*") }

	fun contains(value: String) = excludes.filter { value.contains(it) }.isNotEmpty()
	fun none(value: String) = !contains(value)
}
