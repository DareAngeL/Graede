package com.tajos.studio.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author Rene Tajos Jr.
 */
public class Workbook {

    @JsonProperty("sheets")
    private List<Sheet> mSheets;
    private String workbookName;
    
    public void setWorkbookName(String name) {
        workbookName = name;
    }
    
    public String getWorkbookName() {
        return workbookName;
    }
    
    public void putSheets(List<Sheet> sheets) {
        mSheets = sheets;
    }
    
    public List<Sheet> getSheets() {
        return mSheets;
    }
}
