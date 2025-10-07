package com.nenapiyasa.fees

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.nenapiyasa.fees.database.StudentDatabase
import com.nenapiyasa.fees.model.Student
import com.nenapiyasa.fees.repository.StudentRepository
import com.nenapiyasa.fees.ui.components.StudentItem
import com.nenapiyasa.fees.utils.WhatsAppHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: StudentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = StudentDatabase.getDatabase(this).studentDao()
        repository = StudentRepository(dao)

        setContent {
            var students by remember { mutableStateOf(listOf<Student>()) }
            var name by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                repository.allStudents.collectLatest { students = it }
            }

            Scaffold(
                topBar = { TopAppBar(title = { Text("Nena Piyasa Fees") }) },
                content = { padding ->
                    Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                        // Add Student Fields
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Student Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                lifecycleScope.launch {
                                    repository.insert(Student(name = name, phone = phone))
                                }
                                name = ""
                                phone = ""
                            }
                        }) { Text("Add Student") }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Student List
                        LazyColumn {
                            items(students) { student ->
                                StudentItem(student = student, repository = repository)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // WhatsApp Reminder
                        Button(onClick = {
                            students.filter { !it.isPaid }.forEach { s ->
                                WhatsAppHelper.sendWhatsApp(
                                    this@MainActivity,
                                    s.phone,
                                    "This is to notify you that your payment of LKR.1200 is pending as on this month. Please make it clear as soon as possible. Regards - [Nena Piyasa Higher Education Institute]"
                                )
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Send WhatsApp Reminders")
                        }
                    }
                }
            )
        }
    }
}
