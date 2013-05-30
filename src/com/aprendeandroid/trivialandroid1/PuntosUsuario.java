package com.aprendeandroid.trivialandroid1;

import android.graphics.drawable.Drawable;

public class PuntosUsuario {
	
	private String nickName;
	private Drawable avatar;
	private String puntuacion;

	public PuntosUsuario(String nickName, Drawable avatar, String puntuacion) {
		this.nickName = nickName;
		this.avatar = avatar;
		this.puntuacion = puntuacion;
	}
	
	public String getNickName() {
		return nickName;
	}
	
	public Drawable getAvatar() {
		return avatar;
	}
	
	public String getPuntuacion() {
		return puntuacion;
	}
	
	

}
