package vn.iotstar.bt10_ltmobile;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import vn.iotstar.bt10_ltmobile.R;

public class UploadVideoActivity extends AppCompatActivity {
    private static final int PICK_VIDEO_REQUEST = 1;
    private static final int REQUEST_PERMISSION = 100;
    private Uri videoUri;
    Button btnUploadVideo;
    EditText txtTitle, txtDesc;
    Cloudinary cloudinary;
    DatabaseReference firebaseDB;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_video);
        btnUploadVideo = findViewById(R.id.btnOpenAndUpload);
        txtTitle = findViewById(R.id.txtTitle);
        txtDesc = findViewById(R.id.txtDesc);
        progressDialog = new ProgressDialog(UploadVideoActivity.this);

        // Firebase
        firebaseDB = FirebaseDatabase.getInstance().getReference("videos");

        // Cloudinary config
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "da0zhlez4",
                "api_key", "622443243276779",
                "api_secret", "IW0g7q4nx3wkit53b1QaNyhPRJo"));

        btnUploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndPickVideo();
            }
        });

    }
    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn video"), PICK_VIDEO_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            uploadVideoToCloudinary(videoUri);
        }
    }

    private void uploadVideoToCloudinary(Uri uri) {
        progressDialog.setMessage("Đang upload video...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(() -> {
            try {
                File file = new File(getRealPathFromURI(uri));
                Map uploadResult = cloudinary.uploader().upload(file,
                        ObjectUtils.asMap("resource_type", "video"));

                String videoUrl = (String) uploadResult.get("secure_url");

                saveVideoToFirebase(txtTitle.getText().toString(), txtDesc.getText().toString(), videoUrl);

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();

    }
    private void saveVideoToFirebase(String title, String desc, String url) {
        /*String id = firebaseDB.push().getKey();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        if (user == null) {
            Log.e("FIREBASE", "User chưa đăng nhập");
            return; // hoặc show thông báo
        }
        Map<String, Object> video = new HashMap<>();
        video.put("title", title);
        video.put("desc", desc);
        video.put("url", url);
        if (user != null) {
            video.put("uid", user.getUid());
            video.put("email",user.getEmail());

            if (user.getPhotoUrl() != null) {
                video.put("avatarUrl", user.getPhotoUrl().toString());
            } else {
                video.put("avatarUrl", ""); // hoặc avatar mặc định
            }
        }

        if (id != null) {
            firebaseDB.child(id).setValue(video)
                    .addOnSuccessListener(aVoid ->
                            Log.d("FIREBASE", "Video saved: " + url))
                    .addOnFailureListener(e ->
                            Log.e("FIREBASE", "Save failed", e));
        }*/
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn phải đăng nhập để upload video", Toast.LENGTH_SHORT).show();
            return; // Return early if the user is not logged in
        }

        String uid = user.getUid();
        String email = user.getEmail();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                if (avatarUrl == null || avatarUrl.isEmpty()) {
                    avatarUrl = "https://example.com/default-avatar.png"; // Provide default avatar URL
                }

                DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference("videos").push();

                Map<String, Object> videoMap = new HashMap<>();
                videoMap.put("uid", uid);
                videoMap.put("email", email);
                videoMap.put("avatarUrl", avatarUrl);
                videoMap.put("title", title);
                videoMap.put("desc", desc);
                videoMap.put("url", url);

                videoRef.setValue(videoMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UploadVideoActivity.this, "Upload thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UploadVideoActivity.this, "Upload thất bại", Toast.LENGTH_SHORT).show();
                        Log.e("FIREBASE", "Video upload failed: " + task.getException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadVideoActivity.this, "Lỗi khi lấy avatar người dùng", Toast.LENGTH_SHORT).show();
                Log.e("FIREBASE", "Error fetching user data: " + error.getMessage());
            }
        });

    }
    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
    private void checkPermissionAndPickVideo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, REQUEST_PERMISSION);
            } else {
                openVideoPicker();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            } else {
                openVideoPicker();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openVideoPicker();
            } else {
                Toast.makeText(this, "Bạn phải cấp quyền để chọn video", Toast.LENGTH_SHORT).show();
            }
        }
    }

}