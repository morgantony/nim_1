package com.netease.nim.weyouchats.common.util;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.bhm.sdk.bhmlibrary.utils.DateUtils;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.netease.nim.weyouchats.R;

import java.util.Calendar;
import java.util.Date;

public class PickerUtils {

    public interface OnWheelPickerItemClickListener {

        void onItemClick(int i, int i1, int i2, int i3, int i4, int i5, View view);
    }

    public static TimePickerView getDefaultDatePickerView(Context context, int[] startDateStr
            , int[] endDateStr, int[] selectedDateStr, final OnWheelPickerItemClickListener listener ){

        return getDatePickerView(context, startDateStr, endDateStr, selectedDateStr, false, listener);
    }

    public static TimePickerView getDatePickerView(Context context, int[] startDateStr
            , int[] endDateStr, int[] selectedDateStr,boolean isShiFen, final OnWheelPickerItemClickListener listener ){
        return getDatePickerView(context, startDateStr, endDateStr, selectedDateStr, true, isShiFen, listener);
    }

    public static TimePickerView getDatePickerView(Context context, int[] startDateStr
            , int[] endDateStr, int[] selectedDateStr,boolean day, boolean isShiFen, final OnWheelPickerItemClickListener listener ){

        Calendar selectedDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        //正确设置方式 原因：注意事项有说明
        startDate.set(startDateStr[0], startDateStr[1]-1, startDateStr[2], startDateStr[3], startDateStr[4], startDateStr[5]);
        endDate.set(endDateStr[0], endDateStr[1]-1, endDateStr[2], endDateStr[3], endDateStr[4], endDateStr[5]);
        selectedDate.set(selectedDateStr[0], selectedDateStr[1]-1, selectedDateStr[2], selectedDateStr[3],
                selectedDateStr[4], selectedDateStr[5]);

        return new TimePickerBuilder(context,
                new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {//选中事件回调
                        listener.onItemClick(Integer.parseInt(DateUtils.getYear(date)),
                                Integer.parseInt(DateUtils.getMonth(date)),
                                Integer.parseInt(DateUtils.getDay(date)),
                                Integer.parseInt(DateUtils.getHour(date)),
                                Integer.parseInt(DateUtils.getMinute(date)),
                                Integer.parseInt(DateUtils.getSecond(date)),
                                v);
                    }
                })
                .setType(new boolean[]{true, true, day,isShiFen, isShiFen, false})// 默认全部显示
                .setSubmitText("确定")//确定按钮文字
                .setCancelText("取消")//取消按钮文字
                .setLineSpacingMultiplier(1.6f)
                .setSubCalSize(17)//确定和取消文字大小
                .setSubCalSize(17)
                .setTitleSize(17)//标题文字大小
                .setContentTextSize(18)//滚轮文字大小
                .setSubmitColor(ContextCompat.getColor(context, R.color.color_main))//确定按钮文字颜色
                .setCancelColor(ContextCompat.getColor(context, R.color.color_main))//取消按钮文字颜色
                .setTitleColor(ContextCompat.getColor(context, R.color.color_main))//取消按钮文字颜色
                .setTitleBgColor(ContextCompat.getColor(context, R.color.color_f0f6fa))//标题背景颜色
                .setBgColor(ContextCompat.getColor(context, R.color.white))//滚轮背景颜色 Night mode
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isCyclic(false)//循环与否
                .setDate(selectedDate)// 如果不设置的话，默认是系统时间*/
                .setRangDate(startDate,endDate)//起始终止年月日设定
                .setOutSideCancelable(true)//点击外部dismiss default true
                .isDialog(false)//是否显示为对话框样式
                .build();
    }

    public static String changeTime(int i, int i1, int i2, int i3, int i4, boolean isShiFen, String fu){
        String str1 = fu;
        String str2 = fu;
        String str3 = " ";
        String str4 = ":";
        if(i1 < 10){
            str1 = fu + "0";
        }
        if(i2 < 10){
            str2 = fu + "0";
        }
        if(i3 < 10){
            str3 = " 0";
        }
        if(i4 < 10){
            str4 = ":0";
        }
        if(isShiFen) {
            return i + str1 + i1 + str2 + i2 + str3 + i3 + str4 + i4;//返回 2018-04-01 05:02
        }else{
            return i + str1 + i1 + str2 + i2;//返回 2018-04-01
        }
    }

    public static void showExitDialog(Context context, View.OnClickListener clickListener){
        Dialog dialog = new Dialog(context, R.style.dialogStyle);
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_exit_app);
        Button left = (Button) window.findViewById(R.id.btn_left);
        Button right = (Button) window.findViewById(R.id.btn_right);
        left.setOnClickListener(v -> dialog.dismiss());
        right.setOnClickListener(v -> {
            dialog.dismiss();
            clickListener.onClick(v);
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }
}
