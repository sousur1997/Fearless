package android.srrr.com.fearless;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.srrr.com.fearless.FearlessConstant.HISTORY_INDEX_KEY;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.ViewHolder> {
    private ArrayList<AlertEvent> listData;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View listItem = inflater.inflate(R.layout.history_item_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    public HistoryListAdapter(ArrayList<AlertEvent> list){
        this.listData = list;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final AlertEvent myEvent = listData.get(position);
        holder.alertTime.setText(listData.get(position).getReadableTime());

        holder.map_view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MapPathActivity.class);
                intent.putExtra(HISTORY_INDEX_KEY, position); //sent position to open list of that position
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView alertTime;
        ImageView map_view_btn;

        public ViewHolder(View itemView) {
            super(itemView);
            this.alertTime = itemView.findViewById(R.id.date_time_tv);
            this.map_view_btn = itemView.findViewById(R.id.message_btn);
        }
    }
}
