package com.example.myhomework.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.example.myhomework.utils.FileUtils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.example.myhomework.bean.HistoryRecord;
import com.example.myhomework.R;

import android.widget.RadioButton;
import android.widget.Toast;


import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.example.myhomework.service.HistoryRecordService;
import com.example.myhomework.service.UserService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public class UpdatePhotoActivity extends AppCompatActivity {
    private MapView mMapView ;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private Button button;
    private ImageView imageview;
    private Uri imageUri;
    private String filename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        setContentView(R.layout.activity_update_photo);
        ImageView imageView;
        imageView = findViewById(R.id.picture);
        imageView.setOnClickListener(v -> {
            //???????????????????????????
            PopupMenu popup = new PopupMenu(this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.photo_menu, popup.getMenu());
            //??????????????????????????????
            popup.setOnMenuItemClickListener(item->{
                switch (item.getItemId()) {
                    case R.id.photo:
                        filename= UUID.randomUUID().toString();
                        File outputImage = new File(getExternalCacheDir(),filename);
                        try {
                            if (outputImage.exists()) {
                                outputImage.delete();
                            }
                            outputImage.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (Build.VERSION.SDK_INT < 24) {
                            imageUri = Uri.fromFile(outputImage);

                        } else {
                            ContentValues contentValues = new ContentValues(1);
                            contentValues.put(MediaStore.Images.Media.DATA, outputImage.getPath());
                            imageUri = FileProvider.getUriForFile(this,
                                    getPackageName() + ".fileprovider",
                                    outputImage);
                        }
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                        startActivityForResult(intent,3);
                        break;
                    case R.id.select:
                        openAlbum();
                        break;
                    default:
                        break;
                }
                return false;
            });
            popup.show();
        });


        //????????????????????????
        mMapView = (MapView)findViewById(R.id.baiduMapView);
        //???????????????
        mBaiduMap = mMapView.getMap();
        //???????????????????????????
        init();
        //????????????????????????????????????????????????????????????????????????
        configure();
        //???????????????
        init_location();
        button=findViewById(R.id.push);
        EditText title,msg;
        RadioButton radioButton01,radioButton02,radioButton03,radioButton10,radioButton11;
        title=findViewById(R.id.title);
        msg=findViewById(R.id.msg);
        radioButton01=findViewById(R.id.radioButton01);
        radioButton02=findViewById(R.id.radioButton02);
        radioButton03=findViewById(R.id.radioButton03);
        radioButton10=findViewById(R.id.radioButton10);
        radioButton11=findViewById(R.id.radioButton11);
        button.setOnClickListener(v->{
            String title_s,msg_s;
            title_s=title.getText().toString();
            msg_s=msg.getText().toString();
            if(title_s.equals("")||msg_s.equals("")||img.equals("")){
                Toast.makeText(this,"?????????????????????????????????",Toast.LENGTH_LONG).show();
                return;
            }
            HistoryRecord historyRecord=new HistoryRecord();
            historyRecord.setTitle(title_s);
            historyRecord.setMsg(msg_s);
            historyRecord.setImg(img);
            if(radioButton01.isChecked()){
                historyRecord.setQuestion("????????????");
            }
            if(radioButton02.isChecked()){
                historyRecord.setQuestion("????????????");
            }
            if(radioButton03.isChecked()){
                historyRecord.setQuestion("????????????");
            }
            if(radioButton10.isChecked()){
                historyRecord.setLevel("??????");
            }
            if(radioButton11.isChecked()){
                historyRecord.setLevel("??????");
            }
            historyRecord.setState("?????????");
            historyRecord.setUid(UserService.GetUid());
            HistoryRecordService.addRecord(historyRecord,this,path);
        });
    }


    //??????
    private void openAlbum(){
     //   Toast.makeText(this,"openAlbum",Toast.LENGTH_LONG).show();
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,2);//????????????
    }

    @Override
    protected void onResume()
    {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
    /**
     * ???????????????BDAbstractListener????????????onReceieveLocation?????????????????????????????????????????????MapView???
     */

    public class MyLocationListener extends BDAbstractLocationListener
    {
        @Override
        public void onReceiveLocation(BDLocation location)
        {
            //mapView ???????????????????????????????????????
            if (location == null || mMapView == null)
            {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // ?????????????????????????????????????????????????????????0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

        }
    }

    public void init()
    {
        //???????????????????????????
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(18f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }
    /**
     * ???????????????:
     * ????????????
     * (1)???????????? ??????SDK???????????????????????????NORMAL???????????????, FOLLOWING???????????????, COMPASS???????????????
     * ???2?????????????????????
     * ???3???????????????????????? ????????????????????????????????????
     * ???4?????????????????????????????????
     * ???5?????????????????????????????????
     */

    public void configure()
    {
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true,
                BitmapDescriptorFactory.fromResource(R.drawable.icon_location),
                0xAAFFFF88, 0xAA00FF00));
    }

    /**
     * ??????????????????
     */
    public void init_location()
    {
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(UpdatePhotoActivity.this);
        //??????LocationClientOption??????LocationClient????????????
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // ??????gps
        option.setCoorType("bd09ll"); // ??????????????????
        option.setScanSpan(1000);
        //??????locationClientOption
        mLocationClient.setLocOption(option);
        //??????LocationListener?????????
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //????????????????????????
        mLocationClient.start();
    }
    String path="";
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        switch (requestCode) {
            case 2:
                if (Build.VERSION.SDK_INT >= 19) {
                    Uri uri=data.getData();
                    path= FileUtils.getPath(this,uri);
                    img=path.split("/")[path.split("/").length-1];
                    this.imageview=findViewById(R.id.picture);
                    this.imageview.setImageURI(uri);
                } else {
                    handleImageBeforeKitKat(data);
                    Toast.makeText(this,"4.4??????",Toast.LENGTH_LONG).show();
                }
                break;
            case 3:
                path= imageUri.getPath();
                img=path.split("/")[path.split("/").length-1];
                this.imageview=findViewById(R.id.picture);
                this.imageview.setImageURI(imageUri);
                Bitmap bitmap= null;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //????????????
                MediaStore.Images.Media.insertImage(this.getContentResolver(),
                        bitmap,filename, null);
            default:
                break;

        }
    }
    @TargetApi(19)
    private void handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }
    String img="";
    private  String getImagePath(Uri uri,String selection){
        String path =null;
        Cursor cursor =getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private  void displayImage(String imagePath){
        if(imagePath!=null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            this.imageview=findViewById(R.id.picture);
            this.imageview.setImageBitmap(bitmap);
        }else {
            Toast.makeText(this,"??????????????????",Toast.LENGTH_LONG).show();
        }
    }

}