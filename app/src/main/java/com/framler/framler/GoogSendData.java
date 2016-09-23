package com.framler.framler;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Constantin on 28.08.2016.
 */
public class GoogSendData extends AsyncTask<String, String, String>{
    String Name, Family, Password, Email, Nik, sToken, NAME, FAMILY, NIK, nameFamily, photopath;
    Context ctx;


    @Override
    protected String doInBackground(String... params) {
        String namereg = params[0];
        String regpass = params[1];
        String regemail = params[2];
        String famreg = params[3];
        String nik = params[4];
        String token = params[5];
        if (params.length == 7){
            photopath = params[6];
        }
        String data="";

        int tmp;
        try {
            URL url = new URL("http://framler.com/apiscripts/register.php");
            if (params.length == 7){
                String urlParams = "name=" + namereg + "&password=" + regpass + "&email=" + regemail + "&family=" + famreg + "&nik=" + nik + "&token=" + token + "&photopath=" + photopath;
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                OutputStream os = httpURLConnection.getOutputStream();
                os.write(urlParams.getBytes());
                os.flush();
                os.close();
                InputStream is = httpURLConnection.getInputStream();
                while ((tmp = is.read()) != -1) {
                    data += (char) tmp;
                }
                is.close();
                httpURLConnection.disconnect();

                return data;
            } else {
                String urlParams = "name=" + namereg + "&password=" + regpass + "&email=" + regemail + "&family=" + famreg + "&nik=" + nik + "&token=" + token;
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                OutputStream os = httpURLConnection.getOutputStream();
                os.write(urlParams.getBytes());
                os.flush();
                os.close();
                InputStream is = httpURLConnection.getInputStream();
                while ((tmp = is.read()) != -1) {
                    data += (char) tmp;
                }
                is.close();
                httpURLConnection.disconnect();

                return data;
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }

    }
    @Override
    protected void onPostExecute(String s) {
        NAME = Name;
        FAMILY = Family;
        NIK = Nik;

        if (s.equals("")) {
            s = "Data saved successfully.";
        }

    }



}
