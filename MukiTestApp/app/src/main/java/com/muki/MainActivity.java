package com.muki;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.Random;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.muki.core.MukiCupApi;
import com.muki.core.MukiCupCallback;
import com.muki.core.model.Action;
import com.muki.core.model.DeviceInfo;
import com.muki.core.model.ErrorCode;
import com.muki.core.model.ImageProperties;
import com.muki.core.util.ImageUtils;

import android.Manifest;
import android.support.v4.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private EditText mSerialNumberEdit;
    private TextView mCupIdText;
    private TextView mDeviceInfoText;
    private ImageView mCupImage;
    private SeekBar mContrastSeekBar;
    private ProgressDialog mProgressDialog;

    private Bitmap mImage, img;
    private int mContrast = ImageProperties.DEFAULT_CONTRACT;

    private String mCupId;
    private MukiCupApi mMukiCupApi;

    private Paint mPaint;
    private Canvas mCanvas;

    private Random mRandom;
    private Integer mNuclear;
    private Integer mSolar;
    private Integer mWind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = Bitmap.createBitmap(176,264, Bitmap.Config.RGB_565);
        img.eraseColor(Color.WHITE);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2.5F);
        mPaint.setStyle(Paint.Style.STROKE);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Loading. Please wait...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);



        mMukiCupApi = new MukiCupApi(getApplicationContext(), new MukiCupCallback() {
            @Override
            public void onCupConnected() {
                showToast("Cup connected");
                Log.w("muki", "CONNECT");
            }

            @Override
            public void onCupDisconnected() {
                showToast("Cup disconnected");
                Log.w("muki", "DISCO");
            }

            @Override
            public void onDeviceInfo(DeviceInfo deviceInfo) {
                hideProgress();
                mDeviceInfoText.setText(deviceInfo.toString());
            }

            @Override
            public void onImageCleared() {
                showToast("Image cleared");
            }

            @Override
            public void onImageSent() {
                showToast("Image sent");
            }

            @Override
            public void onError(Action action, ErrorCode errorCode) {
                showToast("Error:" + errorCode + " on action:" + action);
            }
        });

        mSerialNumberEdit = (EditText) findViewById(R.id.serailNumberText);
        mSerialNumberEdit.setText("0003833");
        mCupIdText = (TextView) findViewById(R.id.cupIdText);
        mCupIdText.setText("PAULIG_MUKI_3F99F6");
        mCupId =  "PAULIG_MUKI_3F99F6";
        mDeviceInfoText = (TextView) findViewById(R.id.deviceInfoText);
        mCupImage = (ImageView) findViewById(R.id.imageSrc);

        mContrastSeekBar = (SeekBar) findViewById(R.id.contrastSeekBar);
        mContrastSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mContrast = i - 100;
                showProgress();
                setupImage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mRandom = new Random();

        mWind = mRandom.nextInt(30) + 10;
        mSolar = mRandom.nextInt(20) + 5;
        mNuclear = mRandom.nextInt(10) + 1;

        TextView windTextView = (TextView) findViewById(R.id.textViewWind);
        String postFix = " %";
        windTextView.setText(mWind.toString() + postFix);

        TextView solarTextView = (TextView) findViewById(R.id.textViewSolar);
        solarTextView.setText(mSolar.toString() + postFix);

        TextView nuclearTextView = (TextView) findViewById(R.id.textViewNuclear);
        nuclearTextView.setText(mNuclear.toString() + postFix);

        Typeface font = Typeface.createFromAsset( getAssets(), "fontawesome-webfont.ttf" );
        //Button refreshButton = (Button)findViewById( R.id.refresh );
        //refreshButton.setTypeface(font);
        //TextView sunIcon = (TextView)findViewById( R.id.sun );
        //sunIcon.setTypeface(font);

        reset(null);
    }

    private void setupImage() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                return img;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                mCupImage.setImageBitmap(bitmap);
                hideProgress();
            }
        }.execute();
    }

    public void crop(View view) {
        showProgress();
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
        mImage = ImageUtils.cropImage(image, new Point(100, 0));
        image.recycle();
        setupImage();
    }

    public void reset(View view) {
        showProgress();
        img.eraseColor(Color.WHITE);

        char up = 0x25b4;
        char down = 0x25be;
        drawTxt(mWind.toString() + " % " + String.valueOf(up), Color.BLACK, 30, 80, 50);
        drawTxt(mSolar.toString() + " % " + String.valueOf(down), Color.BLACK, 30, 80, 105);
        drawTxt(mNuclear.toString() + " % " + String.valueOf(up), Color.BLACK, 30, 80, 165);

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        drawTxt(dateFormat.format(Calendar.getInstance().getTime()), Color.BLACK, 30, 5, 229);

        drawTxt("How am I powered?", Color.BLACK, 19, 5, 5);

        Bitmap image = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.wind),50,50,true);
        drawBitmap(image, 5, 50);
        image = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.sun),50,50,true);
        drawBitmap(image, 5, 105);
        image = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.atom),50,50,true);
        drawBitmap(image, 5, 160);

        setupImage();
        image.recycle();
    }

    public void send(View view) {
        showProgress();
        mMukiCupApi.sendImage(img, new ImageProperties(mContrast), mCupId);
    }

    public void drawTxt(String text, int color, int fontsize, int x, int y) {
        mPaint.setColor(color);
        mPaint.setTextSize(fontsize);
        mCanvas = new Canvas(img);
        mCanvas.drawText(text,x,y+fontsize,mPaint);
    }

    public void drawBitmap(Bitmap bitmap, int x, int y) {
        mCanvas = new Canvas(img);
        mCanvas.drawBitmap(bitmap,x,y,mPaint);
    }

    public void clear(View view) {
        showProgress();
        mMukiCupApi.clearImage(mCupId);
    }

    public void request(View view) {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);

        String serialNumber = mSerialNumberEdit.getText().toString();
        showProgress();
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    String serialNumber = strings[0];
                    return MukiCupApi.cupIdentifierFromSerialNumber(serialNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                mCupId = s;
                mCupIdText.setText(mCupId);
                hideProgress();
            }
        }.execute(serialNumber);
    }

    public void deviceInfo(View view) {
        showProgress();
        mMukiCupApi.getDeviceInfo(mCupId);
    }

    private void showToast(final String text) {
        hideProgress();
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    private void showProgress() {
        mProgressDialog.show();
    }

    private void hideProgress() {
        mProgressDialog.dismiss();
    }
}
