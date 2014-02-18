package com.capse.rando2go;

import android.view.View;
import android.widget.TextView;

public class SetMarkerRunnable implements Runnable{
	private String content;
	private DrawView target;
	private double x;
	private double y;
	
	public SetMarkerRunnable(DrawView target, String content, double x, double y){
		this.target = target;
		this.content = content;
		this.x = x;
		this.y = y;
	}
	@Override
	public void run() {
		target.setMarker(content,x,y);
	}

}
