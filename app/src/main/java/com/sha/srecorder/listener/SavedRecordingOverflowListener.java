package com.sha.srecorder.listener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.sha.srecorder.BuildConfig;
import com.sha.srecorder.R;
import com.sha.srecorder.database.AppDatabase;
import com.sha.srecorder.database.RecordedItem;
import com.sha.srecorder.fragment.PlaybackFragment;

import java.io.File;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by Shahul Hameed Shaik on 03/12/2020.
 */
public class SavedRecordingOverflowListener implements OnOverflowItemClickListener{

    @Override
    public void onPlay(Context context, RecordedItem recordedItem) {
        FragmentTransaction transaction = ((FragmentActivity) context)
                .getSupportFragmentManager()
                .beginTransaction();

        PlaybackFragment playbackFragment = new PlaybackFragment().newInstance(recordedItem);
        playbackFragment.show(transaction, "playback_fragment");
    }

    @Override
    public void onShare(OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem) {
        Uri uri = FileProvider.getUriForFile(adapterListener.getContext(), BuildConfig.APPLICATION_ID + ".provider", new File(recordedItem.fileName));

        Intent shareIntent = new Intent();
        shareIntent.setType("audio/mp4");
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        Intent chooser = Intent.createChooser(shareIntent, adapterListener.getContext().getText(R.string.send_to));

        List<ResolveInfo> resInfoList = adapterListener.getContext().getPackageManager().queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            adapterListener.getContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        adapterListener.getContext().startActivity(chooser);
    }

    @Override
    public void onRename(OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem, final int position) {
        showRenameDialog(adapterListener, recordedItem, position);
    }

    @Override
    public void onDelete(OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem, final int position) {
        deleteRecordedItem(adapterListener, recordedItem, position);
    }

    private void showRenameDialog (OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(adapterListener.getContext());

        LayoutInflater inflater = LayoutInflater.from(adapterListener.getContext());
        View view = inflater.inflate(R.layout.dialog_rename, null);

        final EditText input = (EditText) view.findViewById(R.id.new_name);

        builder.setTitle(adapterListener.getContext().getString(R.string.dialog_title_rename));
        builder.setCancelable(true);
        builder.setPositiveButton(adapterListener.getContext().getString(R.string.dialog_action_ok),
                (dialog, id) -> {
                    try {
                        String newFileName = input.getText().toString().trim() + ".mp4";
                        renameRecordedItem(adapterListener, recordedItem, newFileName, position);
                    } catch (Exception e) {
                        Log.e(SavedRecordingOverflowListener.class.getName(), "exception", e);
                    }

                    dialog.cancel();
                });
        builder.setNegativeButton(adapterListener.getContext().getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteRecordedItem(OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem, final int position) {
        getDeleteObservable(adapterListener, recordedItem)
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getDeleteObserver(adapterListener, position));
    }

    private Observable<Integer> getDeleteObservable(OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem)  {
        return Observable.create(emitter -> {
            if (!emitter.isDisposed()) {
                //delete file from storage
                File file = new File(recordedItem.fileName);
                file.delete();

                AppDatabase.getInstance(adapterListener.getContext()).recordedItemDao().delete(recordedItem);
                emitter.onNext(1);
                emitter.onComplete();
            }
        });
    }

    private Observer<Integer> getDeleteObserver(OnRenameDeleteDBChangedListener adapterListener, int position)  {
        Observer observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}

            @Override
            public void onComplete() {}

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Integer s) {
                adapterListener.onDeleteRecordedItem(position);
            }
        };
        return observer;
    }


    private void renameRecordedItem(OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem, String newFileName, final int position) {
        getRenameObservable(adapterListener, recordedItem, newFileName)
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getRenameObserver(adapterListener, recordedItem.fileName, position));
    }

    private Observable<Integer> getRenameObservable(OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem, String newFileName)  {
        return Observable.create(emitter -> {
            File newFile = new File(adapterListener.getContext().getFilesDir()+"/"+newFileName);

            if (!emitter.isDisposed()) {
                if (newFile.exists() && !newFile.isDirectory()) {
                    emitter.onNext(-1);
                } else {
                    //File name is ready to rename
                    try {
                        File originalFile = new File(recordedItem.fileName);
                        if (originalFile.renameTo(newFile)) {
                            recordedItem.fileName = newFile.getAbsolutePath();
                            AppDatabase.getInstance(adapterListener.getContext()).recordedItemDao().update(recordedItem);
                            emitter.onNext(1);
                        } else {
                            emitter.onNext(-1);
                        }
                    } catch (Exception ex) {
                        emitter.onNext(-1);
                        Log.e(SavedRecordingOverflowListener.class.getName(), Log.getStackTraceString(ex));
                    }
                }

                emitter.onComplete();
            }
        });
    }

    private Observer<Integer> getRenameObserver(OnRenameDeleteDBChangedListener adapterListener, String fileName, final int position)  {
        Observer observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}

            @Override
            public void onComplete() {}

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Integer status) {
                if (status == -1) {
                    //file name is not unique, cannot rename file.
                    Toast.makeText(adapterListener.getContext(),
                            String.format(adapterListener.getContext().getString(R.string.toast_file_exists), fileName),
                            Toast.LENGTH_SHORT).show();
                } else {
                    adapterListener.onRenameRecordedItem(position);
                }
            }
        };
        return observer;
    }
}



//                        copyFile(oldFile, newFile);
//                        File oldFilePath = new File(recordedItem.filePath);
//                        oldFilePath.renameTo(file);

//                        boolean isRenamed = renameAppFile(adapterListener.getContext(), recordedItem.fileName.substring(recordedItem.fileName.lastIndexOf("/") +1), newFileName);



//    private renameFileName(Context context, String existingFileName, String newFileName) throws Exception{
////        File from = new File(filePath, existingFileName.substring(existingFileName.lastIndexOf("/") +1));
////        String replaceFileInExisting = existingFileName.replace(existingFileName.substring(existingFileName.lastIndexOf("/") +1), newFileName);
////        File to = new File(filePath, newFileName);
////        return from.renameTo(to);
////        return oldFile.renameTo(newFile);
//
//        File oldFile = context.getFileStreamPath(existingFileName.substring(existingFileName.lastIndexOf("/") +1));
//        File newFile = context.getFileStreamPath(newFileName);
//
//        copyFile(oldFile, newFile);
//    }

//    private void copyFile(File src, File dst) throws IOException {
//        FileChannel inChannel = new FileInputStream(src).getChannel();
//        FileChannel outChannel = new FileOutputStream(dst).getChannel();
//        try
//        {
////            inChannel.transferTo(0, inChannel.size(), outChannel);
//            inChannel.transferFrom(inChannel, outChannel, inChannel.size());
////            FileUtils.copyFile(src, dst, true);
//            src.delete();
//        }
//        finally
//        {
//            if (inChannel != null)
//                inChannel.close();
//            if (outChannel != null)
//                outChannel.close();
//        }
//    }



//    public static void copyFile(File sourceFile, File destFile) throws IOException {
//
//        if (!destFile.exists()) {
//            destFile.createNewFile();
//        }
//
//        FileChannel source = null;
//        FileChannel destination = null;
//
//        try {
//            source = new FileInputStream(sourceFile).getChannel();
//            destination = new FileOutputStream(destFile).getChannel();
//            destination.transferFrom(source, 0, source.size());
//            sourceFile.delete();
//        } finally {
//            if (source != null) {
//                source.close();
//            }
//            if (destination != null) {
//                destination.close();
//            }
//        }
//    }


//    public boolean renameAppFile(Context context, String originalFileName, String newFileName) {
//        File originalFile = context.getFileStreamPath(originalFileName);
//        File newFile = new File(originalFile.getParent(), newFileName);
//        return originalFile.renameTo(newFile);
//    }