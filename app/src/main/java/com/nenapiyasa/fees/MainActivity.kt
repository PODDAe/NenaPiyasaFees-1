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
                        LazyColumn {
                            items(students) { student ->
                                StudentItem(student)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            students.filter { !it.isPaid }.forEach { s ->
                                WhatsAppHelper.sendWhatsApp(
                                    this@MainActivity,
                                    s.phone,
                                    "Your payment of LKR.1200 is pending. Please clear it as soon as possible. Regards - [Nena Piyasa Higher Education Institute]"
                                )
                            }
                        }) { Text("Send WhatsApp Reminders") }
                    }
                }
            )
        }
    }

    @Composable
    fun StudentItem(student: Student) {
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 4.dp) {
            Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${student.name} (${if (student.isPaid) "Paid" else "Unpaid"})")
                Button(onClick = {
                    lifecycleScope.launch {
                        repository.update(student.copy(isPaid = !student.isPaid))
                    }
                }) {
                    Text(if (student.isPaid) "Mark Unpaid" else "Mark Paid")
                }
            }
        }
    }
}

