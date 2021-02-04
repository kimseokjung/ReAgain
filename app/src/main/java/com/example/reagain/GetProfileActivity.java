package com.example.reagain;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Environment.DIRECTORY_PICTURES;

public class GetProfileActivity extends BaseActivity implements View.OnClickListener, AbsListView.OnScrollListener {
    ArrayList<ItemData> arr = new ArrayList<>();
    ArrayList<ItemData> subArr = new ArrayList<>();

    final int REQUEST_IMAGE_GELLERY = 666;

    ExpandableHeightGridView gv;
    TextView mainTitle, profileBoard;
    TextView tvFollowing, tvFollower, boardCount;
    CircleImageView profileImg;
    ImageView modifyImg;
    Button follow;
    SwipeRefreshLayout refreshLayout;
    MyAdapter adapter;
    ProgressBar progressBar;
    File tempFile;

    boolean isScrollListLast = false; // 스크롤이 끝에 닿았니?
    boolean mLockListView = false; //중복 체크 변수
    int OFFSET = 0;
    int IDX = 21;// 몇개씩 불러올 것인가?
    int isLAST = -1; // 0 리스트의 끝인가 -1 초기값 1이면 최대크기

    int isFollow = -1; // 팔로우 했니 안했니

    String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_profile);

        mainTitle = findViewById(R.id.main_title);
        boardCount = findViewById(R.id.board_count);
        profileBoard = findViewById(R.id.profile_board);
        profileImg = findViewById(R.id.profile_img);
        modifyImg = findViewById(R.id.modify_plus);
        follow = findViewById(R.id.follow);
        progressBar = findViewById(R.id.progressBar_profile);
        tvFollowing = findViewById(R.id.board_following);
        tvFollower = findViewById(R.id.board_follower);

        refreshLayout = findViewById(R.id.swiperefresh);

        arr.clear();
        subArr.clear();
        OFFSET = 0;

        userId = getIntent().getStringExtra("userid");
        if(userId.equalsIgnoreCase(getData("userid"))){
            follow.setText("프로필 수정");
        }else {
            modifyImg.setVisibility(View.GONE);
        }
        mainTitle.setText(userId);
        String profilUrl = "http://172.30.1.42:8081/insta/profile_img/" + getData("profileImg");
        Log.d("aa", "" + profilUrl);
        Glide.with(this)
                .load(profilUrl)
                .error(R.drawable.unimg)
                .circleCrop()
                .into(profileImg);


        gv = findViewById(R.id.gridview);
        gv.setExpanded(true);
        gv.setOnScrollListener(this);

        adapter = new MyAdapter(this);
        gv.setAdapter(adapter);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //팝업으로 띄우기

                Intent intent = new Intent(GetProfileActivity.this, PostActivity.class);
                intent.putExtra("idx", arr.get(position).idx);
                intent.putExtra("userid", arr.get(position).userid);
                intent.putExtra("content", arr.get(position).content);
                intent.putExtra("logtime", arr.get(position).logtime);
                intent.putExtra("writeuserimg", arr.get(position).writeuserimg);
                intent.putExtra("likeCount", arr.get(position).likeCount);
                intent.putExtra("isChecked", arr.get(position).isChecked);
                intent.putExtra("imgPath", arr.get(position).imgPath);
                startActivity(intent);
            }
        });

        params.put("userid", userId);
        request("android_profile_list");
        request("android_following");
        request("android_follower");

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                arr.clear();
                subArr.clear();
                OFFSET = 0;
                isLAST = -1;
                request("android_profile_list");
            }
        });
        modifyImg.setOnClickListener(this);
        follow.setOnClickListener(this);
    }


    @Override
    public void response(String response) {
        try {
            JSONObject object = new JSONObject(response);
            String code = object.optString("code");

            if (code.equalsIgnoreCase("600")) {
                //팔로잉에서 복귀
                Log.d("follow", response);
                JSONArray followList = object.optJSONArray("list");
                tvFollowing.setText(String.valueOf(followList.length()));
            } else if (code.equalsIgnoreCase("601")) {
                Log.d("follow", response);
                JSONArray followList = object.optJSONArray("list");
                tvFollower.setText(String.valueOf(followList.length()));
            } else if (code.equalsIgnoreCase("700")) {
                //프로파일 리스트에서 복귀
                refreshLayout.setRefreshing(false);
                JSONArray jsonList = object.optJSONArray("list");
                for (int i = 0; i < jsonList.length(); i++) {
                    JSONObject obj1 = jsonList.optJSONObject(i);
                    arr.add(new ItemData(obj1.optString("idx"), obj1.optString("userid"),
                            obj1.optString("content"), obj1.optString("imgPath"), obj1.optString("logtime"),
                            obj1.optString("writeuserimg"), obj1.optString("likeCount"), obj1.optString("isChecked")));
                }
                boardCount.setText(String.valueOf(arr.size()));
                getItem();
            } else if (code.equalsIgnoreCase("400")) {
                String followObj = object.optString("result");
                if (followObj.equalsIgnoreCase("OK")) {
                    isFollow = 0;
                    follow.setText("언팔로우");
                } else {

                }

            } else if (code.equalsIgnoreCase("401")) {
                String followObj = object.optString("result");
                if (followObj.equalsIgnoreCase("OK")) {
                    isFollow = -1;
                    follow.setText("팔로우");
                } else {

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.modify_plus:
                Log.i("Gellery", "Call");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, REQUEST_IMAGE_GELLERY);
                break;
            case R.id.follow:
                Log.d("aa", "누질렸다");
                params.clear();
                params.put("target", userId);
                params.put("userid", getData("userid"));
                if(userId.equalsIgnoreCase(getData("userid"))){
                    startActivity(new Intent(this, com.example.reagain.ProfileModiActivity.class));
                }else {
                    if (isFollow == -1) {
                        request("android_follow_insert");
                    } else {
                        request("android_follow_delete");
                    }
                }

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GELLERY && resultCode == RESULT_OK) {
            Log.d("profile gellery", "result OK");
            Uri dataUri = data.getData();

            try {
                // 선택한 이미지에서 비트맵 생성
                InputStream in = getContentResolver().openInputStream(dataUri);
                Bitmap img = BitmapFactory.decodeStream(in);
                in.close();
                // 이미지 표시
                Glide.with(this)
                        .load(img)
                        .error(R.drawable.unimg)
                        .circleCrop()
                        .into(profileImg);

//            imageView.setImageBitmap(img);
                Log.d("aa", "사진 불러오기");

                // 선택한 이미지 임시 저장
                String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "EZENSTA" + File.separator;
                File file = new File(strFolderName);
                if (!file.exists())
                    file.mkdirs();

                tempFile = new File(strFolderName, "PROFILE_" + date + ".jpeg");
                OutputStream out = new FileOutputStream(tempFile);
                img.compress(Bitmap.CompressFormat.JPEG, 100, out);

                send2Server(tempFile, userId, "android_profile_img_change");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void send2Server(File file, String userId, String url) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userid", userId)
                .addFormDataPart("files", file.getName(), RequestBody.create(MultipartBody.FORM, file)).build();
        Log.d("send2Server", "업로드 시작 : " + file.getName());
        Request request = new Request.Builder()
                .url("http://172.30.1.42:8081/insta/" + url) // Server URL 은 본인 IP를 입력
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
//                isAA=response.body().string();
                try {
                    Log.d("TEST", "리스폰즈 들옴");
                    String result = response.body().string();
                    Log.d("TEST", "result : " + result);
                    if (result.equalsIgnoreCase("1")) {
                        Log.d("profile img", "갤러리 리절트 값을 받음");
                        savePref("profileImg", file.getName());
                    } else {
                        Log.d("profile img", "갤러리 리절트 값을 못받음");

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void getItem() {
        Log.d("profile", "페이징 입장");
        // 리스트에 다음 데이터를 입력할 동안에 이 메소드가 또 호출되지 않도록 mLockListView 를 true로 설정한다.
        mLockListView = true;

        if (arr.size() <= OFFSET + IDX && isLAST == -1) {
            Log.d("profile", "arr 크기가 같다!!");
            OFFSET = arr.size();
            isLAST = 1;
        } else if (isLAST == 1) {
            Log.d("profile", "arr 크기가 같아서 마지막 값을 받았다!!");
            isLAST = 0;
        } else if (isLAST == -1) {
            Log.d("profile", "arr 크기보다 작아서 페이지값을 받음!");
            OFFSET += IDX;
        }

        // 다음 20개의 데이터를 불러와서 리스트에 저장한다.
        if (arr.size() == OFFSET && isLAST == 0) {
            showToast("마지막 페이지 입니다");
        } else {
            subArr.clear();
            for (int i = 0; i < OFFSET; i++) {

                subArr.add(arr.get(i));
            }
        }
        Log.d("profile", "arr : " + arr.size());
        Log.d("profile", "subArr : " + subArr.size());
        Log.d("profile", "OFFSET : " + OFFSET);
        Log.d("profile", "isLAST : " + isLAST);


        // 1초 뒤 프로그레스바를 감추고 데이터를 갱신하고, 중복 로딩 체크하는 Lock을 했던 mLockListView변수를 풀어준다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                mLockListView = false;
            }
        }, 1000);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d("profile", "이 로그는 스크롤이 변경될때 출력된다!");
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isScrollListLast && mLockListView == false) {
            // 화면이 바닦에 닿을때 처리
            // 로딩중을 알리는 프로그레스바를 보인다.
            if (isLAST != 0)
                progressBar.setVisibility(View.VISIBLE);

            // 다음 데이터를 불러온다.
            getItem();
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

    class ItemHolder {
        ImageView ivGridHolder;
    }

    int pos;

    class MyAdapter extends ArrayAdapter {
        LayoutInflater lnf;

        public MyAdapter(Activity context) {
            super(context, R.layout.item_grid, subArr);
            lnf = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return subArr.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return subArr.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder viewHolder;
            if (convertView == null) {

                convertView = lnf.inflate(R.layout.item_grid, parent, false);
                viewHolder = new ItemHolder();

                viewHolder.ivGridHolder = convertView.findViewById(R.id.iv_grid);


                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ItemHolder) convertView.getTag();
            }
            String mainImg = subArr.get(position).imgPath;
            String imglUrl = "http://172.30.1.42:8081/insta/storage/" + mainImg;

            viewHolder.ivGridHolder.setImageResource(R.drawable.unimg);
            Log.d("aa", "dddd  " + imglUrl);
            Glide.with(GetProfileActivity.this)
                    .load(imglUrl)
                    .error(R.drawable.no_image)
                    .into(viewHolder.ivGridHolder);


            return convertView;
        }
    }

}