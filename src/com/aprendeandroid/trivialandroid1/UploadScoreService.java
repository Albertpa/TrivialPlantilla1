package com.aprendeandroid.trivialandroid1;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class UploadScoreService extends IntentService { //Intent service tiene un hilo y se usa para simplificar los servicios con asynctask
	
	private String message;
	
	public UploadScoreService() { //constructor
		super("score");
	}

	@Override
	protected void onHandleIntent(Intent intent) {//This method is invoked on the worker thread with a request to process
		
		String response = null;
		
		//sacamos de los extras del intent
		long timeWhenStopped = intent.getLongExtra("time", 0);
		int puntuacion = intent.getIntExtra("score", -1);
		int userId = intent.getIntExtra("id", -1);
		
		DefaultHttpClient client = new DefaultHttpClient();
		
		HttpPost httppost = new HttpPost(Constants.PATH_TO_USUARIOS + Constants.PHP_PUNTUACION);

		try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
						        nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(userId)));
						        nameValuePairs.add(new BasicNameValuePair("score", String.valueOf(puntuacion)));
						        nameValuePairs.add(new BasicNameValuePair("tiempo", String.valueOf(timeWhenStopped)));
	        
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        //Para la respuesta del servidor
	        ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        response = client.execute(httppost, responseHandler);
	        //Log.i("miApp", "el servidor dice: "+response);
	        message = "Puntuación subida";
	    } 
		catch (Exception e) {
	    	Log.i(Constants.DEBUG_TAG, "Puntuación no subida", e);
	    	message = "Puntuación no subida";
	    } 
	}
	
	

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Toast.makeText(UploadScoreService.this, message, Toast.LENGTH_SHORT).show();
	}

}
