import com.codeborne.pdftest.PDF;
import com.codeborne.pdftest.matchers.ContainsExactText;
import com.codeborne.pdftest.matchers.DoesNotContainText;
import com.codeborne.selenide.Selenide;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.setMaxStackTraceElementsDisplayed;
import static org.hamcrest.MatcherAssert.assertThat;

public class SelenideDownloadTest {

    public static String csvName = "csv.csv";
    public static String xlsName = "xls.xls";
    public static String pdfName = "D20400_UltraAnalog.pdf";
    public static String pdfPages = "10";
    public static String pdfAuthor = "Provided By ALLDATASHEET.COM(FREE DATASHEET DOWNLOAD SITE)";
    ClassLoader cl = SelenideDownloadTest.class.getClassLoader();

    @Test
    @DisplayName("Проверка наличия слова")
    void downloadTest() throws Exception {
        Selenide.open("https://github.com/junit-team/junit5/blob/main/README.md");
        File textFile = $("#raw-url").download();
        try (InputStream is = new FileInputStream(textFile)) {
            byte[] fileContent = is.readAllBytes();
            String strContent = new String(fileContent, UTF_8);
            org.assertj.core.api.Assertions.assertThat(strContent).contains("JUnit");
        }
    }

    @Test
    @DisplayName("Проверка количества страниц в PDF")
    void pdfTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("pdf/junit-user-guide-5.8.2.pdf")) {
            PDF pdf = new PDF(stream);
            Assertions.assertEquals(166, pdf.numberOfPages);
            assertThat(pdf, new ContainsExactText("Overview"));
        }
    }

    @Test
    @DisplayName("Проверка строки в XLS")
    void xlsTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("xls/Calc.xls")) {
            XLS xls = new XLS(stream);
            String stringCellValue = xls.excel.getSheetAt(0).getRow(7).getCell(0).getStringCellValue();
            org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("Смещение веса");
        }
    }

    @Test
    @DisplayName("Проверка строки в CSV")
    void csvTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("csv/csv.csv");
             CSVReader csvReader = new CSVReader(new InputStreamReader(stream, UTF_8))) {
            List<String[]> content = csvReader.readAll();
            org.assertj.core.api.Assertions.assertThat(content).contains(
                    new String[]{"1997", "Ford", "E350", "ac", "abs", "3000.00"},
                    new String[]{"1999", "Chevy", "Venture Extended Edition", "4900.00"},
                    new String[]{"1996", "Jeep", "Grand Cherokee", "roof", "4799.00"}
            );
        }
    }

    @Test
    @DisplayName("Проверка содержимого .zip файла")
    void zipTest() throws Exception {
        ZipFile zf = new ZipFile("src/test/resources/zip/zip.zip");
        try (InputStream is = cl.getResourceAsStream("zip/zip.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(pdfName)) {
                    try (InputStream stream = zf.getInputStream(entry)) {
                        assert stream != null;
                        PDF pdf = new PDF(stream);
                        Assertions.assertEquals(10, pdfPages);
                        assertThat(pdf, new ContainsExactText("PCM63P-K"));
                        assertThat(pdf, new DoesNotContainText("Cruel"));
                    }
                }
                if (entry.getName().equals(csvName)) {
                    try (InputStream stream = zf.getInputStream(entry)) {
                        assert stream != null;
                        try (CSVReader csvReader = new CSVReader(new InputStreamReader(stream, UTF_8))) {
                            List<String[]> content = csvReader.readAll();
                            org.assertj.core.api.Assertions.assertThat(content).contains(
                                    new String[]{"1997", "Ford", "E350", "ac", "abs", "3000.00"},
                                    new String[]{"1999", "Chevy", "Venture Extended Edition", "4900.00"},
                                    new String[]{"1996", "Jeep", "Grand Cherokee", "roof", "4799.00"});
                        }
                    }
                }
                if (entry.getName().equals(xlsName)) {
                    try (InputStream stream = zf.getInputStream(entry)) {
                        assert stream != null;
                        XLS xls = new XLS(stream);
                        String stringCellValue = xls.excel.getSheetAt(0).getRow(7).getCell(0).getStringCellValue();
                        org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("Смещение веса");
                    }
                }
            }
        }
    }
}
