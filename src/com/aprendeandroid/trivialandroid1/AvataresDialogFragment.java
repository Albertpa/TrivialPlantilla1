package com.aprendeandroid.trivialandroid1;

import java.io.IOException;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;


public class AvataresDialogFragment extends DialogFragment {
	
	AvatarListener listener;
	
	
	//Vienen del Bundle
	String[] avatares;
	String avataresPath;
	
	Context context;
	
	private int numColumnas = 3;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		numColumnas = getResources().getInteger(R.integer.numColumnsAvatar);
		
		Bundle parametros = getArguments();
		
		if(parametros != null){
			avataresPath = parametros.getString("avataresPath");
			avatares = parametros.getStringArray("avatares");
			if(avataresPath == null){
				avataresPath = "";
				
			}
			if(avatares == null){
				String empty[] = {};
				avatares = empty;
			}
		}
		else{
			avataresPath = "assets";
			String empty[] = {};
			avatares= empty;
		}
		
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		   LayoutInflater inflater = getActivity().getLayoutInflater();

	        final View layout = inflater.inflate(R.layout.avatar_dialog, (ViewGroup) getActivity().findViewById(R.id.avatar_root));
	        
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setView(layout);

	        
	        // Now configure the AlertDialog
	        builder.setTitle("Selecciona Avatar");
	        
	        
	        
	        
	        TableLayout table = (TableLayout) layout.findViewById(R.id.tablaAvatares);
	        
	        int rows = avatares.length % numColumnas == 0 ? avatares.length / numColumnas : avatares.length/numColumnas + 1;              
	        
	        for(int i = 0; i < rows; i++) {
	        	
	        	final View layoutRow = inflater.inflate(R.layout.avatar_row, null);
	        	
	        	TableRow row = (TableRow) layoutRow.findViewById(R.id.TableRowFila);        	
	        	table.addView(row);
	    
	        	for(int j = 0; j < numColumnas; j++) {
	        		
	        		// ultima fila que puede no estar completa
	        		int colsUltimaRow = avatares.length % numColumnas == 0 ? numColumnas : avatares.length % numColumnas;
	        		
	        		if(i == rows - 1 && j >= colsUltimaRow){
	        			break;
	        		}
	        		
	                final View layoutIcon = inflater.inflate(R.layout.avatar_table_item, null);
	                ImageView imageView = (ImageView) layoutIcon.findViewById(R.id.avatarItem);
	                
	  
	        		//Leer drawable de Assets
	    			Drawable drawable;
	    			
	    			try {
	    				AssetManager manager = getActivity().getAssets();//manejador de Assets
	    				drawable = Drawable.createFromStream(manager.open(avataresPath + "/" + avatares[i*numColumnas + j]), null);
	    				imageView.setTag(avatares[i*numColumnas + j]);
	    			} 
	    			catch (IOException e) {
	    				e.printStackTrace();
	    				drawable = getResources().getDrawable(R.drawable.no_avatar);
	    				imageView.setTag("");
	    			}
	        		
	    			imageView.setImageDrawable(drawable);
	        		row.addView(imageView);
	        	}
	        }
	        	
	        
	        
	        
	        
	        for (int i = 0; i < table.getChildCount(); i++) {
	            TableRow row = (TableRow) table.getChildAt(i);
	            
	            for(int j = 0; j < row.getChildCount(); j++){
		            row.getChildAt(j).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							listener.avatarEstablecido(v.getTag().toString());
							
							//Como esta vez el boton no es implicito se ha de cerrar manualmente
							AvataresDialogFragment.this.getDialog().cancel();
						}
					});
	            }
	        }
	        
	        
	        
	        //BOTON DE CANCELAR
	        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {}
	        });       
	    
	        
	        // Create the AlertDialog and return it
	        AlertDialog passwordDialog = builder.create();
	        return passwordDialog;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the PasswordListener so we can send events to the host
            listener = (AvatarListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement AvatarListener");
        }
    }
	  
    //INTERFACE
	public static interface AvatarListener {
		public void avatarEstablecido(String avatarSeleccionado);
	}

	

}
