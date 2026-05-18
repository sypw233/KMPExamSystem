package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.utils.DesktopDataTableRow
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.QuestionUtils
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
internal fun BankListPanel(
    banks: List<QuestionBankResponse>,
    selectedBank: QuestionBankResponse?,
    onSelectBank: (QuestionBankResponse) -> Unit,
    onEditBank: (QuestionBankResponse) -> Unit,
    onDeleteBank: (QuestionBankResponse) -> Unit,
    onSearch: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchKeyword by remember { mutableStateOf("") }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 搜索框
            if (onSearch != null) {
                OutlinedTextField(
                    value = searchKeyword,
                    onValueChange = { searchKeyword = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .height(44.dp),
                    placeholder = { Text("搜索题库", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchKeyword.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchKeyword = ""
                                    onSearch("")
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearch(searchKeyword)
                    })
                )
            }

            if (banks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            "暂无题库",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "点击右下角按钮创建新题库",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(banks, key = { it.id }) { bank ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedBank?.id == bank.id)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            onClick = { onSelectBank(bank) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text(
                                    bank.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    bank.description.orEmpty().ifBlank { "暂无描述" },
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "题目数: ${bank.questionCount}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    TextButton(onClick = { onEditBank(bank) }) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("编辑")
                                    }
                                    TextButton(onClick = { onDeleteBank(bank) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("删除")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuestionListPanel(
    selectedBank: QuestionBankResponse?,
    bankQuestions: List<QuestionResponse>,
    onCreateQuestion: () -> Unit,
    onImportQuestions: (QuestionBankResponse) -> Unit,
    onEditQuestion: (QuestionResponse) -> Unit,
    onDeleteQuestion: (QuestionResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = LocalResponsiveConfig.current
    val isCompact = config.screenSize == ResponsiveUtils.ScreenSize.COMPACT

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedBank?.name ?: "请选择题库",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = if (isCompact) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                val bank = selectedBank
                if (bank != null) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        TextButton(
                            onClick = onCreateQuestion,
                            contentPadding = PaddingValues(horizontal = if (isCompact) 8.dp else 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isCompact) "新建" else "新建题目", maxLines = 1)
                        }
                        TextButton(
                            onClick = { onImportQuestions(bank) },
                            contentPadding = PaddingValues(horizontal = if (isCompact) 8.dp else 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isCompact) "导入" else "导入题目", maxLines = 1)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedBank != null) {
                var searchText by remember { mutableStateOf("") }
                var filterType by remember { mutableStateOf<String?>(null) }
                var filterDifficulty by remember { mutableStateOf<String?>(null) }
                var filtersExpanded by remember { mutableStateOf(false) }

                val typeChips = QuestionUtils.questionTypeOptions
                val diffChips = QuestionUtils.difficultyOptions

                val filteredQuestions = bankQuestions.filter { q ->
                    val matchSearch = searchText.isBlank() || q.content.contains(searchText, ignoreCase = true)
                    val matchType = filterType == null || q.type == filterType
                    val matchDiff = filterDifficulty == null || q.difficulty == filterDifficulty
                    matchSearch && matchType && matchDiff
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        placeholder = { Text("搜索题目内容", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(
                        onClick = { filtersExpanded = !filtersExpanded },
                        modifier = Modifier.height(48.dp),
                        contentPadding = PaddingValues(horizontal = if (isCompact) 10.dp else 14.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (filtersExpanded) "收起" else "筛选", maxLines = 1)
                    }
                }

                AnimatedVisibility(
                    visible = filtersExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 4.dp)
                        ) {
                            typeChips.forEach { (key, label) ->
                                FilterChip(
                                    selected = filterType == key,
                                    onClick = { filterType = if (filterType == key) null else key },
                                    label = { Text(label) }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 8.dp)
                        ) {
                            diffChips.forEach { (key, label) ->
                                FilterChip(
                                    selected = filterDifficulty == key,
                                    onClick = { filterDifficulty = if (filterDifficulty == key) null else key },
                                    label = { Text(label) }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

                if (filteredQuestions.isEmpty()) {
                    Text("无匹配题目。", modifier = Modifier.padding(16.dp))
                } else if (isDesktop) {
                    // Desktop table layout
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Table header
                        DesktopDataTableRow(
                            modifier = Modifier.padding(vertical = 4.dp),
                            *arrayOf(
                                0.3f to @Composable {
                                    Text(
                                        "",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                0.4f to @Composable {
                                    Text(
                                        "序号",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                1.5f to @Composable {
                                    Text(
                                        "题目内容",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                0.6f to @Composable {
                                    Text(
                                        "题型",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                0.5f to @Composable {
                                    Text(
                                        "难度",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                0.5f to @Composable {
                                    Text(
                                        "操作",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        )
                        HorizontalDivider()

                        // Table data rows
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            itemsIndexed(filteredQuestions, key = { _, q -> q.id }) { index, question ->
                                DesktopDataTableRow(
                                    modifier = Modifier,
                                    0.3f to @Composable {
                                        Checkbox(
                                            checked = false,
                                            onCheckedChange = null
                                        )
                                    },
                                    0.4f to @Composable {
                                        Text(
                                            "${index + 1}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    1.5f to @Composable {
                                        Text(
                                            question.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    0.6f to @Composable {
                                        Text(
                                            QuestionUtils.questionTypeLabel(question.type),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    0.5f to @Composable {
                                        val diffLabel = QuestionUtils.difficultyOptions
                                            .find { it.first == question.difficulty }?.second
                                            ?: question.difficulty ?: "-"
                                        Text(
                                            diffLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    0.5f to @Composable {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = { onEditQuestion(question) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "编辑",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteQuestion(question) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "移除",
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                    ) {
                        items(filteredQuestions, key = { it.id }) { question ->
                            QuestionCard(
                                question = question,
                                onEdit = { onEditQuestion(question) },
                                onDelete = { onDeleteQuestion(question) }
                            )
                        }
                    }
                }
            } else {
                Text("请选择或点击一个题库查看详情。", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
