package com.aprendeandroid.trivialandroid1;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ScoresAdapter extends ArrayAdapter<PuntosUsuario>{

    Context context;
    int layoutResourceId;
    List<PuntosUsuario> data = null;
    String avataresPath = "";

    public ScoresAdapter(Context context, int layoutResourceId, List<PuntosUsuario> data) {
       //antes de hacerlo online era  public ScoresAdapter(Context context, int layoutResourceId, List<PuntosUsuario> data, String avataresPath) {
    	//se le pasa lo minimo e indispensable, no hace falta el path
    	super(context, layoutResourceId, data);
        
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
       // this.avataresPath = avataresPath;
    }


    //Se crea cada row, se usa un sistema de rows reutilitzables
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        //ScoreHolder holder = null;        
        FileHolder holder = null;
        
        if(row == null){ 
        	
        	//inflater = se añade un layout a la ventana
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
           
            holder = new FileHolder();
            
            holder.txtNickName = (TextView) row.findViewById(R.id.txtNickName);
            holder.imgAvatar = (ImageView) row.findViewById(R.id.imgAvatar);
            holder.txtPuntuacion = (TextView) row.findViewById(R.id.txtPuntuacion);
           
            //el tag es un hueco que nos permite guardar lo que se quiera, no solo texto
            row.setTag(holder);
        }
        else{
            holder = (FileHolder) row.getTag();
        }

        //Ya tenemos el Holder, ya sea por reuso o nuevo
        
        //CONFIGURAMOS LA ROW
        
        //Ponemos el nombre del usuario
        holder.txtNickName.setText(data.get(position).getNickName());		        
  
        
        //Ponemos la puntuacion del usuario en su campo de texto
        holder.txtPuntuacion.setText(data.get(position).getPuntuacion());
        
        
        /*
         * Antes de hacerlo on-line
        //Ponemos el nombre del avatar del usuario
		String avatarName = data.get(position).getAvatar();

		
        
		// parche para compatibilidad con version antigua servidor
		// puede servir para los que no han puesto avatar
		if(Character.isDigit(avatarName.charAt(0))) { // si empieza por un numero
			avatarName = "avatar" + avatarName + ".jpg"; // creamos el nombre del fichero
		}
		
		
		Drawable drawable;
		
		try { // leemos el fichero assets/avatares/avatarX.jpg con open("avatares/avatarX.jpg")
			// context.getAssets() devuelve un AssetsManager
			drawable = Drawable.createFromStream(context.getAssets().open(avataresPath + "/" + avatarName), null);
		} catch (IOException e) {			
			e.printStackTrace();
			drawable = context.getResources().getDrawable(R.drawable.no_avatar);
		}
        
		//Ponemos el avatar
        holder.imgAvatar.setImageDrawable(drawable);        
//        holder.imgAvatar.setImageResource(data.get(position).getAvatar()); 
        */
        //Ahora que es online
        Drawable drawable = data.get(position).getAvatar();
		//Ponemos el Avatar
        holder.imgAvatar.setImageDrawable(drawable); 
        
        
      

        return row;
    }

   
    //Clase para el holder de la row
    //El holder nos permite acceder a los widgets que tiene cada row
    static class FileHolder{
        ImageView imgAvatar;
        TextView txtNickName;
        TextView txtPuntuacion;
    }
    
    
}
