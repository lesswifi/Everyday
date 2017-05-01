package compsci290.edu.duke.myeveryday.Models;

import java.util.ArrayList;
import java.util.List;

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
    private List<String> mImagePaths = new ArrayList<>();
    private String mAudioPath;
    private String mWeather;
    private String mTagID;
    private String mTagName;
    private LatLng mLatLng;
    private String mLocation;
    private Double mSentimentScore;

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

    public List<String> getmImagePaths() {
        return mImagePaths;
    }

    public void setmImagePaths(List<String> mImagePaths) {
        this.mImagePaths = mImagePaths;
    }

    public void addmImagePath(String mImagePath) {
        this.mImagePaths.add(mImagePath);
    }

    public String getmAudioPath() {
        return mAudioPath;
    }

    public void setmAudioPath(String mAudioPath) {
        this.mAudioPath = mAudioPath;
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

    public void setmSentimentScore(Double score) {
        this.mSentimentScore = score;
    }

    public Double getmSentimentScore() {
        return mSentimentScore;
    }
}
