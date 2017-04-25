package compsci290.edu.duke.myeveryday.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

import static android.media.AudioManager.STREAM_MUSIC;

/**
 * Created by pallavishankar on 4/23/17.
 */

public class AudioHelper {


    public static File createAudioFile() throws IOException {
        String timeStamp = TimeUtils.getDatetimeSuffix(System.currentTimeMillis());
        String audioFileName = "aud_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File audio = File.createTempFile(
                audioFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );
        return audio;
    }

    public static void startRecording(MediaRecorder mRecorder, String mAudioFilePath) {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mAudioFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioEncodingBitRate(96000);
        mRecorder.setAudioSamplingRate(44100);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecorder.start();
    }

    public static void stopRecording(MediaRecorder mRecorder) {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
        }
    }

    public static void startPlayback(MediaPlayer mPlayer, String mAudioFilePath) {
        try {
            mPlayer.setAudioStreamType(STREAM_MUSIC);
            mPlayer.setDataSource(mAudioFilePath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopPlayback(MediaPlayer mPlayer) {
        if (mPlayer != null) {
            mPlayer.release();
        }
    }
}
