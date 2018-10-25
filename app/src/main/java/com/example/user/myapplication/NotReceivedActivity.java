package com.example.user.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotReceivedActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference chkDatabaseReference;
    private ChildEventListener mChildEventListener;
    // Views
    private ListView mListView;
    // Values
    private ChatAdapter mAdapter;
    private String lastKey;
    private String uid;

    private int chkTemp;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(NotReceivedActivity.this, MenuActivity.class));
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_received);

        mListView = (ListView) findViewById(R.id.list_message_notReceieved);
        mAdapter = new ChatAdapter(this, 0);
        mListView.setAdapter(mAdapter);

        chkTemp = 0;

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("messages");
        chkDatabaseReference = mFirebaseDatabase.getReference("users").child(uid).child("received");
        chkDatabaseReference.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lastKey = dataSnapshot.getValue().toString();
                lastKey = lastKey.substring(1, lastKey.length()-3);
                mDatabaseReference.addChildEventListener(mChildEventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (chkTemp == 1) {
                    ChatData chatData = dataSnapshot.getValue(ChatData.class);
                    chatData.firebaseKey = dataSnapshot.getKey();
                    mFirebaseDatabase.getReference("users").child(uid).child("received").child(chatData.firebaseKey).setValue(1);
                    mAdapter.add(chatData);
                    mListView.smoothScrollToPosition(mAdapter.getCount());
                }
                if(dataSnapshot.getKey().equals(lastKey)) chkTemp = 1;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String firebaseKey = dataSnapshot.getKey();
                int count = mAdapter.getCount();
                for (int i = 0; i < count; i++) {
                    if (mAdapter.getItem(i).firebaseKey.equals(firebaseKey)) {
                        mAdapter.remove(mAdapter.getItem(i));
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseReference.removeEventListener(mChildEventListener);
    }
}
