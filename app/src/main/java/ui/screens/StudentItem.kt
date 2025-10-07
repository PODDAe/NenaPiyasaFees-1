package com.nenapiyasa.fees.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.nenapiyasa.fees.model.Student
import com.nenapiyasa.fees.repository.StudentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun StudentItem(student: Student, repository: StudentRepository) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 4.dp) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${student.name} (${if (student.isPaid) "Paid" else "Unpaid"})")
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    repository.update(student.copy(isPaid = !student.isPaid))
                }
            }) {
                Text(if (student.isPaid) "Mark Unpaid" else "Mark Paid")
            }
        }
    }
}

