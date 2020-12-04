package com.sha.srecorder.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sha.srecorder.R;
import com.sha.srecorder.database.RecordedItem;
import com.sha.srecorder.listener.OnOverflowItemClickListener;
import com.sha.srecorder.listener.OnRenameDeleteDBChangedListener;
import com.sha.srecorder.listener.OnSavedRecordedItemClickListener;
import com.sha.srecorder.listener.SavedRecordingOverflowListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Shahul Hameed Shaik on 03/12/2020.
 */
public class SavedRecordingAdapter extends RecyclerView.Adapter<SavedRecordingAdapter.ViewHolder> implements OnRenameDeleteDBChangedListener {

    private Context context;
    private List<RecordedItem> recordedItemList = new ArrayList<>();
    private OnSavedRecordedItemClickListener clickListener;

    public SavedRecordingAdapter(Context context) {
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.save_recording_cell_view, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        RecordedItem recordedItem = this.recordedItemList.get(position);
        viewHolder.txtFileName.setText(getFormatFileName(recordedItem.fileName));

        long minutes = TimeUnit.MILLISECONDS.toMinutes(recordedItem.recordedLength);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(recordedItem.recordedLength)
                - TimeUnit.MINUTES.toSeconds(minutes);

        viewHolder.txtFileLength.setText(String.format("%02d:%02d", minutes, seconds));

        String recordedDate = DateUtils.formatDateTime(
                viewHolder.txtFileName.getContext(),
                recordedItem.recordedTime,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
        );

//        String recordedDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date(recordedItem.recordedTime));

        viewHolder.txtRecordedDate.setText(recordedDate);
        viewHolder.bindClickListener(this.clickListener, recordedItem);

        viewHolder.textViewOptions.setOnClickListener(view -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(context, viewHolder.textViewOptions);
            //inflating menu from xml resource
            popup.inflate(R.menu.saved_recording_overflow);
            //adding click listener
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.play:
                        //handle play click
                        viewHolder.onOverflowItemClickListener.onPlay(this.context, this.recordedItemList.get(position));
                        break;
                    case R.id.share:
                        //handle rename click
                        viewHolder.onOverflowItemClickListener.onShare(this, this.recordedItemList.get(position));
                        break;
                    case R.id.rename:
                        //handle rename click
                        viewHolder.onOverflowItemClickListener.onRename(this, this.recordedItemList.get(position), position);
                        break;
                    case R.id.delete:
                        //handle delete click
                        viewHolder.onOverflowItemClickListener.onDelete(this, this.recordedItemList.get(position), position);
                        break;
                }
                return false;
            });
            //displaying the popup
            popup.show();
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (this.recordedItemList.isEmpty()) {
            return 0;
        }
        return this.recordedItemList.size();
    }

    @Override
    public void onRenameRecordedItem(int position) {
        notifyItemChanged(position);
    }

    @Override
    public void onDeleteRecordedItem(int position) {
        this.recordedItemList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    public void newRecordedItemAdded(RecordedItem recordedItem) {
        this.recordedItemList.add(0, recordedItem);
        notifyDataSetChanged();
    }

    public void setRecordedItemList(List<RecordedItem> recordedItemList) {
        if (this.recordedItemList.size() > 0) {
            this.recordedItemList.addAll(recordedItemList);
        } else {
            this.recordedItemList = recordedItemList;
        }

        notifyDataSetChanged();
    }

    private String getFormatFileName(String rawFileName) {
        return rawFileName.substring(rawFileName.lastIndexOf("/") + 1);
    }

    public void setOnItemClickListener(OnSavedRecordedItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView txtFileName;
        TextView txtFileLength;
        TextView txtRecordedDate;
        TextView textViewOptions;
        OnOverflowItemClickListener onOverflowItemClickListener;

        public ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.card_view);
            imageView = view.findViewById(R.id.imageView);
            txtFileName = view.findViewById(R.id.txtFileName);
            txtFileLength = view.findViewById(R.id.txtFileLength);
            txtRecordedDate =  view.findViewById(R.id.txtFileDateAdded);
            textViewOptions =  view.findViewById(R.id.textViewOptions);
            onOverflowItemClickListener = new SavedRecordingOverflowListener();
        }

        public void bindClickListener(OnSavedRecordedItemClickListener onSavedRecordedItemClickListener, RecordedItem recordedItem) {
            cardView.setOnClickListener(v -> onSavedRecordedItemClickListener.onItemClick(recordedItem));
        }
    }
}
