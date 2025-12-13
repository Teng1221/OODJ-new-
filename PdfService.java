package edu.apu.crs.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;

import edu.apu.crs.models.Score;
import edu.apu.crs.models.Student;
import edu.apu.crs.models.Course;

import java.io.File;
import java.io.IOException;
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

            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
            document.setFont(font);

            document.add(
                    new Paragraph("Academic Performance Report").setFontSize(18).setBold());
            document.add(new Paragraph("Generated on: " + new java.util.Date()).setFontSize(8));
            document.add(new Paragraph("created from CRS").setFontSize(8));

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Student Name: " + student.getStudentName()));
            document.add(new Paragraph("Student ID: " + student.getStudentId()));
            document.add(new Paragraph("Year: " + year));
            document.add(new Paragraph("\n"));

            float[] columnWidths = { 1, 2, 4, 2, 1, 1 };
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell("Semester");
            table.addHeaderCell("Course Code");
            table.addHeaderCell("Course Name");
            table.addHeaderCell("Total Credit Hour");
            table.addHeaderCell("Grade");
            table.addHeaderCell("Point");

            int totalCreditSum = 0;

            for (Score s : scores) {
                Course c = courseMap.get(s.getcourseId());
                String title = (c != null) ? c.getCourseName() : "Unknown";
                int credits = (c != null) ? c.getCredits() : 0;
                totalCreditSum += credits;

                table.addCell(String.valueOf(s.getsemester()));
                table.addCell(s.getcourseId());
                table.addCell(title);
                table.addCell(String.valueOf(credits));
                table.addCell(s.getgrade());
                table.addCell(String.valueOf(s.getgradePoint()));
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Total Credit Hour: " + totalCreditSum).setBold());
            document.add(new Paragraph("GPA/CGPA: " + String.format("%.2f", cgpa)).setBold());
            document.add(new Paragraph("\n"));

            document.add(
                    new Paragraph("-------- END OF REPORT --------")
                            .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Degree Classification").setBold().setFontSize(10));
            document.add(new Paragraph("Minimum CGPA = 3.70 First Class\r\n" + //
                    "Minimum CGPA = 3.50 First Class *\r\n" + //
                    "Minimum CGPA = 3.00 Second Upper\r\n" + //
                    "Minimum CGPA = 2.80 Second Upper *\r\n" + //
                    "Minimum CGPA = 2.30 Second Lower\r\n" + //
                    "Minimum CGPA = 2.20 Second Lower *\r\n" + //
                    "Minimum CGPA = 2.00 Third Class\r\n" + //
                    "Minimum CGPA = 1.00 Fail **\r").setFontSize(10));
            document.add(new Paragraph(
                    "* May be awarded at the discretion of the Examination Board, based on the student's overall academic")
                    .setFontSize(10));

            document.close();
            return new File(dest).getAbsolutePath();

        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
