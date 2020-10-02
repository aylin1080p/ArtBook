package com.aylingunes.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main2Activity extends AppCompatActivity {
    Bitmap selectedImage;
    ImageView imageView;
    EditText artNameText, painterNameText, yearText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = findViewById(R.id.imageView);
        artNameText = findViewById(R.id.artNameText);
        painterNameText = findViewById(R.id.painterNameText);
        yearText = findViewById(R.id.yearText);
        button = findViewById(R.id.button);
    }

    public void selectImage(View view){
//izni almalısın, eğer alındıysa tekar sorma
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else {
            //izin varsa burayı çalıştırır kullanıcıyı galeriye götürmeye çalışacağız
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

      if(requestCode == 1){
          if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
              Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
              startActivityForResult(intentToGallery,2);
          }
      }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==2 && resultCode== RESULT_OK && data!= null){

            Uri imageData = data.getData();
            try {
                if(Build.VERSION.SDK_INT>=28){ // apı 28den sonrasında bunu
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);

                }else {

                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    imageView.setImageBitmap(selectedImage);

                }




            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View view) {
        String artName = artNameText.getText().toString();
        String painterName = painterNameText.getText().toString();
        String year = yearText.getText().toString();
        Bitmap smallImage = makeSmallerImage(selectedImage,300);



        // görseli almak veriye çevirmek; kalite 100 üzerinden 50
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try{
            SQLiteDatabase database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY ,artname VARCHAR, year VARCHAR, image BLOB)");

            String sqlString ="INSERT INTO arts(artname,paintername,year, image) VALUES (?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
        sqLiteStatement.bindString(1,artName);
        sqLiteStatement.bindString(2, painterName);
        sqLiteStatement.bindString(3,year);
        sqLiteStatement.bindBlob(4,byteArray);
        sqLiteStatement.execute();


        }catch (Exception e){

        }

finish();
    }


    public Bitmap makeSmallerImage (Bitmap image, int maximumSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitMapRatio = (float) width/ (float) height;
        // yatay dikey kontrolü yapılıyor
        if(bitMapRatio>1){
            width=maximumSize;
            height= (int) (width/bitMapRatio);
        }else{
            height=maximumSize;
            width= (int) (height*bitMapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true); //  küçültülmüş bitmap veriyor
    }
}