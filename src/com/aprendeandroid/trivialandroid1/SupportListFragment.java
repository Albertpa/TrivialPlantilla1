package com.aprendeandroid.trivialandroid1;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


//No existe ListFragmentActivity(support), este Fragment hace que la activity que lo use actue como una ListActivity y tenga support 
public class SupportListFragment extends ListFragment {
	
	private ListItemSelectedListener selectedListener;
	
	private int index = 0;
	
	private int listLayoutId = 0;
	private int emptyViewId = 0;

	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			selectedListener = (ListItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()+ " must implement ListItemSelectedListener in Activity");
		}
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	
		
		Bundle parametros = getArguments(); //Aqui vienen los parametro de este Layout
		if(parametros != null) {
			listLayoutId = parametros.getInt("listLayoutId");
			emptyViewId = parametros.getInt("emptyViewId");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		View myFragmentView  = null;
		try { // layout personalizado
			myFragmentView = inflater.inflate(listLayoutId,container, false);
		} 
		catch (Exception e) { // layout por defecto
			myFragmentView = inflater.inflate(android.R.layout.list_content,container, false);		
		}
		
//		Parche para la 2.x --> ver guia.
		View standardEmptyView = myFragmentView.findViewById(android.R.id.empty);
		if(standardEmptyView != null) {
			standardEmptyView.setId(0);
		}
		return myFragmentView;
	}

	
	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		ListView listView = getListView();
		View emptyView = getActivity().findViewById(emptyViewId);
		if(emptyView != null) {
			listView.setEmptyView(emptyView);
		}
		registerForContextMenu(listView);
	}	

	
	
	
	//Al salir guarda el item seleccionado
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("index", index);
	}
	
	
	
	//Al crearse, si hemos guardado el item por el que nos quedamos, lo deja marcado
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState != null) {
			index = savedInstanceState.getInt("index", 0);
			selectedListener.onListItemSelected(index);
		}	
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		index = position;
		selectedListener.onListItemSelected(index);
	}
	

	//INTERFACE
	public interface ListItemSelectedListener {
		public void onListItemSelected(int index);
	}

}
