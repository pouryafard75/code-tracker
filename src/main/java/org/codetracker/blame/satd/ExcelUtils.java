package org.codetracker.blame.satd;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelUtils {
    public static String COLUMN_CREATED_IN_COMMIT = "created_in_commit";
    public static String COLUMN_DELETED_IN_COMMIT = "deleted_in_commit";
    public static String COLUMN_CREATED_IN_LINE = "created_in_line";
    public static String COLUMN_DELETED_IN_LINE = "deleted_in_line";
    public static String COLUMN_CREATED_IN_FILE = "created_in_file";
    public static String COLUMN_DELETED_IN_FILE = "deleted_in_file";
    public static String COLUMN_UPDATED_IN_LINES = "updated_in_lines";
    public static String COLUMN_UPDATED_IN_COMMITS = "updated_in_commits";
    public static String COLUMN_LABEL = "Label";


    public static List<Map<String, String>> readXlsxToMap(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);
        List<String> columnNames = new ArrayList<>();
        for (Cell cell : headerRow) {
            columnNames.add(cell.getStringCellValue());
        }

        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Map<String, String> rowMap = new HashMap<>();
            for (int j = 0; j < columnNames.size(); j++) {
                Cell cell = row.getCell(j);
                String value = (cell == null) ? "" : cell.toString();
                rowMap.put(columnNames.get(j), value);
            }
            rows.add(rowMap);
        }

        workbook.close();
        return rows;
    }
}
