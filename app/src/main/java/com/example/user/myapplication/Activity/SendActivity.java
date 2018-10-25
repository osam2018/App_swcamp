package com.example.user.myapplication.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myapplication.Chat.ChatData;
import com.example.user.myapplication.GPSInfo;
import com.example.user.myapplication.R;
import com.example.user.myapplication.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class SendActivity extends AppCompatActivity{

    //상황을 보낼때 유저소속과 유저계급이름을 보낼때 저장해둘 변수
    private UserInfo user;
    //유저 uid 저장해둘 변수
    private String uid;

    //gps좌표를 가져올 버튼, 경도위도를 표시할 텍스트뷰
    private Button buttonGps;
    private TextView gpsLa;
    private TextView gpsLo;

    //상황 보고시간,상황상태 라디오박스그룹, 체크된것을 문자열화 해서 저정할 변수
    private String sendTimeString;
    private String sendUrgentString;
    private RadioGroup sendTime;
    private RadioGroup sendUrgent;

    //이미지를 불러올 버튼, 불러온 이미지 띄울 이미지뷰
    private Button buttonPic;
    private ImageView img;
    //사진 경로
    private Uri photoURI;
    //사진을 올릴건지 말건지 파악을 위한 변수
    private int usePic;

    //내용작성할 에디트텍스트
    private EditText message;

    //상황전송 버튼
    private Button buttonSend;

    //전송할 ChatData변수
    private ChatData chatData;
    //gps사용 용이를 위한 변수들 선언
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    private GPSInfo gps;

    //사진선택 할 시 호출됨
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK) return;
        if(requestCode == 0 && resultCode == RESULT_OK){
            //사진 사용한다고 1로 설정
            usePic = 1;
            //사진경로 불러옴
            photoURI = data.getData();
            try{
                //사진경로 이용해 사진을 비트맵으로 전환 후 이미지뷰에 띄움
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                img.setImageBitmap(bitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        //변수들 초기화
        chatData = new ChatData();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        user = new UserInfo();
        sendTimeString = "";
        sendUrgentString = "";
        gpsLa = (TextView)findViewById(R.id.textLatitude);
        gpsLo = (TextView)findViewById(R.id.textLongitude);
        img = (ImageView)findViewById(R.id.sendImageView);
        usePic = 0;

        //사진버튼 가져오고 색깔 하늘색으로 지정
        buttonPic = findViewById(R.id.buttonPic);
        buttonPic.getBackground().setColorFilter(0xFF0099cc, PorterDuff.Mode.MULTIPLY);
        //사진버튼 클릭 시 사진선택하는 액티비티로 전환
        buttonPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, 0);
            }
        });

        //상황전파버튼 불러오고 색 빨간색 지정
        buttonSend = findViewById(R.id.sendButton);
        buttonSend.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        //버튼 클릭 시
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //라디오 버튼 클릭 안했을 시 예외처리
                if (message.getText().toString().isEmpty() || sendTimeString.isEmpty() || sendUrgentString.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "내용을 모두 작성해주십시오.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //chatData에 입력된 값 다 넣어준다
                chatData.userGroup = "소속 : "+user.userGroup;
                chatData.userName = "사용자 : "+user.userName;
                chatData.messageExtra = "보고시기 : " + sendTimeString + " / 상황상태 : " +sendUrgentString;
                chatData.message = message.getText().toString();
                chatData.time = System.currentTimeMillis();

                //일단 messages에 push해서 키값 만들어놓고
                final String key = FirebaseDatabase.getInstance().getReference("messages").push().getKey();
                //나중에 상황을 띄울때 사진을 다운받아서 띄우려면 사진이름이 필요하니까 사진이름을 데이터베이스에 저장할 chatData에도 넣어준다
                if(usePic == 1) chatData.picName = photoURI.getLastPathSegment();
                //아까 push한 키값에 chatData 저장
                FirebaseDatabase.getInstance().getReference("messages").child(key).setValue(chatData);
                //파이어베이스 storage에 사진전송
                if(usePic == 1) {
                    Toast.makeText(getApplicationContext(), "사진 전송 중입니다...", Toast.LENGTH_SHORT).show();
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl("gs://test-767b1.appspot.com");
                    //storage에 경로지정후
                    StorageReference riversRef = storageRef.child("images/").child(key + "/" + photoURI.getLastPathSegment());
                    //업로드
                    UploadTask uploadTask = riversRef.putFile(photoURI);
                    //업로드 성공하든 실패하던 끝난후에 메뉴액티비티로 넘어간다
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(getApplicationContext(), "사진 업로드 실패.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SendActivity.this, MenuActivity.class));
                            finish();
                            exception.printStackTrace();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "상황이 성공적으로 전파되었습니다.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SendActivity.this, MenuActivity.class));
                            finish();
                        }
                    });
                } else {
                    //애초에 사진선택 안했으면 그냥 바로 메뉴액티비티로 넘어간다
                    Toast.makeText(getApplicationContext(), "상황이 성공적으로 전파되었습니다.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SendActivity.this, MenuActivity.class));
                    finish();
                }
            }
        });

        //gps기능 이용할 버튼 지정
        buttonGps = (Button) findViewById(R.id.buttonGps);
        buttonGps.getBackground().setColorFilter(0xFF0099cc, PorterDuff.Mode.MULTIPLY);
        //버튼 클릭 시
        buttonGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //권한이 있는지 파악 후 권한부터 얻어온다
                if(!isPermission){
                    callPermission();
                    return;
                }
                //일단 이용할 GPSInfo 클래스 생성
                gps = new GPSInfo(SendActivity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {
                    //GPSInfo를 통해 알아낸 위도값과 경도값
                    chatData.gpsLatitude = (float)gps.getLatitude();
                    chatData.gpsLongitude = (float)gps.getLongitude();
                    gpsLa.setText("경도 : " + (float)chatData.gpsLatitude);
                    gpsLo.setText("위도 : " + (float)chatData.gpsLongitude);
                    //Geocoder이용해서 경도위도를 주소 값 문자열로 바꿀수 있다
                    Geocoder gCoder = new Geocoder(SendActivity.this, Locale.getDefault());
                    List<Address> addr = null;
                    try{
                        addr = gCoder.getFromLocation(chatData.gpsLatitude,chatData.gpsLongitude,1);
                        Address a = addr.get(0);
                        //얻은 주소값을 chatData에 저장
                        chatData.gpsAddress = a.getAddressLine(0);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    if (addr != null) {
                        if (addr.size()==0) {
                            Toast.makeText(SendActivity.this,"주소정보 없음", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    //gps를 사용할 수 없다면 설정창을 띄어서 직접 gps기능을 켜게한다.
                    gps.showSettingsAlert();
                }
            }
        });
        callPermission();

        //내용 객체 불러오기
        message = (EditText) findViewById(R.id.inputText);

        //라디오 그룹 불러오고 라디오버튼 체크리스너 등록
        sendTime = (RadioGroup) findViewById(R.id.radioGroupTime);
        sendTime.setOnCheckedChangeListener(timeGroupChangeListener);
        sendUrgent = (RadioGroup) findViewById(R.id.radioGroupUrgent);
        sendUrgent.setOnCheckedChangeListener(urgentGroupChangeListener);

        //유저계급성명, 유저소속 불러오기
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("userName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user.userName = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("userGroup").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user.userGroup = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //라디오그룹 체크리스너로 체크되엇으면 해당 변수에 내용 입력
    RadioGroup.OnCheckedChangeListener timeGroupChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.radio_first)
                sendTimeString = "최초";
            else if (checkedId == R.id.radio_mid)
                sendTimeString = "중간";
            else if (checkedId == R.id.radio_last)
                sendTimeString = "최종";
        }
    };
    RadioGroup.OnCheckedChangeListener urgentGroupChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.radio_high)
                sendUrgentString = "긴급";
            else if (checkedId == R.id.radio_soso)
                sendUrgentString = "보통";
            else if (checkedId == R.id.radio_low)
                sendUrgentString = "양호";
        }
    };

    //gps이용을 위한 함수
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessFineLocation = true;
        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }
    private void callPermission() {

        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(SendActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(SendActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }
}