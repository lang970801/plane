package com.example.admin.aircraft.view;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.aircraft.MySetCustomDialog;
import com.example.admin.aircraft.VoiceUitl;
import com.example.admin.aircraft.util.JsonParser;
import com.example.admin.aircraft.util.PermissionsUtils;
import com.example.admin.aircraft.R;
import com.example.admin.aircraft.customview.RockerView;
import com.example.admin.aircraft.customview.RockerView.Direction;
import com.example.admin.aircraft.customview.RockerView.DirectionMode;
import com.example.admin.aircraft.customview.RockerView.OnShakeListener;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static android.content.ContentValues.TAG;

public class ControlPanel extends Activity implements View.OnClickListener {
    private TextView tv;
    private ImageButton suoding,lianjie,luxiang,xuanting,shengying;
    private RockerView telecontrol1,telecontrol2;
    private Button yuyingBt,setBt;
    private Switch bluetooth;
    private boolean flag=true;
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String> ();
    private boolean isAnimFinished = true;
    TextView info_tx;
    private Toast mToast;
    // 引擎类型，有云端和本地
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.control_layout );
        PermissionsUtils.getInstance().chekPermissions(this, permissions, permissionsResult);
        initView();
        initData();
       /* initrokerview();
        initrokerview2();*/
       init ();

    }
    private void initView() {
        yuyingBt=findViewById ( R.id.yuyingBt );
        setBt=findViewById ( R.id.setBt );
        yuyingBt.setOnClickListener ( this );
        setBt.setOnClickListener ( this );

    }
    @Override
    public void onClick(View v) {
        switch (v.getId ()){
            case R.id.yuyingBt:
                recorderView();
                break;
            case R.id.setBt:
                MySetCustomDialog dialog = new MySetCustomDialog(this,"Enter your name",new MySetCustomDialog.OnCustomDialogListener() {

                    @Override
                    public void back(String name) {
                       /* resultText.setText("Enter name is "+name);*/

                    }
                });
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();
                break;

        }
    }
    public void init(){
        mp = MediaPlayer.create(this,R.raw.bgm);
        try
        {
            mp.prepare();
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (!mp.isPlaying())
        {
            mp.start();
        }
    }
    private void initData() {
        //读写权限和录音权限
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                finish();
                showTip("没有录音权限或者文件读写权限");
                return;
            }
        }

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(this, mInitListener);
        Log.e(TAG, "mIatDialog:" + (mIatDialog == null));
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        if (null == mIat) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            finish();
            return;
        }

    }
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };
    private void recorderView() {
        // 显示听写对话框
        mIatDialog.setListener(mRecognizerDialogListener);
        mIatDialog.show();
        showTip("识别语音显示对话框");
    }
    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
    private String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE}; //申请两个权限，录音和文件读写
    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.e ( TAG, "RecognizerDialogListener :" + results.getResultString () );
            Log.e ( TAG, " ,isLast:" + isLast );
            printResult ( results );
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "printResult text :" + text);
        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

       /* info_tx.setText(resultBuffer.toString());*/

    }

    //创建监听权限的接口对象
    PermissionsUtils.IPermissionsResult permissionsResult = new PermissionsUtils.IPermissionsResult() {
        @Override
        public void passPermissons() {
            Toast.makeText(ControlPanel.this, "权限通过，可以做其他事情!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void forbitPermissons() {
            finish();
            Toast.makeText(ControlPanel.this, "权限不通过!", Toast.LENGTH_SHORT).show();
        }
    };


 /* }*/
    public boolean isChangeImg(boolean flag,ImageButton imgVeiw,int defId ,int startid){
        if(flag){
            imgVeiw.setImageResource (defId );
            return false;
        }else {
            imgVeiw.setImageResource (startid);
            return true;
        }
    }
    public void initrokerview(){

        //实时监测摇动方向
        telecontrol1.setOnShakeListener(DirectionMode.DIRECTION_8, new OnShakeListener() {
            //开始摇动时要执行的代码写在本方法里
            @Override
            public void onStart() {

            }
            //结束摇动时要执行的代码写在本方法里
            @Override
            public void onFinish() {
                /*Toast.makeText(MainActivity.this, "已复位", 0).show();*/
            }
            //摇动方向时要执行的代码写在本方法里
            @Override
            public void direction(Direction direction) {
                if (direction == RockerView.Direction.DIRECTION_CENTER){
                  /*  tv.setText("中心");*/
                }else if (direction == RockerView.Direction.DIRECTION_DOWN){
//                    tv.setText("下");
                }else if (direction == RockerView.Direction.DIRECTION_LEFT){
                   /* tv.setText("左");*/
                }else if (direction == RockerView.Direction.DIRECTION_UP){
                   /* tv.setText("上");*/
                }else if (direction == RockerView.Direction.DIRECTION_RIGHT){
                   /* tv.setText("右");*/
                }else if (direction == RockerView.Direction.DIRECTION_DOWN_LEFT){
                   /* tv.setText("左下");*/
                }else if (direction == RockerView.Direction.DIRECTION_DOWN_RIGHT){
                    /*tv.setText("右下");*/
                }else if (direction == RockerView.Direction.DIRECTION_UP_LEFT){
                   /* tv.setText("左上");*/
                }else if (direction == RockerView.Direction.DIRECTION_UP_RIGHT){
                  /*  tv.setText("右上");*/
                }

            }
        });
    }
    public void initrokerview2() {
        //实时监测摇动方向
        telecontrol2.setOnShakeListener ( DirectionMode.DIRECTION_8, new OnShakeListener () {
            //开始摇动时要执行的代码写在本方法里
            @Override
            public void onStart() {

            }

            //结束摇动时要执行的代码写在本方法里
            @Override
            public void onFinish() {
                /*Toast.makeText(MainActivity.this, "已复位", 0).show();*/
            }

            //摇动方向时要执行的代码写在本方法里
            @Override
            public void direction(Direction direction) {
                if (direction == RockerView.Direction.DIRECTION_CENTER) {
                    /*tv.setText ( "中心" );*/
                } else if (direction == RockerView.Direction.DIRECTION_DOWN) {
                    /*tv.setText ( "下" );*/
                } else if (direction == RockerView.Direction.DIRECTION_LEFT) {
                   /* tv.setText ( "左" );*/
                } else if (direction == RockerView.Direction.DIRECTION_UP) {
                   /* tv.setText ( "上" );*/
                } else if (direction == RockerView.Direction.DIRECTION_RIGHT) {
                   /* tv.setText ( "右" );*/
                } else if (direction == RockerView.Direction.DIRECTION_DOWN_LEFT) {
                   /* tv.setText ( "左下" );*/
                } else if (direction == RockerView.Direction.DIRECTION_DOWN_RIGHT) {
                   /* tv.setText ( "右下" );*/
                } else if (direction == RockerView.Direction.DIRECTION_UP_LEFT) {
                  /*  tv.setText ( "左上" );*/
                } else if (direction == RockerView.Direction.DIRECTION_UP_RIGHT) {
                   /* tv.setText ( "右上" );*/
                }
            }
        } );
    }
}