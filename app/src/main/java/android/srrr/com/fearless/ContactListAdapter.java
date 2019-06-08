package android.srrr.com.fearless;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static android.srrr.com.fearless.FearlessConstant.CONTACT_LIST_INDEX_EXTRA;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_NAME_EXTRA;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_PHONE_EXTRA;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_UPDATE_REQUEST;
import static android.srrr.com.fearless.FearlessConstant.MAX_CONTACT_TO_ADD;
import static android.srrr.com.fearless.FearlessConstant.SOS_NUMBER_COUNT;

public class ContactListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Object> listData;
    private Context context;
    private ContactUpdateListener listener;
    private ActivityResultCallback callback;

    public interface ActivityResultCallback{
        void resultCallback(Object object, int index);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        final RecyclerView.ViewHolder holder;
        final View view;
        switch (viewType){
            case R.layout.contact_header_layout:
                view = inflater.inflate(R.layout.contact_header_layout, parent, false);
                holder = new StringViewHolder(view);
                break;
            case R.layout.contact_item_layout:
                view = inflater.inflate(R.layout.contact_item_layout, parent, false);
                holder = new PersonalContactHolder(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PersonalContact personalContact = new PersonalContact(((PersonalContactHolder) holder).C_Name.getText().toString(), ((PersonalContactHolder) holder).C_number.getText().toString());
                        callback.resultCallback(personalContact, holder.getAdapterPosition());
                    }
                });
                break;
            case R.layout.sos_item_layout:
                view = inflater.inflate(R.layout.sos_item_layout, parent, false);
                holder = new SOSContactHolder(view);
                break;
            default:
                view = inflater.inflate(R.layout.contact_header_layout, parent, false);
                holder = new StringViewHolder(view);
                break;
        }
        return holder;
    }
    public ArrayList<Object> getListItem(){
        return listData;
    }

    public ContactListAdapter(Context context, ActivityResultCallback callback, ArrayList<Object> list, ContactUpdateListener listener){
        this.context = context;
        this.listData = list;
        this.callback = callback;
        this.listener = listener; //set up the contact listener
    }

    @Override
    public int getItemViewType(int position) {
        if(listData.get(position) instanceof String){
            return R.layout.contact_header_layout;
        }else if(listData.get(position) instanceof PersonalContact){
            return R.layout.contact_item_layout;
        }else{
            return R.layout.sos_item_layout;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof StringViewHolder){
            ((StringViewHolder)holder).setHeaderText((String)listData.get(position));
        }else if(holder instanceof SOSContactHolder){
            final Contact myContact = (Contact) listData.get(position);
            ((SOSContactHolder)holder).C_Name.setText(((Contact) listData.get(position)).getName());
            ((SOSContactHolder)holder).C_number.setText(((Contact) listData.get(position)).getPhone());
            ((SOSContactHolder)holder).call_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callContact(myContact.getPhone());
                }
            });
        }else if(holder instanceof PersonalContactHolder){
            final PersonalContact myContact = (PersonalContact) listData.get(position);
            ((PersonalContactHolder)holder).C_Name.setText(((PersonalContact) listData.get(position)).getName());
            ((PersonalContactHolder)holder).C_number.setText(((PersonalContact) listData.get(position)).getPhone());
            ((PersonalContactHolder)holder).call_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callContact(myContact.getPhone());
                }
            });

            ((PersonalContactHolder)holder).message_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendSMS(myContact.getPhone());
                }
            });
        }
    }

    private void sendSMS(String number){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("smsto:"+number));
        if(intent.resolveActivity(context.getPackageManager()) != null){
            context.startActivity(intent);
        }
    }

    private void callContact(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        context.startActivity(callIntent);
    }

    public void removeItem(final int pos, RecyclerView.ViewHolder viewHolder){
        final Object item = listData.get(pos);
        final int removedPos = pos;

        listData.remove(viewHolder.getAdapterPosition());
        notifyItemRemoved(viewHolder.getAdapterPosition());

        Snackbar snackbar = Snackbar.make(viewHolder.itemView, "" + item +" deleted.", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listData.add(removedPos, item);
                notifyItemInserted(pos);
            }
        });

        final View snackbarView = snackbar.getView();
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbarView.getLayoutParams();
        params.setMargins(params.leftMargin + 20, params.topMargin, params.rightMargin + 20, params.bottomMargin + 15);

        snackbarView.setLayoutParams(params);
        snackbar.setActionTextColor(viewHolder.itemView.getResources().getColor(R.color.action_text_color));
        snackbar.show();

        listener.onContactUpdate();
    }

    public void addItem(Object item, boolean newItem){
        if(listData.size() >= (SOS_NUMBER_COUNT + 2 + MAX_CONTACT_TO_ADD)){
            Toast.makeText(context, "Cannot add more than " + MAX_CONTACT_TO_ADD + " contacts", Toast.LENGTH_LONG).show();
            return;
        }
        if(!isPresent(item)){
            listData.add(item);
            notifyItemInserted(listData.size()-1);
        }else{
            if(newItem) {
                Toast.makeText(context, "Contact is already present", Toast.LENGTH_LONG).show();
            }
        }

        //if the items are new, not in the local file, the listener will not be used
        if(newItem){
            listener.onContactUpdate();
        }
    }

    public void updateItem(Object item, int index){
        if(item instanceof PersonalContact){
            ((PersonalContact)listData.get(index)).setName(((PersonalContact) item).getName());
            ((PersonalContact)listData.get(index)).setPhone(((PersonalContact) item).getPhone());
            notifyItemChanged(index);
            listener.onContactUpdate();
        }
    }

    private boolean isPresent(Object item){
        for(Object element : listData){
            if(element instanceof PersonalContact){
                if(((PersonalContact) element).getPhone().equals(((PersonalContact)item).getPhone())){
                    return true;
                }
            }
        }
        return false;
    }

    public void onItemMove(int fromPos, int toPos){
        Collections.swap(listData,fromPos, toPos);
        notifyItemMoved(fromPos, toPos);
        listener.onContactUpdate();
    }
    @Override
    public int getItemCount() {
        return listData.size();
    }

    public static class SOSContactHolder extends RecyclerView.ViewHolder{
        TextView C_Name, C_number;
        ImageView call_btn;

        public SOSContactHolder(View itemView) {
            super(itemView);

            this.C_Name = itemView.findViewById(R.id.sos_name_tv);
            this.C_number = itemView.findViewById(R.id.sos_number_tv);
            this.call_btn = itemView.findViewById(R.id.sos_call_list_btn);
        }
    }

    public static class StringViewHolder extends RecyclerView.ViewHolder{
        TextView headerText;

        public StringViewHolder(@NonNull View itemView) {
            super(itemView);

            this.headerText = itemView.findViewById(R.id.contact_header_text);
        }
        public void setHeaderText(String text){
            this.headerText.setText(text);
        }
    }

    public static class PersonalContactHolder extends RecyclerView.ViewHolder{
        TextView C_Name, C_number;
        ImageView call_btn, message_btn;
        CardView cardView;

        public PersonalContactHolder(@NonNull View itemView) {
            super(itemView);
            this.cardView = itemView.findViewById(R.id.card);

            this.C_Name = itemView.findViewById(R.id.contact_name_tv);
            this.C_number = itemView.findViewById(R.id.contact_number_tv);
            this.call_btn = itemView.findViewById(R.id.contact_call_list_btn);
            this.message_btn = itemView.findViewById(R.id.contact_message_btn);
        }
    }
}
