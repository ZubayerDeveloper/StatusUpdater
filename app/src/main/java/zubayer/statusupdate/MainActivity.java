package zubayer.statusupdate;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.support.design.internal.NavigationMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import me.anwarshahriar.calligrapher.Calligrapher;

import static android.widget.Toast.makeText;

public class MainActivity extends Activity {
    EditText edit;
    Button save, post, clear, exit, undo, redo, zoomin, zoomout;
    AlertDialog dialog, checkinternet;
    AlertDialog.Builder builder;
    String status, parseVersionCode;
    StringBuilder stringbuit;
    Button draft;
    ArrayList<String> dates;
    ArrayList<String> names;
    ArrayList<String> undoArray;
    ArrayList<String> redoArray;
    ArrayList<String> restoreArray;
    ArrayList<String> fileArray;
    String date, updateMessage;
    Myadapter2 adaptate;
    View m;
    ListView draftList;
    int arrayPosition, size, textsize = 18;
    int versionCode = 7;
    boolean clicked, cleared = false;
    SharedPreferences retriveAutoSaveText;
    FabSpeedDial fab;
    private AdView mAdView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAdView();
        setFornt();
        initialize();
        checkAppUpdate();
        createAlertDialogue();
        setDraftButtonText();
        retriveAutosaveText();
        firstUndoElement();
        autoSave();
        fab = findViewById(R.id.fabs);
        fab.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.about:
                        checkinternet = builder.create();
                        checkinternet.setButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface da, int but) {
                            }
                        });
                        checkinternet.setMessage(getString(R.string.about));
                        checkinternet.show();
                        break;
                    case R.id.undo:
                        makeUndo();
                        break;
                    case R.id.redo:
                        makeRedo();
                        break;
                    case R.id.save:
                        saveTexts();
                        break;
                    case R.id.post:
                        postToFacebook();
                        break;
                    case R.id.clear:
                        clearText();
                        break;
                }
                return false;
            }

            @Override
            public void onMenuClosed() {

            }
        });

        draft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDrafts();
            }
        });
        draftList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                retriveDraft(position);
            }
        });
    }

    private void setAdView() {
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void makeRedo() {
        try {
            size = (redoArray.size() - 1);
            edit.setText(redoArray.get(size));
            redoArray.remove(redoArray.get(redoArray.size() - 1));
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private void makeUndo() {
        redoArray.add(edit.getText().toString());
        try {
            undoArray.remove(undoArray.get(undoArray.size() - 1));
            size = (undoArray.size() - 1);
            edit.setText(undoArray.get(size));
            cleared = false;
            undoArray.remove(undoArray.get(undoArray.size() - 1));
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private void retriveDraft(int position) {
        clicked = true;
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
        } catch (Exception e) {
        }
        arrayPosition = position;
    }

    private void showDrafts() {
        try {
            readDate();
            readTitle();
            readstatus();
            if (names.isEmpty()) {
            } else {
                dialog.setMessage(null);
                adaptate = new Myadapter2(MainActivity.this, names, dates, fileArray);
                draftList.setAdapter(adaptate);
                dialog.show();
            }
        } catch (Exception e) {
        }
    }

    private void clearText() {
        if (edit.getText().toString().length() == 0) {

        } else {
            checkinternet = builder.create();
            checkinternet.setButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface da, int but) {
                }
            });
            checkinternet.setButton3("Clear text", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface da, int but) {
                    edit.setText("");
                    cleared = true;
                }
            });
            checkinternet.setMessage("Are you sure you want to clear text?");
            checkinternet.show();
        }
    }

    private void postToFacebook() {
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
            Toast toast = makeText(MainActivity.this, "Text copied to clipboard", Toast.LENGTH_SHORT);
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

    private void saveTexts() {
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
                            draft.setText(names.get(0) + "\n" + dates.get(0));
                            myToast("Saved successfully");
                            clicked = false;
                        } catch (Exception e) {
                        }
                    } else if (!clicked) {
                        fileArray.remove(fileArray.get(0));
                        fileArray.add(0, edit.getText().toString());
                        saveState();
                    } else {
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
                    clicked = false;
                    try {
                        draft.setText(names.get(0) + "\n" + dates.get(0));
                        myToast("Saved successfully");
                    } catch (Exception e) {
                    }
                }
            });
            checkinternet.setMessage("Save new file ? or Save to current file ?");
            checkinternet.show();
        }
    }

    private void autoSave() {
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                retriveAutoSaveText.edit().putString("autosave", editable.toString()).commit();
                undoArray.add(edit.getText().toString());

            }
        });
    }

    private void firstUndoElement() {
        if (edit.getText().toString().length() == 0) {
        } else {
            undoArray.add(edit.getText().toString());
        }
        undoArray.add(retriveAutoSaveText.getString("autosave", null));
    }

    private void retriveAutosaveText() {
        retriveAutoSaveText = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        edit.setText(retriveAutoSaveText.getString("autosave", null));
    }

    private void setDraftButtonText() {
        try {
            readTitle();
            readDate();
            if (names.isEmpty()) {
                draft.setText("Empty Draft");
            } else {
                draft.setText(names.get(0) + "\n" + dates.get(0));
            }
        } catch (Exception e) {
        }
    }

    private void createAlertDialogue() {
        builder = new AlertDialog.Builder(MainActivity.this);
        dialog = builder.create();
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
    }

    private void setFornt() {
        final Calligrapher font = new Calligrapher(this);
        font.setFont(this, "kalpurush.ttf", true);
    }

    private void checkAppUpdate() {
        HtmlParser back = new HtmlParser();
        back.execute();
    }

    private void initialize() {
        edit = findViewById(R.id.edit);
        names = new ArrayList<>();
        dates = new ArrayList<>();
        fileArray = new ArrayList<>();
        undoArray = new ArrayList<>();
        redoArray = new ArrayList<>();
        restoreArray = new ArrayList<>();
        m = getLayoutInflater().inflate(R.layout.listview, null);
        draftList = m.findViewById(R.id.ListView);
        draft = findViewById(R.id.spinner);
    }

    @Override
    public void onBackPressed() {
        boolean pressed = false;
        if (!pressed) {
            checkinternet = builder.create();
            checkinternet.setButton("Send mail", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "zubayer.developer@gmail.com"));
                    i.putExtra(Intent.EXTRA_SUBJECT, "Subject: problems");
                    i.putExtra(Intent.EXTRA_TEXT, "Write here your suggestion or problems");
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
            pressed = true;
        } else {
            super.onBackPressed();
        }
    }

    public boolean isInstalled(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void saveStatus() {
        try {
            fileArray.add(0, edit.getText().toString());
            FileOutputStream write = openFileOutput("status", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(fileArray);
            arrayoutput.close();
            write.close();
        } catch (Exception e) {
        }
    }

    public void readstatus() {
        try {
            FileInputStream read = openFileInput("status");
            ObjectInputStream readarray = new ObjectInputStream(read);
            fileArray = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        } catch (Exception e) {
        }
    }

    public void saveListHeading() {
        try {
            names.add(0, headingtrimer());
            FileOutputStream write = openFileOutput("title", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(names);
            arrayoutput.close();
            write.close();
        } catch (Exception e) {
        }
    }

    public void saveListDate() {
        try {
            long mydate = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE dd/MM/yyyy h:mm a");
            date = sdf.format(mydate);

            dates.add(0, date);
            FileOutputStream write = openFileOutput("date", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(dates);
            arrayoutput.close();
            write.close();
        } catch (Exception e) {
        }
    }

    public String headingtrimer() {
        String heading = "";
        String string = edit.getText().toString();
        if (string.length() < 26) {
            heading = string.substring(0, string.length());

        } else {
            heading = (string.substring(0, 26) + "...");

        }
        return (heading);
    }

    public void readTitle() {
        try {
            FileInputStream read = openFileInput("title");
            ObjectInputStream readarray = new ObjectInputStream(read);
            names = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        } catch (Exception e) {
        }
    }

    public void readDate() {
        try {
            FileInputStream read = openFileInput("date");
            ObjectInputStream readarray = new ObjectInputStream(read);
            dates = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        } catch (Exception e) {
        }
    }

    public void saveState() {
        try {
            FileOutputStream write = openFileOutput("title", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(names);
            arrayoutput.close();
            write.close();

        } catch (Exception e) {
        }
        try {
            FileOutputStream write = openFileOutput("date", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(dates);
            arrayoutput.close();
            write.close();
        } catch (Exception e) {
        }
        try {
            FileOutputStream write = openFileOutput("status", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(fileArray);
            arrayoutput.close();
            write.close();
        } catch (Exception e) {
        }
        if (names.isEmpty()) {
            draft.setText("Empty Draft");
        } else {
            draft.setText(names.get(0) + "\n" + dates.get(0));
        }
    }

    public void myToast(String toasttText) {
        Toast toast = makeText(MainActivity.this, toasttText, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void checkUpdate() {
        try {
            SharedPreferences prefs = getSharedPreferences("update", Context.MODE_PRIVATE);
            boolean b = prefs.getBoolean("yesno", false);
            updateMessage = prefs.getString("updateMessage", null);
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
        } catch (Exception e) {
        }
    }

    class HtmlParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("https://drzubayerahmed.wordpress.com/2017/11/29/26/?preview=true").get();
                Elements links = doc.select("p");
                Element link = links.get(1);
                Element message = links.get(3);
                parseVersionCode = link.text();
                updateMessage = message.text();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void b) {

            super.onPostExecute(b);
            if (parseVersionCode != null) {
                Integer parseint = Integer.parseInt(parseVersionCode);
                if (parseint > versionCode) {
                    SharedPreferences prefs = getSharedPreferences("update", Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("yesno", true).commit();
                    prefs.edit().putString("updateMessage", updateMessage).commit();
                    checkUpdate();
                } else if (parseint == versionCode) {
                    SharedPreferences prefs = getSharedPreferences("update", Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("yesno", false).commit();
                    myToast("Your app is uptodate");
                    checkUpdate();
                } else if (parseint < versionCode) {
                    SharedPreferences prefs = getSharedPreferences("update", Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("yesno", false).commit();
                    myToast("Your app is up to date");
                    checkUpdate();
                }
            } else {
                checkUpdate();
            }
        }
    }

}
