package edu.northeastern.group_project_group_8;

public class Account {
    public String AccountNumber;
    public String owner;
    public String platform;

    public Account(String accountNumber, String owner, String platform) {
        AccountNumber = accountNumber;
        this.owner = owner;
        this.platform = platform;
    }
}
