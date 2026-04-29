package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamViewModel

@Composable
internal fun TeacherExamManageDialogHost(
    viewModel: ExamViewModel,
    courses: List<CourseResponse>,
    showCreateDialog: Boolean,
    showEditDialog: ExamResponse?,
    showDeleteConfirm: ExamResponse?,
    showBatchDeleteConfirm: Boolean,
    selectedIds: Set<Long>,
    onDismissCreate: () -> Unit,
    onDismissEdit: () -> Unit,
    onDismissDelete: () -> Unit,
    onDismissBatchDelete: () -> Unit,
    onBatchDeleteFinished: () -> Unit
) {
    if (showCreateDialog) {
        ExamFormDialog(
            title = "新建考试",
            courses = courses,
            onConfirm = { req ->
                viewModel.createExam(req)
                onDismissCreate()
            },
            onDismiss = onDismissCreate
        )
    }

    showEditDialog?.let { exam ->
        ExamFormDialog(
            title = "编辑考试",
            initial = exam,
            courses = courses,
            onConfirm = { req ->
                viewModel.updateExam(exam.id, req)
                onDismissEdit()
            },
            onDismiss = onDismissEdit
        )
    }

    showDeleteConfirm?.let { exam ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("删除考试") },
            text = { Text("确定要删除考试「${exam.title}」吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExam(exam.id)
                        onDismissDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = onDismissDelete) { Text("取消") } }
        )
    }

    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = onDismissBatchDelete,
            title = { Text("批量删除考试") },
            text = { Text("确定要删除选中的 ${selectedIds.size} 项草稿考试吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.batchDeleteExams(selectedIds.toList())
                        onBatchDeleteFinished()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = onDismissBatchDelete) { Text("取消") } }
        )
    }
}
