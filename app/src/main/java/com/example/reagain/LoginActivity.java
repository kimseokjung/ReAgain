package com.example.reagain;

import androidx.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity implements View.OnClickListener{
    String isAutoLogin = null;
    TextView id,pw;
    EditText edId,edPw;
    Button btnLogin;
    Button btnJoin;
    CheckBox autoLogin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        id = findViewById(R.id.user_id);
        pw = findViewById(R.id.user_pw);
        edId = findViewById(R.id.ed_id);
        edPw = findViewById(R.id.ed_pw);
        btnLogin = findViewById(R.id.btn_login);
        btnJoin = findViewById(R.id.btn_join);
        autoLogin = findViewById(R.id.auto_login);

        btnLogin.setOnClickListener(this);
        btnJoin.setOnClickListener(this);
        if(getData("autoLogin").equals("1")){
            autoLogin.setChecked(true);
        }else {
            autoLogin.setChecked(false);
            deletePref();
        }

        if(autoLogin.isChecked()) {
            chkAutoLogin();
        }
    }

    private void chkAutoLogin() {

        if (!getData("userid").equals("@#@#@@$@$")) {
            edId.setText(getData("userid"));
            edPw.setText(getData("pwd"));
            requestForLogin(getData("userid"), getData("pwd"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { //회원 가입 성공
            String userId = getData("userId");
            String pwd = getData("pwd");

            edId.setText(userId);
            edPw.setText(pwd);

            requestForLogin(userId, pwd);//자동 로그인 시도

        } else{ //회원 가입 실패
            showToast("회원가입 실패! 잠시 후 다시시도해주세요");
        }
    }
    String userId = "";
    String pwd = "";
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_join){
            Intent intent =
                    new Intent(this, com.example.reagain.JoinActivity.class);
            startActivity(intent);
        }else if(v.getId() == R.id.btn_login){
            userId = edId.getText().toString().trim();
            pwd = edPw.getText().toString().trim();

            params.clear();
            params.put("input_id", userId);
            params.put("input_pwd", pwd);
            request("android_login");
        }
    }

    @Override
    protected void onResume() {//재개

        super.onResume();
    }

    @Override
    protected void onPause() {//일시정지

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void requestForLogin(String userId, String pwd){
        RequestQueue stringRequest = Volley.newRequestQueue(this);
        String url = "http://172.30.1.42:8081/insta/android_login";

        StringRequest myReq = new StringRequest(Request.Method.POST, url,
                SuccessListener, errorListener) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("input_id", userId);
                params.put("input_pwd", pwd);
                return params;
            }
        };
        myReq.setRetryPolicy(new DefaultRetryPolicy(3000, 0, 1f)
        );
        stringRequest.add(myReq);
    }

    @Override
    public void response(String response) {
        String result = null;
        String code = null;
        String profileImg = null;

        try {
            JSONObject obj = new JSONObject(response);
            result = obj.optString("result");
            code = obj.optString("code");
            profileImg = obj.optString("profileImg");
            Log.d("Login massage", "chk: " + result.toString());

        if(result.equals("OK")) {
            showToast("로그인 성공!!");
            if(autoLogin.isChecked()){
                isAutoLogin = "1";
            }else {
                isAutoLogin = "0";
            }
            savePref("autoLogin",isAutoLogin);
            savePref("userid", edId.getText().toString().trim());
            savePref("profileImg", profileImg);
            savePref("pwd", edPw.getText().toString().trim());
            Log.d("aa","Login!!!!!");
            Intent intent =
                    new Intent(LoginActivity.this, com.example.reagain.MainActivity.class);
            startActivity(intent);
        }else {
            if (code.equals("1000")) {
                showToast("아이디와 비밀번호가 틀렸습니다");
            } else {
                showToast("회원 정보가 없습니다");
            }
            edPw.setText("");
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}