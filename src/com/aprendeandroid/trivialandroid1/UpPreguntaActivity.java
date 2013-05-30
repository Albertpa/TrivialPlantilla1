package com.aprendeandroid.trivialandroid1;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class UpPreguntaActivity extends Activity {
	
	private final int SELECT_PHOTO = 2;
	//private String image_path = null;
	
	//para el control de lineas
	//private Rect mRect;
	
	
	private int preguntaId = -1;
	private String nickName = null;
	private String pregunta = null;
	private String resA = null;
	private String resB = null;
	private String resC = null;
	private String resD = null;	
	private int correctAn = 1;	
	private Uri imagenAsociada = null;
	private String imagenPath = null;	

	private SharedPreferences mGameSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_up_pregunta);
		
		mGameSettings = getSharedPreferences(Constants.GAME_PREFERENCES, Context.MODE_PRIVATE);
	
		/*
		mRect = new Rect();
		addTextChangedListener(new TextWatcher() {

			CharSequence prevSeq;
			int finStart, finEnd;
			boolean textOutBounds = false, changingText = false;

			@Override
			public void afterTextChanged(Editable s) {
				if (textOutBounds) {
					if (!changingText) {
						changingText = true;
						Editable text = getText();
						text.replace(finStart, finEnd, prevSeq);
						textOutBounds = false;
						changingText = false;
					}
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
				if (!changingText)
					prevSeq = s.subSequence(start, start count);
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (!changingText) {

					getLineBounds(getLineCount() - 1, mRect);

					if (mRect.bottom > getHeight() - mRect.height()) {
						// aqui se podria enviar aviso al usuario de fuera de limites
						finStart = start;
						finEnd = start count;
						textOutBounds = true;
					}
				}
			}
		});
		
		*/
		
	
		initPregunta();
		initRespuestas();
		initImagen();
		initCorrectoSpinner();
		
	}	
	
	//a parte de guardarlo cuando se le de al botón, es bueno saber que se puede guardar
	//en cualquier momento que, del ciclo de vida, se haga un onPause(): llamada de telefono,
	//darle a back, giramos el telefono,......
	@Override
	protected void onPause() {
			guardarPreferences();
			super.onPause();
	}
	
	
	///---Inicializadores segun preferences
	
	private void initPregunta() {
		EditText epregunta = (EditText) findViewById(R.id.editTextPregunta);
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_PREGUNTA)) {
			pregunta = mGameSettings.getString(Constants.GAME_PREFERENCES_PREGUNTA, "");
			epregunta.setText(pregunta);
		}
	}
	
	
	
	private void initRespuestas() {	
		
		EditText eresA = (EditText) findViewById(R.id.editTextRespuestaA);		
		
		EditText eresB = (EditText) findViewById(R.id.editTextRespuestaB);	
		
		EditText eresC = (EditText) findViewById(R.id.editTextRespuestaC);
		
		EditText eresD = (EditText) findViewById(R.id.editTextRespuestaD);
		
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_RESPUESTAA)) {
			resA = mGameSettings.getString(Constants.GAME_PREFERENCES_RESPUESTAA, "");
			eresA.setText(resA);
		}
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_RESPUESTAB)) {
			resB = mGameSettings.getString(Constants.GAME_PREFERENCES_RESPUESTAB, "");
			eresB.setText(resB);
		}
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_RESPUESTAC)) {
			resC = mGameSettings.getString(Constants.GAME_PREFERENCES_RESPUESTAC, "");
			eresC.setText(resC);
		}
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_RESPUESTAD)) {
			resD = mGameSettings.getString(Constants.GAME_PREFERENCES_RESPUESTAD, "");
			eresD.setText(resD);
		}
	}
	
	private void initImagen() {
				
		ImageView view = (ImageView) findViewById(R.id.ImageViewQuestion);
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_IMGPREGUNTA)) {
			imagenPath = mGameSettings.getString(Constants.GAME_PREFERENCES_IMGPREGUNTA, "");
			//Log.e(Constants.DEBUG_TAG, "Imagen->"+imagenPath+" ---  "+view.getHeight());
			
			//en este caso parece ser que la vista aun no esta disponible, por lo que el view.getHeight() devuelve 0
			//Bitmap galleryPic = scaleBitmap(imagenPath, view.getHeight());
			//En estos casos es mejor usar:
				//view.getLayoutParams().height;
				//view.getLayoutParams().width;
			Bitmap galleryPic = scaleBitmap(imagenPath, view.getLayoutParams().height);
			
			if(galleryPic != null){
				view.setImageBitmap(galleryPic);
				
			}else{
				Toast toast = Toast.makeText(this, "Imagen fallida", Toast.LENGTH_LONG);
				toast.show();
			}
		
		}
	}
	
	
	private void initCorrectoSpinner() {
		// Populate Spinner control with genders
		final Spinner spinner = (Spinner) findViewById(R.id.Spinner_Correct);
		
		ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.correct , android.R.layout.simple_spinner_item);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_RESPUESTAOK)) {
			correctAn = mGameSettings.getInt(Constants.GAME_PREFERENCES_RESPUESTAOK,0);
			spinner.setSelection(correctAn);
		}
		
		
		// Handle spinner selections
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			
			public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
				correctAn = selectedItemPosition;
			}

			public void onNothingSelected(AdapterView<?> parent) {}
		});
	}
	
	
	
	
	public void seleccionaImagen(View v){
		Intent i;
		i = new Intent(Intent.ACTION_PICK);
		i.setType("image/*");
		startActivityForResult(i, SELECT_PHOTO);
		
	}
	
	public void registro(View v){		
	
		//Aqui se tendran que subir los parametros guardados
		subePregunta();
		/*
		if(preguntaId == -1){//si es nueva
			finish(); //hay que dejar que el server devuelva el id y leerlo
		}
		*/
		finish();
		
		
		
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		
		//Si retorna un resultado correcto
		
		//Dependiendo de la actividad llamada que ha retornado el resultado
		switch(requestCode){			
			case SELECT_PHOTO:
				
				if(resultCode == RESULT_OK){
					ImageView view = (ImageView) findViewById(R.id.ImageViewQuestion);
					
					Uri imageUri = data.getData();
					
					//nos guardamos la uri de la imagen
					imagenAsociada = imageUri;
					imagenPath = getPathFromUri(imageUri);
					
					
					Bitmap galleryPic = scaleBitmap(getPathFromUri(imageUri), view.getHeight());
					
					if(galleryPic != null){
						view.setImageBitmap(galleryPic);
						
					}else{
						Toast toast = Toast.makeText(this, "Imagen fallida", Toast.LENGTH_LONG);
						toast.show();
					}
					
				}
				break;
			
		}
		
	}
	
	
	//---Gestion de preferencias
	private void guardarPreferences() {

		// se capturan aqui, solo es leer los editText, no tienen metodos
		// propios en esta clase

		EditText epregunta = (EditText) findViewById(R.id.editTextPregunta);
		pregunta = epregunta.getText().toString();
		
		EditText eresA = (EditText) findViewById(R.id.editTextRespuestaA);
		resA = eresA.getText().toString();
		
		EditText eresB = (EditText) findViewById(R.id.editTextRespuestaB);
		resB = eresB.getText().toString();
		
		EditText eresC = (EditText) findViewById(R.id.editTextRespuestaC);
		resC = eresC.getText().toString();
		
		EditText eresD = (EditText) findViewById(R.id.editTextRespuestaD);
		resD = eresD.getText().toString();
		
		
		Editor editor = mGameSettings.edit();
		
				editor.putString(Constants.GAME_PREFERENCES_PREGUNTA, pregunta);
				editor.putString(Constants.GAME_PREFERENCES_RESPUESTAA, resA);	
				editor.putString(Constants.GAME_PREFERENCES_RESPUESTAB, resB);	
				editor.putString(Constants.GAME_PREFERENCES_RESPUESTAC, resC);	
				editor.putString(Constants.GAME_PREFERENCES_RESPUESTAD, resD);	
				
				editor.putString(Constants.GAME_PREFERENCES_IMGPREGUNTA, imagenPath);
				
				editor.putInt(Constants.GAME_PREFERENCES_RESPUESTAOK, correctAn);
				

		//Se cierra la edicion y guarda el archivo
		editor.commit();
	}
	
	
	
	//----------------------------Metodos para la selección de imagenes
	/**
	 * Convierte una URI generada por el provider MediaStore (base de datos SQL de archivos media)
	 * p. ej. content://media/external/images/media/36
	 * en un path del sistema de ficheros, p. ej. /mnt/sdcard/DCIM/Camera/IMG_20121127_053546.jpg
	 * @param uri la URI
	 * @return el path
	 */
	private String getPathFromUri(Uri uri) { 
		String path = "";
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		try {
			int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			if (cursor.moveToFirst())
				path = cursor.getString(column_index);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		cursor.close();
		return path;
	}
	

		//metodo de rescalado
	private Bitmap scaleBitmap(String image_path, int maxDimension) {
		Bitmap scaledBitmap;
		
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true; // solo devuelve las dimensiones, no carga bitmap
		scaledBitmap = BitmapFactory.decodeFile(image_path, op); //en op est‡n las dimensiones

		// usamos Math.max porque es mejor que la imagen sea un poco mayor que el
		// control donde se muestra, que un poco menor. Ya que si es menor el control
		// la agranda para ajustarla y se podria pixelar un poco.
		if ((maxDimension < op.outHeight) || (maxDimension < op.outWidth)) {
			// cada dimensiÃ³n de la imagen se dividir por op.inSampleSize al cargar
			op.inSampleSize = Math.round(Math.max((float) op.outHeight / (float) maxDimension,(float) op.outWidth / (float) maxDimension)); //calculamos la proporcion de la escala para que no deforme la imagen y entre en las dimensiones fijadas en la vista
		}

		op.inJustDecodeBounds = false; // ponemos a false op...
		scaledBitmap = BitmapFactory.decodeFile(image_path, op); //...para que ya el bitmap se cargue realmente
		
		return scaledBitmap;
	}

	
	//---------------------------------CONEXION CON EL SERVIDOR PARA ADMINISTRACIÓN DE USUARIOS
	//consulta si la pregunta existe (llamada desde el boton registro del Layout)
	public void subePregunta(){	
		
		EditText epregunta = (EditText) findViewById(R.id.editTextPregunta);
		pregunta = epregunta.getText().toString();
		
		EditText eresA = (EditText) findViewById(R.id.editTextRespuestaA);
		resA = eresA.getText().toString();
		
		EditText eresB = (EditText) findViewById(R.id.editTextRespuestaB);
		resB = eresB.getText().toString();
		
		EditText eresC = (EditText) findViewById(R.id.editTextRespuestaC);
		resC = eresC.getText().toString();
		
		EditText eresD = (EditText) findViewById(R.id.editTextRespuestaD);
		resD = eresD.getText().toString();
		
		
		
		//si la pregunta o sus respuestas estan vacias, tenemos que advertirlo
		if (pregunta.equals("") || 
				resA.equals("") ||
				resB.equals("") ||
				resC.equals("") || 
				resD.equals("")) {
			Toast.makeText(this, "La pregunta y sus respuestas no pueden estar vacias",Toast.LENGTH_LONG).show();
			return;
		}
		
		//guardamos las preferencias
		guardarPreferences();
		
	
		//lanzamos un uploadService
		
		Intent uploadService = new Intent(getApplicationContext(),UploadQuestionService.class);
		
		//Deberia estar el nickname configurado correctamente en las preferencias
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_NICKNAME)) {			
			nickName = mGameSettings.getString(Constants.GAME_PREFERENCES_NICKNAME, "");		
		}		
		uploadService.putExtra("nickname", nickName);
		uploadService.putExtra("pregunta", pregunta);
		uploadService.putExtra("respuestaA", resA);
		uploadService.putExtra("respuestaB", resB);
		uploadService.putExtra("respuestaC", resC);
		uploadService.putExtra("respuestaD", resD);	
		//el servidor espera 1 posicion más: a->1 b->2 c->3 d->4
				//incrementar el correctAn++ antes de enviar
		int respuestaOK = correctAn + 1;
		
		uploadService.putExtra("respuestaOK", respuestaOK);
		uploadService.putExtra("imagenPath", imagenPath);
		
		//Log.e(Constants.DEBUG_TAG, "Pregunta->"+pregunta+"respuestas->" + resA+" - "+resB+" -"+ resC+" - "+resD +"correct->"+respuestaOK+"nickname->"+nickName);
		//Log.e(Constants.DEBUG_TAG, "Imagen->"+imagenPath);
		
		startService(uploadService);
		
		
	}
	
}
