package com.sha.srecorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sha.srecorder.R;
import com.sha.srecorder.adapter.SavedRecordingAdapter;
import com.sha.srecorder.database.AppDatabase;
import com.sha.srecorder.database.RecordedItem;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by Shahul Hameed Shaik on 02/12/2020.
 */
public class SavedRecordingFragment extends Fragment {

    private static final String POSITION = "position";

    private int position;
    private ProgressBar mProgressBar;
    private TextView noDataFound;
    private RecyclerView mRecyclerView;

    private SavedRecordingAdapter savedRecordingAdapter;

    public static SavedRecordingFragment newInstance(int position) {
        SavedRecordingFragment savedRecordingFragment = new SavedRecordingFragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, position);
        savedRecordingFragment.setArguments(b);

        return savedRecordingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt(POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_recordings, container, false);

        this.mProgressBar = view.findViewById(R.id.progressBar);
        this.noDataFound = view.findViewById(R.id.noDataFound);
        this.mRecyclerView = view.findViewById(R.id.recyclerView);
        this.mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        this.mRecyclerView.setLayoutManager(llm);
        this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        getRecordedItems();

        return view;
    }

    private void showProgress() {
        this.mProgressBar.setVisibility(View.VISIBLE);
        this.noDataFound.setVisibility(View.GONE);
        this.mRecyclerView.setVisibility(View.GONE);
    }

    private void hideProgress() {
        this.mProgressBar.setVisibility(View.GONE);
        this.noDataFound.setVisibility(View.GONE);
        this.mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showNoDataFound() {
        this.mProgressBar.setVisibility(View.GONE);
        this.mRecyclerView.setVisibility(View.GONE);
        this.noDataFound.setVisibility(View.VISIBLE);
    }

    private void getRecordedItems() {
        getObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getObserver());
    }

    private Observable<List<RecordedItem>> getObservable()  {
        return Observable.create(emitter -> {
            if (!emitter.isDisposed()) {

                List<RecordedItem> recordedItems = AppDatabase.getInstance(getContext()).recordedItemDao().getAll();

                emitter.onNext(recordedItems);
                emitter.onComplete();
            }
        });
    }

    private Observer<List<RecordedItem>> getObserver()  {
        Observer observer = new Observer<List<RecordedItem>>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                showProgress();
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}

            @Override
            public void onComplete() {}

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull List<RecordedItem> recordedItemList) {

                if (recordedItemList.isEmpty()) {
                    showNoDataFound();
                } else {
                    hideProgress();
                    savedRecordingAdapter = new SavedRecordingAdapter(getActivity(), recordedItemList);
                    mRecyclerView.setAdapter(savedRecordingAdapter);
                }
            }
        };

        return observer;
    }
}
