package ovo.sypw.kmp.examsystem.data.repository

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse

internal suspend fun <Item, Page : Any> fetchAllPages(
    pageSize: Int = 100,
    requestPage: suspend (page: Int, size: Int) -> ApiResponse<Page>,
    content: (Page) -> List<Item>,
    last: (Page) -> Boolean,
    totalPages: (Page) -> Int,
    distinctKey: ((Item) -> Any?)? = null
): List<Item> {
    val items = mutableListOf<Item>()
    var page = 0
    var hasNextPage: Boolean

    do {
        val response = requestPage(page, pageSize)
        if (response.code != 200) throw Exception(response.message)
        val data = response.data ?: break
        items += content(data)
        page += 1
        hasNextPage = !last(data) && page < totalPages(data)
    } while (hasNextPage)

    return distinctKey?.let { key -> items.distinctBy(key) } ?: items
}
