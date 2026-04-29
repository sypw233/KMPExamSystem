package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid

@Composable
internal fun BankListPanel(
    banks: List<QuestionBankResponse>,
    selectedBank: QuestionBankResponse?,
    onSelectBank: (QuestionBankResponse) -> Unit,
    onEditBank: (QuestionBankResponse) -> Unit,
    onDeleteBank: (QuestionBankResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
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
                            Spacer(modifier = Modifier.height(6.dp))
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
                    style = MaterialTheme.typography.titleMedium
                )
                val bank = selectedBank
                if (bank != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onCreateQuestion) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("新建题目")
                        }
                        TextButton(onClick = { onImportQuestions(bank) }) {
                            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导入题目")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedBank != null) {
                var searchText by remember { mutableStateOf("") }
                var filterType by remember { mutableStateOf<String?>(null) }
                var filterDifficulty by remember { mutableStateOf<String?>(null) }

                val typeChips = listOf(
                    "single" to "单选",
                    "multiple" to "多选",
                    "true_false" to "判断",
                    "fill_blank" to "填空",
                    "short_answer" to "简答"
                )
                val diffChips = listOf("easy" to "简单", "medium" to "中等", "hard" to "困难")

                val filteredQuestions = bankQuestions.filter { q ->
                    val matchSearch = searchText.isBlank() || q.content.contains(searchText, ignoreCase = true)
                    val matchType = filterType == null || q.type == filterType
                    val matchDiff = filterDifficulty == null || q.difficulty == filterDifficulty
                    matchSearch && matchType && matchDiff
                }

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("搜索题目内容") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                    typeChips.forEach { (key, label) ->
                        FilterChip(
                            selected = filterType == key,
                            onClick = { filterType = if (filterType == key) null else key },
                            label = { Text(label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    diffChips.forEach { (key, label) ->
                        FilterChip(
                            selected = filterDifficulty == key,
                            onClick = { filterDifficulty = if (filterDifficulty == key) null else key },
                            label = { Text(label) }
                        )
                    }
                }

                if (filteredQuestions.isEmpty()) {
                    Text("无匹配题目。", modifier = Modifier.padding(16.dp))
                } else {
                    ResponsiveLazyVerticalGrid(
                        items = filteredQuestions,
                        key = { it.id },
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                        horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)
                    ) { question ->
                        QuestionCard(
                            question = question,
                            onEdit = { onEditQuestion(question) },
                            onDelete = { onDeleteQuestion(question) }
                        )
                    }
                }
            } else {
                Text("请选择或点击一个题库查看详情。", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
