package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 课程列表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的课程") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getDemoCourses()) { course ->
                CourseCard(
                    courseName = course.name,
                    teacherName = course.teacherName,
                    studentCount = course.studentCount,
                    examCount = course.examCount
                )
            }
        }
    }
}

@Composable
private fun CourseCard(
    courseName: String,
    teacherName: String,
    studentCount: Int,
    examCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "教师：$teacherName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "学生：$studentCount 人",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    Text(
                        text = "考试：$examCount 场",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

// 示例数据
private data class DemoCourse(
    val name: String,
    val teacherName: String,
    val studentCount: Int,
    val examCount: Int
)

private fun getDemoCourses() = listOf(
    DemoCourse("Java 程序设计", "张老师", 45, 3),
    DemoCourse("数据结构与算法", "李老师", 38, 5),
    DemoCourse("计算机网络", "王老师", 42, 2),
    DemoCourse("操作系统", "刘老师", 40, 4),
    DemoCourse("数据库系统", "陈老师", 36, 3)
)
