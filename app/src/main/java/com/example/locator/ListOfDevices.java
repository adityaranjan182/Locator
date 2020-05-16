package com.example.locator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListOfDevices extends AppCompatActivity {
    BluetoothAdapter mybluetoothadapter;
    Button scanbtn;
    ListView scanListView;
    List<String> listofdevices = new ArrayList<>();
    ArrayAdapter<String > myadapter;
    TextView device_list;
    Boolean flag = false;
    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    ProgressBar scanprogress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_devices);
        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        mybluetoothadapter = BluetoothAdapter.getDefaultAdapter();
        scanbtn = findViewById(R.id.scanbtn);
        scanListView = findViewById(R.id.scannedbtlist);
        device_list = findViewById(R.id.device_list);
        scanprogress = findViewById(R.id.scanprogress);
        checkbtpermission();
        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!flag){
                    flag = true;
                    searchdevices();
                }else{
                    listofdevices.clear();
                    myadapter.notifyDataSetChanged();
                    searchdevices();
                }
            }
        });
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myreciever,intentFilter);
            myadapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,listofdevices);
            scanListView.setAdapter(myadapter);
    }
    BroadcastReceiver myreciever  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                BluetoothDevice newdevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listofdevices.add(newdevice.getName());
                myadapter.notifyDataSetChanged();
                updatedatabase();

            }
        }
    };

    public void searchdevices(){
        scanprogress.setVisibility(View.VISIBLE);
        device_list.setVisibility(View.VISIBLE);
        mybluetoothadapter.startDiscovery();
    }

    public void updatedatabase(){
        scanprogress.setVisibility(View.INVISIBLE);
        DocumentReference docref = fstore.collection(fauth.getCurrentUser().getPhoneNumber()).document(MapsActivity.timeStamp);
        Map<String, List> devicesnearby = new HashMap<>();
        LatLng location = MapsActivity.updated;
        String location1 = location.toString();
        String [] arr = new String[listofdevices.size()];
        int i = 0;
        for (String device : listofdevices){
            arr[i] = listofdevices.get(i);
            i++;
        }
        List<String> devices = Arrays.asList(arr);
        devicesnearby.put(location1,devices);
        docref.set(devicesnearby);
        Toast.makeText(getApplicationContext(),"Successfully updated database",Toast.LENGTH_SHORT).show();
    }

    public void checkbtpermission(){
        if(mybluetoothadapter == null){
            Toast.makeText(getApplicationContext(),"Bluetooth is not present in your device",Toast.LENGTH_SHORT).show();
        }else{
            if(!mybluetoothadapter.isEnabled()){
                showBtDisabledAlert();
            }
        }
    }
    private void showBtDisabledAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Bluetooth is disabled .\nWould you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent enablebluetooth = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                                startActivity(enablebluetooth);
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create().show();
    }
}
