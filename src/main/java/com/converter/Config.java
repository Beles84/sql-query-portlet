package com.converter;

import java.io.*;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class Config {

    //Writing Configuration
    public static void SetPreference(String Key, String Value){
        Properties configFile = new Properties();
        try {
            InputStream f = new FileInputStream("configuration.xml");
            configFile.loadFromXML(f);
            f.close();
        }
        catch(IOException e) {
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        configFile.setProperty(Key, Value);
        try {
            OutputStream f = new FileOutputStream("configuration.xml");
            configFile.storeToXML(f,"Configuration file for the Profit System");
            f.close();
        }
        catch(Exception e) {
        }
    }
    //Reading Configurations
    public static String getPreference(String Key) {
        Properties configFile = new Properties();
        try {
            InputStream f = new FileInputStream("configuration.xml");
            configFile.loadFromXML(f);
            f.close();
        }
        catch(IOException e) {
        }
        catch(Exception e) {
            //JOptionPane.showMessageDialog(null , e.getMessage());
            System.out.println(e.getMessage());
        }
        return (configFile.getProperty(Key));
    }
}
