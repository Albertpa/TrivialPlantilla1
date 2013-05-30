package com.aprendeandroid.trivialandroid1;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {//AppWidgetProvider es un BroadCastReciver
	//Viene de los BroadCastIntent se usa para enviar mensajes multidifusion y quien implemente BroadCastReciver lo recive.

	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Intent serviceIntent = new Intent(context, WidgetUpdateService.class); //lanzamos Service WidgetUpdateService actualiza el widget
		context.startService(serviceIntent);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) { //Para el servicio
		Intent serviceIntent = new Intent(context, WidgetUpdateService.class);
		context.stopService(serviceIntent);

		super.onDeleted(context, appWidgetIds);
	}
	
	
	
	

	public static class WidgetUpdateService extends Service { //INICIAMOS UN SERVICIO 

		private WidgetUpdateTask updater;//AsyncTask
		private static final String DEBUG_TAG = "WidgetUpdateService";
		private SharedPreferences settings;

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			settings = getSharedPreferences(Constants.GAME_PREFERENCES, Context.MODE_PRIVATE);
			
			updater = new WidgetUpdateTask();
			updater.execute(startId);
			// if we're killed, restart us with the original Intent so we get an
			// extra data again, should we choose to use it later
			return START_REDELIVER_INTENT; //sigue el servicio mientras el widget este puesto
		}

		@Override
		public void onDestroy() {
			updater.cancel(true);
			super.onDestroy();
		}

		
		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}
		
		//EL ASYNCTASK DE ESTE SERVICIO
		private class WidgetUpdateTask extends AsyncTask<Integer, Void, Boolean> {

			@Override
			protected Boolean doInBackground(Integer... startIds) {
				return widgetUpdate(startIds[0]); //llama a actualizar el widget
			}

			/**
			 * The widget update code
			 * 
			 * @param startId the id of the widget we're dealing with
			 * @return boolean , false on any error
			 */
			private boolean widgetUpdate(int startId) {
				boolean succeeded = false;
				
				Context context = WidgetUpdateService.this;

				WidgetData preguntaData = getWidgetData();//va a metodo mas abajo 

				// preparamos el RemoteView 
				String packageName = context.getPackageName();
				
				RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget);

				//Configuramos su layout
				remoteView.setTextViewText(R.id.widget_nickname_subida, preguntaData.nicknameSubida + "!!");
				remoteView.setTextViewText(R.id.widget_nickname_user, preguntaData.nicknameUser);
				
				setWidgetAvatar(remoteView, preguntaData.imageUrl, R.id.widget_image);//ponemos la imagen mendiante este metodo que esta abajo
								

				try {
					// add click handling
					Intent launchAppIntent = new Intent(context,GameActivity.class);//se lanza el juego de nuevo
					
					PendingIntent launchAppPendingIntent = PendingIntent.getActivity(context, 0, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					//PendingIntent es un objeto que guarda un intent para ser lanzado en el futuro
					
					//hacemos el widget Clicable
					remoteView.setOnClickPendingIntent(R.id.widget_view, launchAppPendingIntent);

					
					
					
					//EL sistema activa cada media hora el servicio, y nosotros escuchamos
					//En el manifest se enlaza con widget_info que tiene la configuracion
					
					
					//Nombre especifico a nivel de aplicaciones instaladas (identificador del proceso a nivel de sistema operativo)
					ComponentName trivialWidget = new ComponentName(context, WidgetProvider.class);

					//Instancia de manager Application Widget
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

					//update the widget
					appWidgetManager.updateAppWidget(trivialWidget, remoteView);
					
					
					succeeded = true;

				} 
				catch (Exception e) {
					Log.e(DEBUG_TAG, "Failed to update widget", e);
				}

				if (!WidgetUpdateService.this.stopSelfResult(startId)) {
					Log.e(DEBUG_TAG, "Failed to stop service");
				}

				return succeeded;

			}
			
			
			
			
			
			
			
			
			/**
			 * Download data for displaying in the Widget
			 * 
			 * @param widgetData
			 * @return
			 */
			private WidgetData getWidgetData() {
				
				String nicknameSubida = null;
				String imageUrl = null;
				String nicknameUser = settings.getString(Constants.GAME_PREFERENCES_NICKNAME, "");
				
				if(nicknameUser.trim().equals("")) {
					return new WidgetData("Unknown", null, nicknameUser);
				}

				DefaultHttpClient client = new DefaultHttpClient();
				try {
					HttpGet httpGet = new HttpGet(Constants.PATH_TO_USUARIOS + Constants.PHP_ULTIMAPREGUNTA);

			        String response;
			        
			        //Para la respuesta del servidor
			        ResponseHandler<String> responseHandler = new BasicResponseHandler();
			        response = client.execute(httpGet, responseHandler);
//			        Log.i("miApp", "el servidor dice: "+response);
			        
			        if(response != null) {
			        	imageUrl = response.substring(0, response.indexOf("$$"));
			        	nicknameSubida = response.substring(response.indexOf("$$") + 2, response.length() - 1); // -1 quitar retorno de carro al final
			        	return new WidgetData(nicknameSubida, imageUrl, nicknameUser);
			        }
				}
				catch (Exception e) {
					Log.i(Constants.DEBUG_TAG, "AppWidget", e);
				}
				return new WidgetData("Unknown", null, nicknameUser);
			}
			

			/**
			 * @param remoteView
			 * @param imageUrl
			 * @param imageId
			 */
			private void setWidgetAvatar(RemoteViews remoteView, String imageUrl, int imageId) {
				
				if (imageUrl != null && imageUrl.length() > 0) {
					URL image;
					
					try {
						image = new URL(imageUrl);
						Log.d(DEBUG_TAG, "imageUrl: " + imageUrl);

						BufferedInputStream stream = new BufferedInputStream(image.openStream());
						Bitmap bitmap = BitmapFactory.decodeStream(stream);

						if (bitmap == null) {
							Log.w(DEBUG_TAG, "Failed to decode image");
							remoteView.setImageViewResource(imageId,R.drawable.noquestion);
						} 
						else {
							remoteView.setImageViewBitmap(imageId, bitmap);
						}
					} 
					catch (MalformedURLException e) {
						Log.e(DEBUG_TAG, "Bad url in image", e);
					} 
					catch (IOException e) {
						Log.e(DEBUG_TAG, "IO failure for image", e);
					}
				} 
				else {
					remoteView.setImageViewResource(imageId, R.drawable.noquestion);
				}
			}

			
			
		}

		
		
		
		
		
		
		//Holder del widget
		public class WidgetData {
			String nicknameSubida;
			String imageUrl;
			String nicknameUser;

			public WidgetData(String nicknameSubida, String imageUrl, String nicknameUser) {
				this.nicknameSubida = nicknameSubida;
				this.imageUrl = imageUrl;
				this.nicknameUser = nicknameUser;
			}

			public String getNicknameSubida() {
				return nicknameSubida;
			}

			public void setNicknameSubida(String nickname) {
				this.nicknameSubida = nickname;
			}

			public String getimageUrl() {
				return imageUrl;
			}

			public void setimageUrl(String imageUrl) {
				this.imageUrl = imageUrl;
			}
			
			public String getNicknameUser() {
				return nicknameUser;
			}

			public void setNicknameUser(String nickname) {
				this.nicknameUser = nickname;
			}			
		}

	}

}
