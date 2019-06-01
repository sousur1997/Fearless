package android.srrr.com.fearless;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.CALL_PHONE;
import static android.srrr.com.fearless.FearlessConstant.CALL_PERMISSION;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_COLLECTION;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_LOCAL_FILENAME;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_UPLOAD_PENDING;
import static android.srrr.com.fearless.FearlessConstant.MAX_CONTACT_TO_ADD;
import static android.srrr.com.fearless.FearlessConstant.PICK_CONTACT;
import static android.srrr.com.fearless.FearlessConstant.PICK_CONTACT_PERMISSION;
import static android.srrr.com.fearless.FearlessConstant.REQUEST_MULTIPLE_PERMISSIONS;

public class SosContactFragment extends Fragment implements ContactUpdateListener {

    private RecyclerView contact_List_view;
    private ArrayList<Object> contact_item_list;
    private ColorDrawable swipeBackground = new ColorDrawable(Color.parseColor("#FFF7F7F7"));
    private Drawable deleteIcon;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String userId;
    private PersonalContact phoneContact;
    private ContactListAdapter adapter;
    private LinearLayoutManager layoutManager;
    private MenuItem uploadItem;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList<PersonalContact> contactArrayList;
    private PreferenceManager manager;
    private ArrayList<PersonalContact> initialList;

    public SosContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sos_contact, container, false);
    }

    private boolean runtime_contact_permission() {
        if (Build.VERSION.SDK_INT >= 21 && ContextCompat.checkSelfPermission(
                getActivity().getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{CALL_PHONE}, CALL_PERMISSION);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PICK_CONTACT_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                runtime_contact_permission();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_CONTACT){
            if(resultCode == Activity.RESULT_OK){
                Uri contactData = data.getData();
                Cursor c;
                phoneContact = new PersonalContact();
                c = getActivity().getContentResolver().query(contactData, null, null, null, null);
                if (c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    try {
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getActivity().getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            System.out.println("number is:" + cNumber);
                            phoneContact.setPhone(cNumber);
                        }
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        phoneContact.setName(name);

                        adapter.addItem(phoneContact, true);
                        contact_List_view.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contact_tab_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        uploadItem = menu.findItem(R.id.contact_upload);

        //when it is synced it will not be shown
        if(manager.getBool(CONTACT_UPLOAD_PENDING, false) == false){
            uploadItem.setVisible(false);
        }else{
            uploadItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.add_new_contact_menu_item){
            if(user != null){
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                getActivity().startActivityForResult(intent, PICK_CONTACT);
            }else{
                AlertDialog dialog;
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setTitle("Cannot Add Personal Contacts");
                dialogBuilder.setMessage("Guest users cannot add their personal contacts");
                dialogBuilder.setCancelable(false);
                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = dialogBuilder.create();
                dialog.show();
            }
            return true;
        }else if(id == R.id.contact_upload){
            contactSyncWithServer();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean addPermission(List<String> permissionsList,String permission) {
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);

            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),permission))
                return false;
        }
        return true;
    }

    private void AccessContact(){
        List<String> permissionsNeeded = new ArrayList<String> ();
        final List<String> permissionsList = new ArrayList<String> ();

        if(!addPermission(permissionsList,Manifest.permission.READ_CONTACTS)) {
            permissionsNeeded.add("Read Contacts");
        }

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                ActivityCompat.requestPermissions(getActivity(),permissionsList.toArray(new String[permissionsList.size()]),REQUEST_MULTIPLE_PERMISSIONS);
                /*showMessageOKCancel(message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });*/
                return;
            }
            ActivityCompat.requestPermissions(getActivity(),permissionsList.toArray(new String[permissionsList.size()]),REQUEST_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String name[] = getResources().getStringArray(R.array.help_line_labels);
        String nums[] = getResources().getStringArray(R.array.help_line_numbers);
        contact_item_list = new ArrayList<>();

        manager = new PreferenceManager(getActivity().getApplicationContext());

        refreshLayout = getView().findViewById(R.id.contact_refresh_layout);
        contact_List_view = getView().findViewById(R.id.contact_list_view);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());

        contact_List_view.setLayoutManager(layoutManager);

        deleteIcon = ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.delete_icon);

        //runtime_contact_permission();
        AccessContact();

        contact_item_list.add("Helpline (SOS) Numbers"); //header before SOS numbers
        for(int i = 0; i<name.length; i++){
            contact_item_list.add(new Contact(name[i], nums[i]));
        }

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            userId = user.getUid();
            contact_item_list.add("Personal Contact Numbers"); //header before personal contacts
        }else{
            //when user is not logged in, disable alert button
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mAuth.getCurrentUser() != null) {
                    //refresh contact from server
                    downloadContacts();
                }
                else {
                    refreshLayout.setRefreshing(false);
                }
            }
        });

        adapter = new ContactListAdapter(getActivity(), contact_item_list, this);
        contact_List_view.setHasFixedSize(true);
        contact_List_view.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        contact_List_view.setAdapter(adapter);

        fetchLocalContactFile();
        setupItemDragable(adapter);
    }

    private void contactSyncWithServer(){
        PersonalContact[] contactArr;

        Gson gson = new Gson();
        String jsonStr = readJsonFile(CONTACT_LOCAL_FILENAME); //read local file from array
        contactArr = gson.fromJson(jsonStr, PersonalContact[].class);
        final Map<String, PersonalContact> personalContacts = new HashMap<>();

        if(contactArr != null) {
            for (int i = 0; i<contactArr.length; i++) {
                if (contactArr[i] != null) {
                    personalContacts.put("" + i, contactArr[i]);
                }
            }
            refreshLayout.setRefreshing(true);
            firestore.collection(CONTACT_COLLECTION).document(userId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        firestore.collection(CONTACT_COLLECTION).document(userId).set(personalContacts).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    manager.setBool(CONTACT_UPLOAD_PENDING, false);
                                    refreshLayout.setRefreshing(false);
                                    uploadItem.setVisible(false);
                                    Toast.makeText(getActivity().getApplicationContext(), "Contact Sync Complete", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }else{
                        Toast.makeText(getActivity().getApplicationContext(), "Unable to Update", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void updateLocalContactFile() {
        //List to store only personal contacts
        contactArrayList = new ArrayList<>();
        for (Object item : adapter.getListItem()) {
            if (item instanceof PersonalContact) {
                contactArrayList.add((PersonalContact) item);
            }
        }

        Gson gson = new Gson();
        String jsonStr = gson.toJson(contactArrayList);
        writeContactListFile(CONTACT_LOCAL_FILENAME, jsonStr); //store contact into local storage

        manager.setBool(CONTACT_UPLOAD_PENDING, true); //contact is not synced with server
        if (uploadItem != null){
            uploadItem.setVisible(true);
        }
    }

    private void downloadContacts(){
        final Gson gson = new Gson();
        //download file from server

        initialList = new ArrayList<>();
        DocumentReference docRef = firestore.collection(CONTACT_COLLECTION).document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot != null) {
                        JsonElement jsonElement = gson.toJsonTree(snapshot.getData());
                        try {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();

                            for (int i = 0; i < MAX_CONTACT_TO_ADD; i++) {
                                if (jsonObject.get("" + i) != null) {
                                    PersonalContact contact = gson.fromJson(jsonObject.get("" + i), PersonalContact.class);
                                    initialList.add(i, contact);
                                }
                            }

                            //add fetched contact into list
                            String jsonStr = gson.toJson(initialList);
                            writeContactListFile(CONTACT_LOCAL_FILENAME, jsonStr); //store contact into local storage

                            for (PersonalContact ct : initialList) {
                                adapter.addItem(ct, false);
                            }
                        }catch (IllegalStateException e){
                            e.printStackTrace();
                        }
                        refreshLayout.setRefreshing(false);
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    refreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void fetchLocalContactFile(){
        PersonalContact[] contactArr;
        Gson gson = new Gson();

        File file = new File(getActivity().getFilesDir(), CONTACT_LOCAL_FILENAME);
        if(file.exists()) {
            String jsonStr = readJsonFile(CONTACT_LOCAL_FILENAME); //read local file from array
            contactArr = gson.fromJson(jsonStr, PersonalContact[].class);

            if (contactArr != null) {
                for (PersonalContact item : contactArr) {
                    if (item != null) {
                        adapter.addItem(item, false);
                    }
                }
            }
        }else{
            //if user is valid, then download contact from server
            if(user != null) {
                downloadContacts();
            }else{
                refreshLayout.setRefreshing(false);
            }
        }
    }

    private String readJsonFile(String filename){
        String listJson = "";
        int n;
        try {
            FileInputStream fis = getActivity().getApplicationContext().openFileInput(filename);
            StringBuffer fileContent = new StringBuffer();

            byte[] buffer = new byte[4096];
            while((n = fis.read(buffer)) != -1){
                fileContent.append(new String(buffer, 0, n));
            }
            fis.close();
            listJson = fileContent.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listJson;
    }

    private void writeContactListFile(String filename, String content){
        try {
            File jsonOpFile = new File(getActivity().getFilesDir(), filename);
            FileOutputStream fout = new FileOutputStream(jsonOpFile);
            OutputStreamWriter writer = new OutputStreamWriter(fout);

            writer.append(content); //add json into file
            writer.close();
            fout.flush();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupItemDragable(final ContactListAdapter adapter){
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT){

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
                if(source.getItemViewType() != target.getItemViewType()){
                    return false;
                }
                adapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if(viewHolder instanceof ContactListAdapter.StringViewHolder){
                    return 0;
                }
                if(viewHolder instanceof ContactListAdapter.SOSContactHolder){
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if(viewHolder instanceof ContactListAdapter.StringViewHolder){
                    return 0;
                }
                if(viewHolder instanceof ContactListAdapter.SOSContactHolder){
                    return 0;
                }
                return super.getMovementFlags(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    adapter.removeItem(viewHolder.getAdapterPosition(), viewHolder);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                ViewGroup.MarginLayoutParams paramDefault, paramChange;
                paramDefault = (ViewGroup.MarginLayoutParams) ((ContactListAdapter.PersonalContactHolder)viewHolder).cardView.getLayoutParams();
                paramChange = paramDefault;
                paramChange.setMargins(10, 2, 10, 2);

                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight())/2;
                if(dX < 0){
                    swipeBackground.setBounds(itemView.getRight() + (int)dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    deleteIcon.setBounds(itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth(), itemView.getTop()+ iconMargin,
                            itemView.getRight() - iconMargin, itemView.getBottom() - iconMargin);
                }

                swipeBackground.draw(c);
                c.save();

                if(dX < 0){
                    c.clipRect(itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth(), itemView.getTop()+ iconMargin,
                            itemView.getRight() - iconMargin, itemView.getBottom() - iconMargin);
                }
                deleteIcon.draw(c);

                if(dY < 0 || dY > 0){
                    ((ContactListAdapter.PersonalContactHolder)viewHolder).cardView.setCardBackgroundColor(ContextCompat.getColor(getActivity().getApplicationContext(),R.color.card_view_color_pressed));
                }
                if(dY == 0){
                    ((ContactListAdapter.PersonalContactHolder)viewHolder).cardView.setCardBackgroundColor(ContextCompat.getColor(getActivity().getApplicationContext(),R.color.card_view_color));
                }
                c.restore();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        };

        ItemTouchHelper helper = new ItemTouchHelper(simpleCallback);
        helper.attachToRecyclerView(contact_List_view);
    }

    @Override
    public void onContactUpdate() {
        updateLocalContactFile();
    }
}
