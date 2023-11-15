package com.example.sm9m2cds1;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileReader; // 파일 입출력 제어
import java.io.BufferedReader; // 버퍼 입출력 제어
import java.io.FileNotFoundException; // 파일 미발견 에러 제어
import java.io.IOException; // 파일 입출력 예외 처리
import java.io.RandomAccessFile; 

import com.example.jnidriver.JNIDriver;
// 랜덤 액세스 파일 제어


public class MainActivity extends ActionBarActivity {

	ReceiveThread mSegThread;
	boolean mThreadRun = true;
	JNIDriver mDriver = new JNIDriver();
	boolean stopB = false;
	
	byte[] led0 ={1,0,0,0,0,0,0,0};
	byte[] led1 ={0,1,0,0,0,0,0,0};
	byte[] ledZero ={0,0,0,0,0,0,0,0};
	byte[] seg ={0,0,0,0,0,0};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	    Button btn =(Button) findViewById(R.id.button1);
	    btn.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mSegThread = new ReceiveThread();
				mSegThread.start();
				mThreadRun =true;
				stopB = false;
			}
		});
	    
	    Button btnStop = (Button)findViewById(R.id.button2);
	    btnStop.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				stopB=true;
			}
		});
	}
    
	private class ReceiveThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while(mThreadRun){
				Message text = Message.obtain();
				
				handler.sendMessage(text);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public Handler handler = new Handler(){
		
		public void handleMessage(Message msg){
			TextView tv;
			FileReader in;
			int in_cda;
			
			try {
				in = new FileReader("/sys/devices/12d10000.adc/iio:device0/in_voltage3_raw");
			
				BufferedReader br = new BufferedReader(in);
				String data = br.readLine();
				tv = (TextView) findViewById(R.id.textView1);
				in_cda =Integer.parseInt(data);
				
				seg[0] = (byte)(in_cda % 1000000 / 100000);		//입력된 숫자 6개의 각 digit를 분류해서 fnd에 6개 숫자를 출력
				seg[1] = (byte)(in_cda % 100000 / 10000);
				seg[2] = (byte)(in_cda % 10000/ 1000);
				seg[3] = (byte)(in_cda % 1000/ 100);
				seg[4] = (byte)(in_cda % 100/ 10);
				seg[5] = (byte)(in_cda % 10);
				int value =0;
				if(stopB ==true){
					tv.setText("CDS : ");
					mDriver.write(ledZero);
					mDriver.segwrite(seg);
					mThreadRun = false;
				}
				else if(in_cda>=3000&&in_cda<3500){
				    tv.setText("CDS : "+data);
					mDriver.write(led0);
					mDriver.segwrite(seg);
				}
				else if(in_cda>=3500){
				    tv.setText("CDS : "+data+"스프링쿨러 작동");
					mDriver.write(led0);
					mDriver.segwrite(seg);
					mDriver.setBuzzer((byte)0x01);
				}
				else if(in_cda<3000){
				    tv.setText("CDS : "+data);
					mDriver.write(led1);
					mDriver.segwrite(seg);
				}
				
				} catch (IOException e) {
				// TODO: handle exception
					e.printStackTrace();
			}
		}
	};
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mDriver.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(mDriver.open("/dev/sm9s5422_led","/dev/sm9s5422_segment","/dev/sm9s5422_piezo")<0){
			Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
		}
		super.onResume();
	}
	
	
}
