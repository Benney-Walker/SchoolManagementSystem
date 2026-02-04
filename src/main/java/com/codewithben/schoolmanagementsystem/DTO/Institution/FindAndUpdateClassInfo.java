package com.codewithben.schoolmanagementsystem.DTO.Institution;

public class FindAndUpdateClassInfo {
    private String levelId;

    private String levelName;

    private String staffId;

    public FindAndUpdateClassInfo(String levelId, String levelName, String staffId) {
        this.levelId = levelId;
        this.levelName = levelName;
        this.staffId = staffId;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
}
