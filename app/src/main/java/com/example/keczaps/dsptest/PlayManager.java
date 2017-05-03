package com.example.keczaps.dsptest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.musicg.wave.Wave;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayManager {

    private ScheduledExecutorService scheduleTaskExecutor;
    private double duration=0.1;
    private int sampleRate=44100;
    private double numSample=duration*sampleRate;
    private double sample[]=new double[(int) numSample];
    private double numSample2=2*sampleRate;
    private byte[] generatedSnd= new byte[(int) numSample2];
    byte[] array1;
    private Handler handler=new Handler();
    private AudioTrack audioTrack;
    private int threadPoolNumberForTask;
    private boolean isPlaying = false;

    public PlayManager(int threadPoolNumberForTask) {
        this.threadPoolNumberForTask = threadPoolNumberForTask;
        //this.genTone();
        array1 = getWaveF();
    }

    public void startPlaying() {

        isPlaying = true;
        scheduleTaskExecutor = Executors.newScheduledThreadPool(threadPoolNumberForTask);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                handler.post(new Runnable(){
                    public void run(){
                        playSound();
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stopPlaying() {
        isPlaying = false;
        scheduleTaskExecutor.shutdownNow();
        audioTrack = null;
    }

    public void genTone(){

        double freq1=5000,freq2=6000,instfreq=0, numerator;

        for (int i=0;i<numSample; i++ ) {
            numerator = (double)(i)/numSample;
            instfreq = freq1+(numerator*(freq2-freq1));
            if ((i % 1000) == 0) {
                Log.e("Current Freq:", String.format("Freq is:  %f at loop %d of %d", instfreq, i, (int)numSample));
            }
            sample[i]=Math.sin(2*Math.PI*i/(sampleRate/instfreq));
        }

        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767)); // max positive sample for signed 16 bit integers is 32767
            // in 16 bit wave PCM, first byte is the low order byte (pcm: pulse control modulation)
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        array1 = Arrays.copyOf(generatedSnd,generatedSnd.length/20);
        Log.i("Lenght array1 ",""+array1.length);
    }

    private void playSound(){

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(array1, 0, array1.length);
        audioTrack.play();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public byte[] getWaveF() {
        Wave wave = new Wave(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath()+"/DSPtest/testGenerated5ms.wav");
        return wave.getBytes();
    }



}