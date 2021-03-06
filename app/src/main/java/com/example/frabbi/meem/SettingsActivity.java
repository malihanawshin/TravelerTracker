package com.example.frabbi.meem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends BottomBarActivity {

    private EditText geteditname;
    private EditText getnewpassword;
    private EditText confirmnewpassword;
    private Button logoutBtn;
    private Button saveChangeBtn;
    private Button uploadPhotoBtn;
    private CircleImageView myimageview;
    private SwitchCompat mode;
    private Spinner time;
    Bitmap pp=null;


    private View.OnClickListener clickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.profilePhoto:
                    openImageGallery();
                    break;

                case R.id.saveChange:
                    saveNewChange();
                    break;

                case R.id.logout:
                    logoutFromAccount();
                    break;

                case R.id.uploadPhoto:
                    uploadPhoto();
                    break;

                case R.id.change_password:
                    //changePassword();
                    confirmnewpassword.setVisibility(View.VISIBLE);
                    break;

                /*case R.id.confirm_changed_password:
                    matchPassword();
                    break;
*/
            }
        }

    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_settings, frameContent);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        settings.setClickable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkResourcePermission();

        Account myAccount = ISystem.loadAccountFromCache(getApplicationContext());

        logoutBtn = (Button) findViewById(R.id.logout);
        logoutBtn.setOnClickListener(clickListener);

        saveChangeBtn = (Button) findViewById(R.id.saveChange);
        saveChangeBtn.setOnClickListener(clickListener);

        uploadPhotoBtn = (Button) findViewById(R.id.uploadPhoto);
        uploadPhotoBtn.setOnClickListener(clickListener);

        myimageview = (CircleImageView) findViewById(R.id.profilePhoto);
        ISystem.getImagebyUrl(getApplicationContext(), Constants.DestinationIp + myAccount.getImagePath(), new VolleyImageCallBack() {
            @Override
            public void success(Bitmap response) {
                myimageview.setImageBitmap(response);
            }
        });
        myimageview.setOnClickListener(clickListener);

        geteditname = (EditText) findViewById(R.id.editname);
        geteditname.setOnClickListener(clickListener);

        getnewpassword = (EditText) findViewById(R.id.change_password);
        getnewpassword.setOnClickListener(clickListener);

        confirmnewpassword = (EditText) findViewById(R.id.confirm_changed_password);
        confirmnewpassword.setOnClickListener(clickListener);

        time = (Spinner) findViewById(R.id.time);

        mode = (SwitchCompat) findViewById(R.id.auto);
        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showSpinner(isChecked);
            }
        });
    }

    protected void saveNewChange()
    {
        Account account = ISystem.loadAccountFromCache(getApplicationContext());

        String changedName=account.getName();
        String changedPass=account.password;

        if(geteditname.getText().toString().isEmpty() && getnewpassword.getText().toString().isEmpty())
            return;

        if(!geteditname.getText().toString().isEmpty()) changedName=geteditname.getText().toString();
        if(!getnewpassword.getText().toString().isEmpty() && matchPassword()) changedPass=getnewpassword.getText().toString();

        ISystem.updateInfo(getApplicationContext(), account.getId(), changedName, changedPass, new VolleyCallBack() {
            @Override
            public void success(String response) {
                Toast.makeText(getApplicationContext(), "Changes Saved !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean matchPassword() {
        if(!confirmnewpassword.getText().toString().equals(getnewpassword.getText().toString())){
            Toast.makeText(this, "Password not matched. Try again", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    protected void logoutFromAccount() {
        ISystem.resetAccountInCache(this);
        Toast.makeText(this, "Logged Out !", Toast.LENGTH_SHORT).show();
        this.finish();
        startActivity(new Intent(SettingsActivity.this, ActivityLogin.class));
    }

    protected void showSpinner(boolean toShow) {
        if (toShow) {
            time.setVisibility(View.VISIBLE);
        } else {
            time.setVisibility(View.GONE);
        }
    }

    private void openImageGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        if (getPackageManager().resolveActivity(intent, 0) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.MY_PERMISSIONS_REQUEST_RESOURCE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_RESOURCE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    String path = getRealPathFromURI(this, selectedImageUri);

                    SharedPreferences sp = getSharedPreferences("profilePhoto", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("image_path", path);
                    editor.commit();

                    Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
                    pp = BitmapFactory.decodeFile(path);
                    myimageview.setImageBitmap(pp);
                }
            }
        }
    }

    protected void uploadPhoto()
    {
        Account account = ISystem.loadAccountFromCache(getApplicationContext());
        ISystem.sendImageToServer(account, pp, getApplicationContext());
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    public boolean checkResourcePermission()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.MY_PERMISSIONS_REQUEST_RESOURCE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.MY_PERMISSIONS_REQUEST_RESOURCE);
            }
            return false;
        }
        else return false;
    }
}