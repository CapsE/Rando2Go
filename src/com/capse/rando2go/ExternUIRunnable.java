package com.capse.rando2go;

import android.widget.TextView;

public class ExternUIRunnable implements Runnable{
	private String content;
	private TextView target;
	
	public ExternUIRunnable(TextView target, String content){
		this.target = target;
		this.content = content;
	}
	@Override
	public void run() {
		target.setText(content);
	}

}
