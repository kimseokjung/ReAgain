package com.example.reagain;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
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

public class PostActivity extends BaseActivity implements View.OnClickListener {
    ArrayList<ItemData> arr = new ArrayList<>();

    CircleImageView userImg;
    TextView mainContent;
    ListView lvReply;
    EditText editReply;
    Button submitReply;
    MyAdapter adapter;

    String idx,postUserId,loginUser,content,writeUserImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        lvReply = findViewById(R.id.reply_list);
        userImg = findViewById(R.id.board_profile);
        mainContent = findViewById(R.id.board_content);
        editReply = findViewById(R.id.reply_ed);
        submitReply = findViewById(R.id.reply_btn);

       loginUser = getIntent().getStringExtra("loginUser");//현재 로그인한 유져 아이디
       idx = getIntent().getStringExtra("idx"); // 글번호
       postUserId = getIntent().getStringExtra("postUserId"); //글작성자 유져아이디
       content = getIntent().getStringExtra("content");
       writeUserImg = getIntent().getStringExtra("writeUserImg");

       String profilUrl = "http://172.30.1.42:8081/insta/profile_img/"+writeUserImg;

        mainContent.setText(postUserId+"  "+content);

        Glide.with(this)
                .load(profilUrl)
                .circleCrop()
                .error(R.drawable.unimg)
                .into(userImg);

       adapter = new MyAdapter(this);
       lvReply.setAdapter(adapter);
        submitReply.setOnClickListener(this);

        replyRequest();

    }

    private void replyRequest() {
        String url = "http://172.30.1.42:8081/insta/android_reply_list";

        Log.d("aa", "" + url);
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest myReq = new StringRequest(Request.Method.POST, url, SuccessReplyListener, ErrorListener) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("idx", idx);
                return params;
            }
        };
        requestQueue.add(myReq);
    }

    Response.Listener<String> SuccessReplyListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.d("post", response);

            try {
                JSONArray jsonList = new JSONArray(response);
                if(jsonList != null) {
                    for (int i = 0; i < jsonList.length(); i++) {
                        JSONObject obj1 = jsonList.optJSONObject(i);
                        arr.add(new ItemData(obj1.optString("replyIdx"), obj1.optString("userid"),
                                obj1.optString("content"),obj1.optString("boardIdx")));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }
    };
    private void replyInsert() {
        String url = "http://172.30.1.42:8081/insta/android_reply_insert";

        Log.d("aa", "" + url);
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest myReq = new StringRequest(Request.Method.POST, url, SuccessReplyInListener, ErrorListener) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("idx", idx);
                params.put("write_user", loginUser);
                params.put("content", editReply.getText().toString());
                return params;
            }
        };
        requestQueue.add(myReq);
    }

    Response.Listener<String> SuccessReplyInListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.d("post", response);
            if(response.equalsIgnoreCase("1")){
                arr.clear();
                replyRequest();
                editReply.setText("");
                showToast("댓글 등록 완료");
            }
            adapter.notifyDataSetChanged();
        }
    };
    private void replyDelete(String replyIdx) {
        String url = "http://172.30.1.42:8081/insta/android_reply_delete";

        Log.d("aa", "" + url);
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest myReq = new StringRequest(Request.Method.POST, url, SuccessReplyDelListener, ErrorListener) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("replyIdx", replyIdx);
                params.put("boardIdx", idx);
                return params;
            }
        };
        requestQueue.add(myReq);
    }

    Response.Listener<String> SuccessReplyDelListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.d("post", response);
            if(response.equalsIgnoreCase("1")){
                arr.clear();
                replyRequest();

            }
            adapter.notifyDataSetChanged();
        }
    };

    Response.ErrorListener ErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d("reply", "list add error");
        }
    };

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.reply_btn){
            replyInsert();
            InputMethodManager mInputMethodManager =
                    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

            mInputMethodManager.hideSoftInputFromWindow(editReply.getWindowToken(), 0);
        }
    }


    class ItemHolder {
        CircleImageView userImg;
        TextView replyContent;
        ImageView deleteImg;
    }

    class MyAdapter extends ArrayAdapter {
        LayoutInflater lnf;

        public MyAdapter(Activity context) {
            super(context, R.layout.item_reply, arr);
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
            PostActivity.ItemHolder viewHolder;
            if (convertView == null) {


                convertView = lnf.inflate(R.layout.item_reply, parent, false);
                viewHolder = new ItemHolder();

                viewHolder.userImg = convertView.findViewById(R.id.reply_profile);
                viewHolder.replyContent = convertView.findViewById(R.id.reply_content);
                viewHolder.deleteImg = convertView.findViewById(R.id.delete_img);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (PostActivity.ItemHolder) convertView.getTag();
            }

            if(loginUser.equalsIgnoreCase(arr.get(position).userid)){
                viewHolder.deleteImg.setVisibility(View.VISIBLE);
            }else {
                viewHolder.deleteImg.setVisibility(View.INVISIBLE);
            }

            viewHolder.deleteImg.setTag(position);
            viewHolder.replyContent.setText(arr.get(position).userid+"  "+arr.get(position).content);
            Glide.with(PostActivity.this)
                    .load(R.drawable.unimg)
                    .circleCrop()
                    .error(R.drawable.unimg)
                    .into(viewHolder.userImg);

            viewHolder.deleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 댓글 삭제
                    int pos = Integer.parseInt(String.valueOf(v.getTag()));
                    String replyIdx = arr.get(pos).idx;
                    replyDelete(replyIdx);

                }
            });


            return convertView;
        }

    }

}

/*
    private void init(){
        LayoutInflater lnf = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = lnf.inflate(R.layout.header_detail, null, false);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvContent = view.findViewById(R.id.tv_content);
        TextView tvWriter = view.findViewById(R.id.tv_writer);
        TextView tvDate = view.findViewById(R.id.tv_date);

        tvTitle.setText("제목");
        tvContent.setText("내용이다");
        tvWriter.setText("개똥이");
        tvDate.setText("2020.02.03");

        lvMain.addHeaderView(view);
    }*/
