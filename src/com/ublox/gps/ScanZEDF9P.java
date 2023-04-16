/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.ublox.gps;

import com.fazecast.jSerialComm.SerialPort;
import com.ublox.serial.SerialPortManager;
import java.util.ArrayList;

/**
 *
 * @author Scooter Willis <willishf@gmail.com>
 */
public class ScanZEDF9P {
    
    private static ArrayList<ZEDF9P> gpsList = new ArrayList<>();
    public static ArrayList<ZEDF9P> dump(){
        ArrayList<SerialPort> unAllocatedSerialPortList = new ArrayList<>();
        SerialPortManager.getSerialPortManager().refreshPorts();
        ArrayList<SerialPort> originalSerialPortList = SerialPortManager.getSerialPortManager().getUnallocatedSerialPorts();
        unAllocatedSerialPortList.addAll(originalSerialPortList);
        for (SerialPort serialPort : unAllocatedSerialPortList) {
            if(serialPort.getPortDescription().equals("u-blox GNSS receiver")){
                serialPort.setBaudRate(9600);
                System.out.println(serialPort.openPort());
                ZEDF9P gps = new ZEDF9P(serialPort.getInputStream(), serialPort.getOutputStream());
                gpsList.add(gps);
                System.out.println("Serial Port Description:" + serialPort.getPortDescription() + " at COM:" + serialPort.getSystemPortName());
            }
        }
        return gpsList;
    }
    
    public static ZEDF9P getPort(String port){
        ArrayList<SerialPort> unAllocatedSerialPortList = new ArrayList<>();
        SerialPortManager.getSerialPortManager().refreshPorts();
        ArrayList<SerialPort> originalSerialPortList = SerialPortManager.getSerialPortManager().getUnallocatedSerialPorts();
        unAllocatedSerialPortList.addAll(originalSerialPortList);
        for (SerialPort serialPort : unAllocatedSerialPortList) {
            if(serialPort.getPortDescription().equals("u-blox GNSS receiver")){
                if(serialPort.getPortLocation().equals(port)){
                    serialPort.setBaudRate(9600);
                    System.out.println(serialPort.openPort());
                    ZEDF9P gps = new ZEDF9P(serialPort.getInputStream(), serialPort.getOutputStream());
                    System.out.println("Serial Port Description:" + serialPort.getPortDescription() + " at COM:" + serialPort.getSystemPortName() + " in port: " + serialPort.getPortLocation());
                    return gps;
                }
            }
        }
        return null;
    }
    
    public static void dumpPortLocation(){
        SerialPort sp = SerialPortManager.getSerialPortManager().getUnallocatedSerialPortLocation("1-6.3");
        System.out.println("Serial Port at:" + sp.getPortDescription() + " " + sp.getPortLocation());
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        ScanZEDF9P.dump();
    }
    
}
