package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionBankActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionBankUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionBankViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import ovo.sypw.kmp.examsystem.utils.file.rememberFileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen() {
    val viewModel: QuestionBankViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val selectedBank by viewModel.selectedBank.collectAsState()
    val bankQuestions by viewModel.bankQuestions.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val fileUtils = rememberFileUtils()

    var createDialog by remember { mutableStateOf(false) }
    var editDialog by remember { mutableStateOf<QuestionBankResponse?>(null) }
    var deleteDialog by remember { mutableStateOf<QuestionBankResponse?>(null) }
    var questionFormDialog by remember { mutableStateOf(false) }
    var editQuestionDialog by remember { mutableStateOf<QuestionResponse?>(null) }
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    var mobileShowQuestions by remember { mutableStateOf(false) }

    fun importQuestionsFromFile(bankId: Long) {
        scope.launch {
            val file = fileUtils.selectFile() ?: return@launch
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
                title = {
                    if (!isDesktop && mobileShowQuestions) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { mobileShowQuestions = false }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                            }
                            Text(selectedBank?.name ?: "题库详情")
                        }
                    } else {
                        Text("题库管理")
                    }
                },
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
                        importQuestionsFromFile(bankId)
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
            if (isDesktop || !mobileShowQuestions) {
                Button(onClick = { createDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("新建题库")
                }
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
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.refreshBanks() }) { Text("重试") }
                }
                is QuestionBankUiState.Success -> {
                    val panelModifier = Modifier
                        .then(
                            if (isDesktop) {
                                Modifier.widthIn(max = ResponsiveUtils.MaxWidths.FULL)
                            } else {
                                Modifier
                            }
                        )
                        .fillMaxSize()
                        .padding(config.screenPadding)

                    if (isDesktop) {
                        Row(
                            modifier = panelModifier,
                            horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)
                        ) {
                            BankListPanel(
                                banks = state.banks,
                                selectedBank = selectedBank,
                                onSelectBank = { viewModel.selectBank(it) },
                                onEditBank = { editDialog = it },
                                onDeleteBank = { deleteDialog = it },
                                modifier = Modifier.weight(0.9f)
                            )
                            QuestionListPanel(
                                selectedBank = selectedBank,
                                bankQuestions = bankQuestions,
                                onCreateQuestion = { questionFormDialog = true },
                                onImportQuestions = { bank -> importQuestionsFromFile(bank.id) },
                                onEditQuestion = { editQuestionDialog = it },
                                onDeleteQuestion = { question ->
                                    selectedBank?.let { bank ->
                                        viewModel.removeQuestionFromBank(bank.id, question.id)
                                    }
                                },
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                    } else if (!mobileShowQuestions) {
                        BankListPanel(
                            banks = state.banks,
                            selectedBank = selectedBank,
                            onSelectBank = {
                                viewModel.selectBank(it)
                                mobileShowQuestions = true
                            },
                            onEditBank = { editDialog = it },
                            onDeleteBank = { deleteDialog = it },
                            modifier = panelModifier
                        )
                    } else {
                        QuestionListPanel(
                            selectedBank = selectedBank,
                            bankQuestions = bankQuestions,
                            onCreateQuestion = { questionFormDialog = true },
                            onImportQuestions = { bank -> importQuestionsFromFile(bank.id) },
                            onEditQuestion = { editQuestionDialog = it },
                            onDeleteQuestion = { question ->
                                selectedBank?.let { bank ->
                                    viewModel.removeQuestionFromBank(bank.id, question.id)
                                }
                            },
                            modifier = panelModifier
                        )
                    }
                }
            }
        }
    }

    QuestionBankDialogHost(
        createDialog = createDialog,
        editDialog = editDialog,
        deleteDialog = deleteDialog,
        questionFormDialog = questionFormDialog,
        editQuestionDialog = editQuestionDialog,
        onCreateBank = { name, desc ->
            viewModel.createBank(name, desc)
            createDialog = false
        },
        onDismissCreateBank = { createDialog = false },
        onUpdateBank = { bank, name, desc ->
            viewModel.updateBank(bank.id, name, desc)
            editDialog = null
        },
        onDismissEditBank = { editDialog = null },
        onDeleteBank = { bank ->
            viewModel.deleteBank(bank.id)
            deleteDialog = null
        },
        onDismissDeleteBank = { deleteDialog = null },
        onCreateQuestion = { request ->
            viewModel.createQuestion(request)
            questionFormDialog = false
        },
        onDismissCreateQuestion = { questionFormDialog = false },
        onUpdateQuestion = { question, request ->
            viewModel.updateQuestion(question.id, request)
            editQuestionDialog = null
        },
        onDismissEditQuestion = { editQuestionDialog = null }
    )
}
