package com.example.user.myapplication;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
public class ChatAdapter extends ArrayAdapter<ChatData> {
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm", Locale.getDefault());
    public ChatAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        ChatData chatData = getItem(position);
        //논리 상 완벽하나 이미지 로드가 정상작동하지 않아서, 성능 상 손해를 감수하고 if문 삭제하고 매번 불러오는 것으로 수정..
       // if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listitem_chat, null);

            viewHolder = new ViewHolder();
            viewHolder.mTxtUserGroup = convertView.findViewById(R.id.txt_userGroup);
            viewHolder.mTxtUserName =  convertView.findViewById(R.id.txt_userName);
            viewHolder.mTxtMessageExtra =  convertView.findViewById(R.id.txt_messageExtra);
            viewHolder.mTxtMessage =  convertView.findViewById(R.id.txt_message);
            viewHolder.mImg =  convertView.findViewById(R.id.img);
            viewHolder.mTxtGps =  convertView.findViewById(R.id.txt_gps);
            viewHolder.mTxtGpsAdress =  convertView.findViewById(R.id.txt_gpsAdress);
            viewHolder.mTxtTime =  convertView.findViewById(R.id.txt_time);

            convertView.setTag(viewHolder);
        //} else {
        //    viewHolder = (ViewHolder) convertView.getTag();
        //}
        viewHolder.mTxtUserGroup.setText(chatData.userGroup);
        viewHolder.mTxtUserName.setText(chatData.userName);
        viewHolder.mTxtMessageExtra.setText(chatData.messageExtra);
        viewHolder.mTxtMessage.setText(chatData.message +"/"+chatData.picName);
        if(!chatData.picName.isEmpty()) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://test-767b1.appspot.com").child("images/").child(chatData.firebaseKey + "/" + chatData.picName);
            GlideApp.with(getContext())
                    .load(storageRef)
                    .into(viewHolder.mImg);
        } else {
            //viewHolder.mImg.setVisibility(View.INVISIBLE);
        }

        if(chatData.gpsLatitude != 0 && chatData.gpsLongitude != 0) {
            viewHolder.mTxtGps.setText("보고좌표 : 위도(" + Float.toString(chatData.gpsLatitude) + "),경도(" + Float.toString(chatData.gpsLongitude) + ")");
            viewHolder.mTxtGpsAdress.setText("보고주소 : "+chatData.gpsAddress);
        } else {
            viewHolder.mTxtGps.setText("보고좌표 없음");
            viewHolder.mTxtGpsAdress.setHeight(0);
        }
        viewHolder.mTxtTime.setText("보고시간 : "+mSimpleDateFormat.format(chatData.time));
        return convertView;
    }

    private class ViewHolder {
        private TextView mTxtUserGroup;
        private TextView mTxtUserName;
        private TextView mTxtMessageExtra;
        private TextView mTxtMessage;
        private ImageView mImg;
        private TextView mTxtGpsAdress;
        private TextView mTxtGps;
        private TextView mTxtTime;
    }
}
