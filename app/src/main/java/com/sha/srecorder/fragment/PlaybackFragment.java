package com.sha.srecorder.fragment;

import android.app.Dialog;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sha.srecorder.R;
import com.sha.srecorder.database.RecordedItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Shahul Hameed Shaik on 03/12/2020.
 */
public class PlaybackFragment extends DialogFragment {

    private static final String ITEM = "recorded_item";

    private boolean isPlaying = false;
    private long minutes = 0;
    private long seconds = 0;

    private SeekBar mSeekBar = null;
    private ImageView mPlayButton = null;
    private TextView mCurrentProgressTextView = null;
    private TextView mFileNameTextView = null;
    private TextView mFileLengthTextView = null;

    private RecordedItem recordedItem;
    private Handler mHandler = new Handler();
    private MediaPlayer mMediaPlayer = null;


    public PlaybackFragment newInstance(RecordedItem recordedItem) {
        PlaybackFragment playbackFragment = new PlaybackFragment();
        Bundle b = new Bundle();
        b.putSerializable(ITEM, recordedItem);
        playbackFragment.setArguments(b);

        return playbackFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordedItem = (RecordedItem) getArguments().getSerializable(ITEM);

        long itemDuration = recordedItem.recordedLength;
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playback, container);
        return view;
    }

    @Override

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFileNameTextView = view.findViewById(R.id.txtFileName);
        mFileLengthTextView = view.findViewById(R.id.txtFileLength);
        mCurrentProgressTextView = view.findViewById(R.id.txtCurrentProgress);

        mSeekBar = view.findViewById(R.id.seekbar);
        ColorFilter filter = new LightingColorFilter
                (getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorPrimary));
        mSeekBar.getProgressDrawable().setColorFilter(filter);
        mSeekBar.getThumb().setColorFilter(filter);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mMediaPlayer != null && fromUser) {
                    mMediaPlayer.seekTo(progress);
                    mHandler.removeCallbacks(mRunnable);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    mCurrentProgressTextView.setText(String.format("%02d:%02d", minutes,seconds));

                    updateSeekBar();

                } else if (mMediaPlayer == null && fromUser) {
                    prepareMediaPlayerFromPoint(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable);
                    mMediaPlayer.seekTo(seekBar.getProgress());

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    mCurrentProgressTextView.setText(String.format("%02d:%02d", minutes,seconds));
                    updateSeekBar();
                }
            }
        });

        mPlayButton = view.findViewById(R.id.playBackControl);
        mPlayButton.setOnClickListener(v -> {
            onPlay(isPlaying);
            isPlaying = !isPlaying;
        });

        mFileNameTextView.setText(getFormatFileName(recordedItem.fileName));
        mFileLengthTextView.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            stopPlaying();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopPlaying();
        }
    }

    private void prepareMediaPlayerFromPoint(int progress) {
        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(recordedItem.fileName);
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(progress);
            mMediaPlayer.start();

            mMediaPlayer.setOnCompletionListener(mp -> stopPlaying());
        } catch (IOException e) {
            Toast.makeText(getContext(), "OOPs something wrong, please try again", Toast.LENGTH_LONG).show();
        }
    }

    // Play start/stop
    private void onPlay(boolean isPlaying){
        if (!isPlaying) {
            //currently MediaPlayer is not playing audio
            if(mMediaPlayer == null) {
                startPlaying(); //start from beginning
            } else {
                resumePlaying(); //resume the currently paused MediaPlayer
            }

        } else {
            //pause the MediaPlayer
            pausePlaying();
        }
    }

    private void startPlaying() {
        mPlayButton.setImageResource(R.drawable.ic_circle_pause_light_blue);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(recordedItem.fileName);
            mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                //stopPlaying();
                mPlayButton.setImageResource(R.drawable.ic_circle_replay_light_blue);
                isPlaying = false;
            });
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.start();

            updateSeekBar();
        } catch (IOException e) {
            Toast.makeText(getContext(), "OOPs something wrong, please try again", Toast.LENGTH_LONG).show();
        }
    }

    private void pausePlaying() {
        mPlayButton.setImageResource(R.drawable.ic_circle_play_light_blue);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.pause();
    }

    private void resumePlaying() {
        mPlayButton.setImageResource(R.drawable.ic_circle_pause_light_blue);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.start();
        updateSeekBar();
    }

    private void stopPlaying() {
        mPlayButton.setImageResource(R.drawable.ic_circle_play_light_blue);
        mHandler.removeCallbacks(mRunnable);
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mSeekBar.setProgress(mSeekBar.getMax());
        isPlaying = !isPlaying;

        mCurrentProgressTextView.setText(mFileLengthTextView.getText());
        mSeekBar.setProgress(mSeekBar.getMax());
    }

    private Runnable mRunnable = () -> {
        if(mMediaPlayer != null) {
            int mCurrentPosition = mMediaPlayer.getCurrentPosition();
            mSeekBar.setProgress(mCurrentPosition);

            long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition)
                    - TimeUnit.MINUTES.toSeconds(minutes);
            mCurrentProgressTextView.setText(String.format("%02d:%02d", minutes, seconds));

            updateSeekBar();
        }
    };

    private void updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000);
    }

    private String getFormatFileName(String rawFileName) {
        return rawFileName.substring(rawFileName.lastIndexOf("/") + 1);
    }
}
