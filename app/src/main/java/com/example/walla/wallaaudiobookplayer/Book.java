package com.example.walla.wallaaudiobookplayer;

import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by walla on 11/25/2016.
 */

public class Book {

    private int bookID;
    private String displayName;
    private ArrayList<Uri> audioFilesUri;

    Book (int bookID, String displayName, ArrayList<Uri> audioFilesUri) {
        this.bookID = bookID;
        this.displayName = displayName;
        this.audioFilesUri = audioFilesUri;
    }

    public int getBookID() {
        return this.bookID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ArrayList<Uri> getAudioFilesUri() {
        return audioFilesUri;
    }
}
