/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ublox.gps;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author gator
 */
public class RTCMTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        RTCMServer server = new RTCMServer(1000);
        System.out.println("Server started");
        ArrayList<ZEDF9P> gps = ScanZEDF9P.dump();
        System.out.println("GPS found");
        ZEDF9P base = gps.get(0);
        base.baseConfigure();
        System.out.println("Base configured");
        
        RTCMClient client = new RTCMClient("192.168.1.18", 1000);
        System.out.println("Client started");
        ZEDF9P rover = gps.get(1);
        rover.roverConfigure();
        rover.setRTCMStream(client);
        System.out.println("Rover configured");
        
        while(true){
            byte[] data = base.readRTCMMessage();
            if(data!=null){
//                System.out.println(Arrays.toString(data));
                server.sendRTCMData(data);
            }
            rover.getPosEcef();
            Thread.sleep(1);
        }
        
    }
    
}
