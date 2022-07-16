package com.tajos.studio.excel;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Rene Tajos Jr
 */
public class ExcelFilesFilter extends FileFilter {

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".xls") ||
               file.getName().endsWith(".xlsx") ||
               file.isDirectory();
    }

    @Override
    public String getDescription() {
        return ".xls, .xlsx";
    }
}