package com.nenapiyasa.fees.utils

import com.nenapiyasa.fees.model.Student
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ExcelHelper {
    fun exportToExcel(students: List<Student>, file: File) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Students")
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Name")
        header.createCell(1).setCellValue("Phone")
        header.createCell(2).setCellValue("Paid")

        students.forEachIndexed { index, student ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(student.name)
            row.createCell(1).setCellValue(student.phone)
            row.createCell(2).setCellValue(if (student.isPaid) "Yes" else "No")
        }

        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
    }

    fun importFromExcel(file: File): List<Student> {
        val students = mutableListOf<Student>()
        val workbook = WorkbookFactory.create(FileInputStream(file))
        val sheet = workbook.getSheetAt(0)
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i)
            val name = row.getCell(0).stringCellValue
            val phone = row.getCell(1).stringCellValue
            val isPaid = row.getCell(2).stringCellValue.equals("Yes", true)
            students.add(Student(name = name, phone = phone, isPaid = isPaid))
        }
        workbook.close()
        return students
    }
}

