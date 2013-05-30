package com.aprendeandroid.trivialandroid1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.util.Log;
import android.widget.Toast;

public class UploadQuestionService extends Service {
	
	private static final String DEBUG_TAG = "UploaderService";
	
	private boolean subidaOk = false;
	
	private UploadTask uploader;
	
	private String strNickname;
	private String pregunta;
	private String respuestaA;
	private String respuestaB;
	private String respuestaC;
	private String respuestaD;
	
	private int respuestaOK;
	private String imagenPath;
	
	
	
	//Como onCreate de Activity
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
			
		//obtenemos extras		
		strNickname = intent.getStringExtra("nickname");
		pregunta = intent.getStringExtra("pregunta");
		respuestaA = intent.getStringExtra("respuestaA");
		respuestaB = intent.getStringExtra("respuestaB");
		respuestaC = intent.getStringExtra("respuestaC");
		respuestaD = intent.getStringExtra("respuestaD");
		imagenPath = intent.getStringExtra("imagenPath");
		
		//el servidor espera: a->1 b->2 c->3 d->4
		respuestaOK = intent.getIntExtra("respuestaOK", 1);
				
		//Log.e(Constants.DEBUG_TAG, "Pregunta->"+pregunta+"respuestas->" + respuestaA+" - "+respuestaB+" -"+ respuestaC+" - "+respuestaD +"correct->"+respuestaOK+"nickname->"+strNickname);
		//Log.e(Constants.DEBUG_TAG, "Imagen->"+imagenPath);

		
		// lanzamos el hilo del servicio
		uploader = new UploadTask();
		uploader.execute();
	
		
		return START_REDELIVER_INTENT;//lo intenta hasta completar la tarea
	}

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	//Hilo del servicio, 2 tareas: Guardar Datos de pregunta en base de datos y guardar la imagen en el servidor, iran juntas por POST 
	private class UploadTask extends AsyncTask<Void, String, Integer> {
		
		//Sigue teniendo acceso a las sharedpreferences
		SharedPreferences settings;			

		@Override
		protected void onPreExecute() {
			settings = getSharedPreferences(Constants.GAME_PREFERENCES,Context.MODE_PRIVATE);
		}

		@Override
		protected Integer doInBackground(Void ...voids) {
			
			//String toastMensaje = "Problema en subida";
			Integer result;
			result = Constants.ResultadoSubida.ERROR.getValue();
			
			subidaOk = subeCuestion();//---->aqui se va a este método, que se encarga de subir datos de la pregunta y devuelve el resultado 
			
			/*
			SharedPreferences.Editor editor = settings.edit();			
			if(!isCancelled()) {
				//esta comparacion DEBERIA SER UN BOOLEANO 
				if (result > Constants.ResultadoSubida.MODIFIED.getValue()) { // nuevo, servidor devuelve id > 0
					toastMensaje = "Nuevo usuario en servidor";
					userId = result;
					editor.putInt(Constants.GAME_PREFERENCES_ID, userId);
					editor.commit();
				}
				else if (result == Constants.ResultadoSubida.MODIFIED.getValue()) { // modificado, servidor devuelve 0
					toastMensaje = "Usuario modificado en servidor";
				}
				else {//error
					publishProgress(toastMensaje); //llamada a onProgressUpdate
					return result;
				}
				publishProgress(toastMensaje); //llamada a onProgressUpdate
			}
			else {
				Constants.ResultadoSubida.ERROR.getValue();
			}	
			*/
			if(subidaOk){
				result = 0;
			}
			return result;
		}

		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			Toast.makeText(UploadQuestionService.this, values[0], Toast.LENGTH_SHORT).show();
		}

		//Le llega el resultado de DoInBackground
		@Override
		protected void onPostExecute(Integer result) {			
			
			if (result == Constants.ResultadoSubida.OK.getValue()) {
				String toastMensaje = "Pregunta subida con éxito";
				Toast.makeText(UploadQuestionService.this, toastMensaje, Toast.LENGTH_SHORT).show();
			}
			else if (result == Constants.ResultadoSubida.ERROR.getValue()) {
				String toastMensaje = "Pregunta no subida con éxito";
				Toast.makeText(UploadQuestionService.this, toastMensaje, Toast.LENGTH_SHORT).show();
			}
		}
		
		
		
		//Enviando datos por POST y con imagen		
		private Boolean subeCuestion() {		
		

			int resultado = Constants.ResultadoSubida.ERROR.getValue();			
			boolean subidaTodo = false;
			
			File file = new File(imagenPath);				
	
			
			//EMPEZAMOS LA PETICION POST PARA SUBIR ESE FILE

			//Simulamos un formulario HTML
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			String response = null;//lo que dice PHP 
			
			
			FileBody fileBody = new FileBody(file);//esto llega a php como $_FILES
			entity.addPart("imagen", fileBody);//Simula el boton type="FILE" de html
			
			try {				
				entity.addPart("nickname", new StringBody(strNickname)); // esto es un $_POST que pone el nickname
				entity.addPart("cuestion", new StringBody(pregunta)); // esto es un $_POST que pone la pregunta
				entity.addPart("respuestaA", new StringBody(respuestaA)); // esto es un $_POST que pone la respuestaA
				entity.addPart("respuestaB", new StringBody(respuestaB)); // esto es un $_POST que ponela respuestaB
				entity.addPart("respuestaC", new StringBody(respuestaC)); // esto es un $_POST que pone la respuestaC
				entity.addPart("respuestaD", new StringBody(respuestaD)); // esto es un $_POST que pone la respuestaD
				entity.addPart("respuestaOK", new StringBody(String.valueOf(respuestaOK))); // esto es un $_POST que pone la respuesta OK
				
				//entity.addPart("cuestion", new StringBody(pregunta)); // esto es un $_POST que pone el nickname
			
			} catch (UnsupportedEncodingException e) {
				Log.e(DEBUG_TAG, "Failed to add form field.", e);
			}
			
			
			//Aqui se crea la peticion
			HttpPost request = new HttpPost(Constants.PATH_TO_USUARIOS + Constants.PHP_SUBIR_PREGUNTA);
			request.setEntity(entity);
			
			// se hace la peticion
			HttpClient client = new DefaultHttpClient();

			try {
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				response = client.execute(request,responseHandler);

		        Log.i("miApp", "el servidor dice: "+response);

		        if(response != null) {//No problemas
		        	//se coge primer caracter de la respuesta que es lo que nos interesa de la respuesta del PHP
			        String shortResp = response.substring(0, 1);
		        	try {
						resultado = Integer.parseInt(shortResp);
					} 
		        	catch (NumberFormatException e) {
						e.printStackTrace();
						resultado = Constants.ResultadoSubida.ERROR.getValue();
					}
		        }

			} catch (ClientProtocolException e) {
				response += "";
				Log.e(DEBUG_TAG, "Unexpected ClientProtocolException", e);
			} catch (IOException e) {
				Log.e(DEBUG_TAG, "Unexpected IOException", e);
			}
			
			if(resultado >=0){
				subidaTodo = true;
			}
			
			return subidaTodo;
		}
	}
}	

