package com.bjw.gpio;

/**
 * @author mywork
 * GPIOJNI
 */
public class GPIOJNI {
	static {
		System.loadLibrary("bjwgpio");
		}
	public static String cmdErr=":����ִ�д����Ƿ�GPIO���������������?";
	public static String fdErr=":�豸�򿪴��������Ƿ��ж�дȨ�ޡ��ں���������Ƿ���ȷ!";
	
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