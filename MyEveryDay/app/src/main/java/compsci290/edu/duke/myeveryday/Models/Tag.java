package compsci290.edu.duke.myeveryday.Models;

/**
 * Created by wangerxiao on 4/17/17.
 */

public class Tag {
    private String mTagID;
    private String mTagName;
    private int journalcount;

    public Tag()
    {

    }

    public String getmTagID() {
        return mTagID;
    }

    public void setmTagID(String mTagID) {
        this.mTagID = mTagID;
    }

    public int getJournalcount() {
        return journalcount;
    }

    public void setJournalcount(int journalcount) {
        this.journalcount = journalcount;
    }

    public String getmTagName() {
        return mTagName;
    }

    public void setmTagName(String mTagName) {
        this.mTagName = mTagName;
    }
}
