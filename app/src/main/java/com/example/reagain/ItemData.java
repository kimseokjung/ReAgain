package com.example.reagain;

public class ItemData {
     String idx;
     String userid;
     String content;
     String imgPath;
     String logtime;
     String writeuserimg;
     String likeCount;
     String isChecked;
     String boardIdx;



    public ItemData(String idx, String userid, String content, String imgPath, String logtime, String writeuserimg, String likeCount, String isChecked) {
        this.idx = idx;
        this.userid = userid;
        this.content = content;
        this.imgPath = imgPath;
        this.logtime = logtime;
        this.writeuserimg = writeuserimg;
        this.likeCount = likeCount;
        this.isChecked = isChecked;
    }

    public ItemData(String idx, String userid, String content, String boardIdx) {
        this.idx = idx;
        this.userid = userid;
        this.content = content;
        this.boardIdx = boardIdx;
    }
    public ItemData(String userid, String writeuserimg) {
        this.userid = userid;
        this.writeuserimg = writeuserimg;
    }

}