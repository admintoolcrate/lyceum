package cc.toolcrate.lyceum.domain

/**
 * Turns what the user typed into a safe FTS4 MATCH expression: each word becomes a prefix term,
 * so "kind wor" finds "kindness" and "words". Only letters and digits survive, and words are
 * lowercased so FTS never reads them as its upper-case operators (OR, AND, NOT, NEAR).
 */
object SearchQuery {
    fun toMatch(input: String): String? {
        val words = input.split(Regex("[^\\p{L}\\p{N}']+"))
            .map { it.replace("'", "").lowercase() }
            .filter { it.isNotBlank() }
        if (words.isEmpty()) return null
        return words.joinToString(" ") { "$it*" }
    }
}
