package com.sensoro.loratool.store;

import android.content.ContentValues;
import android.database.Cursor;

import com.sensoro.loratool.model.DeviceData;
import com.sensoro.loratool.model.UpgradeData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sensoro on 16/9/5.
 */

public class DeviceDataDao {

    public static List<UpgradeData> getUpgradeDataList() {
        List<UpgradeData> list = new ArrayList<>();
        Cursor cursor = LoraDbHelper.instance.getWritableDatabase().query(LoraDbHelper.TABLE_UPGRADE_INFO, null
                , null, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                UpgradeData upgradeData = new UpgradeData();
                upgradeData.setId(cursor.getInt(cursor.getColumnIndex(LoraDbHelper.COLUMN_ID)));
                upgradeData.setFirmwareVersion(cursor.getString(cursor.getColumnIndex(LoraDbHelper.COLUMN_DEVICE_FIRMWARE_VERSION)));
                upgradeData.setData(cursor.getString(cursor.getColumnIndex(LoraDbHelper.COLUMN_DEVICE_UPGRADE_DATA)));
                list.add(upgradeData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return list;
    }

    public static List<DeviceData> getDeviceDataList() {
        List<DeviceData> list = new ArrayList<>();
        Cursor cursor = LoraDbHelper.instance.getWritableDatabase().query(LoraDbHelper.TABLE_DEVICE, null
                , null, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                DeviceData deviceData = new DeviceData();
                deviceData.setId(cursor.getInt(cursor.getColumnIndex(LoraDbHelper.COLUMN_DEVICE_ID)));
                deviceData.setSn(cursor.getString(cursor.getColumnIndex(LoraDbHelper.COLUMN_DEVICE_SN)));
                deviceData.setVersion(cursor.getString(cursor.getColumnIndex(LoraDbHelper.COLUMN_DEVICE_VERSION)));
                deviceData.setData(cursor.getString(cursor.getColumnIndex(LoraDbHelper.COLUMN_DEVICE_CONTENT)));
                list.add(deviceData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return list;
    }



    public static void addDeviceItem(String sn, String data,String version) {
        ContentValues values = new ContentValues();
        values.put(LoraDbHelper.COLUMN_DEVICE_SN, sn);
        values.put(LoraDbHelper.COLUMN_DEVICE_VERSION, version);
        values.put(LoraDbHelper.COLUMN_DEVICE_CONTENT, data);
        LoraDbHelper.instance.getWritableDatabase().insert(LoraDbHelper.TABLE_DEVICE, null, values);
    }

    public static void addUpgradeInfoItem(String firmwareVersion, String data) {
        ContentValues values = new ContentValues();
        values.put(LoraDbHelper.COLUMN_DEVICE_FIRMWARE_VERSION, firmwareVersion);
        values.put(LoraDbHelper.COLUMN_DEVICE_UPGRADE_DATA, data);
        LoraDbHelper.instance.getWritableDatabase().insert(LoraDbHelper.TABLE_UPGRADE_INFO, null, values);
    }


    public static void addDeviceWithNotExistInServer(String sn) {
        if (!isExistOutItem(sn)) {
            ContentValues values = new ContentValues();
            values.put(LoraDbHelper.COLUMN_DEVICE_SN, sn);
            System.out.println("db insert sn ===>" + sn);
            LoraDbHelper.instance.getWritableDatabase().insert(LoraDbHelper.TABLE_DEVICE_OUT, null, values);
        } else {
            System.out.println("db have sn ===>" + sn);
        }

    }

    public static void clearWithNotExistInServer() {
        String  DELETE_DATA = "DELETE FROM device_out ";
        LoraDbHelper.instance.getWritableDatabase().execSQL(DELETE_DATA);
    }

    public static HashMap<String, String> getSnMapWithNotExistInServer() {
        HashMap<String, String> map = new HashMap<>();
        Cursor cursor = LoraDbHelper.instance.getWritableDatabase().query(LoraDbHelper.TABLE_DEVICE_OUT, null
                , null, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                String sn = cursor.getString(cursor.getColumnIndex(LoraDbHelper.COLUMN_DEVICE_SN));
                map.put(sn, sn);
            }
        } catch (Exception e) {

        } finally {
            cursor.close();
        }
        return map;
    }

    public static boolean isExistOutItem(String sn) {
        boolean isExist = false;
        Cursor cursor = LoraDbHelper.instance.getWritableDatabase().query(LoraDbHelper.TABLE_DEVICE_OUT, null
                , LoraDbHelper.COLUMN_DEVICE_SN + "=? ", new String[]{sn}, null, null, null);
        try {
            while (cursor.moveToNext()) {
                isExist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return isExist;
    }

    public static int getDeviceId(String sn, String data) {
        int device_id = 0;
        Cursor cursor = LoraDbHelper.instance.getWritableDatabase().query(LoraDbHelper.TABLE_DEVICE, null
                , LoraDbHelper.COLUMN_DEVICE_SN + "=? AND " + LoraDbHelper.COLUMN_DEVICE_CONTENT + "=?", new String[]{sn, data}, null, null, null);
        try {
            if (cursor.moveToNext()) {
                device_id = cursor.getInt(cursor.getColumnIndex(LoraDbHelper.COLUMN_DEVICE_ID));
            }
        } catch (Exception e) {

        } finally {
            cursor.close();
        }
        return device_id;
    }

    public static void removeDeviceItem(DeviceData deviceData) {
        try {
            String where = LoraDbHelper.COLUMN_DEVICE_ID + " = ?";
            String[] whereValue = { Integer.toString(deviceData.getId()) };
            LoraDbHelper.instance.getWritableDatabase().delete(LoraDbHelper.TABLE_DEVICE, where, whereValue);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeUpgradeInfoItem(UpgradeData upgradeData) {
        try {
            String where = LoraDbHelper.COLUMN_ID + " = ?";
            String[] whereValue = { Integer.toString(upgradeData.getId()) };
            LoraDbHelper.instance.getWritableDatabase().delete(LoraDbHelper.TABLE_UPGRADE_INFO, where, whereValue);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
