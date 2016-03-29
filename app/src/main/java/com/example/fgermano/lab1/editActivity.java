package com.example.fgermano.lab1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnFocusChangeListener;
import android.view.View;
import android.support.v7.app.ActionBar;
import android.widget.Toast;
import java.util.List;
import java.util.ArrayList;
import android.provider.MediaStore;
import android.os.Parcelable;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.app.Activity;
import android.widget.ImageView;
import android.net.Uri;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.content.ContextWrapper;
import java.io.*;

public class editActivity extends AppCompatActivity {

    private SharedPreferences sharedpreferences;
    private String photoDirPAth = "";
    private Bitmap photoPicture;
    private boolean isPhotoSetted = false; // ie, changed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));

        setContentView(R.layout.activity_edit);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        sharedpreferences = getSharedPreferences("profilePref", Context.MODE_PRIVATE);

        setValues();

        setPhoto();
    }

    private void setPhoto()
    {
        photoDirPAth = sharedpreferences.getString("photoDirPath", "");
        if(photoDirPAth != "")
        {
            if(this.photoPicture != null)
            {
                this.photoPicture.recycle();
            }

            this.photoPicture = loadPhotoImageFromStorage(photoDirPAth);
            if(this.photoPicture != null) {
                ImageView iv = (ImageView) findViewById(R.id.imgProfileInLayout);
                iv.setImageBitmap(this.photoPicture);

                isPhotoSetted = true;
            }
        }
    }

    private void setValues() {
        EditText tv = (EditText) findViewById(R.id.nameEditText);
        tv.setText(sharedpreferences.getString("Name", ""));
        tv = (EditText) findViewById(R.id.emailEditText);
        tv.setText(sharedpreferences.getString("Email", ""));
        tv = (EditText) findViewById(R.id.bioEditText);
        tv.setText(sharedpreferences.getString("Bio", ""));
    }

    private void saveValues()
    {
        SharedPreferences.Editor editor = sharedpreferences.edit();

        EditText et = (EditText) findViewById(R.id.nameEditText);
        editor.putString("Name", et.getText().toString());

        et = (EditText) findViewById(R.id.emailEditText);
        editor.putString("Email", et.getText().toString());

        et = (EditText) findViewById(R.id.bioEditText);
        editor.putString("Bio", et.getText().toString());

        if(isPhotoSetted) // photo chosen
        {
            String dirPath = savePhotoToInternalStorage(this.photoPicture);
            editor.putString("photoDirPath", dirPath);
        }
        else
        {
            editor.remove("photoDirPath");
        }

        editor.commit();

        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.changed_saved), Toast.LENGTH_SHORT);
        toast.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveValues();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final int REQUEST_CAMERA = 0;
    private final int SELECT_FILE = 1;

    public void changeImagePressed(View v)
    {
        List<String> values = new ArrayList<String>();

        values.add(getResources().getString(R.string.take_photo));
        values.add(getResources().getString(R.string.chose_library));
        if(isPhotoSetted)
        {
            values.add(getResources().getString(R.string.remove_photo));
        }

        final CharSequence[] items = values.toArray(new CharSequence[values.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(editActivity.this);
        builder.setTitle(getResources().getString(R.string.add_photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(items[0])) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals(items[1])) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, items[1]),
                            SELECT_FILE);
                } else if (items[item].equals(items[2])) {
                    ((ImageView) findViewById(R.id.imgProfileInLayout)).setImageResource(R.drawable.profil_icon);
                    isPhotoSetted = false;
                }
            }
        });
        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(this.photoPicture != null)
        {
            this.photoPicture.recycle();
        }

        this.photoPicture = null;

        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            this.photoPicture = (Bitmap) data.getExtras().get("data");
        }
        else if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK) {

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, options);
                    final int REQUIRED_SIZE = 200;
                    int scale = 1;
                    while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                            && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                        scale *= 2;
                    options.inSampleSize = scale;
                    options.inJustDecodeBounds = false;

                    this.photoPicture = BitmapFactory.decodeFile(filePath, options);
                }
                cursor.close();
        }

        if(this.photoPicture != null) {
                ImageView iv = (ImageView) findViewById(R.id.imgProfileInLayout);
                iv.setImageBitmap(this.photoPicture);

                isPhotoSetted = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.actionbar_edit, menu);
        return true;
    }

    private String savePhotoToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.close();
        } catch (Exception e) {
            Log.d("Lab1", e.getMessage());
        }
        return directory.getAbsolutePath();
    }

    private Bitmap loadPhotoImageFromStorage(String path)
    {
        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e)
        {
            Log.d("Lab1", e.getMessage());
        }

        return null;
    }

    @Override
    protected void onSaveInstanceState (Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable("image", this.photoPicture);
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null)
        {
            this.photoPicture = savedInstanceState.getParcelable("image");
            if(this.photoPicture != null)
            {
                ((ImageView) findViewById(R.id.imgProfileInLayout)).setImageBitmap(this.photoPicture);
            }
        }
    }
}