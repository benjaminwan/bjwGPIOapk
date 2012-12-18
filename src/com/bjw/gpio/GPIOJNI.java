package com.bjw.gpio;

/**
 * @author mywork
 * GPIOJNI
 */
public class GPIOJNI {
	static {
		System.loadLibrary("bjwgpio");
		}
	public static String cmdErr=":命令执行错误，是否GPIO引脚名称输入错误?";
	public static String fdErr=":设备打开错误，请检查是否有读写权限、内核相关驱动是否正确!";
	
	public native static int ReadGPIO(String pin_group,int pin_num);
	public native static int WriteGPIO(String pin_group,int pin_num,int pin_val);
	
	public native static int GetDbg();
	public native static int EnableDbg();
	public native static int DisableDbg();
	
	public native static int GetCfgpin(String pin_group,int pin_num);
	public native static int SetCfgpin(String pin_group,int pin_num,int pin_cfg);
	
	public native static int GetPull(String pin_group,int pin_num);
	public native static int SetPull(String pin_group,int pin_num,int pin_pull);
	
}