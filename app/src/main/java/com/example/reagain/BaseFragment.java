package com.example.reagain;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class BaseFragment extends Fragment {
    Map<String, String> params = new HashMap<String, String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_base, container, false);
    }
    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }


    public void savePref(String key, String value) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void deletePref() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
    public String getData(String key) {
        String value = "";
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", getActivity().MODE_PRIVATE);
        value = sharedPreferences.getString(key, "@#@#@@$@$");
        return value;
    }
    public String sha256(String str) {
        String SHA = "";
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++) sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            SHA = sb.toString();
        }catch(NoSuchAlgorithmException e) { e.printStackTrace(); SHA = null; }
        return SHA;
    }

    public void showDial(String title, String message, DialogInterface.OnClickListener clickListener, DialogInterface.OnClickListener clickListener2){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        if(message.length() > 0) {
            builder.setMessage(message);
        }
        builder.setPositiveButton("삭제",clickListener);
        builder.setNegativeButton("수정", clickListener2);
        builder.create().show();
    }


    public void request(String toGourl){
        RequestQueue stringRequest = Volley.newRequestQueue(getActivity());
        String url = "http://172.30.1.42:8081/insta/"+toGourl ;
        StringRequest myReq = new StringRequest(Request.Method.POST, url,
                SuccessListener, errorListener) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                return params;
            }
        };
        myReq.setRetryPolicy(new DefaultRetryPolicy(3000, 0, 1f)
        );
        stringRequest.add(myReq);
    }

    public void response(String response){
        // 성공시 리스폰값 받자
    }

    Response.Listener<String> SuccessListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            //성공
            Log.d("aabb", response);
            response(response);

        }
    };
    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            //실패
            Log.d("aabb", "Fail");
        }
    };
}