package com.example.reagain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class BoardFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    ArrayList<ItemData> arr = new ArrayList<>();
    ArrayList<ItemData> reArrS = new ArrayList<>();

    SwipeRefreshLayout refreshLayout;
    ListView lvMain;
    ProgressBar progressBar;
    MyAdapter adapter;
    boolean isScrollListLast = false; // 스크롤이 끝에 닿았니?
    boolean mLockListView = false; //중복 체크 변수
    int pg = 1; // 페이지 번호
    String userId = "";

    public BoardFragment() {

    }


    TextView tvTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  v = inflater.inflate(R.layout.fragment_board, container, false);

        Log.d("aadd", "여기를 지나는가?");
        refreshLayout = v.findViewById(R.id.swiperefresh);

        lvMain = v.findViewById(R.id.board_list);
        v.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        new Intent(getActivity(), LoginActivity.class);
                savePref("autoLogin", "0");
                startActivity(intent);

            }
        });
        userId = getData("userid");
        progressBar = v.findViewById(R.id.progressBar);
        adapter = new MyAdapter(getActivity());
        lvMain.setAdapter(adapter);
        lvMain.setOnItemClickListener(this);
        lvMain.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 1. OnScrollListener.SCROLL_STATE_IDLE : 스크롤이 이동하지 않을때의 이벤트(즉 스크롤이 멈추었을때).
                // 2. lastItemVisibleFlag : 리스트뷰의 마지막 셀의 끝에 스크롤이 이동했을때.
                // 3. mLockListView == false : 데이터 리스트에 다음 데이터를 불러오는 작업이 끝났을때.
                // 1, 2, 3 모두가 true일때 다음 데이터를 불러온다.
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isScrollListLast && mLockListView == false) {
                    // 화면이 바닦에 닿을때 처리
                    // 로딩중을 알리는 프로그레스바를 보인다.
                    progressBar.setVisibility(View.VISIBLE);

                    // 다음 데이터를 불러온다.
                    pg++;
                    boardRequest(pg);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // firstVisibleItem : 화면에 보이는 첫번째 리스트의 아이템 번호.
                // visibleItemCount : 화면에 보이는 리스트 아이템의 갯수
                // totalItemCount : 리스트 전체의 총 갯수
                // 리스트의 갯수가 0개 이상이고, 화면에 보이는 맨 하단까지의 아이템 갯수가 총 갯수보다 크거나 같을때.. 즉 리스트의 끝일때. true
                isScrollListLast = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);

            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pg = 1;
                boardRequest(pg);
            }
        });
        pg = 1;
        boardRequest(pg);

        return v;
    }
    public void topView(View view){
        view.scrollTo(0,0);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pg = 1;
        boardRequest(pg);

    }

    private void boardRequest(int pg) {
        mLockListView = true;
        String url = "http://172.30.1.42:8081/insta/android_board_list";

        Log.d("aa", "" + url);
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        StringRequest myReq = new StringRequest(Request.Method.POST, url, SuccessBoardListener, ErrorListener) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("pg", String.valueOf(pg));
                params.put("userid", userId);
                return params;
            }
        };
        requestQueue.add(myReq);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                mLockListView = false;
            }
        },1000);
    }

    Response.Listener<String> SuccessBoardListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.d("boardlist", response);
            if (pg == 1) {
                arr.clear();
            }
            try {
                refreshLayout.setRefreshing(false);
                JSONArray jsonList = new JSONArray(response);
                Log.d("boardlist","json length : "+jsonList.length());
                for (int i = 0; i < jsonList.length(); i++) {
                    JSONObject obj1 = jsonList.optJSONObject(i);
                    arr.add(new ItemData(obj1.optString("idx"), obj1.optString("userid"),
                            obj1.optString("content"), obj1.optString("imgPath"), obj1.optString("logtime"),
                            obj1.optString("writeuserimg"), obj1.optString("likeCount"), obj1.optString("isChecked")));
                    JSONArray replyObj = obj1.optJSONArray("reply");
                    if (replyObj != null) {
                        for (int j = 0; j < replyObj.length(); j++) {
                            JSONObject replyObj2 = replyObj.optJSONObject(j);
                            reArrS.add(new ItemData(replyObj2.optString("replyIdx"), replyObj2.optString("userid"),
                                    replyObj2.optString("content"),obj1.optString("boardIdx")));
                        }
                    }
                }
                Log.d("boardlist","arr size : "+arr.size());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Response.ErrorListener ErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            refreshLayout.setRefreshing(false);
            Log.d("boardlist", "list add error");
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }


    class ItemHolder {
        TextView tvBoardWriter, tvBoardContent, tvBoardLogtime, tvLikeCount, tvBoardReply;
        TextView tvReplyPrv1,tvReplyPrv2;
        ImageView imgBoardImage, icBoardLike, icBoardDots;
        CircleImageView imgBoardProfile;
    }
    int pos;
    class MyAdapter extends ArrayAdapter implements View.OnClickListener {
        LayoutInflater lnf;

        public MyAdapter(Activity context) {
            super(context, R.layout.item, arr);
            lnf = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return arr.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return arr.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder viewHolder;
            if (convertView == null) {


                convertView = lnf.inflate(R.layout.item, parent, false);
                viewHolder = new ItemHolder();

                viewHolder.tvBoardWriter = convertView.findViewById(R.id.board_writer);
                viewHolder.tvBoardContent = convertView.findViewById(R.id.board_content);
                viewHolder.tvBoardLogtime = convertView.findViewById(R.id.board_logtime);
                viewHolder.tvLikeCount = convertView.findViewById(R.id.board_like_count);
                viewHolder.imgBoardProfile = convertView.findViewById(R.id.board_profile);
                viewHolder.imgBoardImage = convertView.findViewById(R.id.board_img);
                viewHolder.icBoardLike = convertView.findViewById(R.id.board_like);
                viewHolder.icBoardDots = convertView.findViewById(R.id.board_dots);
                viewHolder.tvBoardReply = convertView.findViewById(R.id.reply);

//                viewHolder.tvReplyPrv1 = convertView.findViewById(R.id.reply_prv1);
//                viewHolder.tvReplyPrv2 = convertView.findViewById(R.id.reply_prv2);


                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ItemHolder) convertView.getTag();
            }

            viewHolder.tvBoardWriter.setText(arr.get(position).userid);
            viewHolder.tvBoardContent.setText(arr.get(position).content);
            viewHolder.tvBoardLogtime.setText(arr.get(position).logtime);
            viewHolder.tvLikeCount.setText(arr.get(position).likeCount + "명이 좋아합니다");

            if(reArrS != null) {
                for (int i = 0; i < reArrS.size(); i++) {
                    if (arr.get(position).idx == reArrS.get(i).boardIdx)
                        viewHolder.tvReplyPrv1.setText(reArrS.get(i).content);
                }
            }else{
                viewHolder.tvReplyPrv1.setText("");
            }

            if(userId.equalsIgnoreCase(arr.get(position).userid)){
                viewHolder.icBoardDots.setVisibility(View.VISIBLE);
            }else{
                viewHolder.icBoardDots.setVisibility(View.INVISIBLE);
            }
//            viewHolder.imgBoardImage.setText(arr.get(position).imgPath);
//            viewHolder.icBoardLike.setText(arr.get(position).getLike());

            viewHolder.icBoardLike.setOnClickListener(this);
            viewHolder.icBoardLike.setTag(position);


            String profileImg = arr.get(position).writeuserimg;
            String mainImg = arr.get(position).imgPath;
            //스토리지에서 찾아라!!!
            String profilUrl = "http://172.30.1.42:8081/insta/profile_img/" + profileImg;
            String imglUrl = "http://172.30.1.42:8081/insta/storage/" + mainImg;

            Glide.with(getActivity())
                    .load(imglUrl)
                    .error(R.drawable.no_image)
                    .into(viewHolder.imgBoardImage);
            String isChecked = arr.get(position).isChecked;
            int heart_draw = 0;
            if (isChecked.equalsIgnoreCase("1")) {
                heart_draw = R.drawable.heart_fas;
                Log.d("aadd", "1이들어왔다!!" + isChecked);
            } else {
                heart_draw = R.drawable.heart_far;
                Log.d("aadd", "0이들어왔다!!" + isChecked);
            }
            Glide.with(getActivity())
                    .load(heart_draw)
                    .into(viewHolder.icBoardLike);
            Glide.with(getActivity())
                    .load(profilUrl)
                    .circleCrop()
                    .error(R.drawable.unimg)
                    .into(viewHolder.imgBoardProfile);

            viewHolder.tvBoardReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ReplyActivity.class);
                    intent.putExtra("idx",arr.get(position).idx);
                    intent.putExtra("postUserId",arr.get(position).userid);
                    intent.putExtra("content",arr.get(position).content);
                    intent.putExtra("writeUserImg",arr.get(position).writeuserimg);
                    intent.putExtra("loginUser",userId);
                    startActivity(intent);
                }
            });

            viewHolder.icBoardDots.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.board_menu,popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.menu1 :
                                    showToast("수정이다!!");
                                    break;
                                case R.id.menu2 :
                                    showToast("삭제다!!");
                                    params.clear();
                                    params.put("idx", arr.get(position).idx);
                                    request("android_board_delete");
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });

            return convertView;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.board_like) {
                pos = Integer.parseInt(String.valueOf(v.getTag()));
                params.clear();
                params.put("idx", arr.get(pos).idx);
                params.put("userid", userId);
                request("android_like");
            }
        }
    }

    @Override
    public void response(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.getString("code");
            if(code.equalsIgnoreCase("505")){
                arr.clear();
                pg = 1;
                boardRequest(pg);
            }else {
                String likeCount = jsonObject.optString("likeCount");
                int likeCheck = Integer.parseInt(jsonObject.optString("isChecked"));
                String isChecked = String.valueOf(likeCheck==0?1:0);
                Log.d("aa",isChecked);

                arr.get(pos).likeCount = likeCount;
                arr.get(pos).isChecked = isChecked;
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }


//    private void replyRequestSmall(String reIdx) {
//        String url = "http://172.30.1.42:8081/insta/android_reply_list";
//
//        Log.d("aa", "" + url);
//        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
//
//        StringRequest myReq = new StringRequest(Request.Method.POST, url, SuccessReplyListener, ErrorListener) {
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("idx", reIdx);
//                return params;
//            }
//        };
//        requestQueue.add(myReq);
//    }
//
//    Response.Listener<String> SuccessReplyListener = new Response.Listener<String>() {
//
//        @Override
//        public void onResponse(String response) {
//            Log.d("post", response);
//
//            try {
//                JSONArray jsonList = new JSONArray(response);
//                if(jsonList != null) {
//                    for (int i = 0; i < jsonList.length(); i++) {
//                        JSONObject obj1 = jsonList.optJSONObject(i);
//                        reArrS.add(new ItemData(obj1.optString("replyIdx"), obj1.optString("userid"),
//                                obj1.optString("content"),obj1.optString("boardIdx")));
//                    }
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            adapter.notifyDataSetChanged();
//        }
//    };
}