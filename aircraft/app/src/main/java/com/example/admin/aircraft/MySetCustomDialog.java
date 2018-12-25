package com.example.admin.aircraft;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
/**
 * 自定义dialog
 */
public class MySetCustomDialog extends Dialog {
    //定义回调事件，用于dialog的点击事件
    public interface OnCustomDialogListener{
        public void back(String name);
    }

    private String name;
    private OnCustomDialogListener customDialogListener;
    EditText etName;

    public MySetCustomDialog(Context context,String name,OnCustomDialogListener customDialogListener) {
        super(context);
        this.name = name;
        this.customDialogListener = customDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setdialog);
        //设置标题
      /*  setTitle(name);*/
       /* etName = (EditText)findViewById(R.id.edit);
        Button clickBtn = (Button) findViewById(R.id.clickbtn);*/
      /*  clickBtn.setOnClickListener(clickListener);*/
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            customDialogListener.back(String.valueOf(etName.getText()));
            MySetCustomDialog.this.dismiss();
        }
    };

}