package com.bjw.gpio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author mywork
 * 列出所有GPIO口及其配置状态
 * 可以以CSV格式导出所有信息到存储卡
 */
public class ListActivity extends Activity {
	private static final int DIALOGSAVETF = 1;
	Button btnBack,btnGetList,btnExportSD;
	RadioButton radioS3C6410,radioS5PV210;
	CheckBox cBoxAuto;
	ArrayList<ListBean> GpioList = new ArrayList<ListBean>();
	LayoutInflater mInflater;
	listAdapter mAdapter;
	AutoRefresh mAutoRefresh;
	static class gpio_unit{
    	String gpio_group;
    	int gpio_nr;
    	public gpio_unit(String gpio_group,int gpio_nr){
    		this.gpio_group=gpio_group;
    		this.gpio_nr=gpio_nr;
    	}
    }
    static gpio_unit[] s3c6410_gpio_units={
    	new gpio_unit("GPA",8),new gpio_unit("GPB",7),new gpio_unit("GPC",8),
    	new gpio_unit("GPD",5),new gpio_unit("GPE",5),new gpio_unit("GPF",16),
    	new gpio_unit("GPG",7),new gpio_unit("GPH",10),new gpio_unit("GPI",16),
    	new gpio_unit("GPJ",12),new gpio_unit("GPK",16),new gpio_unit("GPL",15),
    	new gpio_unit("GPM",6),new gpio_unit("GPN",16),new gpio_unit("GPO",16),
    	new gpio_unit("GPP",15),new gpio_unit("GPQ",9),
    };
    static gpio_unit[] s5pv210_gpio_unitsUnits={
    	new gpio_unit("GPA0",8),new gpio_unit("GPA1",4),new gpio_unit("GPB",8),
    	new gpio_unit("GPC0",5),new gpio_unit("GPC1",5),new gpio_unit("GPD0",4),
    	new gpio_unit("GPD1",6),new gpio_unit("GPE0",8),new gpio_unit("GPE1",5),
    	new gpio_unit("GPF0",8),new gpio_unit("GPF1",8),new gpio_unit("GPF2",8),
    	new gpio_unit("GPF3",6),new gpio_unit("GPG0",7),new gpio_unit("GPG1",7),
    	new gpio_unit("GPG2",7),new gpio_unit("GPG3",7),new gpio_unit("GPH0",8),
    	new gpio_unit("GPH1",8),new gpio_unit("GPH2",8),new gpio_unit("GPH3",8),
    	new gpio_unit("GPI",7),new gpio_unit("GPJ0",8),new gpio_unit("GPJ1",6),
    	new gpio_unit("GPJ2",8),new gpio_unit("GPJ3",8),new gpio_unit("GPJ4",5),
    	new gpio_unit("MP01",8),new gpio_unit("MP02",4),new gpio_unit("MP03",8),
    	/* Practically, GPIO banks up to MP03 are the configurable gpio banks */
    	/*
    	new gpio_unit("MP04",8),new gpio_unit("MP05",8),new gpio_unit("MP06",8),
    	new gpio_unit("MP07",8),new gpio_unit("MP10",8),new gpio_unit("MP11",8),
    	new gpio_unit("MP12",8),new gpio_unit("MP13",8),new gpio_unit("MP14",8),
    	new gpio_unit("MP15",8),new gpio_unit("MP16",8),new gpio_unit("MP17",8),
    	new gpio_unit("MP18",7),new gpio_unit("MP20",8),new gpio_unit("MP21",8),
    	new gpio_unit("MP22",8),new gpio_unit("MP23",8),new gpio_unit("MP24",8),
    	new gpio_unit("MP25",8),new gpio_unit("MP26",8),new gpio_unit("MP27",8),
    	new gpio_unit("MP28",7),new gpio_unit("ETC0",6),new gpio_unit("ETC1",8),
    	new gpio_unit("ETC2",8),new gpio_unit("ETC4",6),*/
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpio_list);
        setTitle("GPIO List");
        btnBack=(Button)findViewById(R.id.btnListBack);
        btnBack.setOnClickListener(new btnClickListener());
        btnGetList=(Button)findViewById(R.id.btnGetList);
        btnGetList.setOnClickListener(new btnClickListener());
        btnExportSD=(Button)findViewById(R.id.btnExportSD);
        btnExportSD.setOnClickListener(new btnClickListener());
        cBoxAuto=(CheckBox)findViewById(R.id.cBoxAuto);
        cBoxAuto.setOnCheckedChangeListener(new checkboxClickListener());
        radioS3C6410=(RadioButton)findViewById(R.id.radioS3C6410);
        radioS5PV210=(RadioButton)findViewById(R.id.radioS5PV210);
        GridView gpioView=(GridView)findViewById(R.id.gridViewGPIO);
        mInflater = getLayoutInflater();
        mAdapter= new listAdapter();
        gpioView.setAdapter(mAdapter);
        mAutoRefresh=new AutoRefresh();
        mAutoRefresh.setSuspendFlag();
        mAutoRefresh.start();
        if (android.os.Build.BOARD.contains("6410")) {
        	radioS3C6410.setChecked(true);
		} else if (android.os.Build.BOARD.contains("210"))
		{
			radioS5PV210.setChecked(true);
		}
    }
    //---------------------------------------------------------------
    //按钮事件
    class btnClickListener implements View.OnClickListener{
		public void onClick(View v)
		{
			if (v==btnGetList)
			{
				RefreshList();
			}else if (v==btnExportSD) {
				String status=Environment.getExternalStorageState();
				if(!status.equals(Environment.MEDIA_MOUNTED))
				{
					ShowMessage("请插入TF卡");
					return;
				}
				showDialog(DIALOGSAVETF);
			}else if (v==btnBack) {
				ListActivity.this.finish();
			}
		}
    }
    //---------------------------------------------------------------
    private void ShowMessage(String sMsg)
	{
			Toast.makeText(ListActivity.this, sMsg, Toast.LENGTH_SHORT).show();
	}
    //---------------------------------------------------------------
    //获取所有引脚信息
    private ArrayList<ListBean> GetGpioList()
	{
    	ArrayList<ListBean> mList = new ArrayList<ListBean>();
    	if (radioS3C6410.isChecked())
		{
			for (gpio_unit mUnit : s3c6410_gpio_units)
			{
				for (int i = 0; i < mUnit.gpio_nr; i++)
				{
					mList.add(GetPinInfo(mUnit.gpio_group,i));
				}
			}
		}else if (radioS5PV210.isChecked()) {
			for (gpio_unit mUnit : s5pv210_gpio_unitsUnits)
			{
				for (int i = 0; i < mUnit.gpio_nr; i++)
				{
					mList.add(GetPinInfo(mUnit.gpio_group,i));
				}
			}
		}
		return mList;
	}
    //---------------------------------------------------------------
    //获取指定引脚信息
    private ListBean GetPinInfo(String gpio_group,int pin_num)
	{
    	ListBean listBean=new ListBean();
    	listBean.gpio=gpio_group+"("+ String.valueOf(pin_num)+")";//Pin name
		
		int ret = GPIOJNI.ReadGPIO(gpio_group, pin_num);//get pin value
		listBean.value=String.valueOf(ret);
		
		ret=GPIOJNI.GetCfgpin(gpio_group, pin_num);//get pincfg
		listBean.cfgpin=String.valueOf(ret);
		
		ret=GPIOJNI.GetPull(gpio_group,pin_num);//get pinpull
		switch (ret)
		{
		case 0:
			listBean.pull="NONE";
			break;
		case 1:
			listBean.pull="DOWN";
			break;
		case 2:
			listBean.pull="UP";
			break;
		default:
			listBean.pull=String.valueOf(ret);
			break;
		}
		
    	return listBean;
	}
    //---------------------------------------------------------------
    //刷新列表
    private void RefreshList(){
    	GpioList=GetGpioList();
		mAdapter.notifyDataSetChanged();
	}
    //---------------------------------------------------------------
    static class ListBean {
    	public String gpio;
    	public String value;
    	public String cfgpin;
    	public String pull;
    }
    //---------------------------------------------------------------
    static class ViewHolder{
		TextView txtGPIO;
		TextView txtValue;
		TextView txtCfgpin;
		TextView txtPull;
	}
    //---------------------------------------------------------------
    class listAdapter extends BaseAdapter{
		public int getCount()
		{
			return GpioList.size();
		}

		public Object getItem(int position)
		{
			return null;
		}

		public long getItemId(int position)
		{
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_item, null);
				holder.txtGPIO = (TextView) convertView.findViewById(R.id.txtGPIO);
				holder.txtValue = (TextView) convertView.findViewById(R.id.txtValue);
				holder.txtCfgpin = (TextView) convertView.findViewById(R.id.txtCfgpin);
				holder.txtPull = (TextView) convertView.findViewById(R.id.txtPull);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			ListBean listBean = GpioList.get(position);
			holder.txtGPIO.setText(listBean.gpio);
			holder.txtValue.setText(listBean.value);
			holder.txtCfgpin.setText(listBean.cfgpin);
			holder.txtPull.setText(listBean.pull);
			if (listBean.value!=null)
			{
				if (listBean.value.equals("-1")||listBean.value.equals("-2"))//发生错误的行，用红色标示
				{
					holder.txtGPIO.setTextColor(Color.RED);
					holder.txtValue.setTextColor(Color.RED);
					holder.txtCfgpin.setTextColor(Color.RED);
					holder.txtPull.setTextColor(Color.RED);
				}else if (listBean.value.equals("0")) {//引脚低电平的行，用绿色标示
					holder.txtGPIO.setTextColor(Color.GREEN);
					holder.txtValue.setTextColor(Color.GREEN);
					holder.txtCfgpin.setTextColor(Color.GREEN);
					holder.txtPull.setTextColor(Color.GREEN);
				}else if (listBean.value.equals("1")) {//引脚高电平的行，用蓝色标示
					holder.txtGPIO.setTextColor(Color.BLUE);
					holder.txtValue.setTextColor(Color.BLUE);
					holder.txtCfgpin.setTextColor(Color.BLUE);
					holder.txtPull.setTextColor(Color.BLUE);
				}
			}
			return convertView;
		}
    }
    //---------------------------------------------------------------
    //对话框
    @Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOGSAVETF:
			return buildDialogSaveSD(ListActivity.this);
		}
		return null;
	}
    //---------------------------------------------------------------
    //保存文件对话框
    private Dialog buildDialogSaveSD(Context context) {
		LayoutInflater inflater = LayoutInflater.from(this);
		final View textEntryView = inflater.inflate(
				R.layout.input_savefilename, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.alert_dialog_icon);
		builder.setTitle("请输入要保存的文件名(文件保存至TF或SD卡根目录)");
		builder.setView(textEntryView);
		final EditText fileName = (EditText)textEntryView.findViewById(R.id.filename_edit);
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");      
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		fileName.setText(formatter.format(curDate)+".csv");
		builder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
				        SaveFile("/mnt/sdcard/"+fileName.getText().toString());
					}
				});
		builder.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		return builder.create();
	}
    //---------------------------------------------------------------
    //保存文件过程
    private void SaveFile(String Filename)
	{
		File file = new File(Filename);
		if (file.exists())
		{
			file.delete();
		}
		try
		{
			FileOutputStream fos=new FileOutputStream(file);
			String sHeader;
			sHeader = "GPIO,Value,cfgpin,Pull\r\n";
			fos.write(sHeader.getBytes("GB2312"));
			for (int i = 0; i < GpioList.size(); i++)
			{
				String sLine;
				sLine="\"" +GpioList.get(i).gpio+"\",";
				sLine+="\"" +GpioList.get(i).value+"\",";
				sLine+="\"" +GpioList.get(i).cfgpin+"\",";
				sLine+="\"" +GpioList.get(i).pull+ "\"";
				sLine+="\r\n";
				fos.write(sLine.getBytes("GB2312"));
			}
			fos.close();
	        ShowMessage("共" + GpioList.size() +"条信息保存成功");
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			ShowMessage("保存失败:"+e.getMessage());
		} catch (IOException e1)
		{
			e1.printStackTrace();
			ShowMessage("保存失败:"+e1.getMessage());
		}
	}
    //---------------------------------------------------------------
    //checkbox事件
    class checkboxClickListener implements CheckBox.OnCheckedChangeListener{
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (buttonView==cBoxAuto)
			{
				if (cBoxAuto.isChecked())
				{
					mAutoRefresh.setResume();
				} else
				{
					mAutoRefresh.setSuspendFlag();
				}
			}
		}
    }
    //---------------------------------------------------------------
    //自动刷新线程
    class AutoRefresh extends Thread{
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
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						RefreshList();
					}
				});
	        	try
				{
	        		Thread.sleep(1000);//刷新延时1秒
				} catch (Exception e)
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
}
