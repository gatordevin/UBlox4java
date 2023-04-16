/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ublox.gps;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author gator
 */
public class GPSHeadingTest {
    public static void main(String[] args) throws InterruptedException, IOException {
//        ArrayList<ZEDF9P> gps = ScanZEDF9P.dump();
        RTCMClient client = new RTCMClient("10.0.0.51", 1025);
        ZEDF9P frontGps = ScanZEDF9P.getPort("1-2.1");
        ZEDF9P backGps = ScanZEDF9P.getPort("1-2.3");
        frontGps.roverConfigure();
        backGps.roverConfigure();
        frontGps.setRTCMStream(client);
        backGps.setRTCMStream(client);
        while(true){
            double[] frontPos = frontGps.getPosEcef();
            double[] backPos = backGps.getPosEcef();
//            System.out.println("Front Position: " + Arrays.toString(frontPos));
//            System.out.println("Back Position: " + Arrays.toString(backPos));
            if(validateAccuracy(frontPos,backPos)){
                double[] positionDelta = calcDelta(frontPos, backPos);
                System.out.println("Current accuracy: " + (frontPos[3]+backPos[3])/2);
                System.out.println("Position Delta: " + Arrays.toString(positionDelta));
                double distanceApart = calcDistance(positionDelta);
                System.out.println("Distance apart: " + distanceApart);
                double heading = calcheading(frontPos, positionDelta);
                System.out.println("Current heading: " + heading);
            }
//            Thread.sleep(1000);
        }
    }
    
    public static boolean validateAccuracy(double[] frontPos, double[] backPos){
        if(frontPos[3]<10){
            if(backPos[3]<10){
                return true;
            }
        }
        return false;
    }
    
    public static double[] calcDelta(double[] frontPos, double[] backPos){
        double[] distanceDelta = new double[3];
        distanceDelta[0] = frontPos[0]-backPos[0];
        distanceDelta[1] = frontPos[1]-backPos[1];
        distanceDelta[2] = frontPos[2]-backPos[2];
        return distanceDelta;
    }
    
    public static double calcDistance(double[] positionDelta){
        double distance = Math.sqrt(Math.pow(positionDelta[0],2)+Math.pow(positionDelta[1],2)+Math.pow(positionDelta[2],2));
        return distance;
    }
    
    public static double calcheading(double[] originPoint, double[] positionDelta){
        double x = originPoint[0];
        double y = originPoint[1];
        double z = originPoint[2];
        double dx = positionDelta[0];
        double dy = positionDelta[1];
        double dz = positionDelta[2];
        float azimuthCosine = (float) (((-z*x*dx) - (z*y*dy) + ((Math.pow(x,2)+Math.pow(y,2))*dz)) / (Math.sqrt((Math.pow(x,2)+Math.pow(y,2))*(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2))*(Math.pow(dx,2)+Math.pow(dy,2)+Math.pow(dz,2)))));
        float azimuthSine = (float) (((-y*dx) + (x*dy)) / Math.sqrt((Math.pow(x,2)+Math.pow(y,2))*(Math.pow(dx,2)+Math.pow(dy,2)+Math.pow(dz,2))));
        double azimuth = ((float) Math.atan2(azimuthSine, azimuthCosine)*-180/Math.PI);
        return azimuth;
    }
    
}
