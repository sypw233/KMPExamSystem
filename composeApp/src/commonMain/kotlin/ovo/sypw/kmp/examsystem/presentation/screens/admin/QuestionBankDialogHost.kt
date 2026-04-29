package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.runtime.Composable
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse

@Composable
internal fun QuestionBankDialogHost(
    createDialog: Boolean,
    editDialog: QuestionBankResponse?,
    deleteDialog: QuestionBankResponse?,
    questionFormDialog: Boolean,
    editQuestionDialog: QuestionResponse?,
    onCreateBank: (String, String?) -> Unit,
    onDismissCreateBank: () -> Unit,
    onUpdateBank: (QuestionBankResponse, String, String?) -> Unit,
    onDismissEditBank: () -> Unit,
    onDeleteBank: (QuestionBankResponse) -> Unit,
    onDismissDeleteBank: () -> Unit,
    onCreateQuestion: (QuestionRequest) -> Unit,
    onDismissCreateQuestion: () -> Unit,
    onUpdateQuestion: (QuestionResponse, QuestionRequest) -> Unit,
    onDismissEditQuestion: () -> Unit
) {
    if (createDialog) {
        EditBankDialog(
            title = "新建题库",
            initialName = "",
            initialDescription = "",
            onConfirm = onCreateBank,
            onDismiss = onDismissCreateBank
        )
    }

    editDialog?.let { bank ->
        EditBankDialog(
            title = "编辑题库",
            initialName = bank.name,
            initialDescription = bank.description.orEmpty(),
            onConfirm = { name, desc -> onUpdateBank(bank, name, desc) },
            onDismiss = onDismissEditBank
        )
    }

    deleteDialog?.let { bank ->
        DeleteBankDialog(
            bank = bank,
            onConfirm = { onDeleteBank(bank) },
            onDismiss = onDismissDeleteBank
        )
    }

    if (questionFormDialog) {
        QuestionFormDialog(
            onConfirm = onCreateQuestion,
            onDismiss = onDismissCreateQuestion
        )
    }

    editQuestionDialog?.let { question ->
        QuestionFormDialog(
            initial = question,
            onConfirm = { request -> onUpdateQuestion(question, request) },
            onDismiss = onDismissEditQuestion
        )
    }
}
