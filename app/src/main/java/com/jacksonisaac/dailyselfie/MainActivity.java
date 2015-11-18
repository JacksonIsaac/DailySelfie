package com.jacksonisaac.dailyselfie;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ListActivity {

    private ListViewAdapter mAdapter;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private ImageView mImageView;
    private String mCurrentPhotoPath;

    private AlarmManager mAlarmManager;
    private static final long ALARM_DELAY = 2 * 60 * 1000L;
    protected static final long JITTER = 5000L;


    private Intent mNotificationReceiverIntent;
    private PendingIntent mNotificationReceiverPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new ListViewAdapter(getApplicationContext());

        getListView().setAdapter(mAdapter);

        initialize();

        mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        mNotificationReceiverIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                0, mNotificationReceiverIntent, 0);

        // Set repeating alarm.
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + ALARM_DELAY,
                ALARM_DELAY, mNotificationReceiverPendingIntent);

        // Set repeating alarm to fire shortly after previous alarm.
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + ALARM_DELAY
                        + JITTER,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                mNotificationReceiverPendingIntent);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                String filename = (String) mAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, ImageViewActivity.class);
                intent.putExtra("filename", filename);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_camera:
                dispatchTakePictureIntent();
                return true;
            case R.id.action_clear:
                mAdapter.removeAllViews();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            mAdapter.add(mCurrentPhotoPath);
            galleryAddPic();
        }
    }

    private void initialize() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (path.exists()) {
            File files[] = path.listFiles(new ImageFileFilter());

            for (File file : files) {
                String filename = "file:" + file;
                mAdapter.add(filename);
            }
        }
    }

    public class ImageFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (!pathname.isDirectory() && pathname.isFile() && pathname.getName().toString().startsWith("JPEG_")) {
                return true;
            }

            return false;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                // Error occurred while creating the File
                Log.e("TakePicture", "Cannot create image file.");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
