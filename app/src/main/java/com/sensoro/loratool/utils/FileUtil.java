package com.sensoro.loratool.utils;

import android.os.Environment;
import android.util.Log;

import com.sensoro.station.communication.bean.StationInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by sensoro on 17/8/21.
 */

public class FileUtil {
    String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/iottool";
    //保存数据
    public void save(StationInfo stationInfo){

        ObjectOutputStream fos=null;
        try {
            //如果文件不存在就创建文件
            File file=new File(path);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            //获取输出流
            //这里如果文件不存在会创建文件，这是写文件和读文件不同的地方
            fos=new ObjectOutputStream(new FileOutputStream(file));
            //这里不能再用普通的write的方法了
            //要使用writeObject
            fos.writeObject(stationInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (fos!=null) {
                    fos.close();
                }
            } catch (IOException e) {
            }

        }
    }

    public void read(){
        ObjectInputStream ois=null;
        try {
            Log.e("TAG", new File(path).getAbsolutePath()+"<---");
            //获取输入流
            ois=new ObjectInputStream(new FileInputStream(new File(path)));
            //获取文件中的数据
            Object people=ois.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (ois!=null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
