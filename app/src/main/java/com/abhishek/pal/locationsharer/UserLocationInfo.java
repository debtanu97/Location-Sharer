package com.abhishek.pal.locationsharer;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by Abhishek Pal on 08-04-2017.
 */

class UserLocationInfo {

    // Network Protocol : [8 bytes] user id + 8 bytes longitude + 8 bytes lattitude
    public static final int MAX_PACKET_LEN = 32;
    public static final int MAX_USERID_LEN = 16;

    public String mUserId; // Pack this field
    public LatLng mLocation; // Pack this field
    public Marker mMarker;

    public UserLocationInfo() {}

    public UserLocationInfo(String userId){
        mUserId = new String(userId);
    }

    public String getUserId() {
        return mUserId;
    }

    public LatLng getLocation() {
        return mLocation;
    }

    public void setLocation(double latitude, double longitude) {
        mLocation = new LatLng(latitude, longitude);
    }

    public Marker getMarker() {
        return mMarker;
    }
    public void setMarker(Marker marker) {
        mMarker = marker;
    }
    public boolean pack(String str) {
        return false;
    }

    public boolean unpack(String str) {
        return false;
    }

    public boolean serialize(byte[] encodeByteStr, int writeOffset, int[] numBytesEncoded)
            throws UnsupportedEncodingException {
        byte sizeOfDouble = 8;
        numBytesEncoded[0] = 0;

        // Check if input byteStr is sufficient to pack the message
        if(encodeByteStr.length - writeOffset < MAX_PACKET_LEN){
            return false;
        }

        // Check if user id ever populated
        int userIdLen = mUserId.length();
        if (0 == userIdLen) {
            return false;
        }

        // If userId is bigger than expected, consider only first MAX_USERID_LEN bytes
        if (userIdLen > MAX_USERID_LEN) {
            userIdLen = MAX_USERID_LEN;
        }

        for(int i = userIdLen; i <MAX_USERID_LEN; i++) {
            mUserId.concat("0");
        }
        // Pack userId now
        int offset = writeOffset;
        byte[] userByte = mUserId.getBytes("UTF-8");

        System.arraycopy(userByte, 0, encodeByteStr, offset, userIdLen);

        offset = offset + MAX_USERID_LEN;

        byte[] byteStrForDouble = new byte[sizeOfDouble];
        // Pack lattitude now
        ByteBuffer.wrap(byteStrForDouble).putDouble(mLocation.latitude);
        System.arraycopy(byteStrForDouble, 0, encodeByteStr, offset, sizeOfDouble);
        offset = offset + sizeOfDouble;

        // Pack longitude now
        ByteBuffer.wrap(byteStrForDouble).putDouble(mLocation.longitude);
        System.arraycopy(byteStrForDouble, 0, encodeByteStr, offset, 8);
        offset = offset + sizeOfDouble;

        // safety check once again
        if(offset != writeOffset+MAX_PACKET_LEN) {
            return false;
        }

        // Everything is perfect
        numBytesEncoded[0] = MAX_PACKET_LEN;

        return true;
    }

    public boolean deserialize(byte[] decodeByteStr, int readOffset, int[] numBytesDecoded)
            throws UnsupportedEncodingException  {
        byte sizeOfDouble = 8;
        numBytesDecoded[0] = 0;

        // Check if input byteStr is too small, then its wrong input str
        if(decodeByteStr.length - readOffset < MAX_PACKET_LEN) {
            return false;
        }

        // Create space for user id of expected length
        byte[] userByte = new byte[MAX_USERID_LEN];

        //unpack userId
        int offset = readOffset;
        System.arraycopy(decodeByteStr, offset, userByte, 0, MAX_USERID_LEN);
        mUserId = new String(userByte, "UTF-8");
        Log.d(String.valueOf(this),mUserId);
        offset = offset + MAX_USERID_LEN;

        byte[] byteStrForDouble = new byte[sizeOfDouble];
        // Unpack lattitude now
        System.arraycopy(decodeByteStr, offset, byteStrForDouble, 0, sizeOfDouble);
        double latitude = ByteBuffer.wrap(byteStrForDouble).getDouble();
        offset = offset + sizeOfDouble;

        // Pack longitude now
        System.arraycopy(decodeByteStr, offset, byteStrForDouble, 0, sizeOfDouble);
        double longitude = ByteBuffer.wrap(byteStrForDouble).getDouble();
        offset = offset + sizeOfDouble;

        // safety check once again
        if(offset != readOffset + MAX_PACKET_LEN) {
            return false;
        }

        // Everything is perfect
        // Popuate the Location Information now
        mLocation = new LatLng(latitude, longitude);

        numBytesDecoded[0] = MAX_PACKET_LEN;

        return true;
    }
}
