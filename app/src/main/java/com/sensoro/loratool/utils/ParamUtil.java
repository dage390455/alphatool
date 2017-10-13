package com.sensoro.loratool.utils;

import android.content.Context;

import com.sensoro.loratool.R;
import com.sensoro.loratool.constant.Constants;

import java.util.Arrays;

/**
 * Created by sensoro on 17/5/8.
 */

public class ParamUtil {


    public static int getLoraTxpIndex(String band, int loraTxp) {
        int index = 0;
        switch (band) {
            case Constants.LORA_BAND_EU433:
                index = getValueIndex(Constants.LORA_EU433_TXP, loraTxp);
                break;
            case Constants.LORA_BAND_EU868:
                index = getValueIndex(Constants.LORA_EU868_TXP, loraTxp);
                break;
            case Constants.LORA_BAND_AS923:
                index = getValueIndex(Constants.LORA_AS923_TXP, loraTxp);
                break;
            case Constants.LORA_BAND_AU915:
                index = getValueIndex(Constants.LORA_AU915_TXP, loraTxp);
                break;
            case Constants.LORA_BAND_US915:
                index = getValueIndex(Constants.LORA_US915_TXP, loraTxp);
                break;
            case Constants.LORA_BAND_SE433:
                index = getValueIndex(Constants.LORA_SE433_TXP, loraTxp);
                break;
            case Constants.LORA_BAND_SE470:
                index = getValueIndex(Constants.LORA_SE470_TXP, loraTxp);
                break;
            case Constants.LORA_BAND_SE780:
                index = getValueIndex(Constants.LORA_SE780_TXP, loraTxp);
                break;
            case Constants.LORA_BAND_SE915:
                index = getValueIndex(Constants.LORA_SE915_TXP, loraTxp);
                break;


        }
        if (index == -1) {
            return 0;
        }
        return index;
    }

    public static int getLoraDrIndex(String band, int dr) {
        int index = 0;
        switch (band) {
            case Constants.LORA_BAND_EU433:
                index = getValueIndex(Constants.LORA_EU433_DR, dr);
                break;
            case Constants.LORA_BAND_EU868:
                index = getValueIndex(Constants.LORA_EU868_DR, dr);
                break;
            case Constants.LORA_BAND_AS923:
                index = getValueIndex(Constants.LORA_AS923_DR, dr);
                break;
            case Constants.LORA_BAND_AU915:
                index = getValueIndex(Constants.LORA_AU915_DR, dr);
                break;
            case Constants.LORA_BAND_US915:
                index = getValueIndex(Constants.LORA_US915_DR, dr);
                break;
            case Constants.LORA_BAND_SE433:
                index = getValueIndex(Constants.LORA_SE433_DR, dr);
                break;
            case Constants.LORA_BAND_SE470:
                index = getValueIndex(Constants.LORA_SE470_DR, dr);
                break;
            case Constants.LORA_BAND_SE780:
                index = getValueIndex(Constants.LORA_SE780_DR, dr);
                break;
            case Constants.LORA_BAND_SE915:
                index = getValueIndex(Constants.LORA_SE915_DR, dr);
                break;


        }
        if (index == -1) {
            return 0;
        }
        return index;
    }

    public static int getLoraTxp(String band, int index) {

        switch (band) {
            case "SE433":
                return Constants.LORA_SE433_TXP[index];
            case "SE470":
                return Constants.LORA_SE470_TXP[index];
            case "SE780":
                return Constants.LORA_SE780_TXP[index];
            case "SE915":
                return Constants.LORA_SE915_TXP[index];
            case "EU433":
                return Constants.LORA_EU433_TXP[index];
            case "EU868":
                return Constants.LORA_EU868_TXP[index];
            case "US915":
                return Constants.LORA_US915_TXP[index];
            case "AS923":
                return Constants.LORA_AS923_TXP[index];
            case "AU915":
                return Constants.LORA_AU915_TXP[index];
            default:
                return 0;
        }
    }

    public static int getLoraDr(String band, int index) {

        switch (band) {
            case "SE433":
                return Constants.LORA_SE433_DR[index];
            case "SE470":
                return Constants.LORA_SE470_DR[index];
            case "SE780":
                return Constants.LORA_SE780_DR[index];
            case "SE915":
                return Constants.LORA_SE915_DR[index];
            case "EU433":
                return Constants.LORA_EU433_DR[index];
            case "EU868":
                return Constants.LORA_EU868_DR[index];
            case "US915":
                return Constants.LORA_US915_DR[index];
            case "AS923":
                return Constants.LORA_AS923_DR[index];
            case "AU915":
                return Constants.LORA_AU915_DR[index];
            default:
                return 0;
        }
    }

    public static String getLoraSF(String band, int index) {

        switch (band) {
            case "SE433":
                return Constants.LORA_SE433_SF[index];
            case "SE470":
                return Constants.LORA_SE470_SF[index];
            case "SE780":
                return Constants.LORA_SE780_SF[index];
            case "SE915":
                return Constants.LORA_SE915_SF[index];
            case "EU433":
                return Constants.LORA_EU433_SF[index];
            case "EU868":
                return Constants.LORA_EU868_SF[index];
            case "US915":
                return Constants.LORA_US915_SF[index];
            case "AS923":
                return Constants.LORA_AS923_SF[index];
            case "AU915":
                return Constants.LORA_AU915_SF[index];
            default:
                return "";
        }
    }



    public static int getBleTxpIndex(String deviceType, int bleTxp) {
        if (deviceType.equalsIgnoreCase("node")) {
            switch (bleTxp) {
                case -52:
                    return 0;
                case -42:
                    return 1;
                case -38:
                    return 2;
                case -34:
                    return 3;
                case -30:
                    return 4;
                case -20:
                    return 5;
                case -16:
                    return 6;
                case -12:
                    return 7;
                case -8:
                    return 8;
                case -4:
                    return 9;
                case 0:
                    return 10;
                case 4:
                    return 11;
                default:
                    return 0;
            }
        } else {
            switch (bleTxp) {
                case -30:
                    return 0;
                case -20:
                    return 1;
                case -16:
                    return 2;
                case -12:
                    return 3;
                case -8:
                    return 4;
                case -4:
                    return 5;
                case 0:
                    return 6;
                case 4:
                    return 7;
                default:
                    return 0;
            }
        }

    }

    public static int getBleTxp(String deviceType, int index) {
        if (deviceType.equalsIgnoreCase("node")) {
            switch (index) {
                case 0:
                    return -52;
                case 1:
                    return -42;
                case 2:
                    return -38;
                case 3:
                    return -34;
                case 4:
                    return -30;
                case 5:
                    return -20;
                case 6:
                    return -16;
                case 7:
                    return -12;
                case 8:
                    return -8;
                case 9:
                    return -4;
                case 10:
                    return 0;
                case 11:
                    return 4;
                default:
                    return -1;
            }
        } else {
            switch (index) {
                case 0:
                    return -30;
                case 1:
                    return -20;
                case 2:
                    return -16;
                case 3:
                    return -12;
                case 4:
                    return -8;
                case 5:
                    return -4;
                case 6:
                    return 0;
                case 7:
                    return 4;
                default:
                    return -1;
            }
        }
    }

    public static String[] getLoraBandText(Context context, String band) {
        if (band.equals(Constants.LORA_BAND_EU433)) {
            return context.getResources().getStringArray(R.array.signal_eu433_band_array);
        } else if (band.equals(Constants.LORA_BAND_EU868)) {
            return context.getResources().getStringArray(R.array.signal_eu868_band_array);
        }  else if (band.equals(Constants.LORA_BAND_US915)) {
            return context.getResources().getStringArray(R.array.signal_us915_band_array);
        } else if (band.equals(Constants.LORA_BAND_SE470)) {
            return context.getResources().getStringArray(R.array.signal_se470_band_array);
        } else if (band.equals(Constants.LORA_BAND_SE780)) {
            return context.getResources().getStringArray(R.array.signal_se780_band_array);
        } else if (band.equals(Constants.LORA_BAND_SE433)) {
            return context.getResources().getStringArray(R.array.signal_se433_band_array);
        } else if (band.equals(Constants.LORA_BAND_SE915)) {
            return context.getResources().getStringArray(R.array.signal_se915_band_array);
        } else if (band.equals(Constants.LORA_BAND_AU915)) {
            return context.getResources().getStringArray(R.array.signal_au915_band_array);
        } else if (band.equals(Constants.LORA_BAND_AS923)) {
            return context.getResources().getStringArray(R.array.signal_as923_band_array);
        } else {
            return context.getResources().getStringArray(R.array.signal_se433_band_array);
        }
    }

    public static int[] getLoraBandIntArray(String band) {
        if (band.equals(Constants.LORA_BAND_EU433)) {
            return Constants.LORA_BAND_EU_433;
        } else if (band.equals(Constants.LORA_BAND_EU868)) {
            return Constants.LORA_BAND_EU_868;
        } else if (band.equals(Constants.LORA_BAND_US915)) {
            return Constants.LORA_BAND_US_915;
        } else if (band.equals(Constants.LORA_BAND_SE470)) {
            return Constants.LORA_BAND_SE_470;
        } else if (band.equals(Constants.LORA_BAND_SE433)) {
            return Constants.LORA_BAND_SE_433;
        } else if (band.equals(Constants.LORA_BAND_SE780)) {
            return Constants.LORA_BAND_SE_780;
        } else if (band.equals(Constants.LORA_BAND_SE915)) {
            return Constants.LORA_BAND_SE_915;
        } else if (band.equals(Constants.LORA_BAND_AS923)) {
            return Constants.LORA_BAND_AS_923;
        } else if (band.equals(Constants.LORA_BAND_AU915)) {
            return Constants.LORA_BAND_AU_915;
        } else {
            return Constants.LORA_BAND_SE_433;
        }
    }

    public static int getValueIndex(int array[], int value) {
        for (int i = 0; i < array.length ; i ++) {
            if (array[i] == value) {
                return i;
            }
        }
        return 0;
    }
}
