package com.aprendeandroid.trivialandroid1;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class GameActivity extends FragmentActivity {
////////////////Si es FragmentActivity el ProgressBar se ve correctamente
	//Elementos de la vista
	final private int textViewRespuestaId[] = { R.id.TextViewRespuestaA,
			R.id.TextViewRespuestaB, R.id.TextViewRespuestaC,
			R.id.TextViewRespuestaD };
	final private int shapes[] = {R.drawable.rectangle_azul, R.drawable.rectangle_naranja, 
			R.drawable.rectangle_verde, R.drawable.rectangle_morado};	
	
	//Cronometro
	private ImageSwitcher imageSwitcher;
	private Chronometer reloj;
	//public TextView tv; // textview to display the countdown
	private long timeWhenStopped = 0; //se guarda el tiempo para poder recuperarlo
	
	//Timer para la actividad autoevaluable
	Timer t;
	long startTime = 0;
	long endTime = 0;
	
	
	private ProgressBar progressBar= null;
	
	
	private List<Pregunta> listaPreguntas = new ArrayList<Pregunta>();
	private int numPreguntaActual = 0;
	
	final public static int NUM_RESPUESTAS = 4;
	final private String respuestasId[] = {"respuestaA", "respuestaB", "respuestaC", "respuestaD"}; // en el XML
	
	
	private TextView campoSeleccionado;	//cual de los elementos se ha seleccionado al responder

	private int puntuacion = 0;
	final private int incrementoPuntuacion = 10; //puntos por cada pregunta
	
	private SharedPreferences mGameSettings;
	
	//Para la pausa, hilos para la pausa
	Handler handler = null; // deben ser atributo para poder pararlos en onPause()
	Runnable runnable; // si no se intenta mostrar el dialogo de acabar cuando la Activity esta finalizando
	//PAra el hilo online
	DownloadTask downloadTask;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.game);
		
		//CRONOMETRO HACIA ATRAS
		/*
			tv = (TextView) findViewById(R.id.cronometroAtras);		
	         // 10000 is the starting number (in milliseconds)
	        // 1000 is the number to count down each time (in milliseconds)

	        MyCount counter = new MyCount(10000, 1000);
	        counter.start();
	     */
		//FIN CRONOMETRO HACIA ATRAS
		
		
		//Cogemos las preferencias
		mGameSettings = getSharedPreferences(Constants.GAME_PREFERENCES, Context.MODE_PRIVATE);

		// Magia del Scroll
		//Pillamos el campo de la pregunta y le aplicamos un scroll por programacion
		TextView tvp = (TextView) findViewById(R.id.TextViewPregunta);
		tvp.setMovementMethod(ScrollingMovementMethod.getInstance()); // necesario para scroll

		// ImageSwitcher llama a la clase interna MyImageSwitcherFactory está al
		// final
		imageSwitcher = (ImageSwitcher) findViewById(R.id.ImageSwitcherPregunta);
		imageSwitcher.setFactory(new MyImageSwitcherFactory());
		
		// //PREPARAMOS UN  PROGRESSBAR SPINNER PARA MOSTRARLO
		// DURANTE LAS DESCARGAS DE LAS PREGUNTAS		
		progressBar = (ProgressBar) findViewById(R.id.progressBarPregunta);
		
		// cronometro
		reloj = (Chronometer) findViewById(R.id.cronometro);
		
		//Recogemos los valores por si se ha hecho pausa
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_SCORE)) {
			puntuacion = mGameSettings.getInt(Constants.GAME_PREFERENCES_SCORE, 0);
		}
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_CURRENT_QUESTION)) {
			numPreguntaActual = mGameSettings.getInt(Constants.GAME_PREFERENCES_CURRENT_QUESTION, 0);
		}
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_TIME)) {
			timeWhenStopped = mGameSettings.getLong(Constants.GAME_PREFERENCES_TIME, 0);
		}

		
		// ---------------------------------------LECTURA XML MOCK ANTES DE HACERLO ONLINE
		/*
		try {
			cargaRecorreXMLMock(); 

			// Empieza la muestra de preguntas
			if (listaPreguntas.size() > 0 && numPreguntaActual < listaPreguntas.size()) {
				// Actualizamos el contador de preguntas

					setContador(numPreguntaActual + 1);	// arrays empiezan en 0 y el contador en 1			
					muestraPreguntas(numPreguntaActual);
			}
			else {
				imageSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.noquestion));
			}
		} catch (Exception e) {
			Log.e(Constants.DEBUG_TAG, "Failed to load questions", e);
			imageSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.noquestion));
		}
		*/
		downloadTask = new DownloadTask();
		downloadTask.execute();
		
	}
	
	//onCreatePanelMenu 

	
	
	//---------------------------------------------HILO PARA DESCARGAR PREGUNTAS

	


		private class DownloadTask extends AsyncTask<Void, Void, Boolean> {
			
			@Override
			protected void onPreExecute() {
				progressBar.setVisibility(View.VISIBLE);
			}
			
			
			@Override
			protected Boolean doInBackground(Void... voids) {
				boolean result = false;
				XmlPullParser contenidoXML = null;//aqui el XML descargado

				DefaultHttpClient client = new DefaultHttpClient();
				
				HttpGet httpGet = new HttpGet(Constants.PATH_TO_USUARIOS + Constants.PHP_PREGUNTAS); //php devuelve XML con elementos

				try {
					HttpResponse execute = client.execute(httpGet);
					contenidoXML = XmlPullParserFactory.newInstance().newPullParser();
					InputStream content = execute.getEntity().getContent();
					contenidoXML.setInput(content, null); 

					cargaRecorreXML(contenidoXML); //Para version onLine con elementos (esta llamada forma parte del hilo, continuaria despues por el post de este runnable)				
					result = listaPreguntas.size() > 0 && numPreguntaActual < listaPreguntas.size();
				} 
				catch (Exception e) {

				}
				return result;
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				//Empieza la muestra de preguntas
				if (result) {					
					muestraPreguntas(numPreguntaActual);
				}
				else {
					Toast.makeText(GameActivity.this, getResources().getString(R.string.no_preguntas), Toast.LENGTH_SHORT).show();
					progressBar.setVisibility(View.GONE);
				}
			}
			
		}
	
		// --------------------------------------------CARGA DEL XML PARA VERSION
		//Puede estar dentro de la clase del nuevo hilo o fuera, tal y como esta ahora
		private void cargaRecorreXML(XmlPullParser parser) throws XmlPullParserException, IOException {

//			XmlPullParser parser = getResources().getXml(R.xml.bloque_preguntas);
			
			int eventType = -1;

			while (eventType != XmlResourceParser.END_DOCUMENT) {
				
				if (eventType == XmlResourceParser.START_TAG) {

					String strName = parser.getName();

					if (strName.equals("pregunta")) {
				
						String idValue = parser.getAttributeValue(null, "id");
						String imagenValue = parser.getAttributeValue(null, "imagen");
						String cuestionValue = parser.getAttributeValue(null, "cuestion");
						
						String respuestas[] = new String[NUM_RESPUESTAS];
						
						for(int i = 0; i < NUM_RESPUESTAS; i++) {
							respuestas[i] = parser.getAttributeValue(null, respuestasId[i]);
						}

						int correctaValue = Integer.parseInt(parser.getAttributeValue(null, "correcta")) - 1;

						listaPreguntas.add(new Pregunta(idValue, imagenValue,cuestionValue, respuestas, correctaValue));
					}
				}
				eventType = parser.next();
			}
		}
		
		
		// -------------------------------------------DISTRIBUCION DEL CONTENIDO DE LA PREGUNTA ACTUAL POR EL LAYOUT
		private void muestraPreguntas(int numPregunta) {
			
			final int numP = numPregunta;

			//averiguamos la pregunta actual
			final Pregunta pregunta = listaPreguntas.get(numPregunta);
			final String[] respuestas = pregunta.getRespuestas();
			
			
			
			
			// Runnable de la descarga de la imagen asociada a la pregunta actual
			/*
			 * El asyncTask es una clase que tiene un ciclo de vida más definido
			 * El runnable simplemente hace una tarea y avisa cuando acaba, no existe el progress update desde
			 * runnable
			 * 
			 */
			Runnable runnable = new Runnable() {
				final Handler handler = new Handler();
				
				public void run() {

					final Drawable image = getPreguntaImageDrawable(numP); // llamada a la tarea principal del hilo, es un método de la clase
					
					//aqui acaba la tarea principal
					handler.post(new Runnable() { 
						public void run() {
							
							if(campoSeleccionado != null) { // al principio es null
								campoSeleccionado.setBackgroundColor(Color.BLACK);
								campoSeleccionado.setTextColor(Color.WHITE);// y el color de la fuente en blanco
							}	
							
							//Ponemos el enunciado de la Pregunta
							TextView textViewPregunta = (TextView) findViewById(R.id.TextViewPregunta);
							textViewPregunta.scrollTo(0, 0); // para ir al inicio del campo, por si se ha hecho scroll en la pregunta anterior
							textViewPregunta.setText(pregunta.getCuestion());						
							
							//Ponemos las respuestas en sus campos correspondientes
							for (int i = 0; i < textViewRespuestaId.length; i++) {
								final TextView textViewRespuesta = (TextView) findViewById(textViewRespuestaId[i]);
								textViewRespuesta.setText(respuestas[i]);
							}
							

							// Habilitamos la pulsacion de las respuestas
							LinearLayout respLayout = (LinearLayout) findViewById(R.id.layoutRespuestas);
							for (int i = 0; i < respLayout.getChildCount(); i++) {
								respLayout.getChildAt(i).setEnabled(true);
							}	
							
							imageSwitcher.setImageDrawable(image); 
							
							//actualizamos el contador de preguntas del layout
							setContador(numPreguntaActual + 1);
							
							progressBar.setVisibility(View.GONE);
							
							//iniciamos de nuevo el cronometro
							reloj.setBase(SystemClock.elapsedRealtime() - timeWhenStopped);
							reloj.start();
						}
					});
				}
			};	
			
			
			// arranca el hilo
			new Thread(runnable).start();
		}
		
		//Esta funcion es llamada desde el hilo anterior y lo que hace es descargar la imagen
		private Drawable getPreguntaImageDrawable(int numPregunta) {
			Drawable image = null;
			URL imageUrl;
			try {
				// Create a Drawable by decoding a stream from a remote URL
				//imageUrl = new URL(Constants.PATH_TO_SERVER + getPreguntaImageUrl(numPregunta));
				imageUrl = new URL(Constants.PATH_TO_USUARIOS + getPreguntaImageUrl(numPregunta));
				//Log.e(Constants.DEBUG_TAG, "IMAGEN->"+imageUrl);
				
				InputStream stream = imageUrl.openStream();
				Bitmap bitmap = BitmapFactory.decodeStream(stream);
				
				if(bitmap != null) {
					image = new BitmapDrawable(getResources(), bitmap);
				}
				else {
					image = getResources().getDrawable(R.drawable.noquestion);
				}
			} 
			catch (Exception e) {
				Log.e(Constants.DEBUG_TAG, "Decoding Bitmap stream failed");
				image = getResources().getDrawable(R.drawable.noquestion);
			}
			return image;
		}	
		
		
		private String getPreguntaImageUrl(Integer numPregunta) {
			String url = null;
			Pregunta preguntaActual = (Pregunta) listaPreguntas.get(numPregunta);
			if (preguntaActual != null) {
				url = preguntaActual.getImagen();
			}
			return url;
		}
		
		
		
		
	// --------------------------------------------CARGA DEL XML PARA VERSION
		// MOCK
		/*
	private boolean cargaRecorreXMLMock() throws XmlPullParserException,
				IOException {

		XmlPullParser parser = getResources().getXml(R.xml.bloque_preguntas);
		boolean result = false;
		int eventType = -1;

		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_TAG) {

				String strName = parser.getName();

				if (strName.equals("pregunta")) {

					result = true;

					String idValue = parser.getAttributeValue(null, "id");
					String imagenValue = parser.getAttributeValue(null,	"imagen");
					String cuestionValue = parser.getAttributeValue(null,"cuestion");
						
					String respuestas[] = new String[NUM_RESPUESTAS];					
					for(int i = 0; i < NUM_RESPUESTAS; i++) {
							respuestas[i] = parser
									.getAttributeValue(null, respuestasId[i]);
					}

					//la decrementamos para tener el indice correcto del string anterior
					int correctaValue = Integer.parseInt(parser
								.getAttributeValue(null, "correcta")) - 1;

					listaPreguntas.add(new Pregunta(idValue, imagenValue,
								cuestionValue, respuestas, correctaValue));
				}
			}
			eventType = parser.next(); // para que lea el siguiente elemento o
											// el final del fichero
		}

		return result;
	}
	*/
	// -------------------------------------------DISTRIBUCION DEL CONTENIDO DE
	// LA PREGUNTA ACTUAL POR EL LAYOUT
	/*
	private void muestraPreguntas(int numPregunta) {

		if (numPreguntaActual >= listaPreguntas.size()) {
			Toast.makeText(this, getResources().getString(R.string.no_preguntas), Toast.LENGTH_SHORT).show();
			return;
		}
		//Si estamos dentro del rango de preguntas seguimos			
		// averiguamos la pregunta actual
		final Pregunta pregunta = listaPreguntas.get(numPregunta);
		final String[] respuestas = pregunta.getRespuestas();

		//Imagen asociada a la pregunta, la sacamos de Assets igual que los avatares
		Drawable drawable;
		try {
			String imagePath = getResources().getString(
					R.string.preguntas_assets_dir)
					+ "/" + pregunta.getImagen();
			drawable = Drawable.createFromStream(getAssets().open(imagePath),
					null);
		} catch (IOException e) {
			e.printStackTrace();
			drawable = getResources().getDrawable(R.drawable.noquestion);
		}
		imageSwitcher.setImageDrawable(drawable);//Le pasamos al imageSwitcher la imagen

		//ponemos el enunciado
		TextView textViewPregunta = (TextView) findViewById(R.id.TextViewPregunta);
		textViewPregunta.scrollTo(0, 0); // para ir al inicio del campo, por si
												// se ha hecho scroll en la pregunta
												// anterior
		textViewPregunta.setText(pregunta.getCuestion());

		//Ponemos las respuestas en sus campos correspondientes
		for (int i = 0; i < textViewRespuestaId.length; i++) {
			final TextView textViewRespuesta = (TextView) findViewById(textViewRespuestaId[i]);
			textViewRespuesta.setText(respuestas[i]);
		}

				

		// actualizamos el contador de preguntas del layout
		setContador(numPreguntaActual + 1);
			
		// Habilitamos la pulsación de las respuestas
		//En tiempo de carga estan deshabilitadas
		LinearLayout respLayout = (LinearLayout) findViewById(R.id.layoutRespuestas);
		for (int i = 0; i < respLayout.getChildCount(); i++) {
			respLayout.getChildAt(i).setEnabled(true);
		}		
		
			
		// iniciamos de nuevo el cronometro
		reloj.setBase(SystemClock.elapsedRealtime() - timeWhenStopped);
		reloj.start();

	}
	*/
	
	public void onRespuesta(View view) {		
		
		t = new Timer();
		startTime = System.currentTimeMillis();
		
		
		timeWhenStopped = SystemClock.elapsedRealtime() - reloj.getBase();
		reloj.stop();
		

		if (numPreguntaActual >= listaPreguntas.size()) {
			Toast.makeText(this, getResources().getString(R.string.no_preguntas), Toast.LENGTH_SHORT).show();
			return;
		}
		
		// averiguamos la respuesta correcta de la pregunta actual
		int correcta = listaPreguntas.get(numPreguntaActual).getCorrecta();

		boolean acierto = false; // preparamos el mensaje del toast para
									// informar al usuario de si la respuesta es
									// correcta o no
		
		numPreguntaActual++; // incrementamos para la siguiente pregunta

		int i = 0; // variable para el while
		int respuesta = -1;
		
		
		while (i < NUM_RESPUESTAS) { // Corregimos la respuesta del usuario
			if (view.getId() == textViewRespuestaId[i]) {
				respuesta = i;
				if (respuesta == correcta) {
					acierto = true;
					puntuacion += incrementoPuntuacion;
				}
				break;
			}
			i++;
		}		
		
		// Deshabilitamos la pulsación de las respuestas
		LinearLayout respLayout = (LinearLayout) findViewById(R.id.layoutRespuestas);
		respLayout.setEnabled(false);
		for (int j = 0; j < respLayout.getChildCount(); j++) {
			respLayout.getChildAt(j).setEnabled(false);
		}		

		// MARCAMOS VISUALMENTE EL CAMPO DE TEXTO SELECCIONADO POR EL USUARIO
		campoSeleccionado = (TextView) view;
		campoSeleccionado.setBackgroundColor(Color.WHITE);
		campoSeleccionado.setTextColor(Color.BLACK);

		showToastResultado(respuesta, acierto);

		// Creamos un retardo de un segundo para seÃ±alar la respuesta pulsada
		// e imitar el posible retardo de Internet para mostrar un progress
		// bar encima de la imagen
		progressBar.setVisibility(View.VISIBLE);

		// Forma de posponer la ejecución de estamentos
		// en el hilo de la GUI
		// Si p. ej. solo quisieramos actuar sobre un control podriamos usar
		// campoSeleccionado.postDelayed(Runnable action, delayMillis);
		// y en el Runnable.run() solo la líneas con campoSeleccionado

		//Hilo que va "dando vueltas" durante 2 segundos y medio, simulando delay de internet
		handler = new Handler();
		runnable = new Runnable() {
			public void run() {
				// acciones que se ejecutan tras los 2500 milisegundos lo que
				// representa 0.5 sg. mas que Toast.LENGTH_SHORT
				// colocamos de nuevo el campo de texto de la respuesta
				// seleccionada con el fondo negro
				campoSeleccionado.setBackgroundColor(Color.BLACK);
				campoSeleccionado.setTextColor(Color.WHITE);// y el color de la
															// fuente en blanco

				// comprobamos si esta era la ultima pregunta
				
				if (numPreguntaActual < listaPreguntas.size()) {// ------>la
																// partida
																// sigue, aun
																// quedan
																// preguntas
					muestraPreguntas(numPreguntaActual);
				} else {// ------>Acaba la partida si no hay mas preguntas
					acabaPartida();
				}
				
				progressBar.setVisibility(View.GONE);
				
							
				
			}
		};
		/*
		//Llamada fuera del postDelayed, para la actividad autoevaluable
		if (numPreguntaActual < listaPreguntas.size()) {// ------>la
			// partida
			// sigue, aun
			// quedan
			// preguntas
			muestraPreguntas(numPreguntaActual);
		} else {// ------>Acaba la partida si no hay mas preguntas
			acabaPartida();
		}
		//tiempo en el que sale del muestra pregunta
		endTime = System.currentTimeMillis();
		long millisTotal = endTime- startTime;	
		
		Log.e(Constants.DEBUG_TAG, "Start Time actual->"+millisTotal);
		if(millisTotal < 2500){
			
		}else{
			handler.post(runnable);
		}	
		*/
		
		handler.postDelayed(runnable, 2500);

	}
	
	
	private void showToastResultado(int respuesta, boolean acierto) {
		
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_resultado,
				(ViewGroup) findViewById(R.id.toastResultadoLayout));
		ImageView viewResultado = (ImageView) layout.findViewById(R.id.iconoResultado);
		if (acierto) {
			viewResultado.setImageDrawable(getResources().getDrawable(R.drawable.acierto));
		}
		else {
			viewResultado.setImageDrawable(getResources().getDrawable(R.drawable.fallo));
		}
		layout.setBackgroundResource(shapes[respuesta]); 

		Toast toast = new Toast(getApplicationContext());
		toast.setView(layout);
		toast.show();		
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();		
		
		if(timeWhenStopped >= 0) { // para reconocer el reseteo desde acabaPartida()
			timeWhenStopped = SystemClock.elapsedRealtime() - reloj.getBase();
			reloj.stop();
		}
		else {
			timeWhenStopped = 0; // reseteo
		}
		
		//desactivar el hilo, si existe
		if(handler != null) {
			handler.removeCallbacks(runnable);
		}
		
		Editor editor = mGameSettings.edit();
		editor.putInt(Constants.GAME_PREFERENCES_SCORE, puntuacion);
		editor.putInt(Constants.GAME_PREFERENCES_CURRENT_QUESTION, numPreguntaActual);		
		editor.putLong(Constants.GAME_PREFERENCES_TIME, timeWhenStopped);
		editor.commit();
	}
	
	//------------------------------------------------------------ACABA LA PARTIDA, (se acabaron las preguntas)

		private void acabaPartida() {

			// MONTAMOS UN DIALOGO PARA MOSTRAR LA PUNTUACION FINAL (este dialogo carga un layout de res).
			
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.fin_partida_dialog, (ViewGroup) findViewById(R.id.layout_root));

			// aplico fuente de letra externa a los TextView del dialogo
			TextView tituloTextView = (TextView) layout.findViewById(R.id.titulo);
			Typeface font = Typeface.createFromAsset(getAssets(), "fonts/just.ttf");
			tituloTextView.setTypeface(font);

			TextView puntuacionTextView = (TextView) layout .findViewById(R.id.puntos);
			puntuacionTextView.setTypeface(font);
			puntuacionTextView.setText("" + puntuacion);

			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setView(layout);

			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							
							if (puntuacion >= incrementoPuntuacion * listaPreguntas.size() / 2) {
								subePuntuacion(); // llamamos a esta funcion para
													// que
													// suba la puntuación al
													// servidor
								puntuacion = 0;
								numPreguntaActual = 0;
								timeWhenStopped = -1; // que para onPause() no lo tome del reloj							
							} 
							else {
//								puntuacion = 0;
//								numPreguntaActual = 0;
//								timeWhenStopped = -1; // que para onPause() no lo tome del reloj
							}
							
							GameActivity.this.finish(); // llamamos a cerrar la activity TrivialGameActivity
						}
					});

			// Mostramos el alertdialog
			final AlertDialog alertDialog = builder.create(); // esta linea al final, muy importante
			alertDialog.show();
		}

		
		//-------------------------------------------------------------------------------------------------SUBIMOS LA PUNTUACIï¿½N AL SERVIDOR (si el usuario existe)  ---> ï¿½posible servicio?
			private void subePuntuacion(){ 
				SharedPreferences mGameSettings = getSharedPreferences(Constants.GAME_PREFERENCES,Context.MODE_PRIVATE); 
				final int userId = mGameSettings.getInt(Constants.GAME_PREFERENCES_ID, -1);
				
				if(userId == -1) {
					return;
				}
				
				Intent uploadService = new Intent(getApplicationContext(),UploadScoreService.class);
						uploadService.putExtra("id", userId);
						uploadService.putExtra("score", puntuacion);
						uploadService.putExtra("tiempo", timeWhenStopped);			
				startService(uploadService);
			}
		
	
	/*
	private void acabaPartida() {

		 //subePuntuacion(); //llamamos a esta funcion para que suba la
		// puntuación al servidor

		// MONTAMOS UN DIALOGO PARA MOSTRAR LA PUNTUACIï¿½N FINAL (este dialogo
		// carga un layout de res).
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.fin_partida_dialog,
				(ViewGroup) findViewById(R.id.layout_root));

		// aplico fuente de letra externa a los TextView del dialogo
		TextView tituloTextView = (TextView) layout.findViewById(R.id.titulo);
		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/just.ttf");
		tituloTextView.setTypeface(font);

		TextView puntuacionTextView = (TextView) layout
				.findViewById(R.id.puntos);
		puntuacionTextView.setTypeface(font);
		puntuacionTextView.setText("" + puntuacion);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setView(layout);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if(puntuacion == incrementoPuntuacion * listaPreguntas.size()){
							subePuntuacion(); // llamamos a esta funcion para que
											// suba la puntuación al servidor
						}
						else {
							puntuacion = 0;
							numPreguntaActual = 0;
							timeWhenStopped = -1; // que para onPause() no lo tome del reloj
						}
						// alertDialog.dismiss(); //quitamos este dialogo
						GameActivity.this.finish(); // llamamos a cerrar la
													// activity
													// TrivialGameActivity
					}
				});

		// Mostramos el alertdialog
		final AlertDialog alertDialog = builder.create(); // esta linea al
															// final, muy
															// importante
		alertDialog.show();
	}
	
	
	private void subePuntuacion() {		
		Toast.makeText(this, "TODO: Sube puntuación", Toast.LENGTH_SHORT)
				.show();

	}
		
	

			
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Handle item selection
		
		switch(item.getItemId()){
		case R.id.menu_settings:
				startActivity(new Intent(GameActivity.this, HelpActivity.class));
				return true;
			
			
		case R.id.menu_salir:
				GameActivity.this.finish();
				return true;		
			
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	*/
	
	// -----------------------------------------------------METODOS HELPER
	private void setContador(int value) {
		TextView contadorPreguntas = (TextView) findViewById(R.id.contadorPreguntas);
		String sPreg = String.valueOf(value);
		sPreg = String.format("%2s", sPreg).replace(' ', '0'); // relleno con dos 0
																// a la izq.
		String sTot = String.valueOf(listaPreguntas.size());
		sTot = String.format("%2s", sTot).replace(' ', '0'); // con %-2s seria a
																 // la dch.
		contadorPreguntas.setText(sPreg + "/" + sTot);
	}

	// adaptamos el ImageSwitcher para recibir imagenes
	//gestor que configura cada imagen
		
	private class MyImageSwitcherFactory implements ViewSwitcher.ViewFactory {
		public View makeView() {
			ImageView imageView = (ImageView) LayoutInflater.from(
					getApplicationContext()).inflate(
					R.layout.image_switcher_view, imageSwitcher, false);
			return imageView;
		}
	}

	
	// countdowntimer is an abstract class, so extend it and fill in methods
	/*
    public class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            tv.setText("done!");
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tv.setText("Left: " + millisUntilFinished / 1000);
        }
    }
    */
	
	//Se deberia llamar esta funcion en el menu de opciones, pero en este caso no existe titulo, he creido conveniente crear un botón para ello
	//Aún asi tengo que mirar como generar un menú de opciones con FragmentActivity, ya que no parece tan obvio como en una Activity normal
	public void resetValues(View v){
		//resetea los valores y reinicia el juego
		Log.e(Constants.DEBUG_TAG, "Reinicia valores");
		puntuacion = 0;
		numPreguntaActual = 0;
		timeWhenStopped = -1; // que para onPause() no lo tome del reloj	
		//no subimos la puntuación en este caso
		GameActivity.this.finish(); // llamamos a cerrar la activity TrivialGameActivity
	}
	
	
	//Para la actividad autoevaluable
	//tells handler to send a message
	/*
	   class firstTask extends TimerTask {

	        @Override
	        public void run() {
	            h.sendEmptyMessage(0);
	        }
	   };
	*/
}
