package edu.northeastern.group_project_group_8;

import java.time.LocalDate;

public class Holding {
    public String account;
    public String asset;
    public long count;
    public LocalDate startDate;
    public LocalDate endDate;
    public String holdingKey;

    public Holding(String account, String asset, long count, LocalDate startDate, LocalDate endDate) {
        this.account = account;
        this.asset = asset;
        this.count = count;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Holding(String holdingKey, String account, String asset, long count, LocalDate startDate, LocalDate endDate) {
        this.account = account;
        this.asset = asset;
        this.count = count;
        this.startDate = startDate;
        this.endDate = endDate;
        this.holdingKey = holdingKey;
    }
}
