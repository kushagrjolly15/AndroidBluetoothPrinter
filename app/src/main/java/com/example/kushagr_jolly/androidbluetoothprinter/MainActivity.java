package com.example.kushagr_jolly.androidbluetoothprinter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import com.example.kushagr_jolly.androidbluetoothprinter.util.DateUtil;
import com.example.kushagr_jolly.androidbluetoothprinter.util.FontDefine;
import com.example.kushagr_jolly.androidbluetoothprinter.util.Printer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    // will show the statuses like bluetooth open, close or data sent
    TextView myLabel;

    // will enable user to enter any text to be printed

    // android built in classes for bluetooth operations
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    // needed for communication to bluetooth device / network
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// we are going to have three buttons for specific functions
        Button openButton = (Button) findViewById(R.id.open);
        Button sendButton = (Button) findViewById(R.id.send);
        Button closeButton = (Button) findViewById(R.id.close);

// text label and input box
        myLabel = (TextView) findViewById(R.id.label);
        // open bluetooth connection
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    findBT();
                    openBT();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        // send data typed by the user to be printed
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    sendData();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        // close bluetooth connection
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    // close the connection to bluetooth printer.
    void closeBT() throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            myLabel.setText("Bluetooth Closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // this will send text data to be printed by the bluetooth printer
    void sendData() throws IOException{
        try {

            // the text typed by the user
                String titleStr	= "STRUK PEMBAYARAN TAGIHAN LISTRIK" + "\n\n";

                StringBuilder contentSb	= new StringBuilder();

                contentSb.append("IDPEL     : 435353535435353" + "\n");
                contentSb.append("NAMA      : LORENSIUS WLT" + "\n");
                contentSb.append("TRF/DAYA  : 50/12244 VA" + "\n");
                contentSb.append("BL/TH     : 02/14" + "\n");
                contentSb.append("ST/MTR    : 0293232" + "\n");
                contentSb.append("RP TAG    : Rp. 100.000" + "\n");
                contentSb.append("JPA REF   :" + "\n");

                StringBuilder content2Sb = new StringBuilder();

                content2Sb.append("ADM BANK  : Rp. 1.600" + "\n");
                content2Sb.append("RP BAYAR  : Rp. 101.600,00" + "\n");

                String jpaRef	= "XXXX-XXXX-XXXX-XXXX" + "\n";
                String message	= "PLN menyatakan struk ini sebagai bukti pembayaran yang sah." + "\n";
                String message2	= "Rincian tagihan dapat diakses di www.pln.co.id Informasi Hubungi Call Center: "
                        + "123 Atau Hub PLN Terdekat: 444" + "\n";

                long milis		= System.currentTimeMillis();
                String date		= DateUtil.timeMilisToString(milis, "dd-MM-yy / HH:mm")  + "\n\n";

                byte[] titleByte	= Printer.printfont(titleStr, FontDefine.FONT_24PX, FontDefine.Align_CENTER,
                        (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

                byte[] content1Byte	= Printer.printfont(contentSb.toString(), FontDefine.FONT_24PX,FontDefine.Align_LEFT,
                        (byte)0x1A, PocketPos.LANGUAGE_ENGLISH);

                byte[] refByte		= Printer.printfont(jpaRef, FontDefine.FONT_24PX,FontDefine.Align_CENTER,  (byte)0x1A,
                        PocketPos.LANGUAGE_ENGLISH);

                byte[] messageByte	= Printer.printfont(message, FontDefine.FONT_24PX,FontDefine.Align_CENTER,  (byte)0x1A,
                        PocketPos.LANGUAGE_ENGLISH);

                byte[] content2Byte	= Printer.printfont(content2Sb.toString(), FontDefine.FONT_24PX,FontDefine.Align_LEFT,
                        (byte)0x1A, PocketPos.LANGUAGE_ENGLISH);

                byte[] message2Byte	= Printer.printfont(message2, FontDefine.FONT_24PX,FontDefine.Align_CENTER,  (byte)0x1A,
                        PocketPos.LANGUAGE_ENGLISH);

                byte[] dateByte		= Printer.printfont(date, FontDefine.FONT_24PX,FontDefine.Align_LEFT, (byte)0x1A,
                        PocketPos.LANGUAGE_ENGLISH);

                byte[] totalByte	= new byte[titleByte.length + content1Byte.length + refByte.length + messageByte.length +
                        content2Byte.length + message2Byte.length + dateByte.length];


                int offset = 0;
                System.arraycopy(titleByte, 0, totalByte, offset, titleByte.length);
                offset += titleByte.length;

                System.arraycopy(content1Byte, 0, totalByte, offset, content1Byte.length);
                offset += content1Byte.length;

                System.arraycopy(refByte, 0, totalByte, offset, refByte.length);
                offset += refByte.length;

                System.arraycopy(messageByte, 0, totalByte, offset, messageByte.length);
                offset += messageByte.length;

                System.arraycopy(content2Byte, 0, totalByte, offset, content2Byte.length);
                offset += content2Byte.length;

                System.arraycopy(message2Byte, 0, totalByte, offset, message2Byte.length);
                offset += message2Byte.length;

                System.arraycopy(dateByte, 0, totalByte, offset, dateByte.length);

                byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);

            mmOutputStream.write(senddata);

            // tell the user data were sent
            myLabel.setText("Data sent.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null) {
                myLabel.setText("No bluetooth adapter available");
            }

            if(!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    if (device.getName().equals("silbt-010")) {
                        mmDevice = device;
                        break;
                    }
                }
            }

            myLabel.setText("Bluetooth device found.");

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    // tries to open a connection to the bluetooth printer device
    void openBT() throws IOException {
        try {

            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            myLabel.setText("Bluetooth Opened");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
 * after opening a connection to bluetooth printer device,
 * we have to listen and check if a data were sent to be printed.
 */
    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myLabel.setText(data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
