package com.tajos.studio.excel;

import com.tajos.studio.util.GradeUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Rene Tajos Jr
 */
public class ExcelHandler {

    private ExtensionType mExtension;

    private final File mFile;
    
    private final Map<String, Map<String, List<List<Object>>>> tableExcelData = new HashMap<>();
    
    public interface OnHandlingExcelDataListener {
        void onFinishHandling(Map<String, Map<String, List<List<Object>>>> data, boolean success);
    }
    
    public enum ExtensionType {
        XLS, XLSX
    }
    
    public ExcelHandler(File file) {
        mFile = file;
    }
    
    public ExcelHandler setExcelFileExtension(ExtensionType extension) {
        mExtension = extension;
        return this;
    }
    
    public Map<String, Map<String, List<List<Object>>>> getExcelHandledData() {
        return tableExcelData;
    }
    
    public ExcelHandler handle(OnHandlingExcelDataListener listener) {
        new Thread(() -> {
            if (mExtension == ExtensionType.XLS) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(mFile);
                    HSSFWorkbook wb = new HSSFWorkbook(fis);
                    FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
                    int numOfSheet = wb.getNumberOfSheets();
                    Map<String, List<List<Object>>> sheets = new HashMap<>();
                    boolean hasSchoolID = false, hasName = false;
                    
                    for (int i=0; i<numOfSheet; i++) {
                        HSSFSheet sheet=wb.getSheetAt(i);
                        String sheetName = sheet.getSheetName();
                        
                        List<List<Object>> tableCells = new ArrayList<>();
                        int rowIndex = 0;
                        for (Row row : sheet) {
                            List<Object> rows = new ArrayList<>();
                            int colIndex = 0;
                            
                            for (Cell cell : row) {
                                Object cellValue;
                                
                                switch (formulaEvaluator.evaluateInCell(cell).getCellType()) {
                                    case FORMULA -> {
                                        DataFormatter formatter = new DataFormatter();
                                        cellValue = formatter.formatCellValue(cell, formulaEvaluator);
                                    }
                                    default -> {
                                        DataFormatter formatter = new DataFormatter();
                                        cellValue = formatter.formatCellValue(cell);
                                    }
                                }
                                // check if the first row and column 0, 1 has the default
                                // School ID content and Name content. if it has, then
                                // we don't need to shift the excel cell to column 2
                                if (rowIndex == 0 && colIndex == 0) {
                                    if (cellValue.equals("School ID"))
                                        hasSchoolID = true;
                                } else if (rowIndex == 0 && colIndex == 1) {
                                    if (cellValue.equals("Name")) {
                                        hasName = true;
                                    }
                                }
                                // end
                                
                                rows.add(cellValue);
                                colIndex++;
                            }
                            if (!(hasSchoolID && hasName)) {
                                rows.add(0, null); // col 0 should not be populated by excel sheets data
                                rows.add(1, null); // col 1 should not be populated by excel sheets data aswell;
                            }
                            
                            tableCells.add(rows);
                            rowIndex++;
                        }
                        sheets.put(sheetName, tableCells);
                    }   
                    
                    Random random = new Random();
                    int randomInt = random.nextInt();
                    tableExcelData.put("ExcelWorkBook" + randomInt, sheets);
                } catch (FileNotFoundException ex) {
                    GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
                    listener.onFinishHandling(null, false);
                } catch (IOException ex) {
                    GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
                    listener.onFinishHandling(null, false);
                } finally {
                    try {
                        assert fis != null;
                        fis.close();
                        listener.onFinishHandling(tableExcelData, true); // trigger the onFinishHandling
                    } catch (IOException ex) {
                        GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
                        listener.onFinishHandling(null, false);
                    }
                }
                return;
            }
            // else if the extension is XLSX
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(mFile);
                XSSFWorkbook wb = new XSSFWorkbook(fis);   
                FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
                
                int numOfSheet = wb.getNumberOfSheets();
                Map<String, List<List<Object>>> sheets = new HashMap<>();
                
                for (int i=0; i<numOfSheet; i++) {
                    XSSFSheet sheet = wb.getSheetAt(i);
                    String sheetName = sheet.getSheetName();
                    
                    List<List<Object>> tableCells = new ArrayList<>();
                    int rowIndex = 0;
                    boolean hasSchoolID = false, hasName = false;
                    
                    for (Row row : sheet) {
                        List<Object> rows = new ArrayList<>();
                        Iterator<Cell> cellIterator = row.cellIterator();
                        
                        int colIndex = 0;
                        while (cellIterator.hasNext()) {
                            Object cellValue;
                            Cell cell = cellIterator.next();
                            
                            switch (cell.getCellType()) {
                                case FORMULA -> {
                                    DataFormatter formatter = new DataFormatter();
                                    cellValue = formatter.formatCellValue(cell, formulaEvaluator);
                                }
                                default -> {
                                    DataFormatter formatter = new DataFormatter();
                                    cellValue = formatter.formatCellValue(cell);
                                }
                            }
                            // check if the first row and column 0, 1 has the default
                            // School ID content and Name content. if it has, then
                            // we don't need to shift the excel cell to column 2
                            if (rowIndex == 0 && colIndex == 0) {
                                if (cellValue.equals("School ID"))
                                    hasSchoolID = true;
                            } else if (rowIndex == 0 && colIndex == 1) {
                                if (cellValue.equals("Name")) {
                                    hasName = true;
                                }
                            }
                            // end
                            rows.add(cellValue);
                            colIndex++;
                        }
                        if (!(hasSchoolID && hasName)) {
                            rows.add(0, null); // col 0 should not be populated by excel sheets data
                            rows.add(1, null); // col 1 should not be populated by excel sheets data aswell;
                        }
                        
                        tableCells.add(rows);
                        rowIndex++;
                    }
                    sheets.put(sheetName, tableCells);
                }
                
                Random random = new Random();
                int randomInt = random.nextInt();
                tableExcelData.put("ExcelWorkBook" + randomInt, sheets);
                
            } catch (FileNotFoundException ex) {
                GradeUtils.showErrorDialog(ex.getMessage(), "File not found exception");
                listener.onFinishHandling(null, false);
            } catch (IOException ex) {
                GradeUtils.showErrorDialog(ex.getMessage(), "IOException");
                listener.onFinishHandling(null, false);
            } finally {
                try {
                    assert fis != null;
                    fis.close();
                    listener.onFinishHandling(tableExcelData, true); // trigger the onFinishHandling
                } catch (IOException ex) {
                    GradeUtils.showErrorDialog(ex.getMessage(), "IOException");
                    listener.onFinishHandling(null, false);
                }
            }
        }).start();
        
        return this;
    }
}