package com.ucbar.recordkeeping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //39102-55R00|153000-34700101|010221|2|00466
    String constant="39102-55R00";
    TextView shit;
    Button add;
    EditText input_txt;
    protected static final int WRITE_REQUEST_CODE =1;
    String[] storage = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shit=findViewById(R.id.shit);
        input_txt=findViewById(R.id.input_txt);
        add=findViewById(R.id.add_btn);

        mp= MediaPlayer.create(this, R.raw.error);
        Vibrator vi= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(storage, WRITE_REQUEST_CODE);

        }else{

        }

        createfirsttxtfile();
        shit.setText(descending());



        input_txt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction()==KeyEvent.ACTION_DOWN)&&(keyCode==KeyEvent.KEYCODE_ENTER)){
                    add.performClick();
                    return true;
                }
                return false;
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean result=checkinput(input_txt.getText().toString());
                if(result){
                    boolean returnresult=checkduplicate(input_txt.getText().toString());
                    if(returnresult){
                        mp.start();
                        Toast toast = Toast.makeText(MainActivity.this,"Duplicate Value Found", Toast.LENGTH_LONG);
                        toast.getView().setBackgroundColor(Color.parseColor("#FF0000"));
                        toast.show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vi.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            vi.vibrate(500);
                        }

                        input_txt.selectAll();
                    }else {
                        writeintxtfile(input_txt.getText().toString());
                        shit.setText(descending());
                        input_txt.setFocusable(true);
                        input_txt.requestFocus();
                    }
                }else{
                    Toast toast = Toast.makeText(MainActivity.this,"Bin not Match", Toast.LENGTH_LONG);
                    toast.getView().setBackgroundColor(Color.parseColor("#FF0000"));
                    toast.show();
                }




            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    createfirsttxtfile();
                    shit.setText(descending());
                }
                else{
                    //Permission denied.
                }
                break;
        }
    }

    public boolean checkinput(String input){
        boolean result=false;
        if(input.contains("|")){

            String inputarr[]=input.split("\\|");
            String firstvalue=inputarr[0].toString();
            if(firstvalue.equals(constant)){
                result=true;
            }
        }else{

            Toast toast = Toast.makeText(MainActivity.this,"Scan Data is Invalid", Toast.LENGTH_LONG);
            toast.getView().setBackgroundColor(Color.parseColor("#FF0000"));
            toast.show();

        }
        return result;
    }

    public void writeintxtfile(String input){
        String currentdata=readtxtfile();
        String writedata=null;
        if(currentdata.isEmpty()){
             writedata =input;
        }else{
            writedata =currentdata+input;
        }

        File root = new File(Environment.getExternalStorageDirectory()+File.separator+ "/UCBAR");
        try {
            File gpxfile = new File(root, "record.txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(writedata);

            writer.flush();
            writer.close();
            input_txt.setText("");

        } catch (Exception e) {

        }
    }

    public String readtxtfile(){
        String datafromfile=null;
        File root = new File(Environment.getExternalStorageDirectory()+File.separator+ "/UCBAR");

        File file = new File(root,"record.txt");
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        datafromfile=text.toString();
        return datafromfile;

    }

    public void createfirsttxtfile(){


        File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "/UCBAR");

        boolean success = false;
            if (!file.exists()) {
                success=file.mkdir();
            }
            if(success){

                try {
                    File gpxfile = new File(file, "record.txt");
                    FileWriter writer = new FileWriter(gpxfile);
                    writer.append("");
                    writer.flush();
                    writer.close();
                    Toast.makeText(getApplicationContext(),"Folder Created Successfully",Toast.LENGTH_LONG).show();
                } catch (Exception e) {

                }
            }

    }

    public boolean checkduplicate(String input){
        boolean result=false;
        if(readtxtfile().isEmpty()){

        }else{
            String datainfile[]=readtxtfile().split("\n");
            int duplicatecount=0;

            for(int i=0;i<datainfile.length;i++){
                if(input.equals(datainfile[i])){
                    duplicatecount++;

                }
            }
            if(duplicatecount>0){
                result=true;
            }
        }
        return result;
    }

    public String descending(){
        String data=readtxtfile();
        String datainfile[]=readtxtfile().split("\n");

        List<String> list = Arrays.asList(datainfile);
        Collections.reverse(list);
        String[] reversedArray = list.toArray(datainfile);

        String reversedata="";
        for(int i=0;i<reversedArray.length;i++){
            reversedata=reversedata+reversedArray[i]+"\n";
        }
        return  reversedata;
    }



       /* File sdcard = Environment.getExternalStorageDirectory();

        File file = new File(sdcard,"test.txt");
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
            shit.setText(text);
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }*/
       /* try {
            File gpxfile = new File(sdcard, "test.txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append("Shitaranjan Huidrom");
            writer.flush();
            writer.close();
            Toast.makeText(MainActivity.this, "Saved your text", Toast.LENGTH_LONG).show();
        } catch (Exception e) { }*/

       /*File file = new File(sdcard,"file.txt");
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }*/

}