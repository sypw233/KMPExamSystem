package ovo.sypw.kmp.examsystem.data.repository

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.utils.Logger

/**
 * 通用分页全量获取工具
 * 自动翻页直到获取所有数据, 支持去重和安全页数限制
 * @param pageSize 每页大小, 默认100
 * @param requestPage 分页请求函数, 参数为(page, size)
 * @param content 从分页响应中提取列表数据
 * @param last 判断是否为最后一页
 * @param totalPages 获取总页数
 * @param distinctKey 去重键, 按该键去重防止重复数据
 * @param maxPages 最大页数限制, 防止后端异常导致无限循环, 默认100页
 * @return 全量数据列表
 * @throws IllegalStateException 超过最大页数限制时抛出
 */
internal suspend fun <Item, Page : Any> fetchAllPages(
    pageSize: Int = 100,
    requestPage: suspend (page: Int, size: Int) -> ApiResponse<Page>,
    content: (Page) -> List<Item>,
    last: (Page) -> Boolean,
    totalPages: (Page) -> Int,
    distinctKey: ((Item) -> Any?)? = null,
    maxPages: Int = 100
): List<Item> {
    val items = mutableListOf<Item>()
    var page = 0
    var hasNextPage: Boolean

    do {
        if (page >= maxPages) {
            Logger.w("PagingRepositoryUtils", "已达最大页数限制($maxPages), 停止加载. 已加载${items.size}条数据")
            break
        }
        val response = requestPage(page, pageSize)
        if (response.code != 200) throw Exception(response.message)
        val data = response.data ?: break
        items += content(data)
        page += 1
        hasNextPage = !last(data) && page < totalPages(data)
    } while (hasNextPage)

    return distinctKey?.let { key -> items.distinctBy(key) } ?: items
}
