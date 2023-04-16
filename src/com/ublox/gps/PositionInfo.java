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
public class PositionInfo {
    private double x = 0;
    private double y = 0;
    private double z = 0;
    private double accuracy = 0;
    private boolean positionValid = false;
    
    public PositionInfo(){
        
    }
    
    public PositionInfo(double x, double y, double z, double accuracy){
        this.x = x;
        this.y = y;
        this.z = z;
        this.accuracy = accuracy;
        this.positionValid = true;
    }
    public PositionInfo(double data[]){
        this.x = data[0];
        this.y = data[1];
        this.z = data[2];
        this.accuracy = data[3];
        this.positionValid = true;
    }
}
