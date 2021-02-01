package com.example.reagain;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class JoinActivity extends BaseActivity implements View.OnClickListener {
    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;
    boolean isJoinChk;

    TextView chkId,joinEdPost,joinEdAdd1;
    EditText joinEdId, joinEdPw, joinEdPw2, joinEdName, joinEdEmail, joinEdAdd2;
    Button btnJoin, btnJoinFail,btnChk,btnAdd;
    RadioGroup rg;
    RadioButton rb1, rb2;

    String radioText = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        joinEdId = findViewById(R.id.join_ed_id);
        joinEdPw = findViewById(R.id.join_ed_pw);
        joinEdPw2 = findViewById(R.id.join_ed_pw2);
        joinEdName = findViewById(R.id.join_ed_name);
        joinEdPost = findViewById(R.id.join_ed_post);
        joinEdAdd1 = findViewById(R.id.join_ed_add1);
        joinEdAdd2 = findViewById(R.id.join_ed_add2);
        joinEdEmail = findViewById(R.id.join_ed_email);
        chkId = findViewById(R.id.chk_id);

        isJoinChk = false;// 중복체크
        setResult(RESULT_CANCELED);//기본값은 회원가입 실패

        rb1 = findViewById(R.id.radio_1);
        rb2 = findViewById(R.id.radio_2);

        rg = findViewById(R.id.radio_chk);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_1:
                        radioText = rb1.getText().toString().trim();
                        break;
                    case R.id.radio_2:
                        radioText = rb2.getText().toString().trim();
                        break;
                }
            }
        });

        btnJoin = findViewById(R.id.btn_join_ok);
        btnJoinFail = findViewById(R.id.btn_join_fail);
        btnChk = findViewById(R.id.btn_chk);
        btnAdd = findViewById(R.id.btn_add);

        btnJoin.setOnClickListener(this);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(JoinActivity.this, SearchAddress.class)
                        ,SEARCH_ADDRESS_ACTIVITY);
            }
        });
        btnJoinFail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnChk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(joinEdId.getText().toString().trim().length() > 1) {
                    params.clear();
                    params.put("input_id", joinEdId.getText().toString().trim());
                    Log.d("aa",joinEdId.getText().toString().trim());
                    request("android_idcheck");
                }else{
                    showToast("먼저 아이디를 입력해 주세요");
                }
            }
        });

        joinEdName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String pw1 = joinEdPw.getText().toString().trim();
                String pw2 = joinEdPw2.getText().toString().trim();
                if(pw1.equals(pw2)) {
                    chkId.setTextColor(Color.BLUE);
                    chkId.setText("비밀번호가 일치합니다");
                }else{
                    chkId.setTextColor(Color.RED);
                    chkId.setText("비밀번호를 확인해주세요");

                }
                chkId.setVisibility(View.VISIBLE);
            }
        });

        joinEdId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //JoinEdId 안에 텍스트가 바뀌면 무조건 중복 체크 플래그 원상복귀
                btnChk.setEnabled(true);
                isJoinChk = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
                joinEdPost.setText(addr[0].trim());
                joinEdAdd1.setText(addr[1].trim());
        }
    }

    @Override
    public void response(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String result = jsonObject.getString("result");
            String code = jsonObject.getString("code");
            if(code.equalsIgnoreCase("101")){
                if (result.equalsIgnoreCase("OK")) { // 사용가능한 아이디
                    isJoinChk = true;
                    showToast("사용 가능한 아이디");
                    btnChk.setEnabled(false);
                } else { //사용 불가능한 아이디
                    isJoinChk = false;
                    showToast("사용 불가능한 아이디");
                }
            }else{
                /*
                암호와 비번이 일치 result:ok

                아이디는 같은데 비번이 틀리다 result:nk,code:100

                아이디가 없다 result:nk, code:200
                 */
                Log.d("aabb", "chk: " + result.toString());

                switch (result) {
                    case "OK":
                        showToast("회원 가입 성공!!");
                        savePref("userId", joinEdId.getText().toString().trim());  //자동 로그인을 위해 아이디 저장
                        savePref("pwd", joinEdPw.getText().toString().trim()); //자동 로그인을 위해 암호 저장
                        setResult(RESULT_OK);
                        finish();
                        break;
                    case "NK":
                        showToast("회원 가입 실패!!");
                        break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /* JSON 예제 */
    private void jsonTest() {

        try {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("name", "홍길동");
            jsonObject1.put("gender", "M");

            JSONArray jsonArr = new JSONArray();
            jsonArr.put(jsonObject1);


            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject temp = jsonArr.getJSONObject(i);
                String temoName = temp.getString("name");
                String temoGender = temp.getString("gender");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isValid() {
        boolean isValid = true;
        if (joinEdId.getText().toString().trim().length() < 1) {
            isValid = false;
        } else if (joinEdId.getText().toString().trim().contains(" ")) {
            isValid = false;
        } else if (joinEdPw.getText().toString().trim().contains(" ")) {
            isValid = false;
        } else if (joinEdPw.getText().toString().trim().length() < 1) {
            isValid = false;
        } else if (!joinEdPw.getText().toString().trim().equals(joinEdPw2.getText().toString().trim())) {
            isValid = false;
        } else if (joinEdName.getText().toString().length() < 1) {
            isValid = false;
        } else if (joinEdAdd1.getText().toString().length() < 1) {
            isValid = false;
        }
        return isValid;
    }

    @Override
    public void onClick(View v) {
        if (isValid()) {
/*            String id = "input_id=" + joinEdId.getText().toString().trim();
            String pw = "input_pwd=" + joinEdPw.getText().toString().trim();
            String name = "input_name=" + joinEdName.getText().toString().trim();
            String email = "input_email=" + joinEdEmail.getText().toString().trim();
            String add = "input_address=" + joinEdAdd.getText().toString().trim();
            String chk = "input_chk=" + radioText;

            String url = "http://172.30.1.42:8081/insta/android_join.do?"+id+"&"+pw+"&"+name+"&"+email+"&"+add+"&"+chk;
            Log.d("kkk", "dd"+url);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest myReq = new StringRequest(Request.Method.GET, url, successListener, errorListener);
            requestQueue.add(myReq);*/

            /** post **/

            String id = joinEdId.getText().toString().trim();
            String pw = joinEdPw.getText().toString().trim();
            String name = joinEdName.getText().toString().trim();
            String email = joinEdEmail.getText().toString().trim();
            String add = joinEdPost.getText().toString().trim()+","
                    +joinEdAdd1.getText().toString().trim()+","
                    +joinEdAdd2.getText().toString().trim();

            params.clear();
            params.put("input_id", id);
            params.put("input_pwd", pw);
            params.put("input_name", name);
            params.put("input_email", email);
            params.put("input_address", add);
            params.put("input_chk", radioText);
            request("android_join");
        }else {
            showToast("데이터가 올바르지 않습니다.");
        }
    }

}
