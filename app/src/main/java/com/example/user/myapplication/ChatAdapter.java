package com.example.user.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listitem_chat, null);

            viewHolder = new ViewHolder();
            viewHolder.mTxtUserGroup = (TextView) convertView.findViewById(R.id.txt_userGroup);
            viewHolder.mTxtUserName = (TextView) convertView.findViewById(R.id.txt_userName);
            viewHolder.mTxtMessageExtra = (TextView) convertView.findViewById(R.id.txt_messageExtra);
            viewHolder.mTxtMessage = (TextView) convertView.findViewById(R.id.txt_message);
            viewHolder.mTxtGps = (TextView) convertView.findViewById(R.id.txt_gps);
            viewHolder.mTxtGpsAdress = (TextView) convertView.findViewById(R.id.txt_gpsAdress);
            viewHolder.mTxtTime = (TextView) convertView.findViewById(R.id.txt_time);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ChatData chatData = getItem(position);
        viewHolder.mTxtUserGroup.setText(chatData.userGroup);
        viewHolder.mTxtUserName.setText(chatData.userName);
        viewHolder.mTxtMessageExtra.setText(chatData.messageExtra);
        viewHolder.mTxtMessage.setText(chatData.message);
        if(chatData.gpsLatitude != 0 && chatData.gpsLongitude != 0) {
            viewHolder.mTxtGps.setText("보고좌표 : 위도(" + Float.toString(chatData.gpsLatitude) + "),경도(" + Float.toString(chatData.gpsLongitude) + ")");
            viewHolder.mTxtGpsAdress.setText("보고주소 : "+chatData.gpsAddress);
        }
        viewHolder.mTxtTime.setText("보고시간 : "+mSimpleDateFormat.format(chatData.time));
        return convertView;
    }

    private class ViewHolder {
        private TextView mTxtUserGroup;
        private TextView mTxtUserName;
        private TextView mTxtMessageExtra;
        private TextView mTxtMessage;
        private TextView mTxtGpsAdress;
        private TextView mTxtGps;
        private TextView mTxtTime;
    }
}
