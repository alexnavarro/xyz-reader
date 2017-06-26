package com.example.xyzreader.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alexandrenavarro on 25/06/17.
 */

public class Item implements Parcelable {

    private long id;
    private String serverId;
    private String title;
    private String author;
    private String body;
    private String thumbUrl;
    private String photoUrl;
    private String aspectRatio;
    private String publishedDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.serverId);
        dest.writeString(this.title);
        dest.writeString(this.author);
        dest.writeString(this.body);
        dest.writeString(this.thumbUrl);
        dest.writeString(this.photoUrl);
        dest.writeString(this.aspectRatio);
        dest.writeString(this.publishedDate);
    }

    public Item() {
    }

    protected Item(Parcel in) {
        this.id = in.readLong();
        this.serverId = in.readString();
        this.title = in.readString();
        this.author = in.readString();
        this.body = in.readString();
        this.thumbUrl = in.readString();
        this.photoUrl = in.readString();
        this.aspectRatio = in.readString();
        this.publishedDate = in.readString();
    }

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}