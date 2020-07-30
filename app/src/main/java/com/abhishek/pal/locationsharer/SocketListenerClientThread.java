package com.abhishek.pal.locationsharer;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;


/**
 * Created by User on 08-04-2017.
 */


public class SocketListenerClientThread implements Runnable {

    private MapsActivity rLocApp;
    private short mServerPortNum;
    private InetAddress mServerIpAddress;
    private DatagramSocket mClientSocket;

    public SocketListenerClientThread(MapsActivity locationSharingApp) throws UnknownHostException {
        // Keep the reference of main class so that it can be used
        rLocApp = locationSharingApp;
        mServerPortNum = 9790;
        //byte[] servIp = new byte[]{10,2,1,40};
        byte[] servIp = new byte[]{14, (byte)139, (byte)223, (byte)166};
        mServerIpAddress = InetAddress.getByAddress(servIp);

        // Create the client socket, don't specify any particular port to bind
        try {
            mClientSocket = new DatagramSocket();
            mClientSocket.setSoTimeout(rLocApp.mPeriodicTimeout);

            Log.d(String.valueOf(this),"how are you");
        } catch (Exception e) {
            Log.e("SLCT:Constructor", "Exception error", e);
        }
    }

    public void run() {

        Log.d(String.valueOf(this),"Hello am in run");
        // For recurring Use
        byte[] txByteStr = new byte[1400];
        byte[] rxByteStr = new byte[1400];
        int[] encodedLen = new int[1];
        int[] decodedLen = new int[1];

        byte sizeOfDouble=8;
        while (true) {
            // At first send the own location to server
            Log.d(String.valueOf(this),"Hello am in while of run");
            UserLocationInfo myLocationInfo = rLocApp.getMyLocationInfo();
            if (null == myLocationInfo.getLocation()) {
                // something wrong, it sould not come here then;
                Log.e("SLCT", "Own location not yet initialized, thread should not have created. Aborting");
                return;
            }

            try {
                // As of now serialize UserLocationInfo message and send
                if(false == myLocationInfo.serialize(txByteStr, 0, encodedLen)) {
                    // something wrong, it sould not come here then;
                    Log.e("SLCT", "Serialize failed. Aborting");
                    return;
                }

                Log.e(String.valueOf(this),"Sent Packet");
                DatagramPacket txPacket = new DatagramPacket(txByteStr, encodedLen[0], mServerIpAddress, mServerPortNum);
                mClientSocket.send(txPacket);
                Log.e(String.valueOf(this),"Sent Packet");
                // Create a packet so that server response can be stored
                DatagramPacket rxPacket = new DatagramPacket(rxByteStr, rxByteStr.length);
                // Wait for a Response from Server, it will timeout if not received
                mClientSocket.receive(rxPacket);

                if (rxPacket.getLength() > 0) {
//                    UserLocationInfo neighLocation = new UserLocationInfo();
//                    UserLocationInfo neighLocation2 = new UserLocationInfo();
                    UserLocationInfo[] neighLocation=new UserLocationInfo[10];
                    for (int z=0;z<10;z++)
                    {
                        neighLocation[z]=new UserLocationInfo();
                    }
                    int packlen=sizeOfDouble;
                    byte[] numberofusers=new byte[sizeOfDouble];
                    System.arraycopy(rxByteStr,0,numberofusers,0,sizeOfDouble);
                    double no_of_users = ByteBuffer.wrap(numberofusers).getDouble();
                    //int no_of_users=Integer.parseInt((numberofusers.toString()));
                    rLocApp.count=(int)no_of_users;
                    Log.d(String.valueOf(this), String.valueOf(rLocApp.count));
                    for(int k=0;k<rLocApp.count;k++) {
                        if (false == neighLocation[k].deserialize(rxByteStr, packlen, decodedLen)) {
                            Log.e("SLCT", "Deserialize failed. Aborting");
                            return;
                        }
//                        if (false == neighLocation2.deserialize(rxByteStr, 32, decodedLen)) {
//                            Log.e("SLCT", "Deserialize failed. Aborting");
//                            return;
//                        }
                        packlen =packlen+32;
                        Log.d(String.valueOf(this), String.valueOf(packlen));
                    }
                        //if(neighLocation[k].mUserId.equals(null))
                        rLocApp.setNeighbourLocation(neighLocation);
                    //}
                    // Lets sleep for sometime
                    try {
                        Thread.sleep(rLocApp.mPeriodicTimeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (UnsupportedEncodingException e) {
                Log.v("SLCT:run", "UnsupportedEncodingException : ", e);
            } catch (SocketTimeoutException e) {
                Log.v("SLCT:run", "SocketTimeoutException : ", e);
            } catch (IOException e) {
                Log.v("SLCT:run", "IOException : ", e);
            }
        } // End of While
    }
}
