package compsci290.edu.duke.everyday;

/**
 * Created by FD on 4/4/2017.
 */

import android.location.Location;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    public long mDateCreated;

    @SerializedName("mDateModified")
    @Expose
    public long mDateModified;

    @SerializedName("mWeather")
    @Expose
    public String mWeather;

    @SerializedName("mTag")
    @Expose
    public String mTag;

    @SerializedName("mLocation")
    @Expose
    public Location mLocation;


    public JournalEntry() {}

    public JournalEntry(String EntryId, String title, String content, long dateCreated, long dateModified, String tagId, String tagName, String weather, String tag)
    {
        mId = EntryId;
        mTitle = title;
        mContent = content;
        mDateCreated = dateCreated;
        mDateModified = dateModified;
        mWeather = weather;
        mTag = tag;

    }

    public JournalEntry(String title, String content, String tag, long dateCreated) {
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

    public void setDateModified(long t){
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

    public long getDateModified(){
        return mDateModified;
    }


}
