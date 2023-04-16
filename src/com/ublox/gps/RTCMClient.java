/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ublox.gps;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gator
 */
public class RTCMClient {
    private Socket socket;
    private InputStream input;
    public RTCMClient(String ip, int port) throws InterruptedException{
        try {
            socket = new Socket(ip, port);
            if(socket==null){
                System.out.println("Could not open socket to RTCM Server");
                socket = new Socket(ip, port);
                Thread.sleep(1000);
            }
            input = socket.getInputStream();
        } catch (IOException ex) {
            Logger.getLogger(RTCMClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public InputStream getInputStream() throws IOException{
        input = socket.getInputStream();
        return input;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        RTCMClient client = new RTCMClient("10.0.0.51", 1025);
        ZEDF9P gps = ScanZEDF9P.dump().get(0);
        gps.roverConfigure();
        gps.setRTCMStream(client);
        
        double[] lastPos = Arrays.copyOfRange(gps.getPosEcef(), 0, 3);
        while(true){
            double[] newPos = gps.getPosEcef();
            System.out.println("centimeter accuracy: " + newPos[3]);
            double distanceInCm = Math.sqrt(Math.pow(newPos[0]-lastPos[0], 2)+Math.pow(newPos[1]-lastPos[1], 2)+Math.pow(newPos[2]-lastPos[2], 2));
            System.out.println(distanceInCm);
            Thread.sleep(200);
        }
    }
    
}
