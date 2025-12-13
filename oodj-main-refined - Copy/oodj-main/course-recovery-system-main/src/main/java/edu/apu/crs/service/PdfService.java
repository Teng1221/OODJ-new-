package edu.apu.crs.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import edu.apu.crs.models.Score;
import edu.apu.crs.models.Student;
import edu.apu.crs.models.Course;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public class PdfService {

    public String generateStudentReport(Student student, int year, int semester, List<Score> scores, double cgpa,
            Map<String, Course> courseMap) {
        String filename = "Academic_Report_" + student.getStudentId() + "_Y" + year + "S" + semester + ".pdf";
        String dest = "reports/" + filename;

        File directory = new File("reports");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try {
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Academic Performance Report").setFontSize(18).setBold());
            document.add(new Paragraph("Student Name: " + student.getStudentName()));
            document.add(new Paragraph("Student ID: " + student.getStudentId()));
            document.add(new Paragraph("Year: " + year + " | Semester: " + semester));
            document.add(new Paragraph("\n"));

            float[] columnWidths = { 1, 2, 4, 1, 1, 1 };
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell("Sem");
            table.addHeaderCell("Course Code");
            table.addHeaderCell("Course Title");
            table.addHeaderCell("Credits");
            table.addHeaderCell("Grade");
            table.addHeaderCell("Point");

            for (Score s : scores) {
                Course c = courseMap.get(s.getcourseId());
                String title = (c != null) ? c.getCourseName() : "Unknown";
                int credits = (c != null) ? c.getCredits() : 0;

                table.addCell(String.valueOf(s.getsemester()));
                table.addCell(s.getcourseId());
                table.addCell(title);
                table.addCell(String.valueOf(credits));
                table.addCell(s.getgrade());
                table.addCell(String.valueOf(s.getgradePoint()));
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("GPA/CGPA: " + String.format("%.2f", cgpa)).setBold());

            document.close();
            return new File(dest).getAbsolutePath();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
