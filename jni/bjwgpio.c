#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include "bjwgpio.h"  
#include <string.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <jni.h>
#include <assert.h>

//导入日志头文件
#include <android/log.h>
//修改日志tag中的值
#define LOG_TAG "bjwgpio"
//日志显示
//#define DEBUG 去掉注释重新编译，可显示logcat信息
#ifdef DEBUG
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#else
#define LOGD(...)
#define LOGI(...)
#endif

#define GPIO_FILE "/dev/bjw-gpio"
#define GPIO_GET_DBGCTRL   0x40044700
#define GPIO_SET_DBGCTRL   0x40044701
#define GPIO_GET_CFGPIN   0x40044702
#define GPIO_SET_CFGPIN   0x40044703
#define GPIO_GET_PULL  0x40044704
#define GPIO_SET_PULL  0x40044705

struct gpio_info {
	char* pin_group;
	int pin_num;
};

struct gpio_pin_data {
	char* pin_group;
	int pin_num;
	int pin_data;
};

int fd_gpio;
const int fd_err=-2;

char* jstringTostr(JNIEnv* env, jstring jstr)
{        
    char* pStr = NULL;
    jclass     jstrObj   = (*env)->FindClass(env, "java/lang/String");
    jstring    encode    = (*env)->NewStringUTF(env, "utf-8");
    jmethodID  methodId  = (*env)->GetMethodID(env, jstrObj, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray byteArray = (jbyteArray)(*env)->CallObjectMethod(env, jstr, methodId, encode);
    jsize      strLen    = (*env)->GetArrayLength(env, byteArray);
    jbyte      *jBuf     = (*env)->GetByteArrayElements(env, byteArray, JNI_FALSE);
    if (jBuf > 0)
    {
        pStr = (char*)malloc(strLen + 1);
        if (!pStr)
        {
            return NULL;
        }
        memcpy(pStr, jBuf, strLen);
        pStr[strLen] = 0;
    }
    (*env)->ReleaseByteArrayElements(env, byteArray, jBuf, 0);
    char* pUpperStr=pStr; 
    while(*pUpperStr != '\0') 
    { 
        if(*pUpperStr>=97 && *pUpperStr <=122) 
            *pUpperStr-=32; 
        pUpperStr++; 
    } 
    return pStr;
}

jstring strToJstring(JNIEnv* env, const char* pStr)
{
    int        strLen    = strlen(pStr);
    jclass     jstrObj   = (*env)->FindClass(env, "java/lang/String");
    jmethodID  methodId  = (*env)->GetMethodID(env, jstrObj, "", "([BLjava/lang/String;)V");
    jbyteArray byteArray = (*env)->NewByteArray(env, strLen);
    jstring    encode    = (*env)->NewStringUTF(env, "utf-8");
    (*env)->SetByteArrayRegion(env, byteArray, 0, strLen, (jbyte*)pStr);
    return (jstring)(*env)->NewObject(env, jstrObj, methodId, byteArray, encode);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	LOGI("JNI_OnLoad startup!");
	fd_gpio = open(GPIO_FILE, O_RDWR, S_IRUSR | S_IWUSR);
	LOGD("fd_gpio is %d",fd_gpio);
	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_Unload(JavaVM* vm, void* reserved)
{
	LOGI("JNI_UnLoad startup!");
	close(fd_gpio);
	LOGD("fd_gpio is closed");
}

JNIEXPORT jint JNICALL Java_com_bjw_gpio_GPIOJNI_ReadGPIO
  (JNIEnv *env, jclass thiz, jstring pin_group, jint pin_num)
{
	 char cmd_buf[16];
	 char *pin_char;
	 int val;
	 pin_char=jstringTostr(env,pin_group);
	 sprintf(cmd_buf, "%s %d", pin_char,pin_num);
	 LOGD("fd_gpio is %d ,cmd is %s",fd_gpio,cmd_buf);
	 if(fd_gpio<0)
	 	return fd_err;
	 val = read(fd_gpio, cmd_buf, strlen(cmd_buf));
	 LOGD("ReadGPIO return is %d",val);
	 return val;
}

JNIEXPORT jint JNICALL Java_com_bjw_gpio_GPIOJNI_WriteGPIO
  (JNIEnv *env, jclass thiz, jstring pin_group, jint pin_num, jint pin_val)
{
	 char cmd_buf[10];
	 char *pin_char;
	 int val;
	 pin_char=jstringTostr(env,pin_group);
	 sprintf(cmd_buf, "%s %d %d", pin_char,pin_num,pin_val);
	 LOGD("fd_gpio is %d ,cmd is %s",fd_gpio,cmd_buf);
	 if(fd_gpio<0)
	 	return fd_err;
	 val = write(fd_gpio, cmd_buf, strlen(cmd_buf));
	 LOGD("WriteGPIO return is %d",val);
	 return val;
}

JNIEXPORT jint JNICALL Java_com_bjw_gpio_GPIOJNI_GetDbg
  (JNIEnv *env, jclass thiz)
{
	 int val;
	 LOGD("fd_gpio is %d",fd_gpio);
	 if(fd_gpio<0)
	 	return fd_err;
	 val = ioctl(fd_gpio, GPIO_GET_DBGCTRL, 0);
	 LOGD("GetDbg return is %d",val);
	 return val;
}

JNIEXPORT jint JNICALL Java_com_bjw_gpio_GPIOJNI_EnableDbg
  (JNIEnv *env, jclass thiz)
{
	 int val;
	 LOGD("fd_gpio is %d",fd_gpio);
	 if(fd_gpio<0)
	 	return fd_err;
	 val = ioctl(fd_gpio, GPIO_SET_DBGCTRL, 1);
	 LOGD("EnableDbg return is %d",val);
	 return val;
}

JNIEXPORT jint JNICALL Java_com_bjw_gpio_GPIOJNI_DisableDbg
  (JNIEnv *env, jclass thiz)
{
	 int val;
	 LOGD("fd_gpio is %d",fd_gpio);
	 if(fd_gpio<0)
	 	return fd_err;
	 val = ioctl(fd_gpio, GPIO_SET_DBGCTRL, 0);
	 LOGD("DisableDbg return is %d",val);
	 return val;
}

JNIEXPORT jint JNICALL Java_com_bjw_gpio_GPIOJNI_GetCfgpin
  (JNIEnv *env, jclass thiz, jstring pin_group, jint pin_num)
{
    struct gpio_info pin_info;
    pin_info.pin_group=jstringTostr(env,pin_group);
    pin_info.pin_num=pin_num;
    LOGD("fd_gpio is %d ,pin_group is %s,pin_num is %d",fd_gpio,pin_info.pin_group,pin_info.pin_num);
    if(fd_gpio<0)
	 	return fd_err;
    int val = ioctl(fd_gpio, GPIO_GET_CFGPIN, &pin_info);
	LOGD("GetCfgpin return is %d",val);
	return val;
}

JNIEXPORT jint JNICALL Java_com_bjw_gpio_GPIOJNI_SetCfgpin
  (JNIEnv *env, jclass thiz, jstring pin_group, jint pin_num, jint pin_cfg)
{
    struct gpio_pin_data cfg_data;
    cfg_data.pin_group=jstringTostr(env,pin_group);
    cfg_data.pin_num=pin_num;
    cfg_data.pin_data=pin_cfg;
    LOGD("fd_gpio is %d ,pin_group is %s,pin_num is %d,pin_cfg is %d",fd_gpio,cfg_data.pin_group,cfg_data.pin_num,cfg_data.pin_data);
    if(fd_gpio<0)
	 	return fd_err;
    int val = ioctl(fd_gpio, GPIO_SET_CFGPIN, &cfg_data);
	LOGD("SetCfgpin return is %d",val);
	return val;
}

JNIEXPORT jint JNICALL Java_com_bjw_gpio_GPIOJNI_GetPull
  (JNIEnv *env, jclass thiz, jstring pin_group, jint pin_num)
{
    struct gpio_info pin_info;
    pin_info.pin_group=jstringTostr(env,pin_group);;
    pin_info.pin_num=pin_num;
    LOGD("fd_gpio is %d ,pin_group is %s,pin_num is %d",fd_gpio,pin_info.pin_group,pin_info.pin_num);
    if(fd_gpio<0)
	 	return fd_err;
    int val = ioctl(fd_gpio, GPIO_GET_PULL, &pin_info);
	LOGD("GetPull return is %d",val);
	return val;
}

JNIEXPORT jint JNICALL Java_com_bjw_gpio_GPIOJNI_SetPull
  (JNIEnv *env, jclass thiz, jstring pin_group, jint pin_num, jint pin_pull)
{
    struct gpio_pin_data pull_data;
    pull_data.pin_group=jstringTostr(env,pin_group);
    pull_data.pin_num=pin_num;
    pull_data.pin_data=pin_pull;
    LOGD("fd_gpio is %d ,pin_group is %s,pin_num is %d,pin_cfg is %d",fd_gpio,pull_data.pin_group,pull_data.pin_num,pull_data.pin_data);
    if(fd_gpio<0)
	 	return fd_err;
    int val = ioctl(fd_gpio, GPIO_SET_PULL, &pull_data);
	LOGD("SetPull return is %d",val);
	return val;
}

