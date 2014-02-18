package com.capse.rando2go;

import java.util.HashMap;
import java.util.Map;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.plugins.rpc.Rpc;
import org.jwebsocket.client.plugins.rpc.Rrpc;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity implements WebSocketClientListener{
	
	private TextView t;
	private TextView pos;
	private double x;
	private double y;
	private boolean connected = false;
	private int count = 0;
	private BaseTokenClient btc;
	private Handler mHandler;
	private String playerName;
	private Thread refreshThread;
	private DrawView drawView; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initGetLocation();
		initWebsocket();
        System.out.println("Network and Location");
		
		SharedPreferences settings = getSharedPreferences("PlayerData", 0);
	    playerName = settings.getString("playerName", "Name");
	    if(playerName != "Name"){
	    	openRadar();
	    }else{
	    	openHeroName();
	    }		
		mHandler = new Handler();
	}
	
	private void initWebsocket(){
		
		btc = new BaseTokenClient();//create a new instance os the base token client
        btc.addListener(this);//add this class as a listener
        //btc.addListener(new RpcListener());//add an rpc listener
        Rpc.setDefaultBaseTokenClient(btc);//set it to the default btc
        Rrpc.setDefaultBaseTokenClient(btc);//same here
        try{
            System.out.println("connecting...");//debug
            btc.open("ws://31.19.39.17:4545");//try to open the connection to your server
            connected = true;
        }catch(Exception e){
            System.out.println("Error while connecting...");//debug errors
        }
        refreshThread = new Thread(){
        	public void run(){
	        	while(connected == true){
	        		try {
	        			Map m = new HashMap();
	        			m.put("position", String.valueOf(x) + ";" + String.valueOf(y) );
		    			btc.sendText("refresh", JSON.stringify(m) );
		    		} catch (WebSocketException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}
	        		try {
						sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	initWebsocket();
        	}
        };
        refreshThread.start();
	}
	
	private void initGetLocation(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String lp = lm.getBestProvider(criteria, true);
        
        Location posLast = lm.getLastKnownLocation(lp);
        x = posLast.getLatitude();
    	y = posLast.getLongitude();
    	
        LocationListener ll = new mylocationlistener();
        lm.requestLocationUpdates(lp, 2000, 0, ll);
	}
	
   private class mylocationlistener implements LocationListener {
	    @Override
	    public void onLocationChanged(Location location) {
	        if (location != null) {
	            try {
		        	x = location.getLatitude();
		        	y = location.getLongitude();
		        	mHandler.post(new ExternUIRunnable(pos, x + "\n" + y));
		        	Map m = new HashMap();
        			m.put("position", String.valueOf(x) + ";" + String.valueOf(y) );
	    			btc.sendText("refresh", JSON.stringify(m));
	    		} catch (WebSocketException e) {
	    			// TODO Auto-generated catch block
	    			t.setText(e.toString());
	    			e.printStackTrace();
	    		}
	        }
	    }
	    @Override
	    public void onProviderDisabled(String provider) {
	    }
	    @Override
	    public void onProviderEnabled(String provider) {
	    }
	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);        
		return true;
	}

	@Override
	public void processClosed(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Client is disconnected");
		connected = false;
	}

	@Override
	public void processOpened(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Client is connected");
		t.setText("Client is connected");
	}

	@Override
	public void processOpening(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Client is connecting");
	}

	@Override
	public void processPacket(WebSocketClientEvent arg0, WebSocketPacket arg1) {
		// TODO Auto-generated method stub
		System.out.println("Got package: " + arg1.toString());
		HashMap msg = JSON.makeHash(arg1.toString());
		if(msg.get("func") != null){
			System.out.println("Function: " + msg.get("func"));
			if(msg.get("func") == "coords"){
				mHandler.post(new ExternUIRunnable(t, msg.get("coords") + " Count: " + count));
				count++;
			}
			if(msg.get("func").equals("quests")){
				System.out.println("Quest-Paket gefunden!");
				TextView h = (TextView) findViewById(R.id.textView1);
				mHandler.post(new ExternUIRunnable(h, msg.toString()));
			}
			if(msg.get("func").equals("radarQuest")){
				System.out.println("Calling Set Marker");
				mHandler.post(new SetMarkerRunnable(drawView, msg.get("coords").toString(), x,y));
				System.out.println("Marker should be set");
			}
		}
		
	}

	@Override
	public void processReconnecting(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void openHeroName(){
		setContentView(R.layout.choose_name);
    	final Button button = (Button) findViewById(R.id.name_button);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	 SharedPreferences settings = getSharedPreferences("PlayerData", 0);
                 SharedPreferences.Editor editor = settings.edit();
                 t = (TextView)findViewById(R.id.hero_name);
                 System.out.println(t.getText().toString());
                 playerName = t.getText().toString();
                 editor.putString("playerName", t.getText().toString());
                 editor.commit();

                openRadar();
             }
         });
	}
	
	private void openRadar(){
		setContentView(R.layout.radar);
        t = (TextView)findViewById(R.id.HelloWorld);
        pos = (TextView)findViewById(R.id.Position);
        TextView n = (TextView)findViewById(R.id.PlayerName);
        
        if(drawView == null){
        	drawView = new DrawView(this);
        }
        drawView.setBackgroundColor(Color.BLACK);
        
        try {
			Map m = new HashMap();
			m.put("position", String.valueOf(x) + ";" + String.valueOf(y) );
			btc.sendText("getRegion", JSON.stringify(m) );
		} catch (WebSocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        this.addContentView(drawView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        drawView.moveToBack();
        
    	pos.setText(String.valueOf(x) + "\n" + String.valueOf(y)); 
    	n.setText(playerName);
    	
    	ImageButton button = (ImageButton) findViewById(R.id.inventoryButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
           	 openInventory();
            }
        });
        
        button = (ImageButton) findViewById(R.id.questButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
           	 openQuestList();
            }
        });
        
        button = (ImageButton) findViewById(R.id.playerButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
           	 openPlayerList();
            }
        });
	}
	
	private void openInventory(){
		setContentView(R.layout.inventory);
		final ImageButton button = (ImageButton) findViewById(R.id.radarButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
           	 openRadar();
            }
        });
	}
	
	private void openQuestList(){
		setContentView(R.layout.quest_list);
		final ImageButton button = (ImageButton) findViewById(R.id.radarButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
           	 openRadar();
            }
        });
        
        try {
			Map m = new HashMap();
			m.put("position", String.valueOf(x) + ";" + String.valueOf(y) );
			btc.sendText("getQuests", JSON.stringify(m) );
		} catch (WebSocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void openPlayerList(){
		setContentView(R.layout.player_list);
		final ImageButton button = (ImageButton) findViewById(R.id.radarButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
           	 openRadar();
            }
        });
	}
}
