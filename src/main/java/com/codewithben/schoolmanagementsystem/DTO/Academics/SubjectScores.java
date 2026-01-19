package com.codewithben.schoolmanagementsystem.DTO.Academics;

import java.util.List;

public class SubjectScores {
    private String subjectName;

    private String subjectId;

    private List<StudentsScoresTable> scoresTable;

    public SubjectScores(String subjectName, String subjectId, List<StudentsScoresTable> scoresTable) {
        this.subjectName = subjectName;
        this.subjectId = subjectId;
        this.scoresTable = scoresTable;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getSubjectId() {
        return subjectId;
    }


    public List<StudentsScoresTable> getScoresTable() {
        return scoresTable;
    }
}
