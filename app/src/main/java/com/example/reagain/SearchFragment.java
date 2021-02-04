package com.example.reagain;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class SearchFragment extends BaseFragment {
    MyAdapter adapter;
    List<ItemData> list = new ArrayList<>();
    ArrayList<ItemData> searchArr = new ArrayList<>();

    ListView lv;
    EditText search_ed;
    TextView tvSubmit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(getActivity(),v);
        lv = v.findViewById(R.id.lv_search);
        search_ed = v.findViewById(R.id.ed_search);

        request("android_search_list");

        adapter = new MyAdapter(getActivity());
        lv.setAdapter(adapter);

        // 검색창
        search_ed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = search_ed.getText().toString()
                        .toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }
        });

        return v;
    }
    public void init() {

    }


    @Override
    public void response(String response) {
        Log.d("search", ""+response);
        try {
            JSONArray jsonList = new JSONArray(response);
            for (int i = 0; i < jsonList.length(); i++) {
                JSONObject obj1 = jsonList.optJSONObject(i);
                searchArr.add(new ItemData(obj1.optString("userid"), obj1.optString("profileImg")));
            }
            Log.d("search","list : "+list.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class ItemHolder {
        CircleImageView imgSearchProfile;
        TextView tvSearchId;

    }
    class MyAdapter extends ArrayAdapter {

        LayoutInflater lnf;

        public MyAdapter(Activity context) {
            super(context, R.layout.item_search, list);
            lnf = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

            Glide.with(getActivity())
                    .load(profilUrl)
                    .circleCrop()
                    .error(R.drawable.unimg)
                    .into(viewHolder.imgSearchProfile);

            return convertView;
        }

        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            // 문자를 입시력시 초기화 시켜준다.
            Log.d("search",""+charText);
            list.clear();
            if (charText.length() == 0) {
                Log.d("search","입력값이 없다!");
                list.addAll(searchArr); // 문자 입력이 없을때는 모두 보여준다
            } else {// 문자를 입력할때..

                // 리스트의 모든 데이터를 검색한다
                for (ItemData item : searchArr) {
//                    String userid = getActivity().getResources().getString(item.userid);

                    // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
                    if (item.userid.toLowerCase().contains(charText)) {
                        // 검색된 데이터를 리스트에 추가한다
                        list.add(item);
                    }
                }
            }
            notifyDataSetChanged();
        }

    }
}