/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ublox.gps;

/**
 *
 * @author gator
 */
public class UBXCommands {
    //HEADER
    public static final byte HEAD_ONE = (byte) 0xB5;
    public static final byte HEAD_TWO = (byte) 0x62;
    public static final int[] HEADER = new int[]{0xB5, 0x62};
    
    //ACK
    public static final byte[] ACK_ACK = new byte[]{0x05, 0x01}; //RESPONSE SNET IN 1 Second
    public static final byte[] ACK_NAK = new byte[]{0x05, 0x00}; //RESPONSE SNET IN 1 Second
    
    //CFG
    public static final byte[] CFG_CFG = new byte[]{0x06, 0x09};
    public static final byte[] CFG_MSG = new byte[]{0x06, 0x01};
    public static final byte[] CFG_NMEA = new byte[]{0x06, 0x17};
    public static final byte[] CFG_PRT = new byte[]{0x06, 0x00};
    public static final byte[] CFG_USB = new byte[]{0x06, 0x1B};
    public static final byte[] CFG_RATE = new byte[]{0x06, 0x08};
    public static final byte[] CFG_TIMEMODE3 = new byte[]{0x06, 0x71};
    public static final byte[] CFG_VALSET = new byte[]{0x06, (byte) 0x8A};
    public static final byte[] CFG_VALGET = new byte[]{0x06, (byte) 0x8B};
    
    //NAV
    public static final byte[] NAV_POSLLH = new byte[]{0x01, 0x02};
    public static final byte[] NAV_POSECEF = new byte[]{0x01, 0x01};
    public static final byte[] NAV_HPPOSECEF = new byte[]{0x01, 0x013};
    public static final byte[] NAV_PVT = new byte[]{0x01, 0x07};
    public static final byte[] NAV_SAT = new byte[]{0x01, 0x35};
    public static final byte[] NAV_SIG = new byte[]{0x01, 0x43};
    public static final byte[] NAV_STATUS = new byte[]{0x01, 0x03};
    
    //NAV
    public static final byte[] RXM_RTCM = new byte[]{0x02, 0x32};
    
    //Raw
    public static final byte[] RXM_RAWX = new byte[]{0x02, 0x15};
}
