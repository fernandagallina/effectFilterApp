package fernandagallina.filtermypicapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

import fernandagallina.filtermypicapp.effect.EffectContent;

public class MainActivity extends AppCompatActivity implements EffectFragment.OnListFragmentInteractionListener {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int CAMERA_AND_LOCATION_PERMISSION_CODE = 1;
    private static final int RESULT_LOAD_IMAGE = 123;
    private static final int TAKE_PICTURE = 321;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, CAMERA_AND_LOCATION_PERMISSION_CODE);
                }
            }

            getFragmentManager().beginTransaction()
                    .replace(R.id.activity_main, new CaptureChoiceFragment())
                    .commit();
        }
    }

    public void transitionFragments(ImageSource source) {

        switch (source) {
            case CAMERA:
                takePhoto();
                break;

            case GALERIA:
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;
        }
    }

    public void takePhoto() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            }
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case RESULT_LOAD_IMAGE:
                if(resultCode == RESULT_OK){
                    imageUri = data.getData();
                }
                break;

            case TAKE_PICTURE:
                if(resultCode == RESULT_OK) {

                }
        }

        if(imageUri != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.activity_main, new EffectFragment().newInstance(imageUri.toString()))
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        getFragmentManager().beginTransaction()
            .replace(R.id.activity_main, new CaptureChoiceFragment())
            .commit();
    }

    @Override
    public void onListFragmentInteraction(EffectContent.EffectItem item) {
    }
}
