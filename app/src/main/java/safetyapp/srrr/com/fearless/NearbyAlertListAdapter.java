package safetyapp.srrr.com.fearless;

import android.content.Intent;
import safetyapp.srrr.com.fearless.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import static safetyapp.srrr.com.fearless.FearlessConstant.NEARBY_ALERT_OBJECT_KEY;

public class NearbyAlertListAdapter extends RecyclerView.Adapter<NearbyAlertListAdapter.Viewholder> {

    private ArrayList<NearbyAlertDataModel> alertList;
    private Gson gson;

    public static class Viewholder extends RecyclerView.ViewHolder{

        TextView alertTime;
        ImageView map_view_btn;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            this.alertTime = itemView.findViewById(R.id.sos_number_tv);
            this.map_view_btn = itemView.findViewById(R.id.message_btn);
        }
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View listItem = inflater.inflate(R.layout.history_item_layout, viewGroup, false);
        Viewholder viewHolder = new Viewholder(listItem);
        return viewHolder;
    }


    public NearbyAlertListAdapter(ArrayList<NearbyAlertDataModel> list) {
        this.alertList = list;
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder viewHolder, final int i) {
        final NearbyAlertDataModel myEvent = alertList.get(i);
        viewHolder.alertTime.setText(alertList.get(i).getReadableTime());
        viewHolder.map_view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),NearbyAlertMapActivity.class);
                intent.putExtra(NEARBY_ALERT_OBJECT_KEY,i); //sent uid to track a specific user location
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

}
