package com.example.user.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class SendActivity extends AppCompatActivity{
    private UserInfo user;

    private EditText message;
    private TextView gpsLa;
    private TextView gpsLo;

    private RadioGroup sendTime;
    private RadioGroup sendUrgent;

    private ImageView img;
    private Button buttonSend;
    private Button buttonGps;
    private Button buttonPic;

    private String sendTimeString;
    private String sendUrgentString;

    private ChatData chatData;
    //gps
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    private GPSInfo gps;

    private Uri photoURI;
    private int usePic;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK) return;
        if(requestCode == 0 && resultCode == RESULT_OK){
            usePic = 1;
            photoURI = data.getData();
            Log.d("SendActivity","uri:"+String.valueOf(photoURI));
            try{

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

        chatData = new ChatData();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        gpsLa = (TextView)findViewById(R.id.textLatitude);
        gpsLo = (TextView)findViewById(R.id.textLongitude);

        img = (ImageView)findViewById(R.id.sendImageView);
        usePic = 0;

        buttonPic = findViewById(R.id.buttonPic);
        buttonPic.getBackground().setColorFilter(0xFF0099cc, PorterDuff.Mode.MULTIPLY);
        buttonPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, 0);
            }
        });

        buttonSend = findViewById(R.id.sendButton);
        buttonSend.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getText().toString().isEmpty() || sendTimeString.isEmpty() || sendUrgentString.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "내용을 모두 작성해주십시오.", Toast.LENGTH_SHORT).show();
                    return;
                }
                chatData.userGroup = "소속 : "+user.userGroup;
                chatData.userName = "사용자 : "+user.userName;
                chatData.messageExtra = "보고시기 : " + sendTimeString + " / 상황상태 : " +sendUrgentString;
                chatData.message = message.getText().toString();
                chatData.time = System.currentTimeMillis();
                final String key = FirebaseDatabase.getInstance().getReference("messages").push().getKey();
                if(usePic == 1) {
                    chatData.picName = photoURI.getLastPathSegment();
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl("gs://test-767b1.appspot.com");
                    StorageReference riversRef = storageRef.child("images/").child(key + "/" + photoURI.getLastPathSegment());
                    UploadTask uploadTask = riversRef.putFile(photoURI);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(getApplicationContext(), "업로드실패.", Toast.LENGTH_SHORT).show();

                            exception.printStackTrace();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "업로드 성공", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                FirebaseDatabase.getInstance().getReference("messages").child(key).setValue(chatData);
                Toast.makeText(getApplicationContext(), "상황이 성공적으로 전파되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        buttonGps = (Button) findViewById(R.id.buttonGps);
        buttonGps.getBackground().setColorFilter(0xFF0099cc, PorterDuff.Mode.MULTIPLY);
        buttonGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPermission){
                    callPermission();
                    return;
                }
                gps = new GPSInfo(SendActivity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {
                    //GPSInfo를 통해 알아낸 위도값과 경도값
                    chatData.gpsLatitude = (float)gps.getLatitude();
                    chatData.gpsLongitude = (float)gps.getLongitude();
                    gpsLa.setText("경도 : " + (float)chatData.gpsLatitude);
                    gpsLo.setText("위도 : " + (float)chatData.gpsLongitude);
                    //Geocoder
                    Geocoder gCoder = new Geocoder(SendActivity.this, Locale.getDefault());
                    List<Address> addr = null;
                    try{
                        addr = gCoder.getFromLocation(chatData.gpsLatitude,chatData.gpsLongitude,1);
                        Address a = addr.get(0);
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
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
            }
        });

        callPermission();

        message = (EditText) findViewById(R.id.inputText);

        sendTime = (RadioGroup) findViewById(R.id.radioGroupTime);
        sendTime.setOnCheckedChangeListener(timeGroupChangeListener);

        sendUrgent = (RadioGroup) findViewById(R.id.radioGroupUrgent);
        sendUrgent.setOnCheckedChangeListener(urgentGroupChangeListener);
        FirebaseDatabase.getInstance().getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(UserInfo.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

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

    //gps
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