package com.aprendeandroid.trivialandroid1;


import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aprendeandroid.trivialandroid1.AvataresDialogFragment.AvatarListener;
import com.aprendeandroid.trivialandroid1.DatePickerFragment.DatePickerListener;
import com.aprendeandroid.trivialandroid1.PasswordDialogFragment.PasswordListener;



public class SettingsActivity extends FragmentActivity implements DatePickerListener, PasswordListener, AvatarListener{

	private String[] avataresLista;
	
	//datos del usuario para guardar en preferencias
	private int userId = -1;
	private String strNickname = null;
	private String strEmail = null;
	private Long dayOfBirth = 0l;
	private String strPassword = "noSet";
	private int gender = 0;
	private String strAvatar = "0";
	
	private SharedPreferences mGameSettings;
	
	
////--CICLO DE VIDA DE LA ACTIVITY ----////
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings);
		
		//Manager para guardar los datos de settings
		mGameSettings = getSharedPreferences(Constants.GAME_PREFERENCES, Context.MODE_PRIVATE);
		
		initNicknameEntry();
		initEmailEntry();
		initPasswordChooser();
		initDatePicker();
		initAvatar();
		initGenderSpinner();
	}
	
	
	
	//a parte de guardarlo cuando se le de al botón, es bueno saber que se puede guardar
	//en cualquier momento que, del ciclo de vida, se haga un onPause(): llamada de telefono,
	//darle a back, giramos el telefono,......
	@Override
	protected void onPause() {
		guardarPreferences();
		super.onPause();
	}

////--INICIALIZADORES DE WIDGETS SEGUN DATOS DE TRIVIAL PREFERENCES ----////
	
	private void initNicknameEntry() {
		EditText nicknameText = (EditText) findViewById(R.id.nicknameEditText);
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_NICKNAME)) {
			strNickname = mGameSettings.getString(Constants.GAME_PREFERENCES_NICKNAME, "");
			nicknameText.setText(strNickname);
		}
	}

	private void initEmailEntry() {
		EditText emailText = (EditText) findViewById(R.id.emailEditText);
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_EMAIL)) {
			strEmail = mGameSettings.getString(Constants.GAME_PREFERENCES_EMAIL, "");
			emailText.setText(strEmail);
		}
	}
	
	private void initPasswordChooser() {
		// Set password info
		TextView passwordInfo = (TextView) findViewById(R.id.passwordTextView);
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_PASSWORD)) {
			passwordInfo.setText(R.string.settings_pwd_set);
			strPassword = mGameSettings.getString(Constants.GAME_PREFERENCES_PASSWORD, ""); 
		} 
		else {
			passwordInfo.setText(R.string.settings_pwd_not_set);
		}
	}
	
	private void initDatePicker() {
		// Set password info
		TextView dobInfo = (TextView) findViewById(R.id.dayOfBirthTextView);
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_DOB)) {
			dobInfo.setText(DateFormat.format("MMMM dd, yyyy", mGameSettings.getLong(Constants.GAME_PREFERENCES_DOB, 0)));
			dayOfBirth = mGameSettings.getLong(Constants.GAME_PREFERENCES_DOB, 0);
		} else {
			dobInfo.setText(R.string.settings_dob_not_set);
		}
	}
	
	private void initAvatar(){
		AssetManager manager = getResources().getAssets();
		String avataresPath = getResources().getString(R.string.avatar_assets_dir);
		try {
			avataresLista = manager.list(avataresPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_AVATAR)) {
			String avatarSeleccionado = mGameSettings.getString(Constants.GAME_PREFERENCES_AVATAR,"");
			
			ImageView imgAvatar = (ImageView) findViewById(R.id.ImageViewAvatar);
			Drawable drawable;
			
			try {
				drawable = Drawable.createFromStream(getAssets().open(avataresPath + "/" + avatarSeleccionado), null);
			} 
			catch (IOException e) {
				e.printStackTrace();
				drawable = getResources().getDrawable(R.drawable.no_avatar);
			}
			
			imgAvatar.setImageDrawable(drawable);
		}
	}
	
	
	private void initGenderSpinner() {
		// Populate Spinner control with genders
		final Spinner spinner = (Spinner) findViewById(R.id.Spinner_Gender);
		
		ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.genders , android.R.layout.simple_spinner_item);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_GENDER)) {
			gender = mGameSettings.getInt(Constants.GAME_PREFERENCES_GENDER,0);
			spinner.setSelection(gender);
		}
		
		
		// Handle spinner selections
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			
			public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
				gender = selectedItemPosition;
			}

			public void onNothingSelected(AdapterView<?> parent) {}
		});
	}
	
	

////--Lanzadores de DIALOGOS DESDE LA VISTA ----////
	
	public void onPickDateButtonClick(View v){
		DialogFragment newFragment = new DatePickerFragment();
		
		long milis = -1;		
		if (mGameSettings.contains(Constants.GAME_PREFERENCES_DOB)) {
			milis = mGameSettings.getLong(Constants.GAME_PREFERENCES_DOB, 0);
		}
		
		Bundle parametros = new Bundle();
		parametros.putLong("date", milis); 
		
		newFragment.setArguments(parametros);	
		
		
		newFragment.show(getSupportFragmentManager(), "datePicker");
	}
	
	public void onSetPasswordButtonClick(View v){
		DialogFragment newFragment = new PasswordDialogFragment();
		newFragment.show(getSupportFragmentManager(), "password");
	}

	public void seleccionaAvatar(View v){
		DialogFragment newFragment = new AvataresDialogFragment();		
		
		//Preparamos la lista de assets (esta aqui solo para la explicacion 1)
		/*
		AssetManager manager = getResources().getAssets();
		String avataresPath = getResources().getString(R.string.avatar_assets_dir);
		
		try {
			avataresLista = manager.list(avataresPath);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		Bundle parametros = new Bundle();
		parametros.putString("avataresPath", getResources().getString(R.string.avatar_assets_dir));
		parametros.putStringArray("avatares", avataresLista);
		newFragment.setArguments(parametros);
	
		newFragment.show(getSupportFragmentManager(), "avatar");
	}
	
	
	
	
////--Interfaces para los DIALOGOS ----////
	
	@Override
	public void fechaEstablecida(long milis) {
		final TextView dob = (TextView) findViewById(R.id.dayOfBirthTextView);
		dob.setText(DateFormat.format("MMMM dd, yyyy", milis));
		dayOfBirth = milis;
		Editor editor = mGameSettings.edit();
		editor.putLong(Constants.GAME_PREFERENCES_DOB, dayOfBirth);
		editor.commit();
	}

	@Override
	public void passwordEstablecido(String password) {		
		final TextView passwordInfo = (TextView) findViewById(R.id.passwordTextView);
		passwordInfo.setText(R.string.settings_pwd_set);
		strPassword = password;
		if(!strPassword.trim().equals("")) {
			Editor editor = mGameSettings.edit();
			editor.putString(Constants.GAME_PREFERENCES_PASSWORD, strPassword);
			editor.commit();
		}
	}

	@Override
	public void avatarEstablecido(String avatarSeleccionado) {		
		strAvatar = avatarSeleccionado;
		ImageView imgAvatar = (ImageView) findViewById(R.id.ImageViewAvatar);
		Drawable img;
		try {
			img = Drawable.createFromStream(getAssets().open(getResources().getString(R.string.avatar_assets_dir)+"/"+ avatarSeleccionado), null);
			
			Editor editor = mGameSettings.edit();
			editor.putString(Constants.GAME_PREFERENCES_AVATAR, avatarSeleccionado);
			editor.commit();
		} catch (IOException e) {
			img = getResources().getDrawable(R.drawable.no_avatar);
		}
		imgAvatar.setImageDrawable(img);
	}
	
	
////--GESTION DE PREFERENCIAS ----////
	
private void guardarPreferences() {

		// se capturan aqui, solo es leer los editText, no tienen metodos
		// propios en esta clase
		EditText nicknameText = (EditText) findViewById(R.id.nicknameEditText);
		EditText emailText = (EditText) findViewById(R.id.emailEditText);

		strNickname = nicknameText.getText().toString();
		strEmail = emailText.getText().toString();
		
		
		Editor editor = mGameSettings.edit();
		
				editor.putString(Constants.GAME_PREFERENCES_NICKNAME, strNickname);
				editor.putString(Constants.GAME_PREFERENCES_EMAIL, strEmail);
				
				//editor.putString(Constants.GAME_PREFERENCES_PASSWORD, strPassword);
				//editor.putLong(Constants.GAME_PREFERENCES_DOB, dayOfBirth);
				editor.putInt(Constants.GAME_PREFERENCES_GENDER, gender);
				//editor.putString(Constants.GAME_PREFERENCES_AVATAR, strAvatar);

		//Se cierra la edicion y guarda el archivo
		editor.commit();
	}
	


////////////////--------------------------------------------------------LLAMADAS A METODOS DESDE LA VISTA---------------------------------------------------/////////////	
	public void registro(View v) {
		subeUsuario();
		if (userId == -1) { // si es nuevo
			finish(); // hay que dejar que el server devuelva el id y leerlo
		}
	}
	
	
	
	
	////////////////--------------------------------------------------------CONEXION CON EL SERVIDOR PARA ADMINISTRACION DE USUARIOS---------------------------------------------------/////////////
	
	// Consulta si el usuario existe (llamada desde el boton registro del Layout)
	public void subeUsuario() {
		EditText nicknameText = (EditText) findViewById(R.id.nicknameEditText);
		strNickname = nicknameText.getText().toString().trim();
		
		if (strNickname.equals("")) {
			Toast.makeText(this, "Nickname no puede estar vacío",Toast.LENGTH_LONG).show();
			return;
		}
		
		guardarPreferences();
		
		//lanzamos un uploadService
		Intent uploadService = new Intent(getApplicationContext(),UploadUserService.class);
		
		uploadService.putExtra("id", userId);
		uploadService.putExtra("nickname", strNickname);
		uploadService.putExtra("mail", strEmail);
		uploadService.putExtra("dob", dayOfBirth);
		uploadService.putExtra("pass", strPassword);
		uploadService.putExtra("genero", gender);
		uploadService.putExtra("avatar", strAvatar);
		
		startService(uploadService);
	}
	
	
}
