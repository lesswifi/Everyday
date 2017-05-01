package compsci290.edu.duke.myeveryday.Models;

/**
 * Created by yx78 on 4/15/17.
 */

public class Tag {
    private String mTagID;
    private String mTagName;
    private int mJournalcount;

    public Tag() {

    }

    public String getmTagID() {
        return mTagID;
    }

    public void setmTagID(String mTagID) {
        this.mTagID = mTagID;
    }

    public int getJournalcount() {
        return mJournalcount;
    }

    public void setJournalcount(int journalcount) {
        this.mJournalcount = journalcount;
    }

    public String getmTagName() {
        return mTagName;
    }

    public void setmTagName(String mTagName) {
        this.mTagName = mTagName;
    }
}
