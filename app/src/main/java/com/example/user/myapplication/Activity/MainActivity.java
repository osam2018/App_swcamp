package com.example.user.myapplication.Activity;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    //데이터베이스 접근용이를 위한 변소 선언
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference chkDatabaseReference;
    private ChildEventListener mChildEventListener;
    //상황들이 표시될 리스트뷰 선언
    private ListView mListView;
    //어레이 어댑터인 챗어댑터 선언
    private ChatAdapter mAdapter;

    //사용자 uid 불러올 변수 선언
    private String uid;

    //뒤로가기 버튼을 눌렀을때 메뉴액티비티 새로 생성후 전환, 지금액티비티 종료
    @Override
    public void onBackPressed() {
        startActivity(new Intent(MainActivity.this, MenuActivity.class));
        finish();
    }

    //액티비티 생성시 초기화 작업
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initFirebaseDatabase();
    }
    //리스트 뷰 객체 넣어주고 어댑터 생성, 리스트뷰에 지정
    private void initViews() {
        mListView = (ListView) findViewById(R.id.list_message);
        mAdapter = new ChatAdapter(this, 0);
        mListView.setAdapter(mAdapter);
    }

    //데이터베이스 초기화 및 설정
    private void initFirebaseDatabase() {
        //사용자 uid 불러오기
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("messages");
        chkDatabaseReference = mFirebaseDatabase.getReference("users").child(uid).child("received");

        //messages 하위항목에서 사용될 이벤트리스너 생성
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //messages 하위항목에 child가 하나씩 파악이 될때, 즉 chatData가 하나씩 접수될때 어댑터에 하나씩 추가한다
                ChatData chatData = dataSnapshot.getValue(ChatData.class);
                //데이터베이스에서 데이터가 삭제되었을때 파악을 대비해서 키값을 chatData에도 저장
                chatData.firebaseKey = dataSnapshot.getKey();
                //어댑터에 데이터 추가
                mAdapter.add(chatData);
                //스크롤 밑으로
                mListView.smoothScrollToPosition(mAdapter.getCount());

                //미수신 상황 파악을 위한 데이터베이스의 users - uid - received - 현재 받은 메시지의 키값 = 1로 설정한다.
                //NotReceivedActivity에서 상황을 리스트에 추가할 때 그 상황의 키가 여기에 등록되었는지 확인 후 등록되어있지 않다면 미수신 상황으로 파악.
                chkDatabaseReference.child(chatData.firebaseKey).setValue(1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            //삭제되었다면 어댑터에서 제거하는 코드 구현.
            //클라이언트에는 삭제하는 기능을 넣지는 않았음. 파이어베이스 콘솔에서 직접 삭제한다면 호출 될 함수
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
        //위 생성된 리스너를 데이터베이스 messages 하위항목의 child이벤트리스너로
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    //액티비티 종료 시 등록된 리스너도 제거
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseReference.removeEventListener(mChildEventListener);
    }
}
