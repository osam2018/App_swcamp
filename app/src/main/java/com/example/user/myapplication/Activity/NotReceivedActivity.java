package com.example.user.myapplication.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.example.user.myapplication.Chat.ChatAdapter;
import com.example.user.myapplication.Chat.ChatData;
import com.example.user.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotReceivedActivity extends AppCompatActivity {

    //데이터베이스 접근용이를 위한 변수 선언
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference chkDatabaseReference;
    private ChildEventListener mChildEventListener;
    //상황들 표시할 리스트 뷰
    private ListView mListView;
    //리스트뷰에 연결할 어레이어댑터 선언
    private ChatAdapter mAdapter;

    //미수신상황을 파악하기 위한 마지막으로 수신된 상황의 키값을 저장할 변수
    private String lastKey;
    //사용자 uid 저장할 변수
    private String uid;

    //미수신상황 파악을 위한 임시 변수
    private int chkTemp;

    //뒤로가기 눌렀을때 메뉴로 이동
    @Override
    public void onBackPressed() {
        startActivity(new Intent(NotReceivedActivity.this, MenuActivity.class));
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_received);

        //리스트뷰 가져오고 어댑터 연결
        mListView = (ListView) findViewById(R.id.list_message_notReceieved);
        mAdapter = new ChatAdapter(this, 0);
        mListView.setAdapter(mAdapter);

        //임시변수 기본 0으로 저장
        chkTemp = 0;

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("messages");
        chkDatabaseReference = mFirebaseDatabase.getReference("users").child(uid).child("received");
        //users - uid - received 하위항복의 제일 마지막 키 값. 즉, 제일 마지막에 수신한 상황의 키 값 가져오기
        chkDatabaseReference.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lastKey = dataSnapshot.getValue().toString();
                // {-키값=1} 요런식의 스트링으로 얻어지니 앞의 {- 와 뒤의 =1}를 빼기위해 substring 사용
                lastKey = lastKey.substring(1, lastKey.length()-3);

                //리스너의 시간차가 있으니 키값을 얻은 후에 messages 하위항목 데이터를 불러오기 위한 리스너 등록
                //키값을 얻기 전에 메세지들 불러오면 낭패.
                mDatabaseReference.addChildEventListener(mChildEventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //messages 데이터 가져오기 위한 리스너 생성
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //밑에서 chkTemp를 1로 처리한 순간 부터 상황 접수
                if (chkTemp == 1) {
                    //chatData에 데이터 가져오고
                    ChatData chatData = dataSnapshot.getValue(ChatData.class);
                    chatData.firebaseKey = dataSnapshot.getKey();
                    //이제는 수신했으니 users - uid - received 하위항목에 이 상황의 키값도 저장한다.
                    mFirebaseDatabase.getReference("users").child(uid).child("received").child(chatData.firebaseKey).setValue(1);
                    //어댑터에 추가
                    mAdapter.add(chatData);
                    //스크롤 밑으로
                    mListView.smoothScrollToPosition(mAdapter.getCount());
                }

                //첫 상황부터 순서대로 가져와 지니 마지막으로 수신한 상황 다음부터는 미수신한 상황들.
                //위에서 얻어온 마지막으로 수신한 상황의 키값과 지금 불러온 데이터의 키값과 비교해서 일치하면, 그 다음부터 가져오는 데이터는 전부 미수신한 상황.
                //chkTemp 임시변수를 1로 만들어줘서 이제부터 데이터를 수신하도록 한다.
                if(dataSnapshot.getKey().equals(lastKey)) chkTemp = 1;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            //클라이언트에 제거할수 있는 기능은 안넣었지만
            //파이어베이스 콘솔에서 삭제할 시 삭제가 되게 기능하는 함수.
            //chatData에 저장된 키값과 삭제된 데이터의 키값을 비교해서 삭제한다
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

    //액티비티 종료 시 리스너해제
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseReference.removeEventListener(mChildEventListener);
    }
}
