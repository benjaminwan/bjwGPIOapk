package com.bjw.gpio;

import com.bjw.gpio.R.id;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * @author mywork
 * 单个GPIO口的控制
 * 可以设置GPIO高电平或低电平，可以查询IO口电平；
 * 可以配置IO口作用，可以查询IO配置；
 * 可以配置IO口上拉状态，可以查询IO口上拉状态；
 * 可以打开或关闭串口输出的调试信息，也可以查询当前设置是否输出调试信息；
 */
public class PinActivity extends Activity {
	Button btnRead,btnWrite,btnGetdbg,btnEnabledbg,btnDisabledbg,btnGetcfgpin,btnSetcfgpin,btnGetPull,btnSetPull;
	Button btnBack;
	EditText editGroup,editNum;
	CheckBox cBox_dbg;
	RadioButton radiocfg0,radiocfg1,radiocfg2,radiocfg3,radiocfg4,radiocfg5,radiocfg6,radiocfg7;
	RadioButton radiopull0,radiopull1,radiopull2,radioValue0,radioValue1;
	RadioButton radiocfg8,radiocfg9,radiocfg10,radiocfg11,radiocfg12,radiocfg13,radiocfg14,radiocfg15;
	private RadioButton[] radioGroup;
	//---------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pin_ctrl);
		setTitle("Pin Control");
		btnRead = (Button)findViewById(R.id.btnread);
        btnRead.setOnClickListener(new btnOnClickListener());
        btnWrite = (Button)findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(new btnOnClickListener());
        btnGetdbg=(Button)findViewById(R.id.btnGetdbg);
        btnGetdbg.setOnClickListener(new btnOnClickListener());
        btnEnabledbg=(Button)findViewById(R.id.btnEnabledbg);
        btnEnabledbg.setOnClickListener(new btnOnClickListener());
        btnDisabledbg=(Button)findViewById(R.id.btnDisabledbg);
        btnDisabledbg.setOnClickListener(new btnOnClickListener());
        btnGetcfgpin=(Button)findViewById(R.id.btnGetcfgpin);
        btnGetcfgpin.setOnClickListener(new btnOnClickListener());
        btnSetcfgpin=(Button)findViewById(R.id.btnSetcfgpin);
        btnSetcfgpin.setOnClickListener(new btnOnClickListener());
        btnGetPull=(Button)findViewById(R.id.btnGetPull);
        btnGetPull.setOnClickListener(new btnOnClickListener());
        btnSetPull=(Button)findViewById(R.id.btnSetPull);
        btnSetPull.setOnClickListener(new btnOnClickListener());
        editGroup= (EditText)findViewById(R.id.editGroup);
        editNum=(EditText)findViewById(R.id.editNum);
        cBox_dbg=(CheckBox)findViewById(R.id.checkBox_dbg);
        radiocfg0=(RadioButton)findViewById(R.id.radiocfg0);
        radiocfg1=(RadioButton)findViewById(R.id.radiocfg1);
        radiocfg2=(RadioButton)findViewById(R.id.radiocfg2);
        radiocfg3=(RadioButton)findViewById(R.id.radiocfg3);
        radiocfg4=(RadioButton)findViewById(R.id.radiocfg4);
        radiocfg5=(RadioButton)findViewById(R.id.radiocfg5);
        radiocfg6=(RadioButton)findViewById(R.id.radiocfg6);
        radiocfg7=(RadioButton)findViewById(R.id.radiocfg7);
        
        radiocfg8=(RadioButton)findViewById(R.id.radiocfg8);
        radiocfg9=(RadioButton)findViewById(R.id.radiocfg9);
        radiocfg10=(RadioButton)findViewById(R.id.radiocfg10);
        radiocfg11=(RadioButton)findViewById(R.id.radiocfg11);
        radiocfg12=(RadioButton)findViewById(R.id.radiocfg12);
        radiocfg13=(RadioButton)findViewById(R.id.radiocfg13);
        radiocfg14=(RadioButton)findViewById(R.id.radiocfg14);
        radiocfg15=(RadioButton)findViewById(R.id.radiocfg15);

        radioGroup=new RadioButton[16];
        radioGroup[0]=radiocfg0;
        radioGroup[1]=radiocfg1;
        radioGroup[2]=radiocfg2;
        radioGroup[3]=radiocfg3;
        radioGroup[4]=radiocfg4;
        radioGroup[5]=radiocfg5;
        radioGroup[6]=radiocfg6;
        radioGroup[7]=radiocfg7;
        radioGroup[8]=radiocfg8;
        radioGroup[9]=radiocfg9;
        radioGroup[10]=radiocfg10;
        radioGroup[11]=radiocfg11;
        radioGroup[12]=radiocfg12;
        radioGroup[13]=radiocfg13;
        radioGroup[14]=radiocfg14;
        radioGroup[15]=radiocfg15;
        
        
        radiopull0=(RadioButton)findViewById(R.id.radiopull0);
        radiopull1=(RadioButton)findViewById(R.id.radiopull1);
        radiopull2=(RadioButton)findViewById(R.id.radiopull2);
	    for (RadioButton mButton : radioGroup)
		{
	    	mButton.setOnCheckedChangeListener(new radioonCheckedChanged());
		}
        
        
        btnBack=(Button)findViewById(R.id.btnPinBack);
        btnBack.setOnClickListener(new btnOnClickListener());
        radioValue0=(RadioButton)findViewById(R.id.radioValue0);
        radioValue1=(RadioButton)findViewById(R.id.radioValue1);
        //GPIO端口设置
        if (android.os.Build.BOARD.contains("6410")) {
        	editGroup.setText("GPK");
        	editNum.setText("4");
		} else if (android.os.Build.BOARD.contains("210"))
		{
        	editGroup.setText("GPJ2");
        	editNum.setText("0");
		}
	}
	//---------------------------------------------------------------
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
	//---------------------------------------------------------------
	private void ShowMessage(String sMsg)
	{
			Toast.makeText(PinActivity.this, sMsg, Toast.LENGTH_SHORT).show();
	}
	//---------------------------------------------------------------
	class radioonCheckedChanged implements android.widget.CompoundButton.OnCheckedChangeListener{
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			 if (isChecked) {
			      toggle(buttonView.getId());
			 }
		}
	}
	//---------------------------------------------------------------
	public void toggle(int viewId) {
		for (RadioButton mRadio : radioGroup) {
			if (mRadio.getId() != viewId) {
				mRadio.setChecked(false);
			}
		}
	}
	//---------------------------------------------------------------
	class btnOnClickListener implements View.OnClickListener{
		public void onClick(View v)
		{
			String pin_group;
			int pin_num,ret,pin_val=1;
			if (v == btnRead)
			{
				pin_group= editGroup.getText().toString();
				pin_num = Integer.parseInt(editNum.getText().toString());
				ret=GPIOJNI.ReadGPIO(pin_group,pin_num);
				if (ret==0)
				{
					radioValue0.setChecked(true);
				} else if (ret==1) {
					radioValue1.setChecked(true);
				}
				ShowErrMessage(ret,"ReadGPIO"+pin_group+String.valueOf(pin_num));
			}
			else if (v==btnWrite)
			{
				if (radioValue0.isChecked())
				{
					pin_val=0;
				} else if (radioValue1.isChecked())
				{
					pin_val=1;
				}
				pin_group= editGroup.getText().toString();
				pin_num = Integer.parseInt(editNum.getText().toString());
				ret=GPIOJNI.WriteGPIO(pin_group,pin_num,pin_val); 
				ShowErrMessage(ret,"WriteGPIO"+pin_group+String.valueOf(pin_num));
			}
			else if (v==btnGetdbg)
			{
				ret=GPIOJNI.GetDbg();
				ShowErrMessage(ret,"GetDbg");
				switch (ret)
				{
				case 0:
					cBox_dbg.setChecked(false);
					break;
				case 1:
					cBox_dbg.setChecked(true);
					break;
				default:
					break;
				}
			}
			else if (v==btnEnabledbg)
			{
				ret=GPIOJNI.EnableDbg();
				ShowErrMessage(ret,"EnableDbg");
			}
			else if (v==btnDisabledbg)
			{
				ret=GPIOJNI.DisableDbg();
				ShowErrMessage(ret,"DisableDbg");
			}
			else if (v==btnGetcfgpin)
			{
				pin_group= editGroup.getText().toString();
				pin_num = Integer.parseInt(editNum.getText().toString());
				ret=GPIOJNI.GetCfgpin(pin_group,pin_num); 
				ShowErrMessage(ret,"GetCfgpin"+pin_group+String.valueOf(pin_num));
				radioGroup[ret].setChecked(true);
			}
			else if (v==btnSetcfgpin)
			{
				int pin_cfg = 0;
				for (int i = 0; i < radioGroup.length; i++)
				{
					if (radioGroup[i].isChecked())
					{
						pin_cfg=i;
						break;
					}
				}
				pin_group= editGroup.getText().toString();
				pin_num = Integer.parseInt(editNum.getText().toString());
				ret=GPIOJNI.SetCfgpin(pin_group,pin_num,pin_cfg); 
				ShowErrMessage(ret,"SetCfgpin"+pin_group+String.valueOf(pin_num));
			}
			else if (v== btnGetPull)
			{
				pin_group= editGroup.getText().toString();
				pin_num = Integer.parseInt(editNum.getText().toString());
				ret=GPIOJNI.GetPull(pin_group,pin_num);
				ShowErrMessage(ret,"GetPull"+pin_group+String.valueOf(pin_num));
				switch (ret)
				{
				case 0:
					radiopull0.setChecked(true);
					break;
				case 1:
					radiopull1.setChecked(true);
					break;
				case 2:
					radiopull2.setChecked(true);
					break;
				default:
					break;
				}
			}
			else if (v==btnSetPull)
			{
				int pin_pull = 0;
				if (radiopull0.isChecked())
				{
					pin_pull=0;
				} else if (radiopull1.isChecked())
				{
					pin_pull=1;
				}else if (radiopull2.isChecked())
				{
					pin_pull=2;
				}
				
				pin_group= editGroup.getText().toString();
				pin_num = Integer.parseInt(editNum.getText().toString());
				ret=GPIOJNI.SetPull(pin_group,pin_num,pin_pull); 
				ShowErrMessage(ret,"SetPull"+pin_group+String.valueOf(pin_num));
			}
			else if (v==btnBack)
			{
				PinActivity.this.finish();
			}
		}
    }
	//---------------------------------------------------------------
}