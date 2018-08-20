package com.umberto.medicinetracking.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MedicineUtils {
    //Convert string to date
    public static Date dateFromString(String date){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        try {
            return dateFormatter.parse(date);
        } catch (ParseException e) {
            return new Date();
        }
    }

    //Convert date to string
    public static String dateToString(Date date){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        try {
            return dateFormatter.format(date);
        }catch (Exception e){
            return dateFormatter.format(new Date());
        }
    }

    //Convert string to integer
    public static int stringToInt(String value){
        try{
        return Integer.parseInt(value);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    //Convert int to string
    public static String intToString(int value){
        try{
        return Integer.toString(value);
    }catch (Exception e){
        e.printStackTrace();
        return "0";
    }
    }
}
