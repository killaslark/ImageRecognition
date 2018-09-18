package com.example.asus.imagerecognition;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private ImageView imageNumber;
    private Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;

    private int[] chainFrequency = new int[8];


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonPredict = (Button) findViewById(R.id.buttonPredict);
        Button buttonUpload = (Button) findViewById(R.id.buttonUpload);
        imageNumber = (ImageView) findViewById(R.id.imageNumber);

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        buttonPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                predictNumber();
            }
        });

        for (int i = 0; i < 8 ; i++) {
            chainFrequency[i] = 0;
        }

    }


    private void selectImage() {
        final CharSequence[] items ={"Camera","Gallery","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                    imageNumber.setVisibility(View.VISIBLE);
                } else if (items[which].equals("Gallery")) {
                    Intent photopickerIntent = new Intent(Intent.ACTION_PICK);
                    File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    String pictureDirectoryPath = pictureDirectory.getPath();
                    Uri data = Uri.parse(pictureDirectoryPath);
                    photopickerIntent.setDataAndType(data, "image/*");
                    startActivityForResult(photopickerIntent,SELECT_FILE);
                    imageNumber.setVisibility(View.VISIBLE);
                } else if (items[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void predictNumber() {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Vector chainCode = new Vector();
        int pixel;
        int iStart = 0,jStart = 0;
        boolean found = false;


        for (int j = 0; j < height && !found; j++) {
            for (int i = 0; i < width && !found; i++){
                pixel = bitmap.getPixel(i,j);
                if (Color.red(pixel) == 0 && Color.green(pixel) == 0 && Color.blue(pixel) == 0) {
                    // i itu x, j itu y
                    Log.d("Found",i+","+ j);
                        iStart = i;
                        jStart = j;
                    Log.d("Size ", width+","+height);
                    found = true;
                }
            }
        }

            int iKeliling = iStart, jKeliling = jStart;
            checkTimur(iKeliling, jKeliling, bitmap, chainCode, iStart, jStart);


            for (int i = 0 ; i < 8; i ++) {
                Log.d("Arah["+i+"] :" , chainFrequency[i]+" kemunculan");
                chainFrequency[i] = 0;
            }
    }

    private boolean isPixelBlack(int pixel) {
        int limit = 50;
        return (Color.red(pixel) < limit && Color.green(pixel) < limit && Color.blue(pixel) < limit);
    }

    private void checkTimur(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (iKeliling + 1 < bitmap.getHeight()) {

            int pixel = bitmap.getPixel(iKeliling + 1, jKeliling);
//            Log.d("Timur" , iKeliling +","+ jKeliling);
            if (isPixelBlack(pixel)) {
                chainCode.add(0);
                chainFrequency[0]++;
                iKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkUtara(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkTenggara(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }
        }
    }

    private  void checkTenggara(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (jKeliling + 1 < bitmap.getHeight() && iKeliling + 1 < bitmap.getWidth()){
//            Log.d("Tenggara" , iKeliling +","+jKeliling);
            int pixel = bitmap.getPixel(iKeliling + 1, jKeliling + 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(1);
                chainFrequency[1]++;
                jKeliling++;
                iKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkTimur(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkSelatan(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }
        }
    }
    private  void checkSelatan(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (jKeliling + 1 < bitmap.getWidth()){
//            Log.d("Selatan" , iKeliling +","+ jKeliling);
            int pixel = bitmap.getPixel(iKeliling, jKeliling + 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(2);
                chainFrequency[2]++;
                jKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkTimur(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkBaratDaya(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }
        }
    }
    private  void checkBaratDaya(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (iKeliling - 1 >= 0 && jKeliling + 1 < bitmap.getHeight()){
//            Log.d("BaratDaya" , iKeliling +","+jKeliling);
            int pixel = bitmap.getPixel(iKeliling - 1, jKeliling + 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(3);
                chainFrequency[3]++;
                iKeliling--;
                jKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkTimur(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkBarat(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }

        }
    }
    private  void checkBarat(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (iKeliling - 1 >= 0){
//            Log.d("Barat" , iKeliling +","+jKeliling);
            int pixel = bitmap.getPixel(iKeliling - 1, jKeliling );
            if (isPixelBlack(pixel)) {
                chainCode.add(4);
                chainFrequency[4]++;
                iKeliling--;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkSelatan(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkBaratLaut(iKeliling, jKeliling, bitmap, chainCode, iStart, jStart);
            }
        }
    }
    private  void checkBaratLaut(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (jKeliling - 1 >= 0 && iKeliling -1 >= 0){
//            Log.d("BaratLaut" , iKeliling +","+ jKeliling);
            int pixel = bitmap.getPixel(iKeliling - 1, jKeliling - 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(5);
                chainFrequency[5]++;
                jKeliling--;
                iKeliling--;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkBarat(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkUtara(iKeliling, jKeliling, bitmap, chainCode, iStart, jStart);
            }
        }
    }
    private  void checkUtara(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (jKeliling - 1 >= 0){
//            Log.d("Utara" , iKeliling +","+ jKeliling);
            int pixel = bitmap.getPixel(iKeliling, jKeliling - 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(6);
                chainFrequency[6]++;
                jKeliling--;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkBarat(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkTimurLaut(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }
        }
    }
    private  void checkTimurLaut(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (iKeliling + 1 < bitmap.getHeight() && jKeliling - 1 >= 0){
//            Log.d("TimurLaut" , iKeliling +","+jKeliling);
            int pixel = bitmap.getPixel(iKeliling + 1, jKeliling - 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(7);
                chainFrequency[7]++;
                jKeliling--;
                iKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkBarat(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkTimur(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }

        }
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bundle bundle = data.getExtras();
                bitmap = (Bitmap) bundle.get("data");
                imageNumber.setImageBitmap(bitmap);

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(selectedImageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    imageNumber.setImageBitmap(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
                imageNumber.setImageURI(selectedImageUri);
            }
        }
    }
}
