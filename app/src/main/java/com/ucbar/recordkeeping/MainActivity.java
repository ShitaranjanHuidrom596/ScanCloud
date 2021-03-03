package com.ucbar.recordkeeping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
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
import android.widget.ScrollView;
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
    TextView scandata,scancount_txt;
    Button add,save;
    EditText input_txt;
    ScrollView scroll;
    protected static final int WRITE_REQUEST_CODE =1;
    String[] storage = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
    MediaPlayer mp;
    int count=0;
    Dialog saveDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scandata=findViewById(R.id.scandata);
        scancount_txt=findViewById(R.id.scancount_txt);
        input_txt=findViewById(R.id.input_txt);
        add=findViewById(R.id.add_btn);
        save=findViewById(R.id.save_btn);

        scroll=findViewById(R.id.scroll);

        mp= MediaPlayer.create(this, R.raw.error);
        Vibrator vi= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(storage, WRITE_REQUEST_CODE);

        }else{

        }

       // createfirstfolder();


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

                boolean result = checkduplicate(input_txt.getText().toString());
                if (result) {
                    mp.start();
                    Toast toast = Toast.makeText(MainActivity.this, "Duplicate Value Found", Toast.LENGTH_LONG);
                    toast.getView().setBackgroundColor(Color.parseColor("#FF0000"));
                    toast.show();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vi.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        vi.vibrate(500);
                    }
                    input_txt.selectAll();
                } else {
                    scandata.setText(writeinstring(input_txt.getText().toString()));
                    count++;
                    scroll.fullScroll(View.FOCUS_DOWN);
                    scancount_txt.setText(String.valueOf(count));
                    input_txt.setText("");
                    input_txt.setFocusable(true);
                    input_txt.requestFocus();
                }
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
                    //createfirstfolder();
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
                    writeincsvfile(scandata.getText().toString(),filenametxt.getText().toString());
                    saveDialog.dismiss();
                }else{
                    Toast.makeText(getApplicationContext(),"Filename can't be Empty",Toast.LENGTH_LONG).show();
                }
            }
        });
        saveDialog.show();

    }


    public boolean checkduplicate(String input){
        boolean result=false;
        String currentdata=scandata.getText().toString();

        if(currentdata.isEmpty()){

        }else{
            String datainfile[]=currentdata.split("\n");
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

    public String writeinstring(String input){
        String inputdata=scandata.getText().toString();
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


        File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "Smarten");

        MediaScannerConnection.scanFile(this, new String[] {file.toString()}, null, null);  //scanning folder
        boolean success = false;
        if (!file.exists()) {
            success=file.mkdir();
        }else if(file.exists()){
            success=true;
        }
        if(success) {

            try {

                File outputfile = new File(file, filename + timestamp.toString().replace(":", "-") + ".csv");
                FileWriter writer = new FileWriter(outputfile);
                writer.append(inputdata);

                writer.flush();
                writer.close();

                Toast.makeText(getApplicationContext(), "File Save Successfully", Toast.LENGTH_LONG).show();

                count = 0;
                scancount_txt.setText("0");
                scandata.setText("");
            } catch (Exception e) {
                Log.d("Shit",e.toString());
                Toast.makeText(getApplicationContext(), "File Save Failed", Toast.LENGTH_LONG).show();
            }
        }

    }


    private static void fixUsbVisibleFolder(Context context, File folder) {
        if (!folder.exists()) {
            folder.mkdir();
            try {
                File file = new File(folder, "service.tmp");//workaround for folder to be visible via USB
                file.createNewFile();
                MediaScannerConnection.scanFile(context,
                        new String[]{file.toString()},
                        null, (path, uri) -> {
                            file.delete();
                            MediaScannerConnection.scanFile(context,
                                    new String[]{file.toString()} ,
                                    null, null);
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void createfirstfolder(){
        File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "/UCBAR/");

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