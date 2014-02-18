package com.capse.rando2go;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

public class DrawView extends View {
    Paint paint = new Paint();
    ArrayList<Rect> markers = new ArrayList<Rect>();
    Canvas canvas;
	int xCenter;
	int yCenter;

    public DrawView(Context context) {
        super(context);

        paint.setColor(Color.rgb(255, 102, 0));

    }

    @Override
    public void onDraw(Canvas canvas) {
    	this.canvas = canvas;
        xCenter = (int)this.getWidth() /2;
        yCenter = (int)this.getHeight() /2;
    	
    	//canvas.drawRect(new Rect(xCenter-10,yCenter-10,xCenter+20,yCenter+20), paint);
    	paint.setColor(Color.RED);
    	System.out.println("Marker Size: " + markers.size());
    	for(int i = 0; i < markers.size(); i++){
    		canvas.drawRect(markers.get(i), paint);
    	}
	}
	
	public Canvas getCanvas(){
		return canvas;
	}
	
	public void moveToBack() 
	{
	    ViewGroup vg = ((ViewGroup) this.getParent());
	    int index = vg.indexOfChild(this);
	    for(int i = 0; i<index; i++)
	    {
	    vg.bringChildToFront(vg.getChildAt(i));
	    }
	}
	
	public void setMarker(String position, double myX, double myY){
		System.out.println("Making a new Marker...");
		String[] ar = position.split(";");
		float x = Float.valueOf(ar[0]);
		float y = Float.valueOf(ar[1]);
		
		int pointX = (int) (x - myX)*10;
		int pointY = (int) (y - myY)*10;
		
		markers.add(new Rect(xCenter + pointX, yCenter + pointY,xCenter + pointX +10, yCenter + pointY +10));
		this.invalidate();
	}

}
