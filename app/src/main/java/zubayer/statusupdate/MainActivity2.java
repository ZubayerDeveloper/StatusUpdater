package zubayer.statusupdate;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;
import me.anwarshahriar.calligrapher.Calligrapher;
import static android.widget.Toast.makeText;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class MainActivity2 extends AppCompatActivity {
    private AdView mAdView;
    EditText edit;
    Button save,post,clear,exit,undo,redo,zoomin,zoomout;
    AlertDialog dialog,checkinternet;
    AlertDialog.Builder builder;
    String status,parseVersionCode;
    StringBuilder stringbuit;
    Button spinner;
    ArrayList<String> dates;
    ArrayList <String>names;
    ArrayList<String> undoArray;
    ArrayList<String> redoArray;
    ArrayList<String> restoreArray;
    ArrayList<String> fileArray;
    String date,updateMessage;
    Myadapter2 adaptate;
    View m;
    ListView delList;
    int arrayPosition,size,textsize=18;
    int versionCode=7;
    boolean clicked,cleared=false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        edit = (EditText) findViewById(R.id.edit);
        save = (Button) findViewById(R.id.save);
        post = (Button) findViewById(R.id.post);
        clear = (Button) findViewById(R.id.clear);
        undo = (Button) findViewById(R.id.undo);
        redo = (Button) findViewById(R.id.redo);
        exit = (Button) findViewById(R.id.exit);
        spinner =(Button) findViewById(R.id.spinner);
        zoomin =(Button) findViewById(R.id.zoomin);
        zoomout =(Button) findViewById(R.id.zoomout);
        names=new ArrayList<>();
        dates=new ArrayList<>();
        fileArray=new ArrayList<>();
        undoArray =new ArrayList<>();
        redoArray =new ArrayList<>();
        restoreArray=new ArrayList<>();
        m=getLayoutInflater().inflate(R.layout.listview,null);
        delList=(ListView)m.findViewById(R.id.ListView);

        HtmlParser back=new HtmlParser();
        back.execute();
        final Calligrapher font = new Calligrapher(this);
        font.setFont(this, "kalpurush.ttf", true);

        edit.setTextSize(textsize);
        builder=new AlertDialog.Builder(MainActivity2.this);
        dialog=builder.create();
        dialog.setView(m);
        dialog.setCancelable(false);
        dialog.setButton2("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveState();
                names.clear();
                dates.clear();
                fileArray.clear();
            }
        });
        try {
            readTitle();
            readDate();
            if (names.isEmpty()) {
                spinner.setText("Empty Draft");
            }else{
                spinner.setText(names.get(0)+"\n"+dates.get(0));
            }
        }catch (Exception e){}
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity2.this);
        edit.setText(preferences.getString("autosave", null));
        if(edit.getText().toString().length()==0){
        }else {
            undoArray.add(edit.getText().toString());
        }
        undoArray.add(preferences.getString("autosave", null));
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                preferences.edit().putString("autosave", editable.toString()).commit();
                undoArray.add(edit.getText().toString());

            }
        });
               save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String s = edit.getText().toString();
                if (s.length() == 0) {
                    checkinternet = builder.create();
                    checkinternet.setButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface da, int but) {
                        }
                    });
                    checkinternet.setMessage("Please write something to save as your status");
                    checkinternet.show();
                } else {
                    checkinternet = builder.create();
                    checkinternet.setButton("Save current file", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface da, int but) {
                            readDate();
                            readTitle();
                            readstatus();
                            if (fileArray.isEmpty()) {
                                try {
                                    saveListHeading();
                                    saveListDate();
                                    saveStatus();
                                    spinner.setText(names.get(0) + "\n" + dates.get(0));
                                    myToast("Saved successfully");
                                    clicked=false;
                                } catch (Exception e) {
                                }
                            } else if(clicked==false) {
                                int position=fileArray.size()-1;
                                fileArray.remove(fileArray.get(position));
                                int position2=fileArray.size();
                                fileArray.add(position2, edit.getText().toString());
                                saveState();
                            }else {
                                fileArray.remove(arrayPosition);
                                fileArray.add(arrayPosition, edit.getText().toString());
                                saveState();
                        }
                        }
                    });
                    checkinternet.setButton3("Save new", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface da, int but) {
                            readDate();
                            readTitle();
                            readstatus();
                            saveListHeading();
                            saveListDate();
                            saveStatus();
                            clicked=false;
                            try {
                               spinner.setText(names.get(0)+"\n"+dates.get(0));
                               myToast("Saved successfully");
                            } catch (Exception e) {
                            }
                        }
                    });
                    checkinternet.setMessage("Save new file ? or Save to current file ?");
                    checkinternet.show();
                }
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String text = edit.getText().toString();
                if (text.length() == 0) {
                    checkinternet = builder.create();
                    checkinternet.setButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface da, int but) {
                        }
                    });
                    checkinternet.setMessage("No text found to copy and post");
                    checkinternet.show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", edit.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast toast = makeText(MainActivity2.this, "Text copied to clipboard", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    status = edit.getText().toString();
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, status);
                    if (isInstalled("com.facebook.katana")) {
                        i.setPackage("com.facebook.katana");
                        startActivity(i);
                    } else if (isInstalled("com.facebook.lite")) {
                        i.setPackage("com.facebook.lite");
                        startActivity(i);
                    } else {
                        Intent.createChooser(i, "Share using...");
                        startActivity(i);
                    }
                }
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(edit.getText().toString().length()==0){

                }else {
                    checkinternet = builder.create();
                    checkinternet.setButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface da, int but) {
                        }
                    });
                    checkinternet.setButton3("Clear text", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface da, int but) {
                            edit.setText("");
                            cleared=true;
                        }
                    });
                    checkinternet.setMessage("Are you sure you want to clear text?");
                    checkinternet.show();
                }
            }
        });
        spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    readDate();
                    readTitle();
                    readstatus();
                    if (names.isEmpty()) {
                    } else {
                        dialog.setMessage(null);
                        adaptate = new Myadapter2(MainActivity2.this, names, dates,fileArray);
                        delList.setAdapter(adaptate);
                        dialog.show();
                    }
                }catch (Exception e){}
            }
        });
        delList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                clicked=true;
                saveState();
                try {
                    readDate();
                    readTitle();
                    readstatus();
                    if (names.isEmpty()) {
                    } else {
                        edit.setText(fileArray.get(position));
                        dialog.dismiss();
                    }
                }catch (Exception e){}
                arrayPosition=position;
            }
        });
        undo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                redoArray.add(edit.getText().toString());
                try {
                    undoArray.remove(undoArray.get(undoArray.size() - 1));
//                    if(cleared==true) {
//                        size = (undoArray.size() - 2);
//                    }else {
                        size = (undoArray.size() - 1);
//                    }
                    edit.setText(undoArray.get(size));
                    cleared=false;
                    undoArray.remove(undoArray.get(undoArray.size() - 1));
                }catch (ArrayIndexOutOfBoundsException e){}
            }
        });
        redo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                        size = (redoArray.size() - 1);
                    edit.setText(redoArray.get(size));
                    redoArray.remove(redoArray.get(redoArray.size() - 1));
                }catch (ArrayIndexOutOfBoundsException e){}
            }
        });
        zoomin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                edit.setTextSize(++textsize);
            }
        });
        zoomout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                edit.setTextSize(--textsize);
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    public String readFile() {
        String string;
        StringBuffer buffer = new StringBuffer();
        stringbuit = new StringBuilder();
        try {
            FileInputStream read = openFileInput("status");
            Scanner sc = new Scanner(read);

            while (sc.hasNext()) {
                buffer.append(sc.nextLine()).append("\n");

            }
            read.close();
            sc.close();
        } catch (Exception e) {
        }
        string = buffer.toString();
        return string;
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.about:
                checkinternet = builder.create();
                checkinternet.setButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface da, int but) {
                    }
                });
                checkinternet.setMessage(getString(R.string.about));
                checkinternet.show();
                break;
            case R.id.rate:
                Intent i =new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=zubayer.statusupdate"));
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        boolean pressed=false;
        if(!pressed) {
            checkinternet = builder.create();
            checkinternet.setButton("Send mail", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent i =new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"+"zubayer.developer@gmail.com"));
                    i.putExtra(Intent.EXTRA_SUBJECT,"Subject: problems");
                    i.putExtra(Intent.EXTRA_TEXT,"Write here your suggestion or problems");
                    startActivity(i);
                }
            });
            checkinternet.setButton3("Exit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    finish();
                }
            });
            try {
                checkinternet.setMessage(getText(R.string.exit));
            } catch (Exception e) {
            }
            checkinternet.show();
            pressed=true;
        }else {
            super.onBackPressed();
        }
    }
    public boolean isInstalled(String packageName){
        try {
            PackageManager pm =getPackageManager();
            pm.getPackageInfo(packageName,0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return  false;
        }
    }
    public void saveStatus(){
        try{
            fileArray.add(edit.getText().toString());
            FileOutputStream write = openFileOutput("status", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(fileArray);
            arrayoutput.close();
            write.close();
        }catch(Exception e){}
    }
    public void readstatus(){
        try {
            FileInputStream read = openFileInput("status");
            ObjectInputStream readarray = new ObjectInputStream(read);
            fileArray = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        }catch (Exception e){}
    }
    public void saveListHeading(){
        try {
            names.add(headingtrimer());
            FileOutputStream write = openFileOutput("title", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(names);
            arrayoutput.close();
            write.close();
        }catch (Exception e){}
    }
    public void saveListDate(){
        try {
            long mydate = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE dd/MM/yyyy h:mm a");
            date = sdf.format(mydate);

            dates.add(date);
            FileOutputStream write = openFileOutput("date", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(dates);
            arrayoutput.close();
            write.close();
        }catch (Exception e){}
    }
    public String headingtrimer(){
        String heading="";
        String string=edit.getText().toString();
        if(string.length()<26){
            heading=string.substring(0,string.length());

        }else{
            heading=(string.substring(0,26)+"...");

        }
        return (heading);
    }
    public void readTitle(){
        try {
            FileInputStream read = openFileInput("title");
            ObjectInputStream readarray = new ObjectInputStream(read);
            names = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        }catch (Exception e){}
    }
    public void readDate(){
        try {
            FileInputStream read = openFileInput("date");
            ObjectInputStream readarray = new ObjectInputStream(read);
            dates = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        }catch (Exception e){}
    }
    public void saveState(){
        try {
            FileOutputStream write = openFileOutput("title", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(names);
            arrayoutput.close();
            write.close();

        }catch (Exception e){}
        try {
            FileOutputStream write = openFileOutput("date", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(dates);
            arrayoutput.close();
            write.close();
        }catch (Exception e){}
        try{
            FileOutputStream write = openFileOutput("status", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(fileArray);
            arrayoutput.close();
            write.close();
        }catch(Exception e){}
        if(names.isEmpty()){
            spinner.setText("Empty Draft");
        }else {
            spinner.setText(names.get(0)+"\n"+dates.get(0));
        }
    }
    public void myToast(String toasttText){
        Toast toast = makeText(MainActivity2.this, toasttText, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    class HtmlParser extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void...params)
        {
            try{
                Document doc =Jsoup.connect("https://drzubayerahmed.wordpress.com/2017/11/29/26/?preview=true").get();
                Elements links=doc.select("p");
                    Element link=links.get(1);
                    Element message=links.get(3);
                    parseVersionCode=link.text();
                    updateMessage=message.text();
            }catch(Exception e){
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void b)
        {

            super.onPostExecute(b);
            if(parseVersionCode!=null){
                Integer parseint=Integer.parseInt(parseVersionCode);
                if(parseint>versionCode){
                    SharedPreferences prefs=getSharedPreferences("update",Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("yesno",true).commit();
                    prefs.edit().putString("updateMessage",updateMessage).commit();
                    checkUpdate();
                }else  if(parseint==versionCode){
                    SharedPreferences prefs=getSharedPreferences("update",Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("yesno",false).commit();
                    myToast("Your app is uptodate");
                    checkUpdate();
                }else if(parseint<versionCode){
                    SharedPreferences prefs=getSharedPreferences("update",Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("yesno",false).commit();
                    myToast("Your app is up to date");
                    checkUpdate();
                }
            }
            else{
                checkUpdate();
            }
        }
    }
 public  void checkUpdate(){
        try {
            SharedPreferences prefs = getSharedPreferences("update", Context.MODE_PRIVATE);
            boolean b = prefs.getBoolean("yesno", false);
            updateMessage=prefs.getString("updateMessage",null);
            if (b == true) {
                checkinternet = builder.create();
                checkinternet.setButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface da, int but) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=zubayer.statusupdate"));
                        startActivity(i);
                    }
                });
                checkinternet.setMessage(updateMessage);
                checkinternet.setCancelable(false);
                checkinternet.show();
            }
        }catch (Exception e){}
 }
}
