package com.example.hbook.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.hbook.network.ApiService;
import com.example.hbook.model.OcrResponse;
import com.example.hbook.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CameraActivity extends AppCompatActivity {

    private PreviewView viewFinder;
    private ImageCapture imageCapture; // 사진을 캡처하는 역할
    private ExecutorService cameraExecutor; // 카메라 작업을 처리할 별도의 스레드
    private String bookNameFromIntent;  // 이전 화면에서 넘겨받은 책 이름

    // 갤러리에서 사진을 골랐을 때 결과 받아옴
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null &&!uris.isEmpty()) {
                    Toast.makeText(this, uris.size() + "장의 사진을 불러왔습니다.", Toast.LENGTH_SHORT).show();

                    List<File> fileList = new ArrayList<>();
                    for (Uri uri : uris) {
                        File file = uriToFile(uri);
                        if (file != null) fileList.add(file);
                    }

                    uploadMultipleToServer(fileList);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // 이전 화면에서 넘겨준 책 이름 있는지 확인
        bookNameFromIntent = getIntent().getStringExtra("BOOK_NAME");

        viewFinder = findViewById(R.id.viewFinder);
        ImageView btnGallery = findViewById(R.id.btn_gallery);
        View btnCapture = findViewById(R.id.btn_capture);
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvBookTitle = findViewById(R.id.tv_book_title);

        if (bookNameFromIntent != null) {
            tvBookTitle.setText(bookNameFromIntent);
        }

        // 1. 카메라 권한이 있는지 확인
        if (allPermissionsGranted()) {
            startCamera(); // 권한이 있으면 카메라 켜기
        } else {
            // 권한이 없으면 사용자에게 권한 요청 팝업 띄우기
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        }

        // 2. 뒤로가기 로직
        tvBack.setOnClickListener(v -> handleBackButton());

        // 3. 촬영 로직
        btnCapture.setOnClickListener(v -> takePhoto());

        // 4. 갤러리 로직
        btnGallery.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    // 뒤로가기 눌렀을 때 동작 처리
    private void handleBackButton() {
        if (bookNameFromIntent != null) {
            // 메인화면에서 넘어온 경우
            new AlertDialog.Builder(this)
                    .setTitle("스캔 취소")
                    .setMessage("지금 돌아가면 '" + bookNameFromIntent + "' 추가가 취소됩니다. 돌아가시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> finish())
                    .setNegativeButton("아니요", (dialog, which) -> dialog.cancel())
                    .show();

        } else {
            finish();
        }
    }

    // 안드로이드 사진첨 데이터를 실제 파일로 복사
    private File uriToFile(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            File tempFile = new File(getCacheDir(), "temp_gallery_ocr.jpg");
            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 카메라 화면을 띄우는 함수
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // 뷰파인더(미리보기) 설정
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // 사진 캡처 설정
                imageCapture = new ImageCapture.Builder().build();

                // 후면 카메라 기본 선택
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 기존에 연결된 카메라가 있다면 해제하고 새로 바인딩
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "카메라 연결 실패", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // 사진을 찍고 저장하는 함수
    private void takePhoto() {
        if (imageCapture == null) return;

        // 사진이 저장될 폴더와 파일 이름 만들기 (임시 캐시 폴더에 저장)
        File photoFile = new File(getCacheDir(), "temp_ocr.jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // 사진 찍음
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(CameraActivity.this, "사진 저장 완료: ", Toast.LENGTH_SHORT).show();
                uploadToServer(photoFile);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraX", "사진 저장 실패: " + exception.getMessage(), exception);
            }
        });
    }

    // 권한 확인용 유틸리티 함수
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // 서버로 사진을 전송하는 함수
    private void uploadToServer(File photoFile) {
        // 1. 서버 주소 설정
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://egal-furcately-nydia.ngrok-free.dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // 2. 사진 파일을 Multipart 형식으로 변환
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), photoFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", photoFile.getName(), requestFile);

        // 3. 페이지 번호 데이터 준비
        RequestBody pageNumBody = RequestBody.create(MediaType.parse("text/plain"), "1");

        // 4. 통신 시작
        Call<OcrResponse> call = apiService.uploadImage(body, pageNumBody);
        call.enqueue(new Callback<OcrResponse>() {
            @Override
            public void onResponse(Call<OcrResponse> call, Response<OcrResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String resultText = response.body().extracted_text;
                    Toast.makeText(CameraActivity.this, "서버 전송 성공", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CameraActivity.this, ViewerActivity.class);
                    intent.putExtra("OCR_TEXT", resultText);
                    startActivity(intent);

                    finish();
                } else {
                    Toast.makeText(CameraActivity.this, "서버 응답 오류", Toast.LENGTH_SHORT).show();
                }

                // 처리 후 기기에서 임시 파일 삭제
                if (photoFile.exists()) { photoFile.delete(); }
            }

            @Override
            public void onFailure(Call<OcrResponse> call, Throwable t) {
                Toast.makeText(CameraActivity.this, "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                if (photoFile.exists()) { photoFile.delete(); }
            }
        });
    }

    private void uploadMultipleToServer(List<File> fileList) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://egal-furcately-nydia.ngrok-free.dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // 여러 개의 파일 담을 리스트
        List<MultipartBody.Part> parts = new ArrayList<>();

        for (File file : fileList) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            parts.add(body);
        }

        RequestBody pageNumBody = RequestBody.create(MediaType.parse("text/plain"), "1");

        // 리스트 전송
        Call<OcrResponse> call = apiService.uploadMultipleImages(parts, pageNumBody);
        call.enqueue(new Callback<OcrResponse>() {
            @Override
            public void onResponse(Call<OcrResponse> call, Response<OcrResponse> response) {
                for (File f : fileList) { if (f.exists()) f.delete();}
            }

            @Override
            public void onFailure(Call<OcrResponse> call, Throwable t) {
                for (File f : fileList) { if (f.exists()) f.delete(); }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}