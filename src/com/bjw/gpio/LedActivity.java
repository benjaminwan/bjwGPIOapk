package com.bjw.gpio;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * @author mywork
 * 控制4个LED
 * 通过线程可以实现循环亮灭
 */
public class LedActivity extends Activity {
	Button btnReadLeds,btnLedBack;
	RadioButton radioBoard6410,radioBoard210;
	RadioButton radioLEDNONE,radioLEDALL,radioLED1,radioLED2,radioLED3,radioLED4;
	ToggleButton tBtnLed1,tBtnLed2,tBtnLed3,tBtnLed4;
	EditText editLedGroup1,editLedGroup2,editLedGroup3,editLedGroup4;
	EditText editLedNum1,editLedNum2,editLedNum3,editLedNum4;
	LedAllThread mLedAll;
	LednThread mLedn;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.led_ctrl);
        setTitle("LED Control");
        btnReadLeds=(Button)findViewById(R.id.btnReadLeds);
        btnLedBack=(Button)findViewById(R.id.btnLedBack);
        btnReadLeds.setOnClickListener(new btnClickListener());
        btnLedBack.setOnClickListener(new btnClickListener());
        radioBoard6410=(RadioButton)findViewById(R.id.radioBoard6410);
        radioBoard210=(RadioButton)findViewById(R.id.radioBoard210);
        radioBoard6410.setOnClickListener(new radioClickListener());
        radioBoard210.setOnClickListener(new radioClickListener());
        tBtnLed1=(ToggleButton)findViewById(R.id.tBtnLed1);
        tBtnLed2=(ToggleButton)findViewById(R.id.tBtnLed2);
        tBtnLed3=(ToggleButton)findViewById(R.id.tBtnLed3);
        tBtnLed4=(ToggleButton)findViewById(R.id.tBtnLed4);
        tBtnLed1.setOnClickListener(new ToggleBtnOnClickListener());
        tBtnLed2.setOnClickListener(new ToggleBtnOnClickListener());
        tBtnLed3.setOnClickListener(new ToggleBtnOnClickListener());
        tBtnLed4.setOnClickListener(new ToggleBtnOnClickListener());
        editLedGroup1=(EditText)findViewById(R.id.editLedGroup1);
        editLedGroup2=(EditText)findViewById(R.id.editLedGroup2);
        editLedGroup3=(EditText)findViewById(R.id.editLedGroup3);
        editLedGroup4=(EditText)findViewById(R.id.editLedGroup4);
        editLedNum1=(EditText)findViewById(R.id.editLedNum1);
        editLedNum2=(EditText)findViewById(R.id.editLedNum2);
        editLedNum3=(EditText)findViewById(R.id.editLedNum3);
        editLedNum4=(EditText)findViewById(R.id.editLedNum4);
        radioLEDNONE=(RadioButton)findViewById(R.id.radioLEDNONE);
        radioLEDALL=(RadioButton)findViewById(R.id.radioLEDALL);
        radioLED1=(RadioButton)findViewById(R.id.radioLED1);
        radioLED2=(RadioButton)findViewById(R.id.radioLED2);
        radioLED3=(RadioButton)findViewById(R.id.radioLED3);
        radioLED4=(RadioButton)findViewById(R.id.radioLED4);      
        radioLEDNONE.setOnClickListener(new radioClickListener());
        radioLEDALL.setOnClickListener(new radioClickListener());
        radioLED1.setOnClickListener(new radioClickListener());
        radioLED2.setOnClickListener(new radioClickListener());
        radioLED3.setOnClickListener(new radioClickListener());
        radioLED4.setOnClickListener(new radioClickListener());
        mLedAll=new LedAllThread();
        mLedAll.setSuspendFlag();
        mLedAll.start();
        mLedn=new LednThread();
        mLedn.setSuspendFlag();
        mLedn.start();
        if (android.os.Build.BOARD.contains("6410")) {
			radioBoard6410.setChecked(true);
			editLedGroup1.setText("GPK");
			editLedGroup2.setText("GPK");
			editLedGroup3.setText("GPK");
			editLedGroup4.setText("GPK");
			editLedNum1.setText("4");
			editLedNum2.setText("5");
			editLedNum3.setText("6");
			editLedNum4.setText("7");
		} else if (android.os.Build.BOARD.contains("210"))
		{
        	radioBoard210.setChecked(true);
        	editLedGroup1.setText("GPJ2");
			editLedGroup2.setText("GPJ2");
			editLedGroup3.setText("GPJ2");
			editLedGroup4.setText("GPJ2");
			editLedNum1.setText("0");
			editLedNum2.setText("1");
			editLedNum3.setText("2");
			editLedNum4.setText("3");
		}
    }
    //--------------------------------------------------------------------
    //显示错误信息
    private void ShowErrMessage(int ret,String cmd)
	{
		if (ret==-1)
		{
			ShowMessage(cmd+GPIOJNI.cmdErr);
		} else if (ret==-2)
		{
			ShowMessage(cmd+GPIOJNI.fdErr);
		}
	}
	private void ShowMessage(String sMsg)
	{
			Toast.makeText(LedActivity.this, sMsg, Toast.LENGTH_SHORT).show();
	}
	//--------------------------------------------------------------------
	//CPU选择
    class radioClickListener implements View.OnClickListener{
		public void onClick(View v)
		{
			if (v==radioBoard6410)
			{
				editLedGroup1.setText("GPK");
				editLedGroup2.setText("GPK");
				editLedGroup3.setText("GPK");
				editLedGroup4.setText("GPK");
				editLedNum1.setText("4");
				editLedNum2.setText("5");
				editLedNum3.setText("6");
				editLedNum4.setText("7");
			} else if(v==radioBoard210)
			{
				editLedGroup1.setText("GPJ2");
				editLedGroup2.setText("GPJ2");
				editLedGroup3.setText("GPJ2");
				editLedGroup4.setText("GPJ2");
				editLedNum1.setText("0");
				editLedNum2.setText("1");
				editLedNum3.setText("2");
				editLedNum4.setText("3");
			} else if(v==radioLEDNONE)
			{
				mLedAll.setSuspendFlag();
				mLedn.setSuspendFlag();
			}else if(v==radioLEDALL){
				mLedAll.setResume();
				mLedn.setSuspendFlag();
			}else if(v==radioLED1){
				mLedAll.setSuspendFlag();
				mLedn.setLedn(1);
				mLedn.setResume();
			}else if(v==radioLED2){
				mLedAll.setSuspendFlag();
				mLedn.setLedn(2);
				mLedn.setResume();
			}else if(v==radioLED3){
				mLedAll.setSuspendFlag();
				mLedn.setLedn(3);
				mLedn.setResume();
			}else if(v==radioLED4){
				mLedAll.setSuspendFlag();
				mLedn.setLedn(4);
				mLedn.setResume();
			}
		}
    }
	//--------------------------------------------------------------------
    //读取LED状态，设置ToggleButton按钮状态
    private int ReadLed(int led)
	{
    	int ret=-1;
    	String pin_group;
    	int pin_num;
    	switch (led)
		{
		case 1:
			pin_group= editLedGroup1.getText().toString();
			pin_num = Integer.parseInt(editLedNum1.getText().toString());
			break;
		case 2:
			pin_group= editLedGroup2.getText().toString();
			pin_num = Integer.parseInt(editLedNum2.getText().toString());	
			break;
		case 3:
			pin_group= editLedGroup3.getText().toString();
			pin_num = Integer.parseInt(editLedNum3.getText().toString());
			break;
		case 4:
			pin_group= editLedGroup4.getText().toString();
			pin_num = Integer.parseInt(editLedNum4.getText().toString());
			break;
		default:
			return ret;
		}
    	ret=GPIOJNI.ReadGPIO(pin_group,pin_num);
		return ret;
	}
    //--------------------------------------------------------------------
    //设置LED状态
    private void SetLedState(int led,int ret)
   	{
       	switch (led)
   		{
   		case 1:
   			if (ret==0)
   			{
   				tBtnLed1.setChecked(true);
   			}else if (ret==1) {
   				tBtnLed1.setChecked(false);
   			}else {
   				ShowErrMessage(ret,"LED1");
   			}
   			break;
   		case 2:
   			if (ret==0)
   			{
   				tBtnLed2.setChecked(true);
   			}else if (ret==1) {
   				tBtnLed2.setChecked(false);
   			}else {
   				ShowErrMessage(ret,"LED2");
   			}
   			break;
   		case 3:
   			if (ret==0)
   			{
   				tBtnLed3.setChecked(true);
   			}else if (ret==1) {
   				tBtnLed3.setChecked(false);
   			}else {
   				ShowErrMessage(ret,"LED3");
   			}
   			break;
   		case 4:
   			if (ret==0)
   			{
   				tBtnLed4.setChecked(true);
   			}else if (ret==1) {
   				tBtnLed4.setChecked(false);
   			}else {
   				ShowErrMessage(ret,"LED4");
   			}
   			break;
   		default:
   			break;
   		}
   	}
    //--------------------------------------------------------------------
    //读取led状态按钮,返回按钮
    class btnClickListener implements View.OnClickListener{
    	String pin_group;
    	int pin_num;
    	int ret=-1;
		public void onClick(View v)
		{
			if (v==btnReadLeds)
			{
				SetLedState(1,ReadLed(1));
				SetLedState(2,ReadLed(2));
				SetLedState(3,ReadLed(3));
				SetLedState(4,ReadLed(4));
			}else if (v==btnLedBack) {
				LedActivity.this.finish();
			}
		}
    }
	//--------------------------------------------------------------------
    //设置LED状态
    private int WriteLed(int led,int pin_val)
	{
    	int ret=-1;
    	String pin_group;
    	int pin_num;
    	switch (led)
		{
		case 1:
			pin_group= editLedGroup1.getText().toString();
			pin_num = Integer.parseInt(editLedNum1.getText().toString());
			break;
		case 2:
			pin_group= editLedGroup2.getText().toString();
			pin_num = Integer.parseInt(editLedNum2.getText().toString());	
			break;
		case 3:
			pin_group= editLedGroup3.getText().toString();
			pin_num = Integer.parseInt(editLedNum3.getText().toString());
			break;
		case 4:
			pin_group= editLedGroup4.getText().toString();
			pin_num = Integer.parseInt(editLedNum4.getText().toString());
			break;
		default:
			return ret;
		}
    	ret=GPIOJNI.WriteGPIO(pin_group, pin_num, pin_val);
		return ret;
	}
    //--------------------------------------------------------------------
    //LED开关按钮
    class ToggleBtnOnClickListener implements View.OnClickListener{
		public void onClick(View v)
		{
			if (v==tBtnLed1)
			{
				if(tBtnLed1.isChecked())
				{
					ShowErrMessage(WriteLed(1,0),"LED1");
				}else {
					ShowErrMessage(WriteLed(1,1),"LED1");
				}
			} else if (v==tBtnLed2)
			{
				if(tBtnLed2.isChecked())
				{
					ShowErrMessage(WriteLed(2,0),"LED2");
				}else {
					ShowErrMessage(WriteLed(2,1),"LED2");
				}
			}else if (v==tBtnLed3)
			{
				if(tBtnLed3.isChecked())
				{
					ShowErrMessage(WriteLed(3,0),"LED3");
				}else {
					ShowErrMessage(WriteLed(3,1),"LED3");
				}
			}else if (v==tBtnLed4)
			{
				if(tBtnLed4.isChecked())
				{
					ShowErrMessage(WriteLed(4,0),"LED4");
				}else {
					ShowErrMessage(WriteLed(4,1),"LED4");
				}
			}
		}
    }
    //--------------------------------------------------------------------
    //LED循环亮灭线程
    class LedAllThread extends Thread{
    	public boolean suspendFlag = true;// 控制线程的执行
    	@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				synchronized (this)
				{
					while (suspendFlag)
					{
						try
						{
							wait();
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				WriteLed(1,0);//LED1亮
	        	try{
	        		Thread.sleep(200);//延时200ms
				} catch (Exception e){
					e.printStackTrace();
				}
	        	WriteLed(1,1);//LED1灭
				WriteLed(2,0);//LED2亮
				try{
	        		Thread.sleep(200);//延时200ms
				} catch (Exception e){
					e.printStackTrace();
				}
				WriteLed(2,1);//LED2灭
				WriteLed(3,0);//LED3亮
				try{
	        		Thread.sleep(200);//延时200ms
				} catch (Exception e){
					e.printStackTrace();
				}
				WriteLed(3,1);//LED3灭
				WriteLed(4,0);//LED4亮
				try{
	        		Thread.sleep(200);//延时200ms
				} catch (Exception e){
					e.printStackTrace();
				}
				WriteLed(4,1);//LED4灭
			}
		}
    	
    	//线程暂停
		public void setSuspendFlag() {
			this.suspendFlag = true;
		}
		
		//唤醒线程
		public synchronized void setResume() {
			this.suspendFlag = false;
			notify();
		}
    }
    //--------------------------------------------------------------------
    //LED(n)循环亮灭线程
    class LednThread extends Thread{
    	public boolean suspendFlag = true;// 控制线程的执行
    	private int ledn=1;
    	@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				synchronized (this)
				{
					while (suspendFlag)
					{
						try
						{
							wait();
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				WriteLed(ledn,0);//LED(n)亮
	        	try{
	        		Thread.sleep(200);//延时200ms
				} catch (Exception e){
					e.printStackTrace();
				}
	        	WriteLed(ledn,1);//LED(n)灭
	        	try{
	        		Thread.sleep(200);//延时200ms
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
    	
    	//线程暂停
		public void setSuspendFlag() {
			this.suspendFlag = true;
		}
		
		//唤醒线程
		public synchronized void setResume() {
			this.suspendFlag = false;
			notify();
		}

		public int getLedn()
		{
			return ledn;
		}
		public void setLedn(int ledn)
		{
			this.ledn = ledn;
		}
    }
}
