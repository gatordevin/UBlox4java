/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ublox.gps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author gator
 */
public class ZEDF9P implements Runnable, GPS {

    private InputStream input;
    private OutputStream output;
    private RTCMClient rtcmClient;
    private Thread rtcmThread;
    private long timeout = 1000;
    private Queue<byte[]> messages = new LinkedList<>();
    private boolean streamToFile = false;

    public ZEDF9P(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        this.rtcmThread = new Thread(this);
        this.rtcmThread.setDaemon(true);
        this.rtcmThread.start();
    }

    public void setRTCMStream(RTCMClient rtcmClient) {
        this.rtcmClient = rtcmClient;
    }

    public UBXMessage sendUBXMessage(UBXMessage message) {
        messages.add(message.getByteArray());
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis()-start<1000) {
            UBXMessage messageResp = this.readUBXMessage();
            if (messageResp != null){
                start = System.currentTimeMillis();
                if (Arrays.equals(messageResp.getCommand(), message.getCommand())) {
//                    System.out.println("Data response Received");
                    return messageResp;
                } else if (messageResp.getCommand()[0] == UBXCommands.ACK_ACK[0]) {
                    if (Arrays.equals(messageResp.getPayload(), message.getCommand())) {
//                        System.out.println("Acknowledgement Received");
                        return messageResp;
                    }
                }
            }
        }
        return null;
    }

    public UBXMessage readUBXMessage() {
        long currentTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - currentTime < timeout) {
            try {
                if (this.input.available() > 2) {
                    if (this.input.read() == UBXCommands.HEADER[0]) {
                        if (this.input.read() == UBXCommands.HEADER[1]) {
                            byte[] command = this.input.readNBytes(2);
                            int length = ((this.input.read() & 0xFF) | ((this.input.read() & 0xFF) << 8));
                            UBXMessage message = new UBXMessage(command, this.input.readNBytes(length), this.input.readNBytes(2));
                            return message;
                        }
                    }
                }
                Thread.sleep(0);
            } catch (IOException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public void set5Hz() {
        this.sendUBXMessage(new UBXMessage(UBXCommands.CFG_RATE, new byte[]{(byte) 0xC8, 0, 1, 0, 1, 0}));
    }

    public void fixedPositionMode(double Lat, double Long, double Alt) {
        Lat *= 10000000;
        Long *= 10000000;
        Alt *= 100;
        byte[] payload = new byte[40];
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.put((byte) 2);
        buf.put((byte) 1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt((int) Lat);
        buf.putInt((int) Long);
        buf.putInt((int) Alt);

        this.sendUBXMessage(new UBXMessage(UBXCommands.CFG_TIMEMODE3, buf.array()));
    }

    public void gpsPositionMode() {
        byte[] payload = new byte[40];

        this.sendUBXMessage(new UBXMessage(UBXCommands.CFG_TIMEMODE3, payload));
    }

    public byte[] readRTCMMessage() {
        long currentTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - currentTime < timeout) {
            try {
                if (this.input.available() > 3) {
//                    System.out.println("data found");
                    if (this.input.read() == 211) {
                        byte[] headerBytes = this.input.readNBytes(2);
                        if ((headerBytes[0] & 0xFC) == 0x00) {
                            int length = ((headerBytes[0] & 0x3) << 8) | headerBytes[1] & 0xFF;
                            if (length > 0) {
                                byte[] payloadBytes = this.input.readNBytes(length);
                                byte[] checksumBytes = this.input.readNBytes(3);
                                byte[] packetBytes = new byte[6 + length];
                                packetBytes[0] = (byte) 0xD3;
                                for (int i = 0; i < headerBytes.length; i++) {
                                    packetBytes[1 + i] = headerBytes[i];
                                }
                                for (int i = 0; i < payloadBytes.length; i++) {
                                    packetBytes[1 + i + headerBytes.length] = payloadBytes[i];
                                }
                                for (int i = 0; i < checksumBytes.length; i++) {
                                    packetBytes[1 + i + headerBytes.length + payloadBytes.length] = checksumBytes[i];
                                }
                                return packetBytes;
                            }
                        }
                    }
                }
                Thread.sleep(10);
            } catch (IOException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public byte[] readRemoteRTCMMessage() {
        long currentTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - currentTime < timeout) {
            try {
                if (this.rtcmClient.getInputStream().available() > 3) {
//                    System.out.println("data found");
                    if (this.rtcmClient.getInputStream().read() == 211) {
                        byte[] headerBytes = this.rtcmClient.getInputStream().readNBytes(2);
                        if ((headerBytes[0] & 0xFC) == 0x00) {
                            int length = ((headerBytes[0] & 0x3) << 8) | headerBytes[1] & 0xFF;
                            if (length > 0) {
                                byte[] payloadBytes = this.rtcmClient.getInputStream().readNBytes(length);
                                byte[] checksumBytes = this.rtcmClient.getInputStream().readNBytes(3);
                                byte[] packetBytes = new byte[6 + length];
                                packetBytes[0] = (byte) 0xD3;
                                for (int i = 0; i < headerBytes.length; i++) {
                                    packetBytes[1 + i] = headerBytes[i];
                                }
                                for (int i = 0; i < payloadBytes.length; i++) {
                                    packetBytes[1 + i + headerBytes.length] = payloadBytes[i];
                                }
                                for (int i = 0; i < checksumBytes.length; i++) {
                                    packetBytes[1 + i + headerBytes.length + payloadBytes.length] = checksumBytes[i];
                                }
                                return packetBytes;
                            }
                        }
                    }
                }
                Thread.sleep(0);
            } catch (IOException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public void enableUBX() {
        byte[] commandIDs = new byte[]{7, 2, 3};
        for (int i = 0; i < commandIDs.length; i++) {
            this.sendUBXMessage(new UBXMessage(UBXCommands.CFG_MSG, new byte[]{(byte) 1, (byte) commandIDs[i], 0, 0, 1, 1, 0, 0}));
        }
    }

    public void disableUBX() {
        byte[] commandIDs = new byte[]{7, 2, 3};
        for (int i = 0; i < commandIDs.length; i++) {
            this.sendUBXMessage(new UBXMessage(UBXCommands.CFG_MSG, new byte[]{(byte) 1, (byte) commandIDs[i], 0, 0, 0, 0, 0, 0}));
        }
    }

    public void disableNMEA() {
        for (int i = 0; i < 6; i++) {
            this.sendUBXMessage(new UBXMessage(UBXCommands.CFG_MSG, new byte[]{(byte) 240, (byte) i, 0, 0, 0, 0, 0, 0}));
        }
    }

    public void enableRTCM() {
        byte[] commandIDs = new byte[]{5, 0x4A, 0x54, 0x5E, 0x7C, (byte) 0xE6};
        for (int i = 0; i < commandIDs.length; i++) {
            this.sendUBXMessage(new UBXMessage(UBXCommands.CFG_MSG, new byte[]{(byte) 0xF5, (byte) commandIDs[i], 0, 0, 1, 1, 0, 0}));
        }
    }

    public void enableRaw() {
        byte[] commandIDs = new byte[]{0x15};
        for (int i = 0; i < commandIDs.length; i++) {
            this.sendUBXMessage(new UBXMessage(UBXCommands.CFG_MSG, new byte[]{(byte) 0x02, (byte) commandIDs[i], 0, 0, 1, 1, 0, 0}));
        }
    }

    public void disableRaw() {
        byte[] commandIDs = new byte[]{0x15};
        for (int i = 0; i < commandIDs.length; i++) {
            this.sendUBXMessage(new UBXMessage(UBXCommands.CFG_MSG, new byte[]{(byte) 0x02, (byte) commandIDs[i], 0, 0, 0, 0, 0, 0}));
        }
    }

    public void disableRTCM() {
        byte[] commandIDs = new byte[]{5, 0x4A, 0x54, 0x5E, 0x7C, (byte) 0xE6};
        for (int i = 0; i < commandIDs.length; i++) {
            this.sendUBXMessage(new UBXMessage(UBXCommands.CFG_MSG, new byte[]{(byte) 0xF5, (byte) commandIDs[i], 0, 0, 0, 0, 0, 0}));
        }
    }

    public double[] getPosEcef() {
        UBXMessage response = this.sendUBXMessage(new UBXMessage(UBXCommands.NAV_HPPOSECEF, new byte[0]));
        if(response==null){
            return null;
        }
//        long value = ByteBuffer.wrap(Arrays.copyOfRange(response.getPayload(), 8, 12)).order(ByteOrder.LITTLE_ENDIAN).getLong();
        long value
                = ((response.getPayload()[4] & 0xFF) << 0)
                | ((response.getPayload()[5] & 0xFF) << 8)
                | ((response.getPayload()[6] & 0xFF) << 16)
                | ((long) (response.getPayload()[7] & 0xFF) << 24);
//        System.out.println(Arrays.toString(response.getPayload()));

//        float x = ByteBuffer.wrap(Arrays.copyOfRange(response.getPayload(), 8, 12)).order(ByteOrder.LITTLE_ENDIAN).getInt();
//        float x2 = ByteBuffer.wrap(Arrays.copyOfRange(response.getPayload(), 8, 12)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        double x = ByteBuffer.wrap(Arrays.copyOfRange(response.getPayload(), 8, 12)).order(ByteOrder.LITTLE_ENDIAN).getInt();
//        System.out.println(x);
        double y = ByteBuffer.wrap(Arrays.copyOfRange(response.getPayload(), 12, 16)).order(ByteOrder.LITTLE_ENDIAN).getInt();
//        System.out.println(y);
        double z = ByteBuffer.wrap(Arrays.copyOfRange(response.getPayload(), 16, 20)).order(ByteOrder.LITTLE_ENDIAN).getInt();
//        System.out.println(z);

        double xmm = ((int)(response.getPayload()[20])) / 100;
//        System.out.println(xmm);
        double ymm = ((int)(response.getPayload()[21])) / 100;
        double zmm = ((int)(response.getPayload()[22])) / 100;
//        System.out.println(ymm);
//        System.out.println(zmm);

        x += xmm;
        y += ymm;
        z += zmm;

//        System.out.println("X Position: " + x);
//        System.out.println("Y Position: " + y);
//        System.out.println("Z Position: " + z);

        double accuracy = ByteBuffer.wrap(Arrays.copyOfRange(response.getPayload(), 24, 28)).order(ByteOrder.LITTLE_ENDIAN).getInt() / 100;
//        System.out.println("centimeter accuracy:" + accuracy);

        double data[] = new double[]{x, y, z, accuracy};
        return data;
    }

    public void baseConfigure() {
        this.streamToFile = false;
        this.disableRaw();
        this.disableUBX();
        this.enableRTCM();
        this.disableNMEA();
        this.fixedPositionMode(29.61438323, -82.16216583, 3.084);
        this.set5Hz();
    }

    public void roverConfigure() {
        this.streamToFile = false;
        this.disableRaw();
        this.disableUBX();
        this.disableRTCM();
        this.disableNMEA();
        this.gpsPositionMode();
        this.set5Hz();
    }

    public void rawGPSConfigure() {
        this.disableUBX();
        this.disableRTCM();
        this.disableNMEA();
        this.gpsPositionMode();
        this.enableRaw();
        this.streamToFile = true;
    }

    public static void main(String[] args) throws InterruptedException {
        ZEDF9P gps = ScanZEDF9P.dump().get(0);
        gps.baseConfigure();
        while(true){
            Thread.sleep(0);
        }
//        while(true){
//            byte[] rtcmData = gps.readRTCMMessage();
//            if(rtcmData!=null){
//                System.out.println(Arrays.toString(rtcmData));
//            }
//        }
    }
    
    public Path createRecordPath(){
        File filesList[] = new File(".").listFiles();
        int count = 0;
        for(File f : filesList){
            if(f.getName().endsWith(".ubx")){
                count+=1;
            }
        }
        return Paths.get("datarecord_" + count + ".ubx");
        
    }

    @Override
    public void run() {
        Path path = createRecordPath();
        while (true) {
            try {
                while(streamToFile){
                    if(this.input.available()>0){
                        byte[] data = this.input.readNBytes(this.input.available());
//                        System.out.println("saving: " + Arrays.toString(data));
                        System.out.println("saving");
                        Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    }
                    Thread.sleep(0);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                if (this.rtcmClient != null) {
                    InputStream inputStream = this.rtcmClient.getInputStream();
                    while(inputStream==null){
                        System.out.println("No Input Stream to rtcmClient detected");
                        Thread.sleep(1000);
                        inputStream = this.rtcmClient.getInputStream();
                    }
                    if (this.rtcmClient.getInputStream().available() > 0) {
//                        System.out.println("new rtcm");
                        byte[] rtcmMessage = this.readRemoteRTCMMessage();
//                        System.out.println("read rtcm");
                        messages.add(rtcmMessage);
                    }
                }

                byte[] message = messages.poll();
                while (message != null) {
                    this.output.write(message);
                    message = messages.poll();
                }
            } catch (IOException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(ZEDF9P.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public PositionInfo getPosition() {
        double data[] = this.getPosEcef();
        PositionInfo info = new PositionInfo();
        if (data != null) {
            info = new PositionInfo(data);
        }
        return info;
    }
}
