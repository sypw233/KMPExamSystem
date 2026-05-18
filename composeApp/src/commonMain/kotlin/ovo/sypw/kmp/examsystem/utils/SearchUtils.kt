package ovo.sypw.kmp.examsystem.utils

object SearchUtils {
    private val separators = Regex("""[\s\-_.@/\\:,;()\[\]{}]+""")

    fun <T> filterAndSort(
        items: List<T>,
        query: String,
        fields: (T) -> List<String?>
    ): List<T> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return items

        return items.mapIndexedNotNull { index, item ->
            rank(normalizedQuery, fields(item))?.let { rank -> RankedItem(rank, index, item) }
        }
            .sortedWith(compareBy<RankedItem<T>> { it.rank }.thenBy { it.index })
            .map { it.item }
    }

    fun <T> sortByPriority(
        items: List<T>,
        query: String?,
        fields: (T) -> List<String?>
    ): List<T> {
        val normalizedQuery = query?.trim().orEmpty()
        if (normalizedQuery.isBlank()) return items

        return items.mapIndexed { index, item ->
            RankedItem(rank(normalizedQuery, fields(item)) ?: Int.MAX_VALUE, index, item)
        }
            .sortedWith(compareBy<RankedItem<T>> { it.rank }.thenBy { it.index })
            .map { it.item }
    }

    private fun rank(query: String, fields: List<String?>): Int? {
        val normalizedQuery = query.lowercase()
        val normalizedFields = fields.mapNotNull { it?.trim()?.lowercase()?.takeIf(String::isNotBlank) }
        if (normalizedFields.isEmpty()) return null

        if (normalizedFields.any { it == normalizedQuery }) return 0
        if (normalizedFields.any { field -> field.split(separators).any { it == normalizedQuery } }) return 1
        if (normalizedFields.any { it.startsWith(normalizedQuery) }) return 2
        if (normalizedFields.any { it.contains(normalizedQuery) }) return 3
        return null
    }

    private data class RankedItem<T>(
        val rank: Int,
        val index: Int,
        val item: T
    )
}
