package com.example.user.myapplication.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.myapplication.GlideApp;
import com.example.user.myapplication.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
        //viewHolder를 생성하고 각 변수에 각각 뷰를 다 불러온다
        //if (convertView == null) {
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

        //텍스트뷰에 데이터를 다 넣는다
        viewHolder.mTxtUserGroup.setText(chatData.userGroup);
        viewHolder.mTxtUserName.setText(chatData.userName);
        viewHolder.mTxtMessageExtra.setText(chatData.messageExtra);
        viewHolder.mTxtMessage.setText(chatData.message);

        //사진이름도 존재한다면
        if(!chatData.picName.isEmpty()) {
            //파이어베이스 storage에서 지금 해당 chatData의 키와 사진이름을 이용해 사진을 다운로드하고 이미지뷰에 띄운다
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://test-767b1.appspot.com").child("images/").child(chatData.firebaseKey + "/" + chatData.picName);
            GlideApp.with(getContext())
                    .load(storageRef)
                    .into(viewHolder.mImg);
        }

        //gps좌표도 초기화상태 값인 0이 아니라면 보고좌표와 주소를 띄운다
        if(chatData.gpsLatitude != 0 && chatData.gpsLongitude != 0) {
            viewHolder.mTxtGps.setText("보고좌표 : 위도(" + Float.toString(chatData.gpsLatitude) + "),경도(" + Float.toString(chatData.gpsLongitude) + ")");
            viewHolder.mTxtGpsAdress.setText("보고주소 : "+chatData.gpsAddress);
        } else {
            //좌표없다면 없다고 표시
            viewHolder.mTxtGps.setText("보고좌표 없음");
            //주소는 공백이 안생기게 높이 0으로 설정
            viewHolder.mTxtGpsAdress.setHeight(0);
        }
        //시간입력
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
