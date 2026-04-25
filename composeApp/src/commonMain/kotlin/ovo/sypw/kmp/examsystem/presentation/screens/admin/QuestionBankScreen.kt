package ovo.sypw.kmp.examsystem.presentation.screens.admin

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionBankActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionBankUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionBankViewModel
import ovo.sypw.kmp.examsystem.utils.file.rememberFileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen() {
    val viewModel: QuestionBankViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val selectedBank by viewModel.selectedBank.collectAsState()
    val bankQuestions by viewModel.bankQuestions.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val fileUtils = rememberFileUtils()

    var createDialog by remember { mutableStateOf(false) }
    var editDialog by remember { mutableStateOf<QuestionBankResponse?>(null) }
    var deleteDialog by remember { mutableStateOf<QuestionBankResponse?>(null) }
    var questionFormDialog by remember { mutableStateOf(false) }
    var editQuestionDialog by remember { mutableStateOf<QuestionResponse?>(null) }
    val config = LocalResponsiveConfig.current

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is QuestionBankActionState.Success -> {
                snackbar.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            is QuestionBankActionState.Error -> {
                snackbar.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("题库管理") },
                actions = {
                    IconButton(onClick = {
                        viewModel.downloadTemplate(
                            onSuccess = { bytes ->
                                scope.launch {
                                    fileUtils.saveFile(bytes, "question_template.xlsx", "xlsx")
                                }
                            },
                            onError = { scope.launch { snackbar.showSnackbar(it) } }
                        )
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "下载模板")
                    }
                    IconButton(onClick = {
                        val bankId = selectedBank?.id
                        if (bankId == null) {
                            scope.launch { snackbar.showSnackbar("请先选择一个题库") }
                            return@IconButton
                        }
                        scope.launch {
                            val file = fileUtils.selectFile()
                            if (file != null) {
                                val bytes = fileUtils.readBytes(file)
                                viewModel.importQuestions(
                                    bankId = bankId,
                                    fileBytes = bytes,
                                    fileName = file.name,
                                    onSuccess = { },
                                    onError = { scope.launch { snackbar.showSnackbar(it) } }
                                )
                            }
                        }
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "导入")
                    }
                    IconButton(onClick = { viewModel.refreshBanks() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            Button(onClick = { createDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("新建题库")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is QuestionBankUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                is QuestionBankUiState.Error -> Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.refreshBanks() }) {
                        Text("重试")
                    }
                }
                is QuestionBankUiState.Success -> {
                    Row(
                        modifier = Modifier.fillMaxSize().then(if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = 1200.dp) else Modifier).padding(config.screenPadding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(modifier = Modifier.weight(1f)) {
                            if (state.banks.isEmpty()) {
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
                                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(state.banks, key = { it.id }) { bank ->
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (selectedBank?.id == bank.id) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                                            ),
                                            onClick = { viewModel.selectBank(bank) },
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(bank.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text(
                                                    bank.description.orEmpty().ifBlank { "暂无描述" },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text("题目数: ${bank.questionCount}", style = MaterialTheme.typography.labelSmall)
                                                    TextButton(onClick = { editDialog = bank }) {
                                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("编辑")
                                                    }
                                                    TextButton(onClick = { deleteDialog = bank }) {
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
                        Card(modifier = Modifier.weight(1.4f)) {
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedBank?.name ?: "请选择题库", style = MaterialTheme.typography.titleMedium)
                                    val bank = selectedBank
                                    if (bank != null) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            TextButton(onClick = { questionFormDialog = true }) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("新建题目")
                                            }
                                            TextButton(onClick = {
                                                scope.launch {
                                                    val file = fileUtils.selectFile()
                                                    if (file != null) {
                                                        val bytes = fileUtils.readBytes(file)
                                                        viewModel.importQuestions(
                                                            bankId = bank.id,
                                                            fileBytes = bytes,
                                                            fileName = file.name,
                                                            onSuccess = { },
                                                            onError = { scope.launch { snackbar.showSnackbar(it) } }
                                                        )
                                                    }
                                                }
                                            }) {
                                                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("导入题目")
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // ── 筛选与搜索 ──
                                if (selectedBank != null) {
                                    var searchText by remember { mutableStateOf("") }
                                    var filterType by remember { mutableStateOf<String?>(null) }
                                    var filterDifficulty by remember { mutableStateOf<String?>(null) }

                                    val typeChips = listOf("single" to "单选", "multiple" to "多选", "true_false" to "判断", "fill_blank" to "填空", "short_answer" to "简答")
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
                                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(filteredQuestions, key = { it.id }) { question ->
                                                QuestionCard(
                                                    question = question,
                                                    onEdit = { editQuestionDialog = question },
                                                    onDelete = {
                                                        selectedBank?.let { bank ->
                                                            viewModel.removeQuestionFromBank(bank.id, question.id)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text("请在左侧选择题库查看详情。", modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (createDialog) {
        EditBankDialog(
            title = "新建题库",
            initialName = "",
            initialDescription = "",
            onConfirm = { name, desc ->
                viewModel.createBank(name, desc)
                createDialog = false
            },
            onDismiss = { createDialog = false }
        )
    }

    editDialog?.let { bank ->
        EditBankDialog(
            title = "编辑题库",
            initialName = bank.name,
            initialDescription = bank.description.orEmpty(),
            onConfirm = { name, desc ->
                viewModel.updateBank(bank.id, name, desc)
                editDialog = null
            },
            onDismiss = { editDialog = null }
        )
    }

    deleteDialog?.let { bank ->
        DeleteBankDialog(
            bank = bank,
            onConfirm = {
                viewModel.deleteBank(bank.id)
                deleteDialog = null
            },
            onDismiss = { deleteDialog = null }
        )
    }

    if (questionFormDialog) {
        QuestionFormDialog(
            onConfirm = { request ->
                viewModel.createQuestion(request)
                questionFormDialog = false
            },
            onDismiss = { questionFormDialog = false }
        )
    }

    editQuestionDialog?.let { question ->
        QuestionFormDialog(
            initial = question,
            onConfirm = { request ->
                viewModel.updateQuestion(question.id, request)
                editQuestionDialog = null
            },
            onDismiss = { editQuestionDialog = null }
        )
    }
}
