package com.aprendeandroid.trivialandroid1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


public class PasswordDialogFragment extends DialogFragment {

	private PasswordListener listener;
	
	@Override
	public void onAttach(Activity activity) {		
		super.onAttach(activity);
		//Verifica que la activity host implemente el interfaz callback
		try{
			//Instantiate the PasswordListener so we can send events to the host
			listener = (PasswordListener) activity;
		}catch (ClassCastException e){
			//The activity doesn't implement the interface throw exception
			throw new ClassCastException(activity.toString()+" must implement PasswordListener");
		}
	}
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		//siempre necesitamos un inflater cuando queremos insertar un layout cuando
		//generamos una ventana con java
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		//archivo xml del dialog, objeto view->de la activity coger la root (el layout raiz)
		final View layout = inflater.inflate(R.layout.password_dialog, (ViewGroup) getActivity().findViewById(R.id.root));
		
		
		final EditText p1 = (EditText) layout.findViewById(R.id.EditText_Pwd1);
		final EditText p2 = (EditText) layout.findViewById(R.id.EditText_Pwd2);		
		final TextView error = (TextView) layout.findViewById(R.id.TextView_PwdProblem);
		
		
		p2.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				String strPass1 = p1.getText().toString();
				String strPass2 = p2.getText().toString();
				
				if(strPass1.equals(strPass2)){
					error.setText(R.string.settings_pwd_equals);
					error.setTextColor(getResources().getColor(R.color.ok_color));
				}else{
					error.setText(R.string.settings_pwd_not_equals);
					error.setTextColor(getResources().getColor(R.color.error_color));
				}
				
			}
		});
		
		
		
		
		
		AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
		builder.setView(layout);
		
		//Now configure the AlertDialog
		builder.setTitle("Password");
		
		
		//Con la siguiente estructura se monta el boton de cancelar, al clicar YA SE CERRARA SOLO
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});
		
		
		//Con la siguiente estructura se monta el boton de ok, al clicar YA SE CERRARA SOLO
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String strPassword1 = p1.getText().toString();
				String strPassword2 = p2.getText().toString();
				
				if(strPassword1.equals(strPassword2)){
					//Devolvemos el dato
					listener.passwordEstablecido(strPassword1);
					
				}else{
					Log.d("miApp", "No coinciden las passwords. No se salva. Se mantiene la password vieja (si esta generada).");
					error.setText(R.string.settings_pwd_not_equals);
				}
				
			}
		});
		
		
		//create the AlertDialog and return it
		AlertDialog passwordDialog = builder.create();
		return passwordDialog;
		
	}

	//Interface
	public static interface PasswordListener{
		public void passwordEstablecido(String password);
	}
	
	

}
