package com.example.reagain;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_PICTURES;


public class InsertFragment extends BaseFragment implements View.OnClickListener {
    String userId = "";
    String profileImg = "";

    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFilePath = null;
    private Uri photoUri;

    private MediaScanner mMediaScanner; // 사진 저장 시 갤러리 폴더에 바로 반영사항을 업데이트 시켜주려면 이 것이 필요하다(미디어 스캐닝)


    EditText editContent;
    ImageView imageView, backImg, insertCamera;
    Button submitBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_insert, container, false);

        editContent = v.findViewById(R.id.insert_edit_text);
        imageView = v.findViewById(R.id.insert_img);
        backImg = v.findViewById(R.id.insert_btn_c);
        insertCamera = v.findViewById(R.id.insert_camera);
        submitBtn = v.findViewById(R.id.insert_btn);

        insertCamera.bringToFront();
        userId = getData("userid");
        profileImg = getData("profileImg");
        Log.d("aa","  "+userId);
        Log.d("aa","  "+profileImg);

        // 사진 저장 후 미디어 스캐닝을 돌려줘야 갤러리에 반영됨.
        mMediaScanner = MediaScanner.getInstance(getActivity().getApplicationContext());


        // 권한 체크
        TedPermission.with(getActivity().getApplicationContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage("카메라 권한이 필요합니다.")
                .setDeniedMessage("거부하셨습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

        insertCamera.setOnClickListener(this);
        submitBtn.setOnClickListener(this);
        backImg.setOnClickListener(this);

        return v;
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.insert_btn){
            params.clear();
            params.put("userid", userId);
            params.put("imgPath", "unimg.jpg");
            params.put("content", editContent.getText().toString().trim());
            params.put("profileImg", profileImg);
            request("android_board_insert");
        }else if(v.getId() == R.id.insert_camera){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {

                }

                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getPackageName(), photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }else if(v.getId() == R.id.insert_btn_c){
            Intent intent = new Intent(getActivity(),com.example.reagain.MainActivity.class);
            intent.putExtra("code",1001);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void response(String response) {
        Intent intent = new Intent(getActivity(),com.example.reagain.MainActivity.class);
        intent.putExtra("code",1001);
        intent.putExtra("userId",userId);
        intent.putExtra("profileImg",profileImg);
        startActivity(intent);
        getActivity().finish();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
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
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
            Date curDate   = new Date(System.currentTimeMillis());
            String filename  = formatter.format(curDate);

            String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "EZENSTA" + File.separator;
            File file = new File(strFolderName);
            if( !file.exists() )
                file.mkdirs();

            File f = new File(strFolderName + "/" + filename + ".png");
            result = f.getPath();

            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = "Save Error fOut";
            }

            // 비트맵 사진 폴더 경로에 저장
            rotate(bitmap,exifDegree).compress(Bitmap.CompressFormat.PNG, 70, fOut);

            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
                // 방금 저장된 사진을 갤러리 폴더 반영 및 최신화
                mMediaScanner.mediaScanning(strFolderName + "/" + filename + ".png");
            } catch (IOException e) {
                e.printStackTrace();
                result = "File close Error";
            }

            // 이미지 뷰에 비트맵을 set하여 이미지 표현
            imageView.setImageBitmap(rotate(bitmap,exifDegree));


        }
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