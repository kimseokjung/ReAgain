package com.example.reagain;

import androidx.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileModiActivity extends BaseActivity implements View.OnClickListener {
    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;
    RelativeLayout chkPwd;
    TextView nowUserId,chk_pw, modiEdAdd1, modiPost, changeChkPw;
    Button nowPwdSubmit,btnSubmit,btnCansle, btnAdd;
    EditText nowPwd,modiEdPwd1,modiEdPwd2, modiEdName,modiEdEamil,modiEdAdd2 ;
    RadioGroup rg;
    RadioButton rg_user,rg_product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_modi);

        chkPwd = findViewById(R.id.pw_chk_fragment);
        nowPwd = findViewById(R.id.now_pwd);
        rg_user = findViewById(R.id.radio_2);
        rg_product = findViewById(R.id.radio_1);

        nowUserId = findViewById(R.id.modify_ed_id);
        chk_pw = findViewById(R.id.chk_pw);
        modiEdPwd1 = findViewById(R.id.modify_ed_pw);
        modiEdPwd2 = findViewById(R.id.modify_ed_pw2);
        modiEdName = findViewById(R.id.modify_ed_name);
        modiEdEamil = findViewById(R.id.modify_ed_email);
        modiPost = findViewById(R.id.modi_ed_post);
        modiEdAdd1 = findViewById(R.id.modi_ed_add1);
        modiEdAdd2 = findViewById(R.id.modi_ed_add2);

        btnAdd = findViewById(R.id.btn_add);
        btnSubmit = findViewById(R.id.btn_modify_ok);
        btnCansle = findViewById(R.id.btn_modify_fail);
        nowPwdSubmit = findViewById(R.id.btn_now_pwd);
        nowPwdSubmit.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        btnCansle.setOnClickListener(this);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(ProfileModiActivity.this, SearchAddress.class)
                        ,SEARCH_ADDRESS_ACTIVITY);
            }
        });

        modiEdPwd2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String pw1 = modiEdPwd1.getText().toString().trim();
                String pw2 = modiEdPwd2.getText().toString().trim();
                if(pw1.equals(pw2)) {
                    chk_pw.setTextColor(Color.BLUE);
                    chk_pw.setText("비밀번호가 일치합니다");
                }else{
                    chk_pw.setTextColor(Color.RED);
                    chk_pw.setText("비밀번호가 다릅니다");

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SEARCH_ADDRESS_ACTIVITY){
            String result = data.getExtras().getString("data");
            String[] addr = result.split(",");

            if (data != null)
                modiPost.setText(addr[0].trim());
                modiEdAdd1.setText(addr[1].trim());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_now_pwd:
                if (nowPwd.getText().length() < 1) {
                    showToast("비밀번호를 입력해주세요.");
                } else {
                    params.clear();
                    params.put("userid", getData("userid"));
                    params.put("pwd", nowPwd.getText().toString());
                    Log.d("aa", "" + nowPwd.getText().toString());
                    request("android_who_am_i");
                }
                InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mInputMethodManager.hideSoftInputFromWindow(nowPwd.getWindowToken(), 0);
                break;
            case R.id.btn_modify_ok:
                if(isCheck()) {
                    String addr = modiPost.getText().toString().trim()+","
                            + modiEdAdd1.getText().toString().trim()+","
                            + modiEdAdd2.getText().toString();
                    params.clear();
                    params.put("userid", nowUserId.getText().toString());

                    if (isEmptyPass){
                        params.put("pwd", "nope");
                    }else {
                        params.put("pwd", modiEdPwd1.getText().toString().trim());
                    }
                    params.put("nicName", modiEdName.getText().toString().trim());
                    params.put("email", modiEdEamil.getText().toString().trim());
                    params.put("add", addr );
                    Log.d("aa", "" + addr);
                    request("android_change_user_info");
                }else{
                    showToast("비밀번호가 4자리보다 작거나 틀립니다");
                }

                break;
            case R.id.btn_modify_fail:
                finish();
                break;
        }
    }
    boolean isCheck = true;
    boolean isEmptyPass = false;
    public boolean isCheck(){
        if(modiEdPwd1.getText().length() <= 4 && modiEdPwd1.getText().length() >= 1){
            isCheck = false;
        }else if(modiEdPwd2.getText().length() <= 4 &&  modiEdPwd2.getText().length() >= 1){
            isCheck = false;
        }else if(modiEdPwd1.equals(modiEdPwd2)) {
            isCheck = false;
        }else if (modiEdPwd1.getText().length() <= 0 && modiEdPwd2.getText().length() <= 0){
            isCheck = true;
            isEmptyPass = true;
        }
        return isCheck;
    }

    @Override
    public void response(String response) {
        Log.d("aa",""+response);
        try {
            JSONObject obj = new JSONObject(response);
            // 501 - 회원정보 변경에 비밀번호 확인에서 돌아옴
            if(obj.optString("code").equalsIgnoreCase("501")) {
                if (obj.optString("result").equalsIgnoreCase("OK")) {
                    modiEdName.setText(obj.optString("name"));
                    modiEdEamil.setText(obj.optString("email"));

                    // 주소 쪼개기
                    String resultAdd = obj.optString("addr");
                    String[] addr = resultAdd.split(",");
                    if(addr.length <= 1) {
                        modiEdAdd1.setText(addr[0]);
                    }else if(addr.length == 3){
                        modiPost.setText(addr[0]);
                        modiEdAdd1.setText(addr[1]);
                        modiEdAdd2.setText(addr[2]);
                    }
                    nowUserId.setText(obj.optString("userid"));
                    if (obj.optString("chk").equalsIgnoreCase("user")) {
                        rg_user.setChecked(true);
                    } else {
                        rg_product.setChecked(true);
                    }
                    chkPwd.setVisibility(View.GONE);
                } else {
                    showToast("비밀번호를 확인해 주세요");
                }
            // 505 - 회원정보 변경 확인 후 수정했을때 정보값
            }else if(obj.optString("code").equalsIgnoreCase("505")){
                if (obj.optString("result").equalsIgnoreCase("OK")) {
                    finish();
                } else {
                showToast("입력한 값을 확인해 주세요");
                 }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }

    }
}