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
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private Bitmap skeletonBitmap;
    private ImageView imageNumber;
    private ImageView imageSkeleton;
    private TextView textNumber;
    private Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;

    private int[] chainFrequency = new int[8];
    private int[] operand = new int[2];
    private int operator;
    private int operandPointer;
    private final double[][] numberFrequency = new double[][]{
            //Angka 0
            {0.097560976,0.073170732,0.256097561,0.073170732,0.097560976,0.079268293,0.243902439,0.079268293},
            //Angka 1
            {0.053254438,          0,0.384615385,0.047337278,0.082840237, 0.00591716,0.343195266,0.082840237},
            //Angka 2
            {0.190298507,0.044776119,0.104477612,0.141791045,0.22761194,0.029850746,0.097014925,0.164179104},
            //Angka 3
            {0.161172161,0.098901099,0.135531136,0.102564103,0.164835165,0.098901099,0.131868132,0.106227106},
            //Angka 4
            {0.070652174,0.005434783,0.342391304,0.005434783,0.222826087,0.005434783,0.190217391,0.157608696},
            //Angka 5
            {0.215231788,0.069536424,0.145695364,0.079470199,0.195364238,0.072847682,0.158940397,0.062913907},
            //Angka 6
            {0.137339056,0.094420601,0.17167382,0.098712446,0.13304721,0.090128755,0.184549356,0.090128755},
            //Angka 7
            {0.198067633,0,0.188405797,0.120772947,0.183574879,0.004830918,0.193236715,0.111111111},
            //Angka 8
            {0.129943503,0.107344633,0.15819209,0.107344633,0.124293785,0.11299435,0.152542373,0.107344633},
            //Angka 9
            {0.137339056,0.090128755,0.180257511,0.090128755,0.141630901,0.090128755,0.175965665,0.094420601},
            //Simbol +
            {0.246323529,0.003676471,0.246323529,0.003676471,0.246323529,0.003676471,0.246323529,0.003676471},
            //Simbol -
            {0.387096774,0,0.112903226,0,0.387096774,0,0.112903226,0}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonPredict = (Button) findViewById(R.id.buttonPredict);
        Button buttonUpload = (Button) findViewById(R.id.buttonUpload);
        Button buttonThin = (Button) findViewById(R.id.buttonThin);
        imageNumber = (ImageView) findViewById(R.id.imageNumber);
        imageSkeleton = (ImageView) findViewById(R.id.imageSkeleton);
        textNumber = (TextView) findViewById(R.id.textView);

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

        buttonThin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmap != null)
                {
                    Skeletonization skeletonization = new Skeletonization(bitmap);
                    skeletonBitmap = skeletonization.getBitmap();
                    imageSkeleton.setImageBitmap(skeletonBitmap);
                }
            }
        });


        for (int i = 0; i < 8 ; i++) {
            chainFrequency[i] = 0;
        }
        operand[0] = -1;
        operand[1] = -1;
        operator = -1;
        operandPointer = 0;
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
                if (isPixelBlack(pixel)) {
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

        double minSum = -1;
        int idx = -1;
        double[] normalizedFreq = normalize10Histogram(chainFrequency);
        for(int i = 0;i<12;i++) {
            double sum = 0;
            for (int j = 0; j < 8; j++) {
                sum += Math.abs(numberFrequency[i][j] - normalizedFreq[j]);
            }
            if (sum < minSum || minSum == -1) {
                idx = i;
                minSum = sum;
            }
        }
        //Set operand and operator
        if(idx < 10){
            operand[operandPointer] = idx;
            operandPointer = operandPointer+1 > 1 ? 0 : operandPointer+1;
        } else {
            operator = idx;
        }

        for (int i = 0; i < 8; i++) {
            Log.d("Arah[" + i + "] :", chainFrequency[i] + " kemunculan");
            chainFrequency[i] = 0;
        }

        //Update Text
        updateTextView();
        Log.d("Number Predicted", Integer.toString(idx));
    }

    private void updateTextView(){
        String operand1 = operand[0] == -1 ? "Operand1" : Integer.toString(operand[0]);
        String operand2 = operand[1] == -1 ? "Operand2" : Integer.toString(operand[1]);
        String operatorText = "Operator";
        String count = "";

        if(operator == 10) operatorText = "+";
        else if(operator == 11) operatorText = "-";

        if(operand[0] != -1 && operand[1] != -1 && operator != -1)
        {
            if(operator == 10) count = " = " + Integer.toString(operand[0]+operand[1]);
            else if(operator == 11) count = " = " + Integer.toString(operand[0]-operand[1]);
        }

        String predictedText = operand1 + " " + operatorText + " " + operand2 + count;
        textNumber.setText(predictedText);
    }

    private boolean isPixelBlack(int pixel) {
        int limit = 40;
        return (Color.red(pixel) < limit && Color.green(pixel) < limit && Color.blue(pixel) < limit);
    }

    private void checkTimur(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (iKeliling + 1 < bitmap.getWidth()) {

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
        if (jKeliling + 1 < bitmap.getHeight()){
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

    private double[] normalize10Histogram(int[] arr)
    {
        double[] newHist = new double[arr.length];
        int sum = 0;

        for(int i = 0;i < arr.length;i++)
            sum += arr[i];

        for(int i = 0;i < arr.length;i++)
            newHist[i] = (float)arr[i] / (float)sum;

        return newHist;
    }

    private int[] normalizeHistogram(int[] from, int[] to)
    {
        int[] newHist = new int[from.length];
        int oldMin = getMin(from);
        int newMin = getMin(to);
        int oldRange = getMax(from) - oldMin;
        int newRange = getMax(to) - newMin;
        if(from.length == to.length) {
            for (int i = 0; i < from.length; i++)
            {
                newHist[i] = Math.round(((float)((from[i] - oldMin) * newRange)/(float)oldRange) + newMin);
            }
        }
        return newHist;
    }

    private int getMax(int[] arr){
        int max = -1;
        for(int i = 0;i < arr.length;i++)
        {
            if(max < arr[i])
                max = arr[i];
        }
        return max;
    }

    private int getMin(int[] arr){
        int min = 9999;
        for(int i = 0;i < arr.length;i++)
        {
            if(min > arr[i])
                min = arr[i];
        }
        return min;
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
