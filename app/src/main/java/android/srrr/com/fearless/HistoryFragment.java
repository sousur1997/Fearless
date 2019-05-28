package android.srrr.com.fearless;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.utilities.Tree;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static android.srrr.com.fearless.FearlessConstant.HISTORY_COLLECTION;
import static android.srrr.com.fearless.FearlessConstant.HISTORY_LIST_FILE;
import static android.srrr.com.fearless.FearlessConstant.PENDING_FILENAME;

public class HistoryFragment extends Fragment implements ValueEventListener{
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private String userId;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView history_view;
    private Gson gson = new Gson();
    private TreeSet<AlertEvent> historyList;
    private DatabaseReference reference;

    public HistoryFragment() {
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
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshLayout = getView().findViewById(R.id.refresh_layout);
        history_view = getView().findViewById(R.id.history_list_view);

        historyList = new TreeSet<AlertEvent>(new AlertComparator());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if(mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
            DatabaseReference reference = database.getReference();
            reference.child(HISTORY_COLLECTION).child(userId).addValueEventListener(this);
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mAuth.getCurrentUser() != null)
                    syncHistoryTask();
                else
                    refreshLayout.setRefreshing(false);
            }
        });

        syncHistoryTask();
        setupListView();
    }

    private void setupListView(){
        AlertEvent[] AlertArr;
        ArrayList<AlertEvent> AlertList;
        TreeSet<AlertEvent> set = new TreeSet<>(new AlertComparator());
        String jsonString = readJsonFile(HISTORY_LIST_FILE);
        Gson gson = new Gson();

        AlertArr = gson.fromJson(jsonString, AlertEvent[].class);
        try {
            set.addAll(Arrays.asList(AlertArr));
            AlertList = new ArrayList<>(set);
            HistoryListAdapter adapter = new HistoryListAdapter(AlertList);
            history_view.setHasFixedSize(true);
            history_view.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
            history_view.setAdapter(adapter);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void syncHistory(){
        Gson gson = new Gson();
        String pendingFileContent;
        AlertEvent[] pendingArr;
        List<AlertEvent> pendingList;
        Map<String, AlertEvent> finalMap = new HashMap<>();

        final File pendingFile = new File(getActivity().getFilesDir(), PENDING_FILENAME);
        if(pendingFile.exists()){ //if file is present, then there are some pending event history
            pendingFileContent = readJsonFile(PENDING_FILENAME);
            pendingArr = gson.fromJson(pendingFileContent, AlertEvent[].class);

            pendingList = new ArrayList<>(Arrays.asList(pendingArr));

            //if the event has at least one location history, then add it to the final list
            for(AlertEvent event : pendingList){
                if(event.hasLocationHistory()){
                    finalMap.put(event.getTimestamp(), event);
                }
            }
            DatabaseReference ref = database.getReference();
            ref.child(HISTORY_COLLECTION).child(userId).child("UpdateTime:" + new Long(System.currentTimeMillis()).toString()).setValue(finalMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        pendingFile.delete();
                        syncHistoryTask();
                    }
                }
            });
        }else{
            syncHistoryTask();
        }
    }

    private void syncHistoryTask(){
        try {
            DatabaseReference reference = database.getReference();
            reference.child(HISTORY_COLLECTION).child(userId).addValueEventListener(this);
            setupListView();
        }catch (Exception e){
            e.printStackTrace();
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

            listJson = fileContent.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listJson;
    }

    private void writeHistoryListFile(String filename, String content){
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history_tab_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.sync_history_menu_item){
            if(mAuth.getCurrentUser() != null) {
                refreshLayout.setRefreshing(true);
                syncHistory(); //update the pending history then update the menu
            }else {
                refreshLayout.setRefreshing(false);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        File jsonFile = new File(getActivity().getFilesDir(), HISTORY_LIST_FILE);
        if(jsonFile.exists()){
            jsonFile.delete();
        }
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            for (DataSnapshot snapChild : snapshot.getChildren()) {
                AlertEvent event = snapChild.getValue(AlertEvent.class);
                historyList.add(event);
            }
        }
        //make the JSON from updated list by taking from the server, also update file
        String historyListJson = gson.toJson(historyList);
        writeHistoryListFile(HISTORY_LIST_FILE, historyListJson);
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    class AlertComparator implements Comparator<AlertEvent> {

        @Override
        public int compare(AlertEvent e1, AlertEvent e2) {
            Long time1 = Long.parseLong(e1.getTimestamp());
            Long time2 = Long.parseLong(e2.getTimestamp());
            return time2.compareTo(time1);
        }
    }
}
