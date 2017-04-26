package compsci290.edu.duke.myeveryday.Models;

import compsci290.edu.duke.myeveryday.Services.LatLng;

/**
 * Created by wangerxiao on 4/17/17.
 */

public class JournalEntry {
    private String mID;
    private String mTitle;
    private String mContent;
    private long mDateCreated;
    private long mDateModified;
    private long mnextreminder;
    private String mImagePath;
    private boolean mCloudImageExitst;
    private String mAudioPath;
    private boolean mCloudAudioExists;
    private String msketchpath;
    private boolean mcloudsketchExists;
    private String mWeather;
    private String mTagID;
    private String mTagName;
    private String mJourneyType;
    private LatLng mLatLng;
    private String mLocation;

    public String getmID() {
        return mID;
    }

    public void setmID(String mID) {
        this.mID = mID;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public long getmDateCreated() {
        return mDateCreated;
    }

    public void setmDateCreated(long mDateCreated) {
        this.mDateCreated = mDateCreated;
    }

    public long getmDateModified() {
        return mDateModified;
    }

    public void setmDateModified(long mDateModified) {
        this.mDateModified = mDateModified;
    }

    public long getMnextreminder() {
        return mnextreminder;
    }

    public void setMnextreminder(long mnextreminder) {
        this.mnextreminder = mnextreminder;
    }

    public String getmImagePath() {
        return mImagePath;
    }

    public void setmImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public boolean ismCloudImageExitst() {
        return mCloudImageExitst;
    }

    public void setmCloudImageExitst(boolean mCloudImageExitst) {
        this.mCloudImageExitst = mCloudImageExitst;
    }

    public String getmAudioPath() {
        return mAudioPath;
    }

    public void setmAudioPath(String mAudioPath) {
        this.mAudioPath = mAudioPath;
    }

    public boolean ismCloudAudioExists() {
        return mCloudAudioExists;
    }

    public void setmCloudAudioExists(boolean mCloudAudioExists) {
        this.mCloudAudioExists = mCloudAudioExists;
    }

    public String getMsketchpath() {
        return msketchpath;
    }

    public void setMsketchpath(String msketchpath) {
        this.msketchpath = msketchpath;
    }

    public boolean isMcloudsketchExists() {
        return mcloudsketchExists;
    }

    public void setMcloudsketchExists(boolean mcloudsketchExists) {
        this.mcloudsketchExists = mcloudsketchExists;
    }

    public String getmWeather() {
        return mWeather;
    }

    public void setmWeather(String mWeather) {
        this.mWeather = mWeather;
    }

    public String getmTagID() {
        return mTagID;
    }

    public void setmTagID(String mTagID) {
        this.mTagID = mTagID;
    }

    public String getmTagName() {
        return mTagName;
    }

    public void setmTagName(String mTagName) {
        this.mTagName = mTagName;
    }

    public String getmJourneyType() {
        return mJourneyType;
    }

    public void setmJourneyType(String mJourneyType) {
        this.mJourneyType = mJourneyType;
    }

    public LatLng getmLatLng() {
        return mLatLng;
    }

    public void setmLatLng(LatLng latlng) {
        this.mLatLng = latlng;
    }

    public String getmLocation() {
        return mLocation;
    }

    public void setmLocation(String location) {
        this.mLocation = location;
    }
}
