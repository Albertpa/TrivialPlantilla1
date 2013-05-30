package com.aprendeandroid.trivialandroid1;

import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.Time;
import android.widget.DatePicker;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

	private DatePickerListener listener;
	
	private long date = -1; //Aqui se guarda lo que viene en el Bundle
	
	
	
	@Override
	public void onAttach(Activity activity) {
		
		 super.onAttach(activity);
	        // Verify that the host activity implements the callback interface
	        try {
	            // Instantiate the DatePickerListener so we can send events to the host
	            listener = (DatePickerListener) activity; //esta linea es obligatoria
	        } catch (ClassCastException e) {
	            // The activity doesn't implement the interface, throw exception
	            throw new ClassCastException(activity.toString() + " must implement DatePickerListener");
	        }
		
		super.onAttach(activity);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle parametros = getArguments();//Aqui recojemos el Bundle
		
		if(parametros != null){//Si no viene vacio lo leemos
			date = parametros.getLong("date",-1);
		}
	
	}
	
	//Aqui tenemos la seguridad de que esta todo creado, es el último paso del ciclo de vida y 
	//aqui podemos modificar el DatePickerDialog
			@Override
			public void onResume() {  
				
				super.onResume();
				
				Dialog dialog = getDialog(); //Cojemos el dialogo como clase base Dialog

				DatePickerDialog dateDialog = (DatePickerDialog) dialog; //aqui le hacemos casting a DatePickerDialog
				
				int iDay, iMonth, iYear;
				
				long msBirthDate = date; //le ponemos la fecha del Bundle, es -1 si no se establecio
				
				// Check if date exists
				if (msBirthDate > -1) { //Le ponemos la que viene en el Bundle (date)
					Time dateOfBirth = new Time();
					dateOfBirth.set(msBirthDate);

					iDay = dateOfBirth.monthDay;
					iMonth = dateOfBirth.month;
					iYear = dateOfBirth.year;
				} 
				else {
					Calendar cal = Calendar.getInstance(); //Sino, le ponemos la de hoy
					// Today's date fields
					iDay = cal.get(Calendar.DAY_OF_MONTH);
					iMonth = cal.get(Calendar.MONTH);
					iYear = cal.get(Calendar.YEAR);
				}

				dateDialog.updateDate(iYear, iMonth, iDay); //Actualizamos el DatePickerDialog con lo que haya ocurrido
			}
	
	
	
	
	
	
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		
		//Creamos el dialogo con una fecha inicial (la de hoy)
		
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		
		//OnDateSetListener prueba = null;//para probar, sera el callback
		
		return new DatePickerDialog(getActivity(), this, year, month, day);
		
	}


	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		Time dateOfBirth = new Time();
		dateOfBirth.set(dayOfMonth, monthOfYear, year);
		long dtDob = dateOfBirth.toMillis(true);
		
		listener.fechaEstablecida(dtDob);
	}
	
	//Interface comunicar esta activity con SettingsActivity
	public static interface DatePickerListener{		
		public void fechaEstablecida(long milis);
	}
	

}
