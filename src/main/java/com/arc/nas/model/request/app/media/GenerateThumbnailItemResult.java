package com.arc.nas.model.request.app.media;

import java.util.Map;

public class GenerateThumbnailItemResult {

    private int total;

    private int updateCount;

    private int errorCount;

    private Map<String, Object> extendMap;


    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public Map<String, Object> getExtendMap() {
        return extendMap;
    }

    public void setExtendMap(Map<String, Object> extendMap) {
        this.extendMap = extendMap;
    }
}
