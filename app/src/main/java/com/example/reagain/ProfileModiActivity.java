package com.example.reagain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
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
    RelativeLayout chkPwd;
    TextView nowUserId,chk_pw;
    Button nowPwdSubmit,btnSubmit,btnCansle;
    EditText nowPwd,modiEdPwd1,modiEdPwd2, modiEdName,modiEdEamil,modiEdAdd ;
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
        modiEdAdd = findViewById(R.id.modify_ed_add);

        btnSubmit = findViewById(R.id.btn_modify_ok);
        btnCansle = findViewById(R.id.btn_modify_fail);
        nowPwdSubmit = findViewById(R.id.btn_now_pwd);
        nowPwdSubmit.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_now_pwd){
            if(nowPwd.getText().length() < 1){
                showToast("비밀번호를 입력해주세요.");
            }else {
                params.clear();
                params.put("userid", getData("userid"));
                params.put("pwd", nowPwd.getText().toString());
                Log.d("aa",""+nowPwd.getText().toString());
                request("android_who_am_i");
            }
            InputMethodManager mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(nowPwd.getWindowToken(), 0);
        }
    }

    @Override
    public void response(String response) {
        Log.d("aa",""+response);
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.optString("result").equalsIgnoreCase("OK")) {
                modiEdName.setText(obj.optString("name"));
                modiEdEamil.setText(obj.optString("email"));
                modiEdAdd.setText(obj.optString("add"));
                nowUserId.setText(obj.optString("userid"));
                if(obj.optString("chk").equalsIgnoreCase("user")){
                    rg_user.setChecked(true);
                }else {
                    rg_product.setChecked(true);
                }
                chkPwd.setVisibility(View.GONE);
            }else {
                showToast("비밀번호를 확인해 주세요");
            }

        }catch (JSONException e){
            e.printStackTrace();
        }

    }
}