package cc.toolcrate.lyceum.domain

/** Tidies what the user typed into tag names: trimmed, no leading '#', single spaces, de-duplicated ignoring case. */
object Tags {
    const val MAX_LENGTH = 32

    fun normalize(raw: Collection<String>): List<String> {
        val seen = HashSet<String>()
        return raw.mapNotNull { clean(it) }.filter { seen.add(it.lowercase()) }
    }

    fun clean(raw: String): String? {
        val name = raw.trim().trimStart('#').trim().replace(Regex("\\s+"), " ").take(MAX_LENGTH)
        return name.ifEmpty { null }
    }
}
