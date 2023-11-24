package edu.northeastern.group_project_group_8;

import java.time.LocalDate;

public class Holding {
    String account;
    String asset;
    long count;
    LocalDate startDate;
    LocalDate endDate;

    public Holding(String account, String asset, long count, LocalDate startDate, LocalDate endDate) {
        this.account = account;
        this.asset = asset;
        this.count = count;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
