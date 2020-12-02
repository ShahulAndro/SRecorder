package com.sha.srecorder.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sha.srecorder.R;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Shahul Hameed Shaik on 02/12/2020.
 */
public class RecordFragment extends Fragment {
    private static final String POSITION = "position";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean isRecordingStarted;
    private boolean isRecordingPaused;
    private int mRecordPromptCount = 0;
    private long timeWhenPaused = 0;
    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private String mFileName;

    private FloatingActionButton mRecordButton = null;
    private FloatingActionButton mPauseButton = null;
    private TextView mRecordingPrompt;

    private Chronometer mChronometer = null;

    private MediaRecorder mediaRecorder;

    public static RecordFragment newInstance(int position) {
        RecordFragment recordFragment = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, position);
        recordFragment.setArguments(b);

        return recordFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = recordView.findViewById(R.id.chronometer);
        mRecordingPrompt = recordView.findViewById(R.id.recording_status_text);
        mRecordButton = recordView.findViewById(R.id.btnRecord);
        mPauseButton = recordView.findViewById(R.id.btnPause);

        mPauseButton.setVisibility(View.GONE);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RecordFragment.this.isRecordingStarted) {
                    stopRecording();
                } else {
                    checkPermissionAndStartRecording();
                }

            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseRecording();
            }
        });

        return recordView;
    }

    private void checkPermissionAndStartRecording() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            startRecording();
        }
    }

    private void initRecording() {
        String uuid = UUID.randomUUID().toString();
        this.mFileName = getContext().getFilesDir().getPath() + "/" + uuid + ".3gp";
        Log.i(RecordFragment.class.getSimpleName(), mFileName);

        this.mediaRecorder = new MediaRecorder();
    }

    private void startRecording() {
        initRecording();
        this.isRecordingStarted = true;
        this.isRecordingPaused = false;
        this.mPauseButton.setVisibility(View.VISIBLE);

        try {
            this.mStartingTimeMillis = System.currentTimeMillis();

            this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            this.mediaRecorder.setOutputFile(mFileName);
            this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            this.mediaRecorder.prepare();
            this.mediaRecorder.start();
        } catch (IOException e) {
            Toast.makeText(getContext(), "OOPs something wrong, please try again", Toast.LENGTH_LONG).show();
        }

        this.mRecordButton.setImageResource(R.drawable.ic_media_stop);

        startChronometer();

        this.mRecordingPrompt.setText(getString(R.string.record_in_progress));
        this.mRecordPromptCount++;

        //Todo: Show notification service
    }

    private void startRecordingFromPause() {
        this.isRecordingStarted = true;
        this.isRecordingPaused = false;

        this.mPauseButton.setVisibility(View.VISIBLE);
        this.mPauseButton.setImageResource(R.drawable.ic_media_pause);

        //Todo: Start Media Recording from pause

        this.mRecordButton.setImageResource(R.drawable.ic_media_stop);
        Toast.makeText(getActivity(),R.string.toast_recording_start, Toast.LENGTH_SHORT).show();

        this.mRecordingPrompt.setText(getString(R.string.record_in_progress));
        this.mRecordPromptCount++;

        //Todo: Show notification
    }

    private void stopRecording() {
        this.isRecordingStarted = false;
        this.isRecordingPaused = false;

        if (this.mediaRecorder != null) {
            this.mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
            this.mediaRecorder.release();
            this.mediaRecorder = null;
            //Todo: Stop notification
        }

        this.mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);

        this.mChronometer.stop();
        this.mChronometer.setBase(SystemClock.elapsedRealtime());
        this.timeWhenPaused = 0;

        this.mRecordingPrompt.setText(getString(R.string.record_prompt));

        this.mPauseButton.setVisibility(View.GONE);
        this.mPauseButton.setImageResource(R.drawable.ic_media_pause);

        //Todo: Save record into database
    }

    private void pauseRecording() {
        if (this.isRecordingPaused) {
            this.isRecordingPaused = false;
            this.mPauseButton.setImageResource(R.drawable.ic_media_pause);
            this.mChronometer.setBase(SystemClock.elapsedRealtime() + this.timeWhenPaused);
            this.mChronometer.start();

            startRecordingFromPause();
        } else {
            this.isRecordingPaused = true;

            this.mRecordButton.setImageResource(R.drawable.ic_media_stop);
            this.mPauseButton.setImageResource(R.drawable.ic_media_play);

            this.timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
            this.mChronometer.stop();

            Toast.makeText(getActivity(),"Time When paused"+this.timeWhenPaused, Toast.LENGTH_SHORT).show();
        }

        //Todo: Pause Media Recording

    }

    private void startChronometer() {
        this.mChronometer.setBase(SystemClock.elapsedRealtime());
        this.mChronometer.start();
        this.mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (mRecordPromptCount == 0) {
                    mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                } else if (mRecordPromptCount == 1) {
                    mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                } else if (mRecordPromptCount == 2) {
                    mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                    mRecordPromptCount = -1;
                }

                mRecordPromptCount++;
            }
        });
    }
}
