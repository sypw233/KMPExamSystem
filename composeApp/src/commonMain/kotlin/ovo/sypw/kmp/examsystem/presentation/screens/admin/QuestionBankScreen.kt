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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.utils.QuestionUtils
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionRequest
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
                        modifier = Modifier.fillMaxSize().widthIn(max = 1200.dp).padding(16.dp),
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
                                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                Text(QuestionUtils.questionTypeLabel(question.type), style = MaterialTheme.typography.labelSmall)
                                                                if (!question.difficulty.isNullOrBlank()) {
                                                                    val diffLabel = QuestionUtils.difficultyOptions.find { it.first == question.difficulty }?.second ?: question.difficulty
                                                                    Text(diffLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                                                }
                                                            }
                                                            Row {
                                                                IconButton(onClick = { editQuestionDialog = question }) {
                                                                    Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp))
                                                                }
                                                                IconButton(onClick = {
                                                                    selectedBank?.let { bank ->
                                                                        viewModel.removeQuestionFromBank(bank.id, question.id)
                                                                    }
                                                                }) {
                                                                    Icon(Icons.Default.Delete, contentDescription = "移除", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                                                }
                                                            }
                                                        }
                                                        Text(question.content, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                    }
                                                }
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
        AlertDialog(
            onDismissRequest = { deleteDialog = null },
            title = { Text("删除题库") },
            text = { Text("确定删除题库「${bank.name}」吗？此操作不可撤销。") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteBank(bank.id)
                    deleteDialog = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = null }) { Text("取消") }
            }
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

@Composable
private fun EditBankDialog(
    title: String,
    initialName: String,
    initialDescription: String,
    onConfirm: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionFormDialog(
    initial: QuestionResponse? = null,
    onConfirm: (QuestionRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(initial?.type ?: "single") }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf(initial?.difficulty ?: "medium") }
    var options by remember {
        val initialOptions = QuestionUtils.parseOptionsJson(initial?.options)
        mutableStateOf(initialOptions.toMutableList())
    }
    var answer by remember { mutableStateOf(initial?.answer ?: "") }
    var score by remember { mutableStateOf(initial?.score?.toString() ?: "5") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var analysis by remember { mutableStateOf(initial?.analysis ?: "") }

    val showOptions = selectedType == "single" || selectedType == "multiple"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "新建题目" else "编辑题目") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("题目内容") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = QuestionUtils.questionTypeOptions.first { it.first == selectedType }.second,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("题目类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        QuestionUtils.questionTypeOptions.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                selectedType = value
                                typeExpanded = false
                                if (value != "single" && value != "multiple") {
                                    options = mutableListOf()
                                } else if (options.isEmpty()) {
                                    options = mutableListOf("", "")
                                }
                                answer = ""
                            })
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = difficultyExpanded,
                    onExpandedChange = { difficultyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = QuestionUtils.difficultyOptions.first { it.first == selectedDifficulty }.second,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("难度") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = difficultyExpanded, onDismissRequest = { difficultyExpanded = false }) {
                        QuestionUtils.difficultyOptions.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                selectedDifficulty = value
                                difficultyExpanded = false
                            })
                        }
                    }
                }

                if (showOptions) {
                    Text("选项管理", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    options.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${('A' + index)}.",
                                modifier = Modifier.width(28.dp),
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = option,
                                onValueChange = { newValue ->
                                    options = options.toMutableList().also { it[index] = newValue }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("选项内容") }
                            )
                            IconButton(onClick = {
                                options = options.toMutableList().also { it.removeAt(index) }
                                val letter = ('A' + index).toString()
                                if (selectedType == "single" && answer == letter) answer = ""
                                if (selectedType == "multiple") {
                                    val sel = answer.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
                                    sel.remove(letter)
                                    answer = sel.sorted().joinToString(",")
                                }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "删除选项", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    TextButton(onClick = {
                        options = options.toMutableList().also { it.add("") }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加选项")
                    }
                    // ── 可视化答案选择 ──
                    Text("正确答案", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    when (selectedType) {
                        "single" -> {
                            options.forEachIndexed { idx, opt ->
                                if (opt.isNotBlank()) {
                                    val letter = ('A' + idx).toString()
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = answer == letter, onClick = { answer = letter })
                                        Text("$letter. $opt", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        "multiple" -> {
                            val selectedSet = answer.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
                            options.forEachIndexed { idx, opt ->
                                if (opt.isNotBlank()) {
                                    val letter = ('A' + idx).toString()
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = letter in selectedSet,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedSet.add(letter) else selectedSet.remove(letter)
                                                answer = selectedSet.sorted().joinToString(",")
                                            }
                                        )
                                        Text("$letter. $opt", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedType == "true_false") {
                    Text("正确答案", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = answer == "true",
                            onClick = { answer = "true" },
                            label = { Text("正确") }
                        )
                        FilterChip(
                            selected = answer == "false",
                            onClick = { answer = "false" },
                            label = { Text("错误") }
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("答案") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("答案内容") }
                    )
                }

                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it.filter { c -> c.isDigit() } },
                    label = { Text("分值") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("分类 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = analysis,
                    onValueChange = { analysis = it },
                    label = { Text("解析 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val optionsJson = QuestionUtils.buildOptionsJson(options).takeIf { it.isNotBlank() }

                    onConfirm(
                        QuestionRequest(
                            content = content.trim(),
                            type = selectedType,
                            options = optionsJson,
                            answer = answer.trim(),
                            analysis = analysis.trim().ifBlank { null },
                            difficulty = selectedDifficulty,
                            category = category.trim().ifBlank { null },
                            score = score.toIntOrNull() ?: 5
                        )
                    )
                },
                enabled = content.isNotBlank() && answer.isNotBlank()
            ) {
                Text(if (initial == null) "创建" else "保存")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
