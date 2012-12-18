package com.bjw.gpio;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * @author mywork
 * 如示波器一样显示GPIO高低电平状态，用来学习surfaceview的使用；
 * 以轮询的方式查询，只是做来玩的。
 * 按menu键可以弹出设置菜单
 * 可以设置成其它GPIO口，比如210开发板GPJ0(4~7)是负责LCD控制的，可以看到高低电平变化。
 * 4个开关按钮用来控制设置好的4个IO口
 */
public class OscilloscopeActivity extends Activity implements Callback {
	//菜单和对话框
	private static final int ITEM0 = Menu.FIRST;
	private static final int DIALOG1 = 1;
	
	int screenWidth,screenHeight;//surfaceview宽度和高度
	Button btnOscBack;
	SurfaceView surfaceViewOsc;
	SurfaceHolder sfHolder;
	CheckBox cBoxON;
	Canvas canvas;
	ToggleButton tBtnLed1,tBtnLed2,tBtnLed3,tBtnLed4;
	
	float Y1Max,Y1Min,Y2Max,Y2Min,Y3Max,Y3Min,Y4Max,Y4Min;//最大值和最小值y坐标
	float centerXStart[],centerXStop[],centerYStart[],centerYStop[];//中心线坐标
	int lastX;
	float lastY1,lastY2,lastY3,lastY4;//存储上一次的坐标值
	float y1=0,y2=0,y3=0,y4=0;//当前y坐标值
	Paint paintCenter;// 中心线画笔
	Paint paintLine;// 4条曲线画笔
	
	String pin_group1,pin_group2,pin_group3,pin_group4;
	int pin_num1,pin_num2,pin_num3,pin_num4;
	
	DrawThread mDrawThread;
	GPIOThread mGPIOThread;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.osc);
        setTitle("Oscilloscope");
        cBoxON=(CheckBox)findViewById(R.id.cBoxON);
        cBoxON.setOnClickListener(new btnClickListener());
        btnOscBack=(Button)findViewById(R.id.btnOscBack);   
        btnOscBack.setOnClickListener(new btnClickListener());
        tBtnLed1=(ToggleButton)findViewById(R.id.tBtnLed1);
        tBtnLed2=(ToggleButton)findViewById(R.id.tBtnLed2);
        tBtnLed3=(ToggleButton)findViewById(R.id.tBtnLed3);
        tBtnLed4=(ToggleButton)findViewById(R.id.tBtnLed4);
        tBtnLed1.setOnClickListener(new ToggleBtnOnClickListener());
        tBtnLed2.setOnClickListener(new ToggleBtnOnClickListener());
        tBtnLed3.setOnClickListener(new ToggleBtnOnClickListener());
        tBtnLed4.setOnClickListener(new ToggleBtnOnClickListener());
        surfaceViewOsc=(SurfaceView)findViewById(R.id.surfaceViewOsc);
        sfHolder=surfaceViewOsc.getHolder();
        sfHolder.addCallback(this);
    	//中心线画笔初始化
        paintCenter=new Paint();
        paintCenter.setColor(Color.CYAN);
//        paintCenter.setAntiAlias(true);
//        paintCenter.setStyle(Paint.Style.STROKE);
        PathEffect effects = new DashPathEffect(new float[]{5,2,5,2},1);  
        paintCenter.setPathEffect(effects);
        
        //曲线画笔初始化
        paintLine=new Paint();
        paintLine.setColor(Color.GREEN);
        paintLine.setStrokeWidth(1);
        //GPIO端口设置
        if (android.os.Build.BOARD.contains("6410")) {
        	pin_group1="GPK";
        	pin_group2="GPK";
        	pin_group3="GPK";
        	pin_group4="GPK";
        	pin_num1=4;
        	pin_num2=5;
        	pin_num3=6;
        	pin_num4=7;
		} else if (android.os.Build.BOARD.contains("210"))
		{
        	pin_group1="GPJ2";
        	pin_group2="GPJ2";
        	pin_group3="GPJ2";
        	pin_group4="GPJ2";
        	pin_num1=0;
        	pin_num2=1;
        	pin_num3=2;
        	pin_num4=3;
		}
        mGPIOThread=new GPIOThread();
        mGPIOThread.setSuspendFlag();
        mGPIOThread.start();
    }
    //--------------------------------------------------------------------
    //按钮事件
    class btnClickListener implements View.OnClickListener{
		public void onClick(View v)
		{
			if (v==cBoxON){
				if (cBoxON.isChecked())
				{
					mDrawThread.setResume();
					mGPIOThread.setResume();
				} else
				{
					mDrawThread.setSuspendFlag();
					mGPIOThread.setSuspendFlag();
				}
			}
			else if (v==btnOscBack) {
				mDrawThread.setSuspendFlag();
				mGPIOThread.setSuspendFlag();
				OscilloscopeActivity.this.finish();
			}
		}
    }
    
    //--------------------------------------------------------------------
    //surface初始化
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
	{	
	}

	public void surfaceCreated(SurfaceHolder arg0)
	{
		//初始化数据
		screenWidth=surfaceViewOsc.getWidth();
	    screenHeight=surfaceViewOsc.getHeight();
	    setTitle(getTitle().toString()+" Width:"+screenWidth+" Height:"+screenHeight);
	    centerXStart=new float[4];
	    centerXStop=new float[4];
	    centerYStart=new float[4];
	    centerYStop=new float[4];
	    
		centerXStart[0]=0;
		centerXStart[1]=0;
		centerXStart[2]=0;
		centerXStart[3]=0;
		centerXStop[0]=screenWidth;
		centerXStop[1]=screenWidth;
		centerXStop[2]=screenWidth;
		centerXStop[3]=screenWidth;
		
		centerYStart[0]=screenHeight/5;
		centerYStart[1]=screenHeight/5*2;
		centerYStart[2]=screenHeight/5*3;
		centerYStart[3]=screenHeight/5*4;
		centerYStop[0]=screenHeight/5;
		centerYStop[1]=screenHeight/5*2;
		centerYStop[2]=screenHeight/5*3;
		centerYStop[3]=screenHeight/5*4;
		Y1Max=screenHeight/15*2;
		Y1Min=screenHeight/15*4;
		Y2Max=screenHeight/15*5;
		Y2Min=screenHeight/15*7;
		Y3Max=screenHeight/15*8;
		Y3Min=screenHeight/15*10;
		Y4Max=screenHeight/15*11;
		Y4Min=screenHeight/15*13;
		mDrawThread=new DrawThread();
        mDrawThread.setSuspendFlag();
        mDrawThread.start();
        y1=Y1Max;
        y2=Y2Max;
        y3=Y3Max;
        y4=Y4Max;
        DrawCenterLine();
	}

	public void surfaceDestroyed(SurfaceHolder arg0)
	{
	}
	//--------------------------------------------------------------------
	//清屏并画中心线
	public void DrawCenterLine()
	{
		canvas = sfHolder.lockCanvas(new Rect(0, 0, screenWidth,screenHeight));
		canvas.drawColor(Color.BLACK);// 清除画布
		canvas.drawLine(centerXStart[0], centerYStart[0], centerXStop[0], centerYStop[0], paintCenter);
		canvas.drawLine(centerXStart[1], centerYStart[1], centerXStop[1], centerYStop[1], paintCenter);
		canvas.drawLine(centerXStart[2], centerYStart[2], centerXStop[2], centerYStop[2], paintCenter);
		canvas.drawLine(centerXStart[3], centerYStart[3], centerXStop[3], centerYStop[3], paintCenter);
		sfHolder.unlockCanvasAndPost(canvas);
	}
	//--------------------------------------------------------------------
	//画线
	public void Draw4Line(int pos)
	{
		canvas = sfHolder.lockCanvas(new Rect(0, 0,screenWidth,screenHeight));

		if (pos!=0 && y1!=lastY1)
		{
			canvas.drawLine(lastX, lastY1, pos, y1, paintLine);
		}
		if (pos!=0 && y2!=lastY2)
		{
			canvas.drawLine(lastX, lastY2, pos, y2, paintLine);
		}
		if (pos!=0 && y3!=lastY3)
		{
			canvas.drawLine(lastX, lastY3, pos, y3, paintLine);
		}
		if (pos!=0 && y4!=lastY4)
		{
			canvas.drawLine(lastX, lastY4, pos, y4, paintLine);
		}
		canvas.drawPoint(pos, y1, paintLine);
		canvas.drawPoint(pos, y2, paintLine);
		canvas.drawPoint(pos, y3, paintLine);
		canvas.drawPoint(pos, y4, paintLine);
		lastX=pos;
		lastY1=y1;
		lastY2=y2;
		lastY3=y3;
		lastY4=y4;
		sfHolder.unlockCanvasAndPost(canvas);
	}
	//--------------------------------------------------------------------
    //读取GPIO线程
    class GPIOThread extends Thread{
    	public boolean suspendFlag = true;// 控制线程的执行
		Canvas canvas;
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
				int ret;
				ret=GPIOJNI.ReadGPIO(pin_group1, pin_num1);
				if (ret==0)
				{
					y1=Y1Min;
				}else if (ret==1) {
					y1=Y1Max;
				}else {
					mDrawThread.setSuspendFlag();
				}
				ret=GPIOJNI.ReadGPIO(pin_group2, pin_num2);
				if (ret==0)
				{
					y2=Y2Min;
				}else if (ret==1) {
					y2=Y2Max;
				}else {
					mDrawThread.setSuspendFlag();
				}
				ret=GPIOJNI.ReadGPIO(pin_group3, pin_num3);
				if (ret==0)
				{
					y3=Y3Min;
				}else if (ret==1) {
					y3=Y3Max;
				}else {
					mDrawThread.setSuspendFlag();
				}
				ret=GPIOJNI.ReadGPIO(pin_group4, pin_num4);
				if (ret==0)
				{
					y4=Y4Min;
				}else if (ret==1) {
					y4=Y4Max;
				}else {
					mDrawThread.setSuspendFlag();
				}
				
				try
				{
					sleep(5);
				} catch (InterruptedException e)
				{
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
    }
    //--------------------------------------------------------------------
    //绘图线程
    class DrawThread extends Thread{
    	public boolean suspendFlag = true;// 控制线程的执行
		Canvas canvas;
    	int x=0;
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
				if (x==0)
				{
					DrawCenterLine();
				}
				Draw4Line(x);
				x++;
				if (x > screenWidth)
				{
					x=0;
					DrawCenterLine();
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
    }
    //--------------------------------------------------------------------
    //设置菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, ITEM0, 0, "GPIO口设置");
        return true;
    }
    //------------------------------------------
    //菜单响应事件
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
 		{
 		case ITEM0:
 			showDialog(DIALOG1);
 			break;
 			
 		default:
 			break;
 		}
     	
    	return super.onOptionsItemSelected(item);
    }
    //------------------------------------------
    //创建对话框
  	@Override
  	protected Dialog onCreateDialog(int id) {
  		switch (id)
  		{
  		case DIALOG1:
  			return buildDialog(OscilloscopeActivity.this);
  		}
  		return null;
  	}
  	//------------------------------------------
  	//接收刷新设置对话框
    private Dialog buildDialog(Context context) {
		LayoutInflater inflater = LayoutInflater.from(this);
		final View textEntryView = inflater.inflate(
				R.layout.alert_dialog_text_entry, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("请设置GPIO 1~4");
		builder.setView(textEntryView);
		final EditText editGPIOGroup1 = (EditText)textEntryView.findViewById(R.id.editTextIO_Group1);
		final EditText editGPIOGroup2 = (EditText)textEntryView.findViewById(R.id.editTextIO_Group2);
		final EditText editGPIOGroup3 = (EditText)textEntryView.findViewById(R.id.editTextIO_Group3);
		final EditText editGPIOGroup4 = (EditText)textEntryView.findViewById(R.id.editTextIO_Group4);
		final EditText editGPIONum1=(EditText)textEntryView.findViewById(R.id.editTextIO_Num1);
		final EditText editGPIONum2=(EditText)textEntryView.findViewById(R.id.editTextIO_Num2);
		final EditText editGPIONum3=(EditText)textEntryView.findViewById(R.id.editTextIO_Num3);
		final EditText editGPIONum4=(EditText)textEntryView.findViewById(R.id.editTextIO_Num4);
		if (android.os.Build.BOARD.contains("6410")) {
			editGPIOGroup1.setText("GPK");
			editGPIOGroup2.setText("GPK");
			editGPIOGroup3.setText("GPK");
			editGPIOGroup4.setText("GPK");
			editGPIONum1.setText("4");
			editGPIONum2.setText("5");
			editGPIONum3.setText("6");
			editGPIONum4.setText("7");
		} else if (android.os.Build.BOARD.contains("210"))
		{
			editGPIOGroup1.setText("GPJ2");
			editGPIOGroup2.setText("GPJ2");
			editGPIOGroup3.setText("GPJ2");
			editGPIOGroup4.setText("GPJ2");
			editGPIONum1.setText("0");
			editGPIONum2.setText("1");
			editGPIONum3.setText("2");
			editGPIONum4.setText("3");
		}
		builder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						pin_group1=editGPIOGroup1.getText().toString();
						pin_group2=editGPIOGroup2.getText().toString();
						pin_group3=editGPIOGroup3.getText().toString();
						pin_group4=editGPIOGroup4.getText().toString();
						pin_num1=Integer.parseInt(editGPIONum1.getText().toString());
						pin_num2=Integer.parseInt(editGPIONum2.getText().toString());
						pin_num3=Integer.parseInt(editGPIONum3.getText().toString());
						pin_num4=Integer.parseInt(editGPIONum4.getText().toString());
					}
				});
		builder.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		return builder.create();
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
    //设置LED状态
    private int WriteLed(int led,int pin_val)
	{
    	int ret=-1;
    	String pin_group;
    	int pin_num;
    	switch (led)
		{
		case 1:
			pin_group= pin_group1;
			pin_num = pin_num1;
			break;
		case 2:
			pin_group= pin_group2;
			pin_num = pin_num2;
			break;
		case 3:
			pin_group= pin_group3;
			pin_num = pin_num3;
			break;
		case 4:
			pin_group= pin_group4;
			pin_num = pin_num4;
			break;
		default:
			return ret;
		}
    	ret=GPIOJNI.WriteGPIO(pin_group, pin_num, pin_val);
		return ret;
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
			Toast.makeText(OscilloscopeActivity.this, sMsg, Toast.LENGTH_SHORT).show();
	}
}
