package com.example.cnandezv.touchgamebt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    Handler bluetoothIn;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;


    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    int count =0;
    int count2 =0;
    int count3 =0;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ++count;
            TextView mytext = (TextView) findViewById(R.id.mytext);
            mytext.setText(Integer.toString(count));
        }
    };

    Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ++count2;
            TextView mytext2 = (TextView) findViewById(R.id.mytext2);
            mytext2.setText(Integer.toString(count2));
        }
    };

    Handler handler3 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ++count3;
            TextView mytext3 = (TextView) findViewById(R.id.mytext3);
            mytext3.setText(Integer.toString(count3));
        }
    };

    Handler Ocultar = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Button pl1 = (Button) findViewById(R.id.player1);
            Button pl2 = (Button) findViewById(R.id.player2);
            Button pl3 = (Button) findViewById(R.id.player3);
            pl1.setVisibility(View.GONE);
            pl2.setVisibility(View.GONE);
            pl3.setVisibility(View.GONE);
            if (count >= count2) {
                if (count >= count3) {

                    TextView mytext3 = (TextView) findViewById(R.id.mytext3);
                    mytext3.setText("El ganador es el jugador 1");
                }

            } else {
                if (count2 > count3) {
                    TextView mytext3 = (TextView) findViewById(R.id.mytext3);
                    mytext3.setText("El ganador es el jugador 2");
                } else {
                    TextView mytext3 = (TextView) findViewById(R.id.mytext3);
                    mytext3.setText("El ganador es el jugador 3");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume(){
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        //Log.i("teamDolphin", "address : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }

        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }


    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    public void Click1(View view)
    {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                long futureTime = System.currentTimeMillis()+1;
                while (System.currentTimeMillis() < futureTime)
                {
                    synchronized (this){
                        try{
                            wait(futureTime - System.currentTimeMillis());
                        } catch(Exception e){

                        }
                    }
                }
                handler.sendEmptyMessage(0);
            }
        };
        Thread mythread = new Thread(r);
        mythread.start();
    }

    public void Click2(View view2)
    {
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                long futureTime = System.currentTimeMillis()+1;
                while (System.currentTimeMillis() < futureTime)
                {
                    synchronized (this){
                        try{
                            wait(futureTime - System.currentTimeMillis());
                        } catch(Exception e){

                        }
                    }
                }
                handler2.sendEmptyMessage(0);
            }
        };
        Thread mythread2 = new Thread(r2);
        mythread2.start();
    }
    public void Click3(View view3)
    {
        Runnable r3 = new Runnable() {
            @Override
            public void run() {
                long futureTime = System.currentTimeMillis()+1;
                while (System.currentTimeMillis() < futureTime)
                {
                    synchronized (this){
                        try{
                            wait(futureTime - System.currentTimeMillis());
                        } catch(Exception e){

                        }
                    }
                }
                handler3.sendEmptyMessage(0);
            }
        };
        Thread mythread3 = new Thread(r3);
        mythread3.start();
    }

    public void Winner(View win){
        Button pl1 = (Button) findViewById(R.id.player1);
        Button pl2 = (Button) findViewById(R.id.player2);
        Button pl3 = (Button) findViewById(R.id.player3);
        pl1.setVisibility(View.VISIBLE);
        pl2.setVisibility(View.VISIBLE);
        pl3.setVisibility(View.VISIBLE);
        Runnable w = new Runnable() {
            @Override
            public void run() {
                long futureTime = System.currentTimeMillis()+10000;
                while (System.currentTimeMillis() < futureTime)
                {
                    synchronized (this){
                        try{
                            wait(futureTime - System.currentTimeMillis());
                        } catch(Exception e){

                        }
                    }
                }
                Ocultar.sendEmptyMessage(0);
            }
        };
        Thread mythread = new Thread(w);
        mythread.start();

    }
}
