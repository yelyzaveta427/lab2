package com.example.pasir_ihor_kotenko.dto;
public class GroupNotificationMessage {
    private String type = "GROUP_EXPENSE_ADDED";
    private Long groupId;
    private String groupName;
    private String title;
    private double amount;
    private double userShare;
    private String createdByEmail;
    private String message;
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getUserShare() { return userShare; }
    public void setUserShare(double userShare) { this.userShare = userShare; }
    public String getCreatedByEmail() { return createdByEmail; }
    public void setCreatedByEmail(String createdByEmail) { this.createdByEmail = createdByEmail; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
