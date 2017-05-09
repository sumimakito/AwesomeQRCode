package com.github.sumimakito.awesomeqrsample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sumimakito.awesomeqr.AwesomeQRCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private final int BKG_IMAGE = 822;
    private final int LOGO_IMAGE = 379;

    private ImageView qrCodeImageView;
    private EditText etColorLight, etColorDark, etContents, etMargin, etSize;
    private Button btGenerate, btSelectBG, btRemoveBackgroundImage;
    private CheckBox ckbWhiteMargin;
    private Bitmap backgroundImage = null;
    private AlertDialog progressDialog;
    private boolean generating = false;
    private CheckBox ckbAutoColor;
    private TextView tvAuthorHint;
    private ScrollView scrollView;
    private EditText etDotScale;
    private TextView tvJSHint;
    private CheckBox ckbBinarize;
    private CheckBox ckbRoundedDataDots;
    private EditText etBinarizeThreshold;
    private Bitmap qrBitmap;
    private Button btOpen;
    private EditText etLogoMargin;
    private EditText etLogoScale;
    private EditText etLogoCornerRadius;
    private Button btRemoveLogoImage;
    private Button btSelectLogo;
    private Bitmap logoImage;
    private ViewGroup configViewContainer, resultViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configViewContainer = (ViewGroup) findViewById(R.id.configViewContainer);
        resultViewContainer = (ViewGroup) findViewById(R.id.resultViewContainer);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        tvAuthorHint = (TextView) findViewById(R.id.authorHint);
        tvJSHint = (TextView) findViewById(R.id.jsHint);
        qrCodeImageView = (ImageView) findViewById(R.id.qrcode);
        etColorLight = (EditText) findViewById(R.id.colorLight);
        etColorDark = (EditText) findViewById(R.id.colorDark);
        etContents = (EditText) findViewById(R.id.contents);
        etSize = (EditText) findViewById(R.id.size);
        etMargin = (EditText) findViewById(R.id.margin);
        etDotScale = (EditText) findViewById(R.id.dotScale);
        btSelectBG = (Button) findViewById(R.id.backgroundImage);
        btSelectLogo = (Button) findViewById(R.id.logoImage);
        btRemoveBackgroundImage = (Button) findViewById(R.id.removeBackgroundImage);
        btRemoveLogoImage = (Button) findViewById(R.id.removeLogoImage);
        btGenerate = (Button) findViewById(R.id.generate);
        btOpen = (Button) findViewById(R.id.open);
        ckbWhiteMargin = (CheckBox) findViewById(R.id.whiteMargin);
        ckbAutoColor = (CheckBox) findViewById(R.id.autoColor);
        ckbBinarize = (CheckBox) findViewById(R.id.binarize);
        ckbRoundedDataDots = (CheckBox) findViewById(R.id.rounded);
        etBinarizeThreshold = (EditText) findViewById(R.id.binarizeThreshold);
        etLogoMargin = (EditText) findViewById(R.id.logoMargin);
        etLogoScale = (EditText) findViewById(R.id.logoScale);
        etLogoCornerRadius = (EditText) findViewById(R.id.logoRadius);
        etBinarizeThreshold = (EditText) findViewById(R.id.binarizeThreshold);

        ckbAutoColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etColorLight.setEnabled(!isChecked);
                etColorDark.setEnabled(!isChecked);
            }
        });

        ckbBinarize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etBinarizeThreshold.setEnabled(isChecked);
            }
        });

        btSelectBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                intent.setType("image/*");
                startActivityForResult(intent, BKG_IMAGE);
            }
        });

        btSelectLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                intent.setType("image/*");
                startActivityForResult(intent, LOGO_IMAGE);
            }
        });

        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qrBitmap != null) saveBitmap(qrBitmap);
            }
        });

        btRemoveBackgroundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundImage = null;
                Toast.makeText(MainActivity.this, "Background image removed.", Toast.LENGTH_SHORT).show();
            }
        });

        btRemoveLogoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoImage = null;
                Toast.makeText(MainActivity.this, "Logo image removed.", Toast.LENGTH_SHORT).show();
            }
        });

        btGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    generate(etContents.getText().length() == 0 ? "Makito loves Kafuu Chino." : etContents.getText().toString(),
                            etSize.getText().length() == 0 ? 800 : Integer.parseInt(etSize.getText().toString()),
                            etMargin.getText().length() == 0 ? 20 : Integer.parseInt(etMargin.getText().toString()),
                            etDotScale.getText().length() == 0 ? 0.3f : Float.parseFloat(etDotScale.getText().toString()),
                            ckbAutoColor.isChecked() ? Color.BLACK : Color.parseColor(etColorDark.getText().toString()),
                            ckbAutoColor.isChecked() ? Color.WHITE : Color.parseColor(etColorLight.getText().toString()),
                            backgroundImage,
                            ckbWhiteMargin.isChecked(),
                            ckbAutoColor.isChecked(),
                            ckbBinarize.isChecked(),
                            etBinarizeThreshold.getText().length() == 0 ? 128 : Integer.parseInt(etBinarizeThreshold.getText().toString()),
                            ckbRoundedDataDots.isChecked(),
                            logoImage,
                            etLogoMargin.getText().length() == 0 ? 10 : Integer.parseInt(etLogoMargin.getText().toString()),
                            etLogoCornerRadius.getText().length() == 0 ? 8 : Integer.parseInt(etLogoCornerRadius.getText().toString()),
                            etLogoScale.getText().length() == 0 ? 10 : Float.parseFloat(etLogoScale.getText().toString())
                    );
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error occurred, please check your configs.", Toast.LENGTH_LONG).show();
                }
            }
        });

        tvAuthorHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/SumiMakito/AwesomeQRCode";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        tvJSHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/SumiMakito/Awesome-qr.js";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        acquireStoragePermissions();
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void acquireStoragePermissions() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data.getData() != null) {
            try {
                Uri imageUri = data.getData();
                if (requestCode == BKG_IMAGE) {
                    backgroundImage = BitmapFactory.decodeFile(ContentHelper.absolutePathFromUri(this, imageUri));
                    Toast.makeText(this, "Background image added.", Toast.LENGTH_SHORT).show();
                } else if (requestCode == LOGO_IMAGE) {
                    logoImage = BitmapFactory.decodeFile(ContentHelper.absolutePathFromUri(this, imageUri));
                    Toast.makeText(this, "Logo image added.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (requestCode == BKG_IMAGE) {
                    Toast.makeText(this, "Failed to add the background image.", Toast.LENGTH_SHORT).show();
                } else if (requestCode == LOGO_IMAGE) {
                    Toast.makeText(this, "Failed to add the logo image.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void generate(final String contents, final int size, final int margin, final float dotScale,
                          final int colorDark, final int colorLight, final Bitmap background, final boolean whiteMargin,
                          final boolean autoColor, final boolean binarize, final int binarizeThreshold, final boolean roundedDD,
                          final Bitmap logoImage, final int logoMargin, final int logoCornerRadius, final float logoScale) {
        if (generating) return;
        generating = true;
        progressDialog = new ProgressDialog.Builder(this).setMessage("Generating...").setCancelable(false).create();
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    qrBitmap = AwesomeQRCode.create(contents, size, margin, dotScale, colorDark,
                            colorLight, background, whiteMargin, autoColor, binarize, binarizeThreshold,
                            roundedDD, logoImage, logoMargin, logoCornerRadius, logoScale);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            qrCodeImageView.setImageBitmap(qrBitmap);
                            configViewContainer.setVisibility(View.GONE);
                            resultViewContainer.setVisibility(View.VISIBLE);
                            if (progressDialog != null) progressDialog.dismiss();
                            generating = false;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null) progressDialog.dismiss();
                            configViewContainer.setVisibility(View.VISIBLE);
                            resultViewContainer.setVisibility(View.GONE);
                            generating = false;
                        }
                    });
                }
            }
        }).start();
    }

    private void saveBitmap(Bitmap bitmap) {
        FileOutputStream fos = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            File outputFile = new File(getPublicContainer(), System.currentTimeMillis() + ".png");
            fos = new FileOutputStream(outputFile);
            fos.write(byteArray);
            fos.close();
            Toast.makeText(this, "Image saved to " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save the image.", Toast.LENGTH_LONG).show();
        }
    }

    public static File getPublicContainer() {
        File musicContainer = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File aqr = new File(musicContainer, "AwesomeQR");
        if (aqr.exists() && !aqr.isDirectory()) {
            aqr.delete();
        }
        aqr.mkdirs();
        return aqr;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (configViewContainer.getVisibility() != View.VISIBLE) {
                configViewContainer.setVisibility(View.VISIBLE);
                resultViewContainer.setVisibility(View.GONE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
