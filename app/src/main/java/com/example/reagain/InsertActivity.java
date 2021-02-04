package com.example.reagain;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Environment.DIRECTORY_PICTURES;

public class InsertActivity extends BaseActivity implements View.OnClickListener {
    String userId = "";
    String profileImg = "";

    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private static final int REQUEST_IMAGE_GELLERY = 673;
    private String imageFilePath = null;
    private Uri photoUri;
    File tempFile;

    private MediaScanner mMediaScanner; // 사진 저장 시 갤러리 폴더에 바로 반영사항을 업데이트 시켜주려면 이 것이 필요하다(미디어 스캐닝)

    EditText editContent;
    ImageView imageView;
    ImageView backImg;
    ImageView insertCamera;
    Button submitBtn, modifyOK, modifyFail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        editContent = findViewById(R.id.insert_edit_text);
        imageView = findViewById(R.id.insert_img);
        backImg = findViewById(R.id.insert_btn_c);
        insertCamera = findViewById(R.id.insert_camera);
        submitBtn = findViewById(R.id.insert_btn);
        modifyOK = findViewById(R.id.btn_modify_ok);
        modifyFail = findViewById(R.id.btn_modify_fail);

        insertCamera.bringToFront();
        userId = getData("userid");
        profileImg = getData("profileImg");
        Log.d("aa", "  " + userId);
        Log.d("aa", "  " + profileImg);

        // 사진 저장 후 미디어 스캐닝을 돌려줘야 갤러리에 반영됨.
        mMediaScanner = MediaScanner.getInstance(getApplicationContext());

        insertCamera.setOnClickListener(this);
        submitBtn.setOnClickListener(this);
        backImg.setOnClickListener(this);

        TedPermission.with(this.getApplicationContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage("카메라 권한이 필요합니다.")
                .setDeniedMessage("거부하셨습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA)
                .check();

    }
    String isAA = ""; // okhttp통신 후 값을 받을 공간
    public void send2Server(File file, String userId, String content, String profileImg){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userid", userId)
                .addFormDataPart("content", content)
                .addFormDataPart("profileImg", profileImg)
                .addFormDataPart("files", file.getName(), RequestBody.create(MultipartBody.FORM, file)).build();
        Log.d("upload", "업로드 시작" + file.getName());
        Request request = new Request.Builder()
                .url("http://172.30.1.42:8081/insta/android_board_insert") // Server URL 은 본인 IP를 입력
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                isAA=response.body().string();
                try {
                    Log.d("TEST","리스폰즈 들옴");
                    JSONObject obj = new JSONObject(response.body().string());
                    isAA = obj.optString("result");
                    Log.d("TEST","리스폰즈 리절트값 입력완료!");
                    if (isAA != null){
                        Intent intent = new Intent(InsertActivity.this, com.example.reagain.MainActivity.class);
                        intent.putExtra("code", 1001);
                        intent.putExtra("userId", userId);
                        intent.putExtra("profileImg", profileImg);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.insert_btn) {
           String content = editContent.getText().toString().trim();
           //okhttp3 통신하러 고고
            send2Server(tempFile, userId, content, profileImg);

        } else if (v.getId() == R.id.insert_camera) {
            AlertDialog.Builder builer = new AlertDialog.Builder(this);
            builer.setIcon(R.mipmap.ic_launcher);
            builer.setTitle("이미지 선택");
            View cameraView = getLayoutInflater().inflate(R.layout.item_insert_modify, null, false);
            final RadioGroup rg = cameraView.findViewById(R.id.rg);

            builer.setView(cameraView);//다이얼로그 리소스 불러옴
            builer.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int ra = rg.getCheckedRadioButtonId();
                    switch (ra) {
                        case R.id.camera:
                            String state = Environment.getExternalStorageState();
                            // 외장 메모리 검사
                            if (Environment.MEDIA_MOUNTED.equals(state)) {
                                sendTakePhotoIntent(); // 사진찍으로 가자
                            } else {
                                showToast("저장공간이 접근 불가능한 기기입니다");
                                return;
                            }
                            break;
                        case R.id.gellery:
                            getGellery();
                            break;
                    }
                }
            });
            builer.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builer.setCancelable(true);
            builer.create().show();


        } else if (v.getId() == R.id.insert_btn_c) {
            finish();
        }
    }

    private void getGellery() {
        Log.i("Gellery", "Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_IMAGE_GELLERY);
    }

    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpeg",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("aa", "" + requestCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("aa", "사진 불러오기2" + photoUri);
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            int exifOrientation;
            int exifDegree;

            if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegress(exifOrientation);
            } else {
            exifDegree = 0;
            }
            String result = "";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault());
            Date curDate = new Date(System.currentTimeMillis());
            String filename = formatter.format(curDate);

            String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "EZENSTA" + File.separator;
            File file = new File(strFolderName);
            if (!file.exists())
            file.mkdirs();

            File f = new File(strFolderName + "/" + filename + ".jpeg");
            result = f.getPath();

            FileOutputStream fOut = null;
            try {
            fOut = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
            e.printStackTrace();
            result = "Save Error fOut";
            }

            // 비트맵 사진 폴더 경로에 저장
            rotate(bitmap, exifDegree).compress(Bitmap.CompressFormat.JPEG, 70, fOut);

            try {
            fOut.flush();
            } catch (IOException e) {
            e.printStackTrace();
            }
            try {
            fOut.close();
            // 방금 저장된 사진을 갤러리 폴더 반영 및 최신화
            mMediaScanner.mediaScanning(strFolderName + "/" + filename + ".jpeg");
            } catch (IOException e) {
            e.printStackTrace();
            result = "File close Error";
            }

            if (bitmap != null) {
                // 이미지 뷰에 비트맵을 set하여 이미지 표현
                imageView.setImageBitmap(rotate(bitmap, exifDegree));
            }
            try{
                String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//                String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "EZENSTA" + File.separator;
                tempFile = new File(strFolderName, "TEST_" + date + ".jpeg");
                OutputStream out = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }catch (Exception e){
                e.printStackTrace();
            }

        } else if (requestCode == REQUEST_IMAGE_GELLERY && resultCode == RESULT_OK) {
            Log.d("aa", "사진 불러오기1" + data.getData());
            Uri dataUri = data.getData();
            imageView.setImageURI(dataUri);

            try {
                // 선택한 이미지에서 비트맵 생성
                InputStream in = getContentResolver().openInputStream(dataUri);
                Bitmap img = BitmapFactory.decodeStream(in);
                in.close();
                // 이미지 표시
                imageView.setImageBitmap(img);
                Log.d("aa", "사진 불러오기");

                // 선택한 이미지 임시 저장
                String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
               // String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "EZENSTA" + File.separator;
                String strFolderName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "EZENSTA" + File.separator;
                File file = new File(strFolderName);
                if(!file.exists()){
                    file.mkdirs();
                }
                tempFile = new File(strFolderName, "TEST_" + date + ".jpeg");
                Log.d("imgUri" ,""+tempFile);
                OutputStream out = new FileOutputStream(tempFile);
                img.compress(Bitmap.CompressFormat.JPEG, 100, out);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(resultCode == RESULT_OK)
            insertCamera.setVisibility(View.GONE);
    }



    private int exifOrientationToDegress(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            showToast("권한이 허용됨");
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            showToast("권한이 거부됨");
        }

    };

}
