package com.example.sinvoicedemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoicePlayer;
import com.libra.sinvoice.SinVoiceRecognition;

public class MainActivity extends Activity implements SinVoiceRecognition.Listener, SinVoicePlayer.Listener {
    private final static String TAG = "MainActivity";
    private final static int MAX_NUMBER = 5;
    private final static int MSG_SET_RECG_TEXT = 1;
    private final static int MSG_RECG_START = 2;
    private final static int MSG_RECG_END = 3;

    private final static String CODEBOOK = "12345";

    private Handler mHanlder;
    private SinVoicePlayer mSinVoicePlayer;
    private SinVoiceRecognition mRecognition;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSinVoicePlayer = new SinVoicePlayer(CODEBOOK);
        mSinVoicePlayer.setListener(this);

        mRecognition = new SinVoiceRecognition(CODEBOOK);
        mRecognition.setListener(this);

        final TextView playTextView = (TextView) findViewById(R.id.playtext);
        TextView recognisedTextView = (TextView) findViewById(R.id.regtext);
        mHanlder = new RegHandler(recognisedTextView);

        Button playStart = (Button) this.findViewById(R.id.start_play);
        playStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String text = genText(6);
                playTextView.setText(text);
                mSinVoicePlayer.play(text);
            }
        });

        Button playStop = (Button) this.findViewById(R.id.stop_play);
        playStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSinVoicePlayer.stop();
            }
        });

        Button recognitionStart = (Button) this.findViewById(R.id.start_reg);
        recognitionStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mRecognition.start();
            }
        });

        Button recognitionStop = (Button) this.findViewById(R.id.stop_reg);
        recognitionStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mRecognition.stop();
            }
        });
    }

    
    /**
     * 随机生成count位数,
     * 6位数，前后加token，验证时根据token判断
     * @param count
     * @return
     */
    private String genText(int count) {
    	
        StringBuilder sb = new StringBuilder();
//        sb.append("4");
        int pre = 0;
        while (count > 0) {
            int x = (int) (Math.random() * MAX_NUMBER + 1);
            if (Math.abs(x - pre) > 0) {
                sb.append(x);
                --count;
                pre = x;
            }
        }
//        sb.append("2");

        return sb.toString();
    }

    private static class RegHandler extends Handler {
        private StringBuilder mTextBuilder = new StringBuilder();
        private TextView mRecognisedTextView;

        public RegHandler(TextView textView) {
            mRecognisedTextView = textView;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SET_RECG_TEXT://正在设置识别的字符
                char ch = (char) msg.arg1;
                mTextBuilder.append(ch);
                if (null != mRecognisedTextView) {
                	//最终的识别结果
                    mRecognisedTextView.setText(mTextBuilder.toString());
                }
                break;

            case MSG_RECG_START://识别开始，删除旧数据
                mTextBuilder.delete(0, mTextBuilder.length());
                LogHelper.d(TAG, "recognition start:"+" msg.arg1:"+msg.arg1);
                break;

            case MSG_RECG_END://识别结束,检查结果
                LogHelper.d(TAG, "recognition end:"+" msg.arg1:"+msg.arg1);
                LogHelper.d(TAG, "result "+mTextBuilder.toString());
                
//                if ((mTextBuilder.toString()).startsWith("4") && (mTextBuilder.toString()).endsWith("2")) {
//                	LogHelper.d(TAG, "识别成功 ："+mTextBuilder.toString());
//				}else {
//					LogHelper.d(TAG, "识别失败 ："+mTextBuilder.toString());
//				}
                if (msg.arg1 == 6) {
                	LogHelper.d(TAG, "识别成功 ："+mTextBuilder.toString());
				} else {
					LogHelper.d(TAG, "识别失败 ："+mTextBuilder.toString());
				}
                
                
                
                break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public void onRecognitionStart(int token) {//识别开始
//        mHanlder.sendEmptyMessage(MSG_RECG_START);
    	mHanlder.sendMessage(mHanlder.obtainMessage(MSG_RECG_START, token, 0));
    }

    @Override
    public void onRecognition(char ch) {//正在识别字符
        mHanlder.sendMessage(mHanlder.obtainMessage(MSG_SET_RECG_TEXT, ch, 0));
    }

    @Override
    public void onRecognitionEnd(int token) {//识别结束
//        mHanlder.sendEmptyMessage(MSG_RECG_END);
    	mHanlder.sendMessage(mHanlder.obtainMessage(MSG_RECG_END, token, 0));
        
    }

    @Override
    public void onPlayStart() {
        LogHelper.d(TAG, "start play");
    }

    @Override
    public void onPlayEnd() {
        LogHelper.d(TAG, "stop play");
    }

}
