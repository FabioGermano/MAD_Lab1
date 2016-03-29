package com.example.fgermano.lab1;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity  {

    private SharedPreferences sharedpreferences;
    private Bitmap photoPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));

        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        sharedpreferences = getSharedPreferences("profilePref", Context.MODE_PRIVATE);

        setValues();

        setPhoto();
    }

    @Override
    protected void onStart() {
        super.onStart();

        setValues();
    }

    private void setValues()
    {
        TextView tv = (TextView)findViewById(R.id.nameContentLabel);
        tv.setText(sharedpreferences.getString("Name", ""));
        tv = (TextView)findViewById(R.id.emailContentLabel);
        tv.setText(sharedpreferences.getString("Email", ""));
        tv = (TextView)findViewById(R.id.bioContentLabel);
        tv.setText(sharedpreferences.getString("Bio", ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent intent = new Intent(this, editActivity.class);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setPhoto()
    {
        String photoDirPAth = sharedpreferences.getString("photoDirPath", "");
        ImageView iv = (ImageView) findViewById(R.id.imgProfileInLayout);

        if(this.photoPicture != null)
        {
            this.photoPicture.recycle();
        }

        if(photoDirPAth != "")
        {
            this.photoPicture = loadPhotoImageFromStorage(photoDirPAth);
            if(this.photoPicture != null) {
                iv.setImageBitmap(photoPicture);
            }
        }
    }

    private Bitmap loadPhotoImageFromStorage(String path)
    {
        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (FileNotFoundException e)
        {
            Log.d("Lab1", e.getMessage());
        }

        return null;
    }
}
