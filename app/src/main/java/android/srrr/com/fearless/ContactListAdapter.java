package android.srrr.com.fearless;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {
    private ArrayList<Contact> listData;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View listItem = inflater.inflate(R.layout.contact_item_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    public ContactListAdapter(ArrayList<Contact> list){
        this.listData = list;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Contact myContact = listData.get(position);
        holder.C_Name.setText(listData.get(position).getName());
        holder.C_number.setText(listData.get(position).getPhone());
        holder.call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Call to: " + myContact.getName(), Toast.LENGTH_LONG).show();
            }
        });

        holder.message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Message to: " + myContact.getName(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView C_Name, C_number;
        ImageView call_btn, message_btn;

        public ViewHolder(View itemView) {
            super(itemView);

            this.C_Name = itemView.findViewById(R.id.alert_time_tv);
            this.C_number = itemView.findViewById(R.id.date_time_tv);
            this.call_btn = itemView.findViewById(R.id.call_list_btn);
            this.message_btn = itemView.findViewById(R.id.message_btn);
        }
    }
}
