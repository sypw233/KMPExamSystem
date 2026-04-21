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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionBankActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionBankUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionBankViewModel

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
    var createDialog by remember { mutableStateOf(false) }
    var editDialog by remember { mutableStateOf<QuestionBankResponse?>(null) }
    var deleteDialog by remember { mutableStateOf<QuestionBankResponse?>(null) }
    var addQuestionDialog by remember { mutableStateOf(false) }

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
                title = { Text("Question Bank") },
                actions = {
                    IconButton(onClick = { viewModel.refreshBanks() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            Button(onClick = { createDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("New bank")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is QuestionBankUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                is QuestionBankUiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp)
                )
                is QuestionBankUiState.Success -> {
                    Row(
                        modifier = Modifier.fillMaxSize().widthIn(max = 1200.dp).padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(modifier = Modifier.weight(1f)) {
                            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.banks) { bank ->
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
                                                bank.description.orEmpty().ifBlank { "No description" },
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("questions: ${bank.questionCount}", style = MaterialTheme.typography.labelSmall)
                                                TextButton(onClick = { editDialog = bank }) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Edit")
                                                }
                                                TextButton(onClick = { deleteDialog = bank }) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Delete")
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
                                    Text(selectedBank?.name ?: "Select a question bank", style = MaterialTheme.typography.titleMedium)
                                    if (selectedBank != null) {
                                        TextButton(onClick = { addQuestionDialog = true }) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add question")
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                if (selectedBank == null) {
                                    Text("Choose a bank on the left to view details.")
                                } else if (bankQuestions.isEmpty()) {
                                    Text("No questions in this bank yet.")
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(bankQuestions) { question ->
                                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(questionTypeLabel(question.type), style = MaterialTheme.typography.labelSmall)
                                                        TextButton(onClick = {
                                                            viewModel.removeQuestionFromBank(selectedBank!!.id, question.id)
                                                        }) {
                                                            Text("Remove")
                                                        }
                                                    }
                                                    Text(question.content, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
        }
    }

    if (createDialog) {
        EditBankDialog(
            title = "Create question bank",
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
            title = "Edit question bank",
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
            title = { Text("Delete question bank?") },
            text = { Text("Delete ${bank.name}? This action cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteBank(bank.id)
                    deleteDialog = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    if (addQuestionDialog && selectedBank != null) {
        AddQuestionDialog(
            allQuestions = allQuestions,
            existingQuestionIds = bankQuestions.map { it.id }.toSet(),
            onAdd = { qid -> viewModel.addQuestionToBank(selectedBank!!.id, qid) },
            onDismiss = { addQuestionDialog = false }
        )
    }
}

private fun questionTypeLabel(type: String): String = when (type) {
    "single" -> "单选"
    "multiple" -> "多选"
    "true_false" -> "判断"
    "fill_blank" -> "填空"
    "short_answer" -> "简答"
    else -> type
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
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddQuestionDialog(
    allQuestions: List<QuestionResponse>,
    existingQuestionIds: Set<Long>,
    onAdd: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add question to bank") },
        text = {
            if (allQuestions.isEmpty()) {
                Text("No available questions.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allQuestions) { question ->
                        val alreadyAdded = existingQuestionIds.contains(question.id)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(question.content, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(questionTypeLabel(question.type), style = MaterialTheme.typography.labelSmall)
                                }
                                Button(
                                    onClick = { onAdd(question.id) },
                                    enabled = !alreadyAdded
                                ) {
                                    Text(if (alreadyAdded) "Added" else "Add")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = {}
    )
}
