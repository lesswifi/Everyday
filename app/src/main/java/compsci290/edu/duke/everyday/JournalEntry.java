package compsci290.edu.duke.everyday;

/**
 * Created by FD on 4/4/2017.
 */

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

@IgnoreExtraProperties
public class JournalEntry {
    @SerializedName("mId")
    @Expose
    public String mId;

    @SerializedName("mTitle")
    @Expose
    public String mTitle;

    @SerializedName("mContent")
    @Expose
    public String mContent;

    @SerializedName("mDateCreated")
    @Expose
    public Date mDateCreated;

    @SerializedName("mDateModified")
    @Expose
    public Date mDateModified;

    @SerializedName("mWeather")
    @Expose
    public String mWeather;

    @SerializedName("mTag")
    @Expose
    public String mTag;

    public JournalEntry() {}

    public JournalEntry(String EntryId, String title, String content, Date dateCreated, Date dateModified, String tagId, String tagName, String weather, String tag)
    {
        mId = EntryId;
        mTitle = title;
        mContent = content;
        mDateCreated = dateCreated;
        mDateModified = dateModified;
        mWeather = weather;
        mTag = tag;

    }

    public JournalEntry(String title, String content, String tag, Date dateCreated) {
        mTitle = title;
        mContent = content;
        mDateCreated = dateCreated;
        mTag = tag;
    }


    public void setTitle(String t){
        mTitle = t;
    }

    public void setContent(String c){
        mContent = c;
    }

    public void setDateModified(Date t){
        mDateModified = t;
    }

    public void setJournalId(String j){
        mId = j;
    }

    public String getJournalId(){
        return mId;
    }

    public String getTitle(){
        return mTitle;
    }

    public Date getDateModified(){
        return mDateModified;
    }


}
