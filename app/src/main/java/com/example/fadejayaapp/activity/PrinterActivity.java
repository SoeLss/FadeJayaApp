package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.example.fadejayaapp.R;
import java.util.ArrayList;
import java.util.Set;

public class PrinterActivity extends AppCompatActivity {

    private ListView lvDevices;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> deviceList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer); // Layout isi ListView saja

        lvDevices = findViewById(R.id.lvDevices);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new ArrayList<>();

        loadPairedDevices();

        // Saat device dipilih
        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            String info = deviceList.get(position);
            // Format Info biasanya: "Nama Printer\n00:11:22:33:44:55"
            String address = info.substring(info.length() - 17);
            String name = info.substring(0, info.length() - 17).trim();

            savePrinter(name, address);
        });
    }

    private void loadPairedDevices() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth tidak didukung", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Mohon hidupkan Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ambil device yang sudah PAIRING di HP
        // Note: Untuk Android 12+ butuh Runtime Permission check (BLUETOOTH_CONNECT)
        try {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    deviceList.add(device.getName() + "\n" + device.getAddress());
                }
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
                lvDevices.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Tidak ada printer yang terhubung (Pairing dulu di Setting HP)", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Butuh Izin Bluetooth: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePrinter(String name, String address) {
        // Simpan ke SharedPreferences khusus Printer
        SharedPreferences pref = getSharedPreferences("PrinterConfig", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("printer_name", name);
        editor.putString("printer_address", address);
        editor.apply();

        Toast.makeText(this, "Printer Disimpan: " + name, Toast.LENGTH_SHORT).show();
        finish();
    }
}