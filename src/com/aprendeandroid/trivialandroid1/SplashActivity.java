package com.aprendeandroid.trivialandroid1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Quitar la barra ANTES del setContentView
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		
		startAnimating();
	}

	private void startAnimating() {
		
		//movimiento barra de colores
		ImageView barraColores = (ImageView) findViewById(R.id.ImageViewTop);
		Animation movimientoVertical = AnimationUtils.loadAnimation(this, R.anim.translate_anim);
		barraColores.startAnimation(movimientoVertical);
		
		//Zoom logotipo
    	ImageView logotipoAndroid = (ImageView) findViewById(R.id.ImagenViewLogo);
    	Animation zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_anim);
    	logotipoAndroid.startAnimation(zoomAnimation);
    	
    	//Fade Titulo
    	TextView tituloPrincipal = (TextView) findViewById(R.id.TextViewTitulo);
    	Animation fade1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
    	tituloPrincipal.startAnimation(fade1);

    	//Fade subTitulo
        TextView subtitulo = (TextView) findViewById(R.id.TextViewSubtitulo);
        Animation fade2 = AnimationUtils.loadAnimation(this, R.anim.fade_in2);
        subtitulo.startAnimation(fade2);

        
        //Animacion tiempo final para lanzar la siguiente activity, fade falso
        final TextView pie = (TextView) findViewById(R.id.TextViewPie);
        final Animation fadeFinal = AnimationUtils.loadAnimation(this, R.anim.fade_final);        
        //pie.startAnimation(fadeFinal); 

     
       //Preguntar a fade2 para lanzar fade final
        //Listener
        fade2.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {				
				
				//solo si pie i fadefinal son de tipo final
				pie.startAnimation(fadeFinal);
			}
		});
       
        
        //Preguntar a fade final solo, para saber cuando acaba toda la animacion
        //Listener
        fadeFinal.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
				startActivity(new Intent(SplashActivity.this, MenuActivity.class));
				
			}
		});
        
        
        //animaciones para TableView
        Animation spinin = AnimationUtils.loadAnimation(this, R.anim.custom_anim);
        
        LayoutAnimationController control = new LayoutAnimationController(spinin);
        
        TableLayout tabla = (TableLayout) findViewById(R.id.TableLayout01);
	        for(int i =0; i<tabla.getChildCount(); i++){
	        	TableRow row = (TableRow)tabla.getChildAt(i);
	        	row.setLayoutAnimation(control);
	        	//setear el orden a 1=reverse (0 normal y 2 random)
	        	control.setOrder(1);
	        }
                
	       // Log.i("miApp", "while2:"+fade2.hasStarted());
	}
		

	
}
