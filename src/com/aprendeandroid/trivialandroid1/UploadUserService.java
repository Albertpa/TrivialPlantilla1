package com.aprendeandroid.trivialandroid1;

import java.io.File;
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
import android.util.Log;
import android.widget.Toast;

public class UploadUserService extends Service {
	
	private static final String DEBUG_TAG = "UploaderService";
	
	private int userId;
	private String strNickname;
	private String strEmail;
	private Long dayOfBirth;
	private String strPassword;
	private int gender;
	private String strAvatar;	
	private boolean subidaOk = false;
	private UploadTask uploader;

	
	
	//Como onCreate de Activity
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
			
		userId = intent.getIntExtra("id", -1);
		strNickname = intent.getStringExtra("nickname");
		strEmail = intent.getStringExtra("mail");
		dayOfBirth = intent.getLongExtra("dob", 0);
		strPassword = intent.getStringExtra("pass");
		gender = intent.getIntExtra("genero", 0);
		strAvatar = intent.getStringExtra("avatar");
		
		
		//imitando un problema de conexion que retrasa 10sg.
//		Handler handler = new Handler(); 
//		handler.postDelayed(new Runnable() {
//			
//			public void run() {
//				uploader = new UploadTask();
//				uploader.execute();
//			}
//		}, 10000);
		
		// lanzamos el hilo del servicio
		uploader = new UploadTask();
		uploader.execute();
	
		
		return START_REDELIVER_INTENT;//lo intenta hasta completar la tarea
	}

	//Momento en el que se "enganchan" el servicio con la aplicación
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	//Hilo del servicio, 2 tareas: Guardar Datos de usuario en base de datos y guardar la imagen en el servidor 
	private class UploadTask extends AsyncTask<Void, String, Integer> {
		
		/*Las sharedPreferences, aunque el servicio este fuera de la aplicacion
			tiene acceso a las sharedPreferences, 
			aunq le manda datos por los extras, cuando conseguimos guardar en el servidor
			tendremos que actualizar las sharedPreferences para que esten totalmente
			sincronizadas, este es el mejor momento para hacerlo
		*/
		SharedPreferences settings;			

		@Override
		protected void onPreExecute() {
			settings = getSharedPreferences(Constants.GAME_PREFERENCES,Context.MODE_PRIVATE);
		}

		@Override
		protected Integer doInBackground(Void ...voids) {
			
			String toastMensaje = "Problema en subida";
			
			Integer result = subeUsuario();//---->aqui se va a este método, que se encarga de surbir DATOS DE USUARIO y devuelve el resultado 
			
			SharedPreferences.Editor editor = settings.edit();
			
			if(!isCancelled()) {
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
				
				
				result = subeAvatar();//---->aqui se va a este mŽtodo, que se encarga de surbir EL AVATAR y devuelve el resultado 
			}
			else {
				Constants.ResultadoSubida.ERROR.getValue();
			}				
			
			return result;
		}

		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			Toast.makeText(UploadUserService.this, values[0], Toast.LENGTH_SHORT).show();
		}

		
		@Override
		protected void onPostExecute(Integer result) {
			if (result == Constants.ResultadoSubida.OK.getValue()) {
				String toastMensaje = "Avatar subido";
				Toast.makeText(UploadUserService.this, toastMensaje, Toast.LENGTH_SHORT).show();
			}
			else if (result == Constants.ResultadoSubida.ERROR.getValue()) {
				String toastMensaje = "Avatar no subido";
				Toast.makeText(UploadUserService.this, toastMensaje, Toast.LENGTH_SHORT).show();
			}
		}
		
		
		
		
		
		
		private Integer subeUsuario() {
			
			int resultado = Constants.ResultadoSubida.ERROR.getValue(); //inicializamos como si diera error
			
			String response = null; //respuesta del php como string, luego se pasa a int y se lleva a resultado
			
			DefaultHttpClient client = new DefaultHttpClient();
			
			try {
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(8);
							        nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(userId)));
							        nameValuePairs.add(new BasicNameValuePair("nickname", strNickname));
									nameValuePairs.add(new BasicNameValuePair("pass", strPassword));
									nameValuePairs.add(new BasicNameValuePair("mail", strEmail));
									nameValuePairs.add(new BasicNameValuePair("dob", String.valueOf(dayOfBirth))); 
									nameValuePairs.add(new BasicNameValuePair("genero", String.valueOf(gender)));
				
				String url = Constants.PATH_TO_USUARIOS + Constants.PHP_USUARIO + "?" + URLEncodedUtils.format(nameValuePairs, null);

				HttpGet httpGet = new HttpGet(url);

		        ResponseHandler<String> responseHandler = new BasicResponseHandler();
		        response = client.execute(httpGet, responseHandler);
		        
		        if(response != null) {
		        	try {
						resultado = Integer.parseInt(response); //aqui se establece lo que devuelve el PHP realmente
					} catch (NumberFormatException e) {
						resultado = Constants.ResultadoSubida.ERROR.getValue();
					}
		        }
		    } 
			catch (ClientProtocolException e) {
				// mensaje por defecto
		    } 
			catch (IOException e) {
				// mensaje por defecto
		    }				
			
			return resultado; //devolvemos la respuesta de php a doInBackground
		}
		

		
		
		
		
		private Integer subeAvatar() {
			
			int resultado = Constants.ResultadoSubida.ERROR.getValue();
			
			String avatar = settings.getString(Constants.GAME_PREFERENCES_AVATAR, "0");
			
			if(avatar.equals("0")) {
				return resultado;
			}
		
			
			String avataresPath = getResources().getString(R.string.avatar_assets_dir);
			AssetManager manager = getResources().getAssets();	

			
			InputStream inputStream;
			try {
				inputStream = manager.open(avataresPath + "/" + avatar);
			} 
			catch (IOException e2) {
				e2.printStackTrace();
				return resultado;
			}
			
			
			File file; //se crea el fichero temporal vacio
			try {
				String extension = avatar.substring(avatar.lastIndexOf('.'));//busca el ultimo "." y corta a partir de hay incluyendo el "."
				file = File.createTempFile("tmp_image", extension, getCacheDir());//reconstruimos el nombre del archivo
			} 
			catch (IOException e1) {
				e1.printStackTrace();
				return resultado;
			}


			OutputStream out; //Se prepara el FILE temporal
			try {
				out = new FileOutputStream(file);//se verifica que no hay problemas de escritura
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
				return resultado;
			}
			
			
			//Aqui es donde realmente se hace la copia
			byte buf[] = new byte[1024];
			int len;
			try {
				while ((len = inputStream.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
				
				inputStream.close();						
			} 
			catch (IOException e) {
				e.printStackTrace();
				return resultado;
			}
			
			
			
			//EMPEZAMOS LA PETICION POST PARA SUBIR ESE FILE TEMPORAL

			//Simulamos un formulario HTML
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			String response = null;//lo que dice PHP 

			
			FileBody fileBody = new FileBody(file);//esto llega a php como $_FILES
			entity.addPart("uploadedfile", fileBody);//Simula el boton type="FILE" de html
			
			try {
				entity.addPart("id", new StringBody(String.valueOf(userId))); // esto es un $_POST que pone userId (id es como se llama la variable)
			} catch (UnsupportedEncodingException e) {
				Log.e(DEBUG_TAG, "Failed to add form field.", e);
			}
			
			
			//Aqui se crea la peticion
			HttpPost request = new HttpPost(Constants.PATH_TO_USUARIOS + Constants.PHP_IMAGEN);
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

			file.delete(); //Borramos el archivo temporal
			return resultado;
		}
	}
}	

