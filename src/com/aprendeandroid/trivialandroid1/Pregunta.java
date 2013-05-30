package com.aprendeandroid.trivialandroid1;

public class Pregunta {
	
	private String id;
	private String imagen;
	private String cuestion;
	public String respuestas[] = new String[4];


	private int correcta;
	

	
	public Pregunta(String id, String imagen, String cuestion,String[] respuestas, int correcta) {
		this.id = id;
		this.imagen = imagen;
		this.cuestion = cuestion;
		this.respuestas = respuestas;
		this.correcta = correcta;
	}

	public String getId() {
		return id;
	}

	public String getImagen() {
		return imagen;
	}


	public String getCuestion() {
		return cuestion;
	}

	public String[] getRespuestas() {
		return respuestas;
	}


	public int getCorrecta() {
		return correcta;
	}

}
