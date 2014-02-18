package com.capse.rando2go;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JSON {
	public static String stringify(Map mp) {
	    Iterator it = mp.entrySet().iterator();
	    String out = "{";
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        out += "\"" + pairs.getKey() + "\":\""+ pairs.getValue() + "\"}";
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	    return out;
	}
	
	public static HashMap makeHash(String s){
		HashMap mp = new HashMap();
		s = s.substring(1, s.length() -1);
		String[] ar = s.split(",");
		
		for(int i = 0; i < ar.length; i++){
			
			String[] arr = ar[i].split(":");
			mp.put(arr[0].replace("\"",""), arr[1].replace("\"",""));
		}
		return mp;
	}
}
