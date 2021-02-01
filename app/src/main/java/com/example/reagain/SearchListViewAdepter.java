package com.example.reagain;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchListViewAdepter extends BaseAdapter {
    Context context;
    ArrayList<ItemData> arr;
    List<ItemData> list = null;
    LayoutInflater lnf;

    public SearchListViewAdepter(Context context, List<ItemData> list) {
        this.context = context;
        this.list = list;
        lnf = LayoutInflater.from(context);
        this.arr = new ArrayList<>();
        this.arr.addAll(list);

    }

    public class ItemHolder {
        CircleImageView imgSearchProfile;
        TextView tvSearchId;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder viewHolder;
        ItemData data = list.get(position);

        if (convertView == null) {


            convertView = lnf.inflate(R.layout.item_search, parent, false);
            viewHolder = new ItemHolder();

            viewHolder.tvSearchId = convertView.findViewById(R.id.tv_search_id);
            viewHolder.imgSearchProfile = convertView.findViewById(R.id.img_search_profile);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ItemHolder) convertView.getTag();
        }

        viewHolder.tvSearchId.setText(data.userid);

        String profileImg = data.writeuserimg;
        String profilUrl = "http://172.30.1.42:8081/insta/profile_img/" + profileImg;

        Glide.with(context)
                .load(profilUrl)
                .circleCrop()
                .error(R.drawable.unimg)
                .into(viewHolder.imgSearchProfile);

        return convertView;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        // 문자를 입시력시 초기화 시켜준다.
        list.clear();
        if (charText.length() == 0) {
            list.addAll(arr); // 문자 입력이 없을때는 모두 보여준다
        } else {// 문자를 입력할때..

            // 리스트의 모든 데이터를 검색한다
            for (ItemData data : arr) {
                String userid = context.getResources().getString(Integer.parseInt(data.userid));

                // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
                if (userid.toLowerCase().contains(charText)) {
                    // 검색된 데이터를 리스트에 추가한다
                    list.add(data);
                }
            }
        }
        notifyDataSetChanged();
    }

}
