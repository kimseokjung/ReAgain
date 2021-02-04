package com.example.reagain;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends BaseActivity implements View.OnClickListener {

    TextView tvBoardWriter, tvBoardContent, tvBoardLogtime, tvLikeCount, tvBoardReply;
    ImageView imgBoardImage, icBoardLike, icBoardDots, backImg;
    CircleImageView imgBoardProfile;
    String isChecked ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        tvBoardWriter = findViewById(R.id.board_writer);
        tvBoardContent = findViewById(R.id.board_content);
        tvBoardLogtime = findViewById(R.id.board_logtime);
        tvLikeCount = findViewById(R.id.board_like_count);
        imgBoardProfile = findViewById(R.id.board_profile);
        imgBoardImage = findViewById(R.id.board_img);
        icBoardLike = findViewById(R.id.board_like);
        icBoardDots = findViewById(R.id.board_dots);

        backImg = findViewById(R.id.back_img);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvBoardWriter.setText(getIntent().getStringExtra("userid"));
        tvBoardContent.setText(getIntent().getStringExtra("content"));
        tvBoardLogtime.setText(getIntent().getStringExtra("logtime"));
        tvLikeCount.setText(getIntent().getStringExtra("likeCount"));

        icBoardLike.setOnClickListener(this);

        String imglUrl = "http://172.30.1.42:8081/insta/storage/"
                + getIntent().getStringExtra("imgPath");
        String profilUrl = "http://172.30.1.42:8081/insta/profile_img/"
                + getIntent().getStringExtra("writeuserimg");
        Glide.with(this)
                .load(imglUrl)
                .error(R.drawable.no_image)
                .into(imgBoardImage);

        isChecked = getIntent().getStringExtra("isChecked");
        int heart_draw = 0;
        if (isChecked.equalsIgnoreCase("1")) {
            heart_draw = R.drawable.heart_fas;
            Log.d("aadd", "1이들어왔다!!" + isChecked);
        } else {
            heart_draw = R.drawable.heart_far;
            Log.d("aadd", "0이들어왔다!!" + isChecked);
        }
        Glide.with(this)
                .load(heart_draw)
                .into(icBoardLike);
        Glide.with(this)
                .load(profilUrl)
                .circleCrop()
                .error(R.drawable.unimg)
                .into(imgBoardProfile);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.board_like) {
            String pos = getIntent().getStringExtra("idx");
            params.clear();
            params.put("idx", pos);
            params.put("userid", getIntent().getStringExtra("userid"));
            request("android_like");
        }
    }

    @Override
    public void response(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.getString("code");
            if(code.equalsIgnoreCase("505")){

            }else {
                String likeCount = jsonObject.optString("likeCount");
                int likeCheck = Integer.parseInt(jsonObject.optString("isChecked"));
                String isChecked = String.valueOf(likeCheck==0?1:0);
                Log.d("aa",isChecked);

                tvLikeCount.setText(likeCount);
                this.isChecked = isChecked;
                int heart_draw = 0;
                if (isChecked.equalsIgnoreCase("1")) {
                    heart_draw = R.drawable.heart_fas;
                    Log.d("aadd", "1이들어왔다!!" + isChecked);
                } else {
                    heart_draw = R.drawable.heart_far;
                    Log.d("aadd", "0이들어왔다!!" + isChecked);
                }
                Glide.with(this)
                        .load(heart_draw)
                        .into(icBoardLike);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
}