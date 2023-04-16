/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ublox.gps;

import java.util.Arrays;

/**
 *
 * @author gator
 */
public class UBXMessage {
    private byte[] command;
    private byte[] payload;
    private byte[] checksum = new byte[]{0,0};
    public UBXMessage(byte[] command, byte[] payload, byte[] checksum){
        this.command =  command;
        this.payload = payload;
        this.checksum = checksum;
    }
    
    public UBXMessage(byte[] command, byte[] payload){
        this.command = command;
        this.payload = payload;
        this.checksum = calcChecksum();
    }
    
    public UBXMessage(byte[] data){
        this.command =  Arrays.copyOfRange(data, 2, 4);
        int length = ((data[4] & 0xFF) | ((data[5] & 0xFF) << 8));
        this.payload = Arrays.copyOfRange(data, 6, 6+length);
        this.checksum = Arrays.copyOfRange(data, 6+length, 6+length+2);
    }
    
    public byte[] calcChecksum(){
        int[] checksumBuffer = new int[]{0,0};
        byte[] dataBuffer = this.getByteArray();
        for(byte data : Arrays.copyOfRange(dataBuffer, 2, dataBuffer.length-2)){
            checksumBuffer[0] = checksumBuffer[0] + data;
            checksumBuffer[1] = checksumBuffer[1] + checksumBuffer[0];
        }
        byte[] checksumByteBuffer = new byte[2];
        checksumByteBuffer[0] = (byte) (checksumBuffer[0] & 0xFF);
        checksumByteBuffer[1] = (byte) (checksumBuffer[1] & 0xFF);
        return checksumByteBuffer;
    }
    
    public byte[] getByteArray(){
        byte[] byteArray = new byte[8+this.getLength()];
        byteArray[0] = UBXCommands.HEAD_ONE;
        byteArray[1] = UBXCommands.HEAD_TWO;
        byteArray[2] = this.command[0];
        byteArray[3] = this.command[1];
        byteArray[4] = (byte) (this.getLength() & 0xFF);
        byteArray[5] = (byte) (this.getLength() >> 8);
        for(int i = 0; i<this.getLength(); i++){
            byteArray[6+i] = this.payload[i];
        }
        byteArray[6+this.getLength()] = this.checksum[0];
        byteArray[7+this.getLength()] = this.checksum[1];
        return byteArray;
    }
    
    public byte getClassID(){
        return this.command[0];
    }
    
    public byte getMessageID(){
        return this.command[1];
    }
    
    public byte[] getCommand(){
        return this.command;
    }
    
    public byte[] getPayload(){
        return this.payload;
    }
    
    public int getLength(){
        return this.payload.length;
    }
    
    public byte[] getChecksum(){
        return this.checksum;
    }
}
