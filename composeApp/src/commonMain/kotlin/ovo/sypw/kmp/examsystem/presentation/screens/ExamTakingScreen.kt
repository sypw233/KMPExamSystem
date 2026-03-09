package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.presentation.navigation.NavigationManager
import ovo.sypw.kmp.examsystem.utils.StringUtils.format

/**
 * 考试进行界面（全屏模式）
 * 完全隐藏导航栏，显示考试题目和答题区域
 */
@Composable
fun ExamTakingScreen(
    examId: Long,
    navigationManager: NavigationManager = koinInject(),
    onExitExam: () -> Unit
) {
    // 考试计时器
    var remainingSeconds by remember { mutableStateOf(7200) } // 120 minutes = 7200 seconds
    
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    // 显示退出确认对话框
    var showExitDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ExamTopBar(
                examTitle = "Java 程序设计期末考试",  // 示例标题
                remainingSeconds = remainingSeconds,
                onExitClick = { showExitDialog = true },
                onSubmitClick = { showExitDialog = true } // 复用逻辑进行提交
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 示例题目
            itemsIndexed(getDemoQuestions()) { index, question ->
                QuestionCard(
                    questionNumber = index + 1,
                    question = question
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 退出确认对话框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("确认提交/退出?") },
            text = { Text("确定要提交当前试卷并退出吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        navigationManager.exitExamMode()
                        onExitExam()
                        showExitDialog = false
                    }
                ) {
                    Text("确认提交")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("继续作答")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamTopBar(
    examTitle: String,
    remainingSeconds: Int,
    onExitClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = examTitle,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "剩余时间：${formatTime(remainingSeconds)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remainingSeconds < 600) MaterialTheme.colorScheme.error 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onExitClick) {
                Icon(Icons.Default.Close, contentDescription = "取消考试")
            }
        },
        actions = {
            Button(
                onClick = onSubmitClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("提交试卷")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun QuestionCard(
    questionNumber: Int,
    question: DemoQuestion
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "$questionNumber. ${question.content}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (question.type) {
                QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE -> {
                    question.options?.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (question.type == QuestionType.SINGLE_CHOICE) {
                                RadioButton(
                                    selected = false,
                                    onClick = { }
                                )
                            } else {
                                Checkbox(
                                    checked = false,
                                    onCheckedChange = { }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${('A' + index)}. $option",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = false, onClick = { })
                            Text("正确")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = false, onClick = { })
                            Text("错误")
                        }
                    }
                }
                QuestionType.SHORT_ANSWER -> {
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("请输入答案") },
                        minLines = 3
                    )
                }
            }
        }
    }
}

// 辅助函数：格式化时间
private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}

// 示例数据
private enum class QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
}

private data class DemoQuestion(
    val content: String,
    val type: QuestionType,
    val options: List<String>? = null
)

private fun getDemoQuestions() = listOf(
    DemoQuestion(
        "Java中，关于类的继承说法正确的是？",
        QuestionType.SINGLE_CHOICE,
        listOf("Java支持多重继承", "子类可以继承父类的私有成员", "子类可以调用父类的构造方法", "子类不能重写父类的方法")
    ),
    DemoQuestion(
        "以下哪些是Java的基本数据类型？（多选）",
        QuestionType.MULTIPLE_CHOICE,
        listOf("int", "String", "boolean", "char")
    ),
    DemoQuestion(
        "Java是一种编译型语言。",
        QuestionType.TRUE_FALSE
    ),
    DemoQuestion(
        "请简述Java中接口和抽象类的区别。",
        QuestionType.SHORT_ANSWER
    )
)
