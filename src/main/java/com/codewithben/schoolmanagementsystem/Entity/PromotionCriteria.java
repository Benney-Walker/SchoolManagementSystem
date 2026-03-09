package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PromotionCriteria {
    @Id
    private String levelId;

    private String promotionGradeId;

    private double totalPassScore;

    public PromotionCriteria() {}

    public PromotionCriteria(String levelId, String promotionGradeId, double totalPassScore) {
        this.levelId = levelId;
        this.promotionGradeId = promotionGradeId;
        this.totalPassScore = totalPassScore;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getPromotionGradeId() {
        return promotionGradeId;
    }

    public void setPromotionGradeId(String promotionGradeId) {
        this.promotionGradeId = promotionGradeId;
    }

    public double getTotalPassScore() {
        return totalPassScore;
    }

    public void setTotalPassScore(double totalPassScore) {
        this.totalPassScore = totalPassScore;
    }
}
