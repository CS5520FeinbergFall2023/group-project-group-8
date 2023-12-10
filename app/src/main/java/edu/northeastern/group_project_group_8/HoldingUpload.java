package edu.northeastern.group_project_group_8;

import java.time.LocalDate;

public class HoldingUpload {
    public String account;
    public String asset;
    public long count;
    public String startDate;
    public String endDate;

    public HoldingUpload(String account, String asset, long count, String startDate, String endDate) {
        this.account = account;
        this.asset = asset;
        this.count = count;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
