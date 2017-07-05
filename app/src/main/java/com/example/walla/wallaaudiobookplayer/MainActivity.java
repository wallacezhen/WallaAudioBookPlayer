package com.example.walla.wallaaudiobookplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;

    ArrayList<Book> audioBooks;
    MediaPlayer currentBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        // ************************** code starts here ****************************

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setMainActivityListeners();
        setFilePermission();
        setRootFolder();

        Menu bookList = navigationView.getMenu();
        displayBookList(bookList);

        setSpeedOptions();
    }

    private void setSpeedOptions() {
        final Spinner speedOptions = (Spinner)findViewById(R.id.speedOptions);
        String[] speeds = getSpeedStrings();

        ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, speeds);
        speedOptions.setAdapter(arrayAdapter);

        speedOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentBook != null) {
                    float selectedSpeed = Float.parseFloat(
                            speedOptions.getItemAtPosition(i).toString());
                    changeCurrentBookSpeed(selectedSpeed);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private String[] getSpeedStrings() {
        return new String[]{"1.0","1.2","1.4","1.6","1.8","2.0"};
    }

    private void changeCurrentBookSpeed(float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (currentBook.isPlaying()) {
                currentBook.setPlaybackParams(currentBook.getPlaybackParams().setSpeed(speed));
            } else {
                currentBook.setPlaybackParams(currentBook.getPlaybackParams().setSpeed(speed));
                currentBook.pause();
            }
        }
    }

    private void setRootFolder() {
        String root = Environment.getExternalStorageDirectory().toString();
        root = root + "/Books";

        audioBooks = getBooks(root);
    }

    private void displayBookList(Menu bookList) {
        for (Book book : audioBooks) {
            bookList.add(0, book.getBookID(), Menu.NONE, book.getDisplayName());
        }
    }

    private void setFilePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    private ArrayList<Book> getBooks(String path) {
        ArrayList<Book> books = new ArrayList<>();

        File home = new File(path);
        File[] bookFiles = getBookFiles(home);

        if (bookFiles == null) {
            return null;
        } else {
            int bookID = 0;
            for (File book : bookFiles) {
                books.add(getNewBook(book, bookID));
                bookID++;
            }
        }
        return books;
    }

    private File[] getBookFiles(File home) {
        ArrayList<File> bookArray = new ArrayList<>();
        File[] bookFiles = home.listFiles();

        for (File book : bookFiles) {
            if (hasMP3File(book)) {
                bookArray.add(book);
            }
        }

        File[] returnBookFiles = new File[bookArray.size()];
        for (int i = 0; i < bookArray.size(); i++) {
            returnBookFiles[i] = bookArray.get(i);
        }

        return returnBookFiles;
    }

    private boolean hasMP3File(File book) {
        File[] files = book.listFiles();

        for (File file : files) {
            if (file.getName().toLowerCase().contains(".mp3")){
                return true;
            }
        }

        return false;
    }

    private Book getNewBook(File bookFile, int bookID) {
        String displayName = new String(bookFile.getName());
        File[] tempAudioFiles = bookFile.listFiles();
        int mediaPlayerCount = tempAudioFiles.length;
        ArrayList<Uri> audioFiles = new ArrayList<>();

        for (int i = 0; i < mediaPlayerCount; i++) {
            if (tempAudioFiles[i].toString().toLowerCase().contains(".mp3")) {
                audioFiles.add(Uri.parse(tempAudioFiles[i].getAbsolutePath()));
            }
        }

        return (new Book(bookID, displayName, audioFiles));
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        ImageButton forwardButton = (ImageButton) findViewById(R.id.imageButtonForward);
        ImageButton backwardButton = (ImageButton) findViewById(R.id.imageButtonBackward);
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.imageButtonPlayPause);

        if (action == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    forwardButton.callOnClick();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    backwardButton.callOnClick();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    playPauseButton.callOnClick();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    playPauseButton.callOnClick();
                    return true;
                case KeyEvent.KEYCODE_BREAK:
                    playPauseButton.callOnClick();
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        } else if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_VOLUME_MUTE:
                    return super.dispatchKeyEvent(event);
            }
        }
        return true;
    }

    private void setMainActivityListeners() {
        ImageButton forwardButton = (ImageButton) findViewById(R.id.imageButtonForward);
        ImageButton backwardButton = (ImageButton) findViewById(R.id.imageButtonBackward);
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.imageButtonPlayPause);

        setButtonImages(forwardButton, backwardButton, playPauseButton);

        setButtonForward(forwardButton);
        setButtonBackward(backwardButton);
        setButtonPlayPause(playPauseButton);

    }

    private void setButtonImages(ImageButton forward, ImageButton backward, ImageButton playPause) {
        forward.setBackgroundResource(R.drawable.forward);
        backward.setBackgroundResource(R.drawable.backward);
        playPause.setBackgroundResource(R.drawable.play);
    }

    private void setButtonForward(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBook == null) {
                    Toast.makeText(MainActivity.this, "Please select a book", Toast.LENGTH_SHORT).show();
                    return;
                }
                int currentPosition = currentBook.getCurrentPosition();
                if (currentPosition + 5000 >= currentBook.getDuration()) {
                    currentBook.seekTo(currentBook.getDuration()-1);
                } else {
                    currentBook.seekTo(currentPosition + 5000);
                }
            }
        });
    }

    private void setButtonBackward(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBook == null) {
                    Toast.makeText(MainActivity.this, "Please select a book", Toast.LENGTH_SHORT).show();
                    return;
                }
                int currentPosition = currentBook.getCurrentPosition();
                if (currentPosition > 5000) {
                    currentBook.seekTo(currentPosition - 5000);
                } else {
                    currentBook.seekTo(0);
                }
            }
        });
    }

    private void setButtonPlayPause(final ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBook == null) {
                    Toast.makeText(MainActivity.this, "Please select a book", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currentBook.isPlaying()) {
                    currentBook.pause();
                    button.setBackgroundResource(R.drawable.play);
                } else {
                    currentBook.start();
                    button.setBackgroundResource(R.drawable.pause);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        resetCurrentBook(item.getItemId());

        ImageButton playPauseButton = (ImageButton) findViewById(R.id.imageButtonPlayPause);
        playPauseButton.callOnClick();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void resetCurrentBook(int id) {
        if (currentBook != null) {
            currentBook.reset();
            currentBook.release();
        }
        Book bookSelected = audioBooks.get(id);

        currentBook = MediaPlayer.create(this, bookSelected.getAudioFilesUri().get(0));
        changeBookTitleDisplay(bookSelected);
        resetPlayPauseButton();
        resetSpeed();
    }

    private void resetPlayPauseButton() {
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.imageButtonPlayPause);
        playPauseButton.setBackgroundResource(R.drawable.play);
    }

    private void resetSpeed() {
        Spinner speedOptions = (Spinner) findViewById(R.id.speedOptions);
        speedOptions.setSelection(0);
    }

    private void changeBookTitleDisplay(Book book) {
        TextView bookTitleDisplay = (TextView) findViewById(R.id.bookTitleDisplay);
        bookTitleDisplay.setText(book.getDisplayName());

        Toast.makeText(this, "Playing: " + book.getDisplayName(), Toast.LENGTH_SHORT).show();
    }
}


