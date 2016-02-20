package com.sh.autocall;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    protected static final String TAG = "MainActivity";

    private int defCallInterval = 10 * 1000;
    private int defHangUpInterval = 10 * 1000;

    private static final int startCall = 1001;
    private static final int stopCall = 1002;

    private static final int PHONE_STATE_CHANGED = 101;

    /**
     * Called when the activity is first created.
     */
    private Button btn_start = null;
    private Button btn_stop = null;
    private EditText et_phone_no = null;
    private EditText et_call_interval = null;
    private EditText et_hand_up_interval = null;
    private boolean isRunnable = true;
    private boolean endCall = false;
    private String telePhotoNo = null;
    private String callInterval = null;
    private String hangUpInterval = null;
    ITelephony iPhoney = null;
    //private TelephonyManager;
//    Thread callThread = null;

    boolean isFirstStart = true;

    // 检查权限
    PackageManager pm = null;
    boolean callPhonePermission = false;
    boolean readPhoneStatePermission = false;

    TelephonyManager tm = null;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case startCall:
                    startCallPhone();
                    break;

                case stopCall:
                    stopCallPhone();
                    break;

            }

            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pm = getPackageManager();
        callPhonePermission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.CALL_PHONE", this.getPackageName()));
        readPhoneStatePermission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.READ_PHONE_STATE", this.getPackageName()));

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        et_phone_no = (EditText) findViewById(R.id.et_phone_no);
        et_call_interval = (EditText) findViewById(R.id.et_call_interval);
        et_hand_up_interval = (EditText) findViewById(R.id.et_hand_up_interval);

//        et_phone_no.setText("10086");
//        et_call_interval.setText("10");
//        et_hand_up_interval.setText("10");

        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        iPhoney = getITelephony(this);//获取电话实例
        btn_start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if ((et_phone_no.getText().toString() != null && et_phone_no.getText().toString().length() > 0) && !et_phone_no.getText().toString().equals("请输入电话号码")) {
                    telePhotoNo = et_phone_no.getText().toString().trim();
                    //System.out.println(telePhotoNo);
                    isRunnable = true;
                    startCallPhone();
//                    callThread.start();
                } else
                    Toast.makeText(MainActivity.this, "请输入你所要拨打的电话号码", Toast.LENGTH_SHORT).show();

            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isFirstStart = true;
                isRunnable = false;
//                System.exit(0);
                // finish();

            }
        });

        et_phone_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (et_phone_no.getText().toString().equals("请输入电话号码"))
                    et_phone_no.setText("");//

            }
        });

//        callThread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    while (isRunnable) {
//
//                        int state = -1;
//                        if (readPhoneStatePermission) {
//                            state = tm.getCallState();
//                        }
//
//                        if (state == TelephonyManager.CALL_STATE_IDLE) {
//                            startCallPhone();
//                        } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
//                            stopCallPhone();
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    /**
     * 通过反射得到实例
     *
     * @param context
     * @return
     */
    private static ITelephony getITelephony(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context
                .getSystemService(TELEPHONY_SERVICE);
        Class<TelephonyManager> c = TelephonyManager.class;
        Method getITelephonyMethod = null;
        try {
            getITelephonyMethod = c.getDeclaredMethod("getITelephony",
                    (Class[]) null); // 获取声明的方法
            getITelephonyMethod.setAccessible(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        ITelephony iTelephony = null;
        try {
            iTelephony = (ITelephony) getITelephonyMethod.invoke(
                    mTelephonyManager, (Object[]) null); // 获取实例
            return iTelephony;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iTelephony;
    }


    private void startCallPhone() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telePhotoNo));
        if (callPhonePermission) {
            if (isFirstStart) {
                isFirstStart = false;
            } else {
                try {
                    callInterval = et_call_interval.getText().toString();
                    if (callInterval != null && callInterval.length() > 0 && Integer.valueOf(callInterval.trim()) > 0) {
                        Thread.sleep(Integer.valueOf(callInterval.trim()) * 1000); // 延时拨打
                    } else {
                        Thread.sleep(defCallInterval); // 延时拨打
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (isRunnable) {
                int state = -1;
                if (readPhoneStatePermission) {
                    state = tm.getCallState();
                }

                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    startActivity(intent);
                    Log.d(TAG, "拨打电话==============");

                    Message msg = Message.obtain();
                    msg.what = stopCall;
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    private void stopCallPhone() {
        try {
            hangUpInterval = et_hand_up_interval.getText().toString();
            if (hangUpInterval != null && hangUpInterval.length() > 0 && Integer.valueOf(hangUpInterval.trim()) > 0) {
                Thread.sleep(Integer.valueOf(hangUpInterval.trim()) * 1000); // 延时挂断
            } else {
                Thread.sleep(defHangUpInterval); // 延时挂断
            }

            int state = -1;
            if (readPhoneStatePermission) {
                state = tm.getCallState();
            }

            if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                do {
                    endCall = iPhoney.endCall();
                    Log.d(TAG, "挂断电话============== " + endCall);
                } while (!endCall);

                Message msg = Message.obtain();
                msg.what = startCall;
                mHandler.sendMessage(msg);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //System.out.println("是否成功挂断："+endCall);
    }
}
