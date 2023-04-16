/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ublox.gps;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gator
 */
public class RTCMServer implements Runnable{
    private ServerSocket server;
    private Thread serverThread;
    private ArrayList<Socket> socketConnections = new ArrayList<Socket>();
    public RTCMServer(int port){
        try {
            this.server = new ServerSocket(port);
            serverThread = new Thread(this);
            serverThread.start();
        } catch (IOException ex) {
            Logger.getLogger(RTCMServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendRTCMData(byte[] data){
        System.out.println("Sending data");
        if(data!=null){
            if(data.length>0){
                try{
                    for(Socket socket : socketConnections){
                        try {
                            socket.getOutputStream().write(data);
                        } catch (IOException ex) {
                            Logger.getLogger(RTCMServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }catch(ConcurrentModificationException exception){
                    System.out.println("Need thread lock");
                }
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        RTCMServer server = new RTCMServer(1025);
        ZEDF9P gps = ScanZEDF9P.dump().get(0);
//        gps.roverConfigure();
//        while(true){
//            System.out.println(Arrays.toString(gps.getPosEcef()));
//            Thread.sleep(0);
//        }
        gps.baseConfigure();
        while(true){
            byte[] data = gps.readRTCMMessage();
            server.sendRTCMData(data);
            Thread.sleep(10);
        }
        
    }

    @Override
    public void run() {
        while(true){
            try {
                Socket socket = this.server.accept();
                System.out.println("New client");
                socketConnections.add(socket);
                try {
                    Thread.sleep(0);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RTCMServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(RTCMServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
