package com.aprendeandroid.trivialandroid1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		//Quitar la barra ANTES del setContentView
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.menu);
		
		ListView menuList = (ListView) findViewById(R.id.ListViewMenu);
		
		String[] items = { getResources().getString(R.string.menuGame),
							getResources().getString(R.string.menuScores),
							getResources().getString(R.string.menuSettings),
							getResources().getString(R.string.menuSubir),
							getResources().getString(R.string.menuHelp)							
				};
		
		//adaptador generico: adapta la array items con el aspecto R.layout.menu_item  en este contexto (this)
		ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, R.layout.menu_item, items);
		menuList.setAdapter(adapt);
		
		menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
				// arg0 -> parent, arg1-> itemCliked, arg2-> position, arg3->id o puntero del objeto pulsado
				
				TextView textViewPulsado = (TextView) itemClicked;
				String strText = textViewPulsado.getText().toString();
				
				if( strText.equalsIgnoreCase(getResources().getString(R.string.menuGame)) ){
					startActivity(new Intent(MenuActivity.this, GameActivity.class));
				}
				else if( strText.equalsIgnoreCase(getResources().getString(R.string.menuSettings)) ){
					startActivity(new Intent(MenuActivity.this, SettingsActivity.class));
				}
				else if( strText.equalsIgnoreCase(getResources().getString(R.string.menuScores)) ){
					startActivity(new Intent(MenuActivity.this, ScoresActivity.class));
				}
				else if( strText.equalsIgnoreCase(getResources().getString(R.string.menuHelp)) ){
					startActivity(new Intent(MenuActivity.this, HelpActivity.class));
				}
				else if( strText.equalsIgnoreCase(getResources().getString(R.string.menuSubir)) ){
					startActivity(new Intent(MenuActivity.this, UpPreguntaActivity.class));
				}
			}
		});
	}
	
	
	
	
	
	
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

}
