package com.henneth.rtsBibNumber;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.henneth.rtsBibNumber.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import helper.SessionManager;

public class  MainActivity extends AppCompatActivity implements postToServer.AsyncResponse, postToServerRTS.AsyncResponse, RadioGroup.OnCheckedChangeListener {

    public static String DEVICE_ADDRESS = "device_address";
    private static String mConnectedDeviceName = null;
    private static String deviceName;
    private static String ckpt_name;
    private static boolean isConnected;

    private SessionManager session;

    // variables
    public String teamMemberLetterStr = "";
    public String checkpoint_name = null;
    public String teamModeOrNot = "Solo";

    // For referencing this activity
    static Activity thisActivity = null;

    // Tag List
    private ArrayAdapter<String> tagAdapter;
    private ArrayAdapter<String> shadowAdapter;
    ListView tagListView;

    // Tracking tag statuses
    ArrayList<String> list = new ArrayList<String>();
    ArrayList<TagRecord> incompleteTagRecord = new ArrayList<TagRecord>();
    ArrayList<TagRecord> fullList = new ArrayList<TagRecord>();
    ArrayList<String> doneList = new ArrayList<>();
    int numOfTags;
    ArrayList<TagRecord> tagTimeList = new ArrayList<TagRecord>();

    // ConnectivityBroadcastReceiver
    BroadcastReceiver receiver;

    // Form controls
    public EditText inputRaceNumber;
    public Button btnSend;
    SegmentedRadioGroup teamMemberLetter;
    private ListView epclist;
    private Menu menu;

    private CustomListAdapter seqAdapter;
    private ArrayList<seqTag> seqArray = new ArrayList<seqTag>();
    private ArrayList<String> tagArray = new ArrayList<String>();
    TextWatcher tw = null;
//    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


//    private void requestForRecordAudioPermission() {
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
//    }
//    private boolean checkIfAlreadyhavePermission() {
//        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
//        if (result == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("onCreate", "onCreate");

//        int MyVersion = Build.VERSION.SDK_INT;
//        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
//            if (!checkIfAlreadyhavePermission()) {
//                requestForRecordAudioPermission();
//                return;
//            }
//        }

//        // session manager
//        session = new SessionManager(getApplicationContext());
//
//        if (!session.isLoggedIn()) {
//            logoutUser();
//        }

        setContentView(R.layout.activity_get_rfid);
        thisActivity = this;

        // checkpoint name
        checkpoint_name =  PreferenceManager.getDefaultSharedPreferences(this).getString("checkpoint_name", null);
        if (checkpoint_name == null || checkpoint_name.isEmpty()) {
            showAlertDialog();
        }

        // inputs
        inputRaceNumber = (EditText) findViewById(R.id.race_num);
        tw = new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // Limit max length of epc
//                String string = inputRaceNumber.getText().toString();
//                String s_num = string.replaceAll("[^0-9]", "");
//                String s_letter = string.replaceAll("[^A-Za-z]", "");
//                if (s_num.length() > 4) {
//                    s_num = s_num.substring(0,3);
//                }
//                inputRaceNumber.setText("fwe");
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                // Clear checked button in button group
                if (teamMemberLetter.getCheckedRadioButtonId() != -1) {
                    teamMemberLetter.clearCheck();
                }

                // Limit max length of epc
                int pos = inputRaceNumber.getSelectionStart();
                String s_num = s.toString().replaceAll("[^0-9]", "");
                String s_letter = s.toString().replaceAll("[^A-Za-z]", "");
                if (!teamModeOrNot.equals("Solo")) {
                    if (s.toString().replaceAll("[^0-9]", "").length() > 3) {
                        s_num = s_num.substring(0, 3);
                    }
                }
                if (s.toString().replaceAll("[^0-9]", "").length() > 4) {
                    s_num = s_num.substring(0, 4);
                }
                if (!s.toString().equals(s_num + s_letter)) {
                    inputRaceNumber.removeTextChangedListener(tw);
                    inputRaceNumber.setText(s_num + s_letter);
                    inputRaceNumber.setSelection(pos > (s_num + s_letter).length() ? (s_num + s_letter).length() : pos);
                    inputRaceNumber.addTextChangedListener(tw);
                }
            }
        };
        inputRaceNumber.addTextChangedListener(tw);

        // team member letter
        teamMemberLetter = (SegmentedRadioGroup) findViewById(R.id.team_member_letter);
        teamMemberLetter.setOnCheckedChangeListener(this);

        teamModeOrNot =  PreferenceManager.getDefaultSharedPreferences(this).getString("team_mode", "Solo");
        Log.d("teamModeOrNot", "teamModeOrNot: " + teamModeOrNot);
        if (!teamModeOrNot.equals("Solo")) {
            teamMemberLetter.setVisibility(View.VISIBLE);
        } else {
            teamMemberLetter.setVisibility(View.GONE);
        }

        // send button
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // checking
                if (checkpoint_name == null || checkpoint_name.isEmpty()) {
                    toast("Please set the checkpoint ID.");
                    showAlertDialog();
                    return;
                }
                if (inputRaceNumber.getText().toString().isEmpty()) {
                    toast("Please enter a bib number.");
                    return;
                }

                int a_index = inputRaceNumber.getText().toString().indexOf("A");
                int b_index = inputRaceNumber.getText().toString().indexOf("B");
                int c_index = inputRaceNumber.getText().toString().indexOf("C");
                int d_index = inputRaceNumber.getText().toString().indexOf("D");
                if (teamModeOrNot.equals("Team") && a_index == -1 && b_index == -1 && c_index == -1 && d_index == -1) {
                    toast("Please choose the letter for this team member.");
                    return;
                }

                // make string and send
                String chip_str = inputRaceNumber.getText().toString();
                inputRaceNumber.setText("");
                ListRefresh(chip_str);
            }
        });

        // lists
        epclist = (ListView) findViewById(R.id.tagListView);
//        epclist.setCacheColorHint(Color.TRANSPARENT);
//        epclist.setOnItemClickListener(new epclistItemClick());
        seqAdapter = new CustomListAdapter(this, R.layout.tag_list, this.seqArray);
        epclist.setAdapter(this.seqAdapter);

        tagListView = (ListView) findViewById(R.id.tagListView);
        shadowAdapter = new ArrayAdapter<String>(this, R.layout.tag_list);
        tagAdapter = new CustomAdapter(thisActivity, R.layout.tag_list, list);
        tagListView.setAdapter(tagAdapter);

        try {
            String filename = "shadowAdapter.txt";
            File sdcardPath = Environment.getExternalStorageDirectory();
            File FilePath = new File(sdcardPath, filename);

            Log.d("msgStr", FilePath.toString());

            FileInputStream fis = new FileInputStream(FilePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<String> returnlist = (ArrayList<String>) ois.readObject();
            ois.close();

            for (String sEpc : returnlist){
                shadowAdapter.add(sEpc);
            }
        } catch (Exception e) {
            Log.d("msgStr", "Fail to get previous shadowAdapter data.");
        }

        try {
            String filename = "fullList.txt";
            File sdcardPath = Environment.getExternalStorageDirectory();
            File FilePath = new File(sdcardPath, filename);

            Log.d("msgStr", FilePath.toString());

            FileInputStream fis = new FileInputStream(FilePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<TagRecord> returnlist = (ArrayList<TagRecord>) ois.readObject();
            ois.close();
            Log.d("fullList", "fullList: " + returnlist);
            TagRecord testTagRecord = returnlist.get(0);
            for (TagRecord t : returnlist){
                fullList.add(t);
            }
        } catch (Exception e) {
            Log.d("msgStr", "Fail to get previous fullList data.");
        }

        try {
            String filename = "tagRecord.txt";
            File sdcardPath = Environment.getExternalStorageDirectory();
            File FilePath = new File(sdcardPath, filename);

            FileInputStream fis = new FileInputStream(FilePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<String> returnlist = (ArrayList<String>) ois.readObject();
            ois.close();

            numOfTags = returnlist.size();
            for (String tag : returnlist){
                tagAdapter.add(tag);
            }

            for (int i = 0; i < numOfTags; i++){
                doneList.add(String.valueOf(i));
            }
        } catch (Exception e) {
            Log.d("msgStr", "Fail to get previous tagAdapter data.");
        }

        try {
            String filename = "incompleteTagRecord.txt";
            File sdcardPath = Environment.getExternalStorageDirectory();
            File FilePath = new File(sdcardPath, filename);

            FileInputStream fis = new FileInputStream(FilePath.toString());
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<TagRecord> returnlist = (ArrayList<TagRecord>) ois.readObject();
            ois.close();

            for (TagRecord t : returnlist){
                incompleteTagRecord.add(t);
                doneList.remove(t.position);
            }

            ((CustomAdapter)tagAdapter).setWholeDoneList(doneList);
            ((CustomAdapter)tagListView.getAdapter()).notifyDataSetChanged();

        } catch (Exception e) {
            Log.d("msgStr", "Fail to get previous incompleteTagRecord data.");
        }
        registerConnectivityBroadcastReceiver();
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.overflow_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        logoutUser();
        switch (item.getItemId()) {
            case R.id.set_checkpoint_id:
                showAlertDialog();
                return true;
            case R.id.settings:
                Intent intent = new Intent();
                intent.setClassName(this, "com.henneth.rtsBibNumber.LoginActivity");
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        Log.d("onStart", "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        teamModeOrNot =  PreferenceManager.getDefaultSharedPreferences(this).getString("team_mode", "Solo");
        Log.d("teamModeOrNot", "teamModeOrNot: " + teamModeOrNot);
        if (!teamModeOrNot.equals("Solo")) {
            teamMemberLetter.setVisibility(View.VISIBLE);
        } else {
            teamMemberLetter.setVisibility(View.GONE);
        }
    }

    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set checkpoint ID");
        builder.setMessage("Please enter the checkpoint ID");

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;

        final EditText inputCheckpointName = new EditText(this);
        inputCheckpointName.setInputType(InputType.TYPE_CLASS_TEXT);
        inputCheckpointName.setLayoutParams(layoutParams);
        if (checkpoint_name != null && !checkpoint_name.isEmpty()) {
            inputCheckpointName.setText(checkpoint_name);
        }

        linearLayout.addView(inputCheckpointName);
        linearLayout.setPadding(60, 0, 60, 0);

        builder.setView(linearLayout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkpoint_name = inputCheckpointName.getText().toString();

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(thisActivity);
                pref.edit().putString("checkpoint_name", checkpoint_name).commit();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private boolean checkIfShort(String sEpc) {
        boolean tooshort = false;
        String time = getTime();
        for (TagRecord t : fullList) {
            if (t.sEpc.equals(sEpc)) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date pastTime = null;
                try {
                    pastTime = format.parse(t.time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date currentTime = null;
                try {
                    currentTime = format.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if ((currentTime.getTime() - pastTime.getTime()) <= 900000) {
                    Log.d("time interval", "time interval: " + (currentTime.getTime() - pastTime.getTime()));
                    Log.d("too short", "past: " + pastTime + "now: " + currentTime);
                    tooshort = true;
                } else {
                    Log.d("time interval", "time interval: " + (currentTime.getTime() - pastTime.getTime()));
                    Log.d("not short", "past: " + pastTime + "now: " + currentTime);
                    tooshort = false;
                    t.time = time;
                }
            } else {
                Log.d("not same", "not same");

            }
        }
        return tooshort;
    }
    private boolean checkPostion(String sEpc) {
        int latestPosition = -1;
        for(int i=0 ; i<shadowAdapter.getCount() ; i++){
            Object obj = shadowAdapter.getItem(i);
            if (String.valueOf(obj).equals(sEpc)){
                latestPosition = i;
            }
        }
        return (shadowAdapter.getPosition(sEpc) < 0 || shadowAdapter.getCount() - latestPosition > 5);
    }
    private void ListRefresh(String sEpc) {

//        if(shadowAdapter.getPosition(sEpc) > 0 && shadowAdapter.getCount() - shadowAdapter.getPosition(sEpc) < 10){
//            Toast.makeText(thisActivity, sEpc+ " has been scanned recently", Toast.LENGTH_SHORT).show();
//        }
        boolean limitTimeOrNot =  PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_15_mins", false);
        Log.d("limitTimeOrNot", "limitTimeOrNot: " + limitTimeOrNot);
        if (limitTimeOrNot == true) {
            if (checkIfShort(sEpc)) {
                return;
            }
        }
        boolean alternationLimitOrNot =  PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_10_chips", false);
        Log.d("alternationLimitOrNot", "alternationLimitOrNot: " + alternationLimitOrNot);
        if (alternationLimitOrNot == true) {
            if (!checkPostion(sEpc)) {
                return;
            }
        }

//        int latestPosition = -1;
//        for(int i=0 ; i<shadowAdapter.getCount() ; i++){
//            Object obj = shadowAdapter.getItem(i);
//            Log.d("obj", "obj: " + obj);
//            if (String.valueOf(obj).equals(sEpc)){
//                latestPosition = i;
//            }
//        }

//        Log.d("sEpc", String.valueOf(shadowAdapter.getCount() - shadowAdapter.getPosition(sEpc))); not include self correct
//        if(shadowAdapter.getPosition(sEpc) < 0 || shadowAdapter.getCount() - latestPosition > 10){
//        if(shadowAdapter.getPosition(sEpc) < 0){
            try{
                //Toast.makeText(CreateRecord.this,sEpc + "|-" +tagAdapter.getCount() , Toast.LENGTH_SHORT).show();

                // save Epc for future checking
                shadowAdapter.add(sEpc);
                for(int i=0 ; i<shadowAdapter.getCount() ; i++){
                    Object obj = shadowAdapter.getItem(i);
                    Log.d("shadowAdapter", String.valueOf(obj));
                }
                Log.d("shadowAdapter", "EndLoop");

                // make a backup copy of shadowAdapter
                ArrayList<String> shadowAdapterList = new ArrayList<String>();
                for (int i = 0; i < shadowAdapter.getCount(); i++)
                    shadowAdapterList.add(shadowAdapter.getItem(i));
                writeToFile("shadowAdapter.txt", shadowAdapterList);

                // get current time
                String time = getTime();

                // add information to list
                tagAdapter.insert(sEpc + "\n" + time, 0);
                // make a backup copy of tagAdapter
                ArrayList<String> tagAdapterList = new ArrayList<String>();
                for (int i = 0; i < tagAdapter.getCount(); i++)
                    tagAdapterList.add(tagAdapter.getItem(i));
                writeToFile("tagRecord.txt", tagAdapterList);

                // get position
                String position = String.valueOf(tagAdapter.getCount()-1);

                // get device ID
                String android_id = Settings.Secure.getString(thisActivity.getContentResolver(), Settings.Secure.ANDROID_ID);

                // get value from settings
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String ckpt_name = checkpoint_name;
                String server = prefs.getString("pref_server", "empty");

                Log.d("server", server);
                Log.d("server", ckpt_name);

                // save for resend
                TagRecord tagRecord = new TagRecord(sEpc, time, ckpt_name, android_id, deviceName, position);
                incompleteTagRecord.add(tagRecord);
                writeToFile("incompleteTagRecord.txt", incompleteTagRecord);
                fullList.add(tagRecord);
                writeToFile("fullList.txt", fullList);

                // post to server
                if (server.equals("RTS")) {
                    new postToServerRTS(this).execute("http://m.racetimingsolutions.com/rfid-gun/v2", sEpc, time, android_id, ckpt_name, position);
                } else if (server.equals("Sports Timing")) {
                    new postToServer(this).execute("http://livetime.sportstiming.dk/LiveTimeService.asmx", sEpc, time, ckpt_name, deviceName, position);
                } else {
                    toast("Please set the destination server.");
                }
            } catch(Exception ex) {
                Toast.makeText(thisActivity,ex.toString()+ "|-" +tagAdapter.getCount() , Toast.LENGTH_SHORT).show();
            }
    }
    public String getTime() {
//        Calendar c = Calendar.getInstance();
//        int second = c.get(Calendar.SECOND);
//        int minute = c.get(Calendar.MINUTE);
//        int hour = c.get(Calendar.HOUR_OF_DAY);
//        return hour + ":" + minute + ":" + second;
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }
    private void writeToFile(String name, ArrayList list) {
        try {
            File sdcardPath = Environment.getExternalStorageDirectory();
            File FilePath = new File(sdcardPath, name);
            Log.d("msgNew", FilePath.toString());
//            FileOutputStream fos = new FileOutputStream(sdcardPath.toString() + "/" + name);
            FileOutputStream fos = new FileOutputStream(FilePath);
//            FileOutputStream fos = thisActivity.openFileOutput(name, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(list);
            oos.close();
            Log.d("msgNew", "Saved to file.");
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    public void registerConnectivityBroadcastReceiver(){
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new ConnectivityBroadcastReceiver();
        this.registerReceiver(receiver, filter);
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager
//                    .EXTRA_NO_CONNECTIVITY, false);
//            NetworkInfo info1 = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager
//                    .EXTRA_NETWORK_INFO);
//            NetworkInfo info2 = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager
//                    .EXTRA_OTHER_NETWORK_INFO);
            ConnectivityManager cm =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (isConnected) {
                sendOneIncompleteTag();
            }
            Log.d("IsConnected", String.valueOf(isConnected));
        }
    }

    /**
     * Toast
     */
    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void processFinish(String position) {
        for (TagRecord t : incompleteTagRecord) {
            if (t.position.equals(position)) {
                incompleteTagRecord.remove(t);
                break;
            }
        }
        writeToFile("incompleteTagRecord.txt", incompleteTagRecord);

        if (!incompleteTagRecord.isEmpty()) {
            TagRecord t = incompleteTagRecord.get(0);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String ckpt_name = prefs.getString("pref_ckpt_name", "empty");
            String server = prefs.getString("pref_server", "empty");

            // post to server
            if (server.equals("RTS")) {
                new postToServerRTS(this).execute("http://m.racetimingsolutions.com/rfid-gun/v2", t.sEpc, t.time, t.android_id, ckpt_name, t.position);
            } else if (server.equals("Sports Timing")) {
                new postToServer(this).execute("http://livetime.sportstiming.dk/LiveTimeService.asmx", t.sEpc, t.time, ckpt_name, t.deviceName, t.position);
            } else {
                toast("Please set the destination server.");
            }
//            new postToServer(this).execute("http://m.racetimingsolutions.com/rfid-gun", t.sEpc, t.time, t.android_id, t.deviceName, t.position);
//            new postToServer(this).execute("http://livetime.sportstiming.dk/LiveTimeService.asmx", t.sEpc, t.time, ckpt_name, t.deviceName, t.position);
        }
        ((CustomAdapter)tagAdapter).setDoneList(position);
        ((CustomAdapter)tagListView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void throwNoNetworkToast() {
        Toast.makeText(thisActivity, "No Network Connection." , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void throwAuthenticationFailedToast() {
        Toast.makeText(thisActivity, "Authentication Failed." , Toast.LENGTH_SHORT).show();
    }

    public void sendOneIncompleteTag() {
        // get value from settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String ckpt_name = prefs.getString("pref_ckpt_name", "empty");
        String server = prefs.getString("pref_server", "empty");

        if (!incompleteTagRecord.isEmpty()) {
            TagRecord t = incompleteTagRecord.get(0);

            // post to server
            if (server.equals("RTS")) {
                new postToServerRTS(this).execute("http://m.racetimingsolutions.com/rfid-gun/v2", t.sEpc, t.time, t.android_id, ckpt_name, t.position);
            } else if (server.equals("Sports Timing")) {
                new postToServer(this).execute("http://livetime.sportstiming.dk/LiveTimeService.asmx", t.sEpc, t.time, ckpt_name, t.deviceName, t.position);
            } else {
                toast("Please set the destination server.");
            }
//            new postToServer(this).execute("http://m.racetimingsolutions.com/rfid-gun", t.sEpc, t.time, t.android_id, t.deviceName, t.position);
//            new postToServer(this).execute("http://livetime.sportstiming.dk/LiveTimeService.asmx", t.sEpc, t.time, ckpt_name, t.deviceName, t.position);
        }
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == teamMemberLetter) {
            if (checkedId == R.id.A) {
                teamMemberLetterStr = "A";
            } else if (checkedId == R.id.B) {
                teamMemberLetterStr = "B";
            } else if (checkedId == R.id.C) {
                teamMemberLetterStr = "C";
            } else if (checkedId == R.id.D) {
                teamMemberLetterStr = "D";
            }

            inputRaceNumber.setText(inputRaceNumber.getText().toString().replaceAll("[ABCD]", "") + teamMemberLetterStr);
            inputRaceNumber.setSelection(inputRaceNumber.length());
        }
    }

//    /**
//     * Logout User
//     */
//    private void logoutUser()
//    {
//        session.setLogin(false);
//
//        // Launching the login activity
//        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//        startActivity(intent);
//        finish();
//    }

    @Override
    public void onStop() {
        Log.d("onStop","onStop");
        super.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy","onDestroy");
        this.unregisterReceiver(receiver);
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}

