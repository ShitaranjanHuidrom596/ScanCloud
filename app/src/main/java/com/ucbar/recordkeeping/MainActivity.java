package com.ucbar.recordkeeping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
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
import android.view.Window;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //39102-55R00|153000-34700101|010221|2|00466
    String constant="39102-55R00";
    TextView shit,scancount_txt;
    Button add,save;
    EditText input_txt;
    protected static final int WRITE_REQUEST_CODE =1;
    String[] storage = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
    MediaPlayer mp;
    int count=0;
    Dialog saveDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shit=findViewById(R.id.shit);
        scancount_txt=findViewById(R.id.scancount_txt);
        input_txt=findViewById(R.id.input_txt);
        add=findViewById(R.id.add_btn);
        save=findViewById(R.id.save_btn);

        mp= MediaPlayer.create(this, R.raw.error);
        Vibrator vi= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(storage, WRITE_REQUEST_CODE);

        }else{

        }

        createfirstfolder();


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

                        shit.setText(writeinstring(input_txt.getText().toString()));
                        count++;
                        scancount_txt.setText(String.valueOf(count));
                        input_txt.setText("");
                        input_txt.setFocusable(true);
                        input_txt.requestFocus();
                    }

        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showsavedialog();
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    createfirstfolder();
                }
                else{
                    //Permission denied.
                }
                break;
        }
    }

    public void showsavedialog(){
        saveDialog = new Dialog(this);
        saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        saveDialog.setContentView(R.layout.save_dialog);

        Button okbtn = saveDialog.findViewById(R.id.btn_ok);
        EditText filenametxt = saveDialog.findViewById(R.id.filename_txt);

        okbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(filenametxt.equals(""))){
                    writeincsvfile(shit.getText().toString(),filenametxt.getText().toString());
                    saveDialog.dismiss();
                }else{
                    Toast.makeText(getApplicationContext(),"Filename can't be Empty",Toast.LENGTH_LONG).show();
                }
            }
        });
        saveDialog.show();

    }

    public String writeinstring(String input){
        String inputdata=shit.getText().toString();
        if(inputdata.equals("")){
            inputdata=input;
        }else if(!inputdata.equals("")){
            inputdata=inputdata+"\n"+input;
        }
        return  inputdata;
    }

    public void writeincsvfile(String inputdata,String filename){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());


        File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "/UCBAR");

        boolean success = false;

            try {
                File gpxfile = new File(file, filename+timestamp+".csv");
                FileWriter writer = new FileWriter(gpxfile);
                writer.append(inputdata);

                writer.flush();
                writer.close();
                Toast.makeText(getApplicationContext(),"File Save Successfully",Toast.LENGTH_LONG).show();
                count=0;
                scancount_txt.setText("0");
                shit.setText("");
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"File Save Failed",Toast.LENGTH_LONG).show();
            }


    }

    public void createfirstfolder(){
        File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "/UCBAR");

        boolean success = false;
        if (!file.exists()) {
            success=file.mkdir();
        }
        if(success){
            Toast.makeText(getApplicationContext(),"Folder Created Successfully",Toast.LENGTH_LONG).show();
        }
    }

    public void createcsvfile(String filename){


        File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "/UCBAR");

        boolean success = false;
            if (!file.exists()) {
                success=file.mkdir();
            }
            if(success){

                try {
                    File gpxfile = new File(file, filename+".csv");
                    FileWriter writer = new FileWriter(gpxfile);
                    writer.append("");
                    writer.flush();
                    writer.close();
                    Toast.makeText(getApplicationContext(),"File Save Successfully",Toast.LENGTH_LONG).show();
                } catch (Exception e) {

                }
            }

    }


   /* public String descending(){
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
    }*/



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