package com.example.reagain;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.List;

public class MainActivity extends BaseActivity {
    static int SCR;
    final static int HOME = 0;
    final static int SEARCH = 1;
    final static int INSERT = 2;
    final static int SHOP = 3;
    final static int PROFILE =4;

    Fragment menu1HomeF;
    Fragment menu2SearchF;
    Fragment menu4IProfileF;

    Intent intent = null;
    BoardFragment boardFragment;
    List<ItemData> list = null;

    private long time= 0;
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if(SCR == HOME){
            if(System.currentTimeMillis()-time>=2000){
                time=System.currentTimeMillis();
                showToast("뒤로 버튼을 한번 더 누르면 로그아웃 됨니다.");
            }else if(System.currentTimeMillis()-time<2000){
                finish();
            }
        }else if(SCR == INSERT){
            // 글작성에서 뒤로가기 버튼 누르면 보드로 이동한다!
            showToast("뒤로가기!!");
//            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,menu1HomeF).commit();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menu1HomeF = new BoardFragment();
        menu2SearchF = new SearchFragment();
        menu4IProfileF = new ProfileFragment();

        // 하단 네비바 설정
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,menu1HomeF).commit();
        SCR = HOME;

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        if(SCR == HOME){
                            boardFragment = (BoardFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                            boardFragment.getView().scrollTo(0,0);
                        }else {
                            SCR = HOME;
                            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, menu1HomeF).commit();
                        }
                        return true;
                    case R.id.navigation_search:
                        SCR = SEARCH;
//                        new SearchFragment(list);
                        getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment,menu2SearchF).commit();
                        return true;
                    case R.id.navigation_insert:
                        SCR = INSERT;
                        startActivity(intent);
                        //getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment,menu2InsertF).commit();
                        return true;
                    case R.id.navigation_shop:
                        SCR = SHOP;
                        return true;
                    case R.id.navigation_profile:
                        SCR = PROFILE;
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,menu4IProfileF).commit();
                        return true;
                }
                return false;
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        intent = new Intent(this, com.example.reagain.InsertActivity.class);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(getData("autoLogin").equalsIgnoreCase("0")){
            deletePref();
        }
    }
}