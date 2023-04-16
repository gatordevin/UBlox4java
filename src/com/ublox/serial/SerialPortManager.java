/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ublox.serial;

import com.fazecast.jSerialComm.SerialPort;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is designed to manage serial ports on the system that have a
 * particular device name attached and allow the port to be reserved or released
 * by classes that need to communicate with the device
 *
 * @author gator
 */
public class SerialPortManager implements Runnable {

    private static SerialPortManager serialPortManager;
    //   private static Thread managerThread;
    //   private static ArrayList<SerialPortListener> serialPortListeners = new ArrayList<SerialPortListener>();
    //keep track of allocated ports
    private static final ArrayList<SerialPort> allocatedPortList = new ArrayList<SerialPort>();
    //keep track of unallocated ports
    private static final ArrayList<SerialPort> unallocatedPortList = new ArrayList<SerialPort>();
    
    SerialPortManager() {
//        managerThread = new Thread(this);
//        managerThread.start();
        refreshPorts(); //grab current list of serial ports. If run as a thread then this will be updated for new ports or ports removed
    }

    /**
     * A convenience method to make it easier to have just one copy used by
     * multiple classes
     *
     * @return
     */
    public static SerialPortManager getSerialPortManager() {
        if (serialPortManager == null) {
            serialPortManager = new SerialPortManager();
        }
        return serialPortManager;
    }

    /**
     * Reserve a port so it doesn't get allocated in the future
     *
     * @param serialPort
     * @return
     */
    public boolean reservePort(SerialPort serialPort) {
        if (allocatedPortList.contains(serialPort)) {
            return false;
        }
        if (unallocatedPortList.contains(serialPort) == false) {
            return false;
        }
        unallocatedPortList.remove(serialPort);
        allocatedPortList.add(serialPort);
        return true;
    }

    /**
     * Release a port that was allocated prior
     *
     * @param serialPort
     * @return
     */
    public boolean releasePort(SerialPort serialPort) {
        if (unallocatedPortList.contains(serialPort)) {
            return false;
        }
        if (allocatedPortList.contains(serialPort) == false) {
            return false;
        }
        allocatedPortList.remove(serialPort);
        unallocatedPortList.add(serialPort);
        return true;
    }

    /**
     * Need to find serial ports with a particular device type on it
     *
     * @param commonName
     * @return
     */
    public ArrayList<SerialPort> getUnallocatedSerialPortsCommonName(String commonName) {
        ArrayList<SerialPort> serialPortList = new ArrayList<SerialPort>();
        for (SerialPort serialPort : unallocatedPortList) {
            if (serialPort.getPortDescription().equals(commonName)) {
                serialPortList.add(serialPort);
            }
        }
        return serialPortList;
    }

    /**
     * Based on location of where the USB com port is plugged in return that serial port
     * @param portLocation
     * @return 
     */
    public SerialPort getUnallocatedSerialPortLocation(String portLocation) {
        for (SerialPort serialPort : unallocatedPortList) {
            if (serialPort.getPortLocation().equals(portLocation)) {
                return serialPort;
            }
        }
        return null;
    }

    /**
     * Need to find serial ports with a particular device type on it
     *
     * @param description
     * @return
     */
    public ArrayList<SerialPort> getAllocatedSerialPorts(String description) {
        ArrayList<SerialPort> serialPortList = new ArrayList<SerialPort>();
        for (SerialPort serialPort : allocatedPortList) {
            if (serialPort.getPortDescription().equals(description)) {
                serialPortList.add(serialPort);
            }
        }
        return serialPortList;
    }

    /**
     * As comm ports get plugged in the list of unallocated ports will grow The
     * ports if reserved will be taken out of the list
     *
     * @return
     */
    public ArrayList<SerialPort> getUnallocatedSerialPorts() {
        return unallocatedPortList;
    }

    /**
     * Get a list of currently allocated ports that were reserved
     *
     * @return
     */
    public ArrayList<SerialPort> getAllocatedSerialPorts() {
        return allocatedPortList;
    }

    //  public void addSerialPortListener(SerialPortListener listener){
    //      serialPortListeners.add(listener);
    //  }
    private boolean containsSerialPort(SerialPort serialPort, ArrayList<SerialPort> serialPortList) {
        for (SerialPort sp : serialPortList) {
            if (serialPort.getSystemPortName().equals(sp.getSystemPortName()) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     */
    public void refreshPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort serialPort : ports) {
            if (containsSerialPort(serialPort, allocatedPortList)) {
                continue;
            }
            if (containsSerialPort(serialPort, unallocatedPortList)) {
                continue;
            }
            unallocatedPortList.add(serialPort);
        }
    }

    @Override
    public void run() {

        while (true) {
            refreshPorts();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SerialPortManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
