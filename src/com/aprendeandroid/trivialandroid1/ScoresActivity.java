package com.aprendeandroid.trivialandroid1;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.aprendeandroid.trivialandroid1.SupportListFragment.ListItemSelectedListener;



import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

public class ScoresActivity extends FragmentActivity implements ListItemSelectedListener {

	//ArrayList<PuntosUsuario> listaPuntosUsuarios = new ArrayList<PuntosUsuario>();
	
	
	SupportListFragment listFrag;
	ScoresAdapter adapter;
	ScoreDownloaderTask  downloaderTask = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.scores);
		
		//Creamos el Fragment
		listFrag = new SupportListFragment();
		
		Bundle parametros = new Bundle(); //Enviamos en un Bundle los parametros de como lo queremos este layout
		parametros.putInt("listaLayoutId", R.layout.list_fragment);
		parametros.putInt("emptyViewId", android.R.id.empty);
		//parametros.putInt("emptyViewId", R.id.empty_list_view);
		listFrag.setArguments(parametros);
		
		//Lo colocamos en el layout con trasnaction
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.listPlace, listFrag, "LIST");
		ft.commit();
		/*
		//Iniciamos la carga y recorrido de xml
		XmlPullParser contenidoXML = null;
		
		try {
			contenidoXML = getResources().getXml(R.xml.consulta_scores);
			recorrerXMLElementos(contenidoXML);
		} catch (Exception e) {
			
		}*/
		montarLista();
	}

	
	
	//Recorrido del xml por elementos ANTES de hacerlo online
	/*
		private void recorrerXMLElementos(XmlPullParser parser) throws XmlPullParserException, IOException {

			int eventType = -1;

			String nicknameValue = null;
			String avatarName = null;
			String puntuacionValue = null;

			while (eventType != XmlPullParser.END_DOCUMENT) { //1
				switch (eventType) {

				case XmlPullParser.START_TAG: //2
					// get tag name
					String tagName = parser.getName();

					// if <nickname>
					if (tagName.equalsIgnoreCase("nickname")) {
						// Log.i("miApp",parser.nextText());
						nicknameValue = parser.nextText();
					}
					// if <avatar>
					else if (tagName.equalsIgnoreCase("avatar")) {
						// Log.i("miApp",parser.nextText());
						avatarName = parser.nextText();
						// drawableId = getDrawable(this, avatarName);
					}
					// if <puntuacion>
					else if (tagName.equalsIgnoreCase("puntuacion")) {
						// Log.i("miApp",parser.nextText());
						puntuacionValue = parser.nextText();

						//como sera el ultimo, lo guardamos
						listaPuntosUsuarios.add(new PuntosUsuario(nicknameValue,avatarName, puntuacionValue));
					}

					break;
				}
				// jump to next event
				eventType = parser.next();
			}
			montarLista();
		}
	*/





		private void montarLista() {
			if(adapter == null || adapter.getCount()==0){
				//contexto, recurso como queremos que salgan los elementos de la lista, lista en si, referencia del directorio de los avatares
			  //adapter = new ScoresAdapter(this, R.layout.row_score ,  listaPuntosUsuarios, getResources().getString(R.string.avatar_assets_dir));
				adapter = new ScoresAdapter(this, R.layout.row_score ,  new ArrayList<PuntosUsuario>());
				
				listFrag.setListAdapter(adapter);
//				adapter.clear();  // para probar empty view
				
				//INICIAMOS LA CARGA Y RECORRIDOD E XML
				downloaderTask = new ScoreDownloaderTask();
				downloaderTask.execute(Constants.PATH_TO_USUARIOS, Constants.PHP_SCORES, adapter);
			}
			
			
			

		}

		public void onListItemSelected(int index) {
			/*PuntosUsuario puntos = adapter.getItem(index);
			Log.i(Constants.DEBUG_TAG,
					puntos.getNickName() + " " + puntos.getAvatar() + " "
							+ puntos.getPuntuacion());
							*/
		}
		
		
		
		/////////////  AsyncTask para descarga
				
		// String, tipo de paramentros recibidos en doInBackground() desde execute()
		// PuntosUsuario, tipo de objetos que se muestran en la GUI, publishProgress() --> onProgressUpdate() 
		// Boolean, resultado de doInBackground() que recibe onPostExecute()
		
		private class ScoreDownloaderTask extends AsyncTask<Object, PuntosUsuario, Boolean> {
			
			//Aqui guardamos lo que vienen en el Array como parametros
		    String pathToUsuarios;
		    String phpScores;
		    ScoresAdapter scoresAdapter;
		
		    
		    
		    @Override
		    protected void onPreExecute() {
		        ScoresActivity.this.setProgressBarIndeterminateVisibility(true);
		    }
		
		   
		
		    //Aqui no se puede comunicar con la GUI
		    @Override		   
		    protected Boolean doInBackground(Object... params) {
		        boolean result = false;
		        
		        //Castings OBLIGATORIOS en el orden añadido
		        pathToUsuarios = (String) params[0];
		        phpScores = (String) params[1];
		        scoresAdapter = (ScoresAdapter) params[2];
		
				XmlPullParser contenidoXML = null;// aqui el XML descargado
		
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(pathToUsuarios + phpScores);			
		
				try {
						//Proceso de descarga
					HttpResponse execute = client.execute(httpGet);
					contenidoXML = XmlPullParserFactory.newInstance().newPullParser();
					InputStream content = execute.getEntity().getContent();
					contenidoXML.setInput(content, null);
		
					recorrerXMLElementos(contenidoXML);
				} 
				catch (Exception e) {
					Log.i(Constants.DEBUG_TAG, e.getMessage());
		
				}
				
				return result;
		    }
		    
		    
		    //Se comunica con la GUI conforme va descargando elementos
		    @Override		  
		    protected void onProgressUpdate(PuntosUsuario... values) {
		    	for(int i = 0; i < values.length; i++) {
		    		scoresAdapter.add(values[i]);  //vamos mostrando los elementos en la lista      		
		    	}
		    }
		    
		    //
		    @Override
		    protected void onPostExecute(Boolean result) {
		        Log.i(Constants.DEBUG_TAG, "onPostExecute");
		        ScoresActivity.this.setProgressBarIndeterminateVisibility(false);
		    }
		    
		    @Override
		    protected void onCancelled() {
		        Log.i(Constants.DEBUG_TAG, "onCancelled");
		        ScoresActivity.this.setProgressBarIndeterminateVisibility(false);
		    }
		    
		    
		    
		    
			// Recorrido del XML por elementos, para la version onLine
		    //El mismo hilo descarga y recorre, por eso esta aqui
			private void recorrerXMLElementos(XmlPullParser parser) throws XmlPullParserException, IOException {        
		
				int eventType = -1;
		
				String nicknameValue = null;
				String avatarName = null;
				String puntuacionValue = null;
				Drawable drawable = null;
		
				while (eventType != XmlPullParser.END_DOCUMENT) {
					switch (eventType) {
		
					case XmlPullParser.START_TAG:
						// get tag name
						String tagName = parser.getName();
		
						// if <nickname>
						if (tagName.equalsIgnoreCase("nickname")) {
							// Log.i("miApp",parser.nextText());
							nicknameValue = parser.nextText();
						}
						// if <avatar>
						else if (tagName.equalsIgnoreCase("avatar")) {
							// Log.i("miApp",parser.nextText());
							avatarName = pathToUsuarios + parser.nextText();
							
							drawable = getAvatar(avatarName);
						}
						// if <puntuacion>
						else if (tagName.equalsIgnoreCase("puntuacion")) {
							// Log.i("miApp",parser.nextText());
							puntuacionValue = parser.nextText();
							
							//Llamar al metodo progressUpdate y le pase al adapter el puntos usuario
							publishProgress(new PuntosUsuario(nicknameValue, drawable, puntuacionValue));
							
						}
		
						break;
					}
					// jump to next event
					eventType = parser.next();
				}
			}
			
			private Drawable getAvatar(String avatarUrl) {
				Bitmap bitmap = null;
				Drawable imageD = null;
				
				if (avatarUrl != null && avatarUrl.length() > 0) {
					URL image;				
					try {
						image = new URL(avatarUrl);
						Log.d(Constants.DEBUG_TAG, "avatarUrl: " + avatarUrl);
		
						// Parche para la 2.x
		//				BufferedInputStream stream = new BufferedInputStream(image.openStream());
						
						//CLASE PARCHE PARA 2.X
						FlushedInputStream stream = new FlushedInputStream(image.openStream());
						
						bitmap = BitmapFactory.decodeStream(stream);					
		
						if (bitmap == null) {
							Log.w(Constants.DEBUG_TAG, "Failed to decode image");
							imageD = getResources().getDrawable(R.drawable.no_avatar);
						}
						else {
							imageD = new BitmapDrawable(getResources(), bitmap);
						}
					}
					catch (Exception e) {
						Log.i(Constants.DEBUG_TAG, "Problemas con imagen", e);
						imageD = getResources().getDrawable(R.drawable.no_avatar);
					}
				}
				else {
					imageD = getResources().getDrawable(R.drawable.no_avatar);
				}
				return imageD;
			}
		    
		}
		
		
		 //Esto es un parche PARA QUE FUNCIONE BIEN EL LA LISTA
	    static class FlushedInputStream extends FilterInputStream { // http://code.google.com/p/android/issues/detail?id=6066
	        public FlushedInputStream(InputStream inputStream) {
	            super(inputStream);
	        }

	        @Override
	        public long skip(long n) throws IOException {
	            long totalBytesSkipped = 0L;
	            while (totalBytesSkipped < n) {
	                long bytesSkipped = in.skip(n - totalBytesSkipped);
	                if (bytesSkipped == 0L) {
	                      int Byte = read();
	                      if (Byte < 0) {
	                          break;  // we reached EOF
	                      } else {
	                          bytesSkipped = 1; // we read one byte
	                      }
	               }
	                totalBytesSkipped += bytesSkipped;
	            }
	            return totalBytesSkipped;
	        }
	    }

}
