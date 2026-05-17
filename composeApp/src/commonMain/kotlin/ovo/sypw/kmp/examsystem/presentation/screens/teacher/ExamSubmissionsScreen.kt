package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveScrollableGrid
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.ProctoringDataResponse
import ovo.sypw.kmp.examsystem.data.dto.SubmissionResponse
import ovo.sypw.kmp.examsystem.data.dto.SubmissionStatus
import ovo.sypw.kmp.examsystem.presentation.components.common.ErrorContent
import ovo.sypw.kmp.examsystem.presentation.components.common.LoadingContent
import ovo.sypw.kmp.examsystem.presentation.components.common.adaptiveDialogModifier
import ovo.sypw.kmp.examsystem.presentation.components.common.adaptiveDialogProperties
import ovo.sypw.kmp.examsystem.data.dto.submissionStatus
import ovo.sypw.kmp.examsystem.presentation.viewmodel.GradeSubmissionViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ProctoringUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SubmissionsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamSubmissionsScreen(
    examId: Long,
    onBack: () -> Unit
) {
    val viewModel: GradeSubmissionViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val proctoringState by viewModel.proctoringState.collectAsState()
    val config = LocalResponsiveConfig.current

    var selectedSubmissionId by remember { mutableStateOf<Long?>(null) }
    var proctoringSubmission by remember { mutableStateOf<SubmissionResponse?>(null) }

    LaunchedEffect(examId) {
        viewModel.loadSubmissions(examId)
    }

    selectedSubmissionId?.let { id ->
        GradeSubmissionScreen(
            submissionId = id,
            viewModel = viewModel,
            onBack = {
                selectedSubmissionId = null
                viewModel.loadSubmissions(examId) // reload list
            }
        )
        return
    }

    proctoringSubmission?.let { submission ->
        LaunchedEffect(submission.id) {
            viewModel.loadProctoringData(submission.id)
        }
        ProctoringDialog(
            submission = submission,
            state = proctoringState,
            onDismiss = {
                proctoringSubmission = null
                viewModel.clearProctoringData()
            },
            onRetry = { viewModel.loadProctoringData(submission.id) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("答卷批阅") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is SubmissionsUiState.Loading -> {
                    LoadingContent(message = "加载提交记录...")
                }
                is SubmissionsUiState.Error -> {
                    ErrorContent(message = state.message, onRetry = { viewModel.loadSubmissions(examId) })
                }
                is SubmissionsUiState.Success -> {
                    if (state.submissions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("暂无学生提交答卷", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        ResponsiveScrollableGrid(
                            items = state.submissions,
                            key = { it.id },
                            contentPadding = PaddingValues(config.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                            horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing),
                            modifier = Modifier
                                .then(if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = ResponsiveUtils.MaxWidths.STANDARD) else Modifier)
                                .fillMaxWidth()
                        ) { submission ->
                            SubmissionCard(
                                submission = submission,
                                onClick = { selectedSubmissionId = submission.id },
                                onOpenProctoring = { proctoringSubmission = submission }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionCard(
    submission: SubmissionResponse,
    onClick: () -> Unit,
    onOpenProctoring: () -> Unit
) {
    val config = LocalResponsiveConfig.current
    val isGraded = submission.submissionStatus == SubmissionStatus.GRADED
    val statusColor = if (isGraded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val statusIcon = if (isGraded) Icons.Default.CheckCircle else Icons.Default.Schedule

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(config.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("学生: ${submission.userName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("切屏次数: ${submission.switchCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (submission.totalScore != null) {
                        Text("总分: ${submission.totalScore}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGraded) "已批改" else "待批改",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
                TextButton(onClick = onOpenProctoring) {
                    Text("监考记录")
                }
            }
        }
    }
}

@Composable
private fun ProctoringDialog(
    submission: SubmissionResponse,
    state: ProctoringUiState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        modifier = adaptiveDialogModifier(),
        properties = adaptiveDialogProperties(),
        title = { Text("监考记录") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "学生: ${submission.userName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "考试: ${submission.examTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when (state) {
                    ProctoringUiState.Idle,
                    ProctoringUiState.Loading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("正在加载监考记录...")
                        }
                    }

                    is ProctoringUiState.Error -> {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onRetry) {
                            Text("重试")
                        }
                    }

                    is ProctoringUiState.Success -> {
                        ProctoringSummary(data = state.data)
                        val events = state.data.eventItems()
                        if (events.isEmpty()) {
                            Text(
                                text = "暂无切屏或失焦记录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            events.forEachIndexed { index, event ->
                                ProctoringEventRow(index = index + 1, event = event)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ProctoringSummary(data: ProctoringDataResponse) {
    val autoSubmitted = data.proctoringData.booleanValue("autoSubmitted")
    val reason = data.proctoringData.stringValue("reason")

    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (autoSubmitted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("切屏/失焦次数: ${data.switchCount}", fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "提交状态: ${data.status.toSubmissionStatusLabel()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (autoSubmitted) {
                Text(
                    text = reason ?: "已触发自动交卷",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ProctoringEventRow(index: Int, event: ProctoringEventItem) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$index. ${event.typeLabel}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            event.time?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            event.detail?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private data class ProctoringEventItem(
    val type: String,
    val detail: String?,
    val time: String?
) {
    val typeLabel: String
        get() = when (type.lowercase()) {
            "tab_switch" -> "切换页面"
            "exit_fullscreen" -> "退出全屏"
            "blur", "window_focus_lost" -> "考试窗口失焦"
            else -> type.ifBlank { "监考事件" }
        }
}

private fun ProctoringDataResponse.eventItems(): List<ProctoringEventItem> {
    val events = proctoringData["events"] as? JsonArray ?: return emptyList()
    return events.mapNotNull { element ->
        val obj = element as? JsonObject ?: return@mapNotNull null
        ProctoringEventItem(
            type = obj.stringValue("type").orEmpty(),
            detail = obj.stringValue("detail"),
            time = obj.stringValue("timestamp")?.replace("T", " ")?.take(19)
        )
    }
}

private fun JsonObject.stringValue(key: String): String? =
    (this[key] as? JsonPrimitive)?.contentOrNull

private fun JsonObject.booleanValue(key: String): Boolean =
    (this[key] as? JsonPrimitive)?.booleanOrNull == true

private fun Int.toSubmissionStatusLabel(): String = when (this) {
    0 -> "进行中"
    1 -> "已提交"
    2 -> "已批改"
    else -> "未知"
}
