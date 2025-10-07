package com.nenapiyasa.fees.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nenapiyasa.fees.model.Student
import com.nenapiyasa.fees.repository.StudentRepository
import com.nenapiyasa.fees.utils.ExcelHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ExportImportScreen(repository: StudentRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }

    // Load students
    LaunchedEffect(Unit) {
        repository.allStudents.collect { students = it }
    }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                scope.launch(Dispatchers.IO) {
                    try {
                        val file = createTempFile(context, uri)
                        val imported = ExcelHelper.importFromExcel(file)
                        imported.forEach { repository.insert(it) }
                        showToast(context, "Imported ${imported.size} students successfully!")
                    } catch (e: Exception) {
                        showToast(context, "Failed to import: ${e.message}")
                    }
                }
            }
        }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri: Uri? ->
            uri?.let {
                scope.launch(Dispatchers.IO) {
                    try {
                        val outputStream = context.contentResolver.openOutputStream(uri)
                        val file = File.createTempFile("students", ".xlsx")
                        ExcelHelper.exportToExcel(students, file)
                        outputStream?.use { out ->
                            FileInputStream(file).use { it.copyTo(out) }
                        }
                        showToast(context, "Exported ${students.size} students successfully!")
                    } catch (e: Exception) {
                        showToast(context, "Failed to export: ${e.message}")
                    }
                }
            }
        }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Backup & Restore", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { exportLauncher.launch("Students.xlsx") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export to Excel")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { importLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Import from Excel")
        }
    }
}

// Helper functions
private fun createTempFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File.createTempFile("import", ".xlsx", context.cacheDir)
    FileOutputStream(file).use { out ->
        inputStream?.copyTo(out)
    }
    return file
}

private fun showToast(context: Context, msg: String) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}

