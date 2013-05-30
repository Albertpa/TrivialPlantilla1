package com.aprendeandroid.trivialandroid1;

public class Constants {
	
	// Game preference values
	//Nombre del archivo, como se llamara
    public static final String GAME_PREFERENCES = "GamePrefs";
    
    public static final String GAME_PREFERENCES_PLAYER_ID = "ServerId"; // Integer

    //Se guarda desde Settings
    public static final String GAME_PREFERENCES_ID = "Id"; // Integer
    public static final String GAME_PREFERENCES_NICKNAME = "Nickname"; // String
    public static final String GAME_PREFERENCES_AVATAR = "Avatar"; // String
    public static final String GAME_PREFERENCES_PASSWORD = "Password"; // String
    public static final String GAME_PREFERENCES_EMAIL = "Email"; // String
    public static final String GAME_PREFERENCES_DOB = "DOB"; // Long
    public static final String GAME_PREFERENCES_GENDER = "Gender";  // Integer, in array order: Male (1), Female (2), and Undisclosed (0)
    
    //Se guarda desde UpPregunta
    public static final String GAME_PREFERENCES_PREGUNTA = "Pregunta"; // String
    public static final String GAME_PREFERENCES_RESPUESTAA = "RespuestaA"; // String
    public static final String GAME_PREFERENCES_RESPUESTAB = "RespuestaB"; // String
    public static final String GAME_PREFERENCES_RESPUESTAC = "RespuestaC"; // String
    public static final String GAME_PREFERENCES_RESPUESTAD = "RespuestaD"; //String
    public static final String GAME_PREFERENCES_IMGPREGUNTA = "ImgPregunta"; //String
    public static final String GAME_PREFERENCES_RESPUESTAOK = "RespuestaOK";  // Integer, in array order: A (1), B (2), C (3), D(4)
    
    
    //se guarda desde Game
    public static final String GAME_PREFERENCES_SCORE = "Score"; // Integer
    public static final String GAME_PREFERENCES_TIME = "Time"; // Long
    public static final String GAME_PREFERENCES_CURRENT_QUESTION = "CurQuestion"; // Integer

    public static final String PATH_TO_SERVER = "http://www.bishoport.com/trivial_android/";
        
    public static final String PATH_TO_USUARIOS = "http://www.bishoport.com/trivial_android/usuarios/";
    public static final String PHP_SCORES = "consulta_scores_xml.php";  
    public static final String PHP_PREGUNTAS = "consultaXMLattrib.php"; 
    public static final String PHP_PUNTUACION = "subir_puntuacion.php";
    public static final String PHP_USUARIO = "operacion_usuario_get2.php";
    public static final String PHP_IMAGEN = "subirimagen1.php";
    public static final String PHP_ULTIMAPREGUNTA = "ultimapregunta.php";
    public static final String PHP_SUBIR_PREGUNTA = "subir_pregunta.php";
    
    public static final String DEBUG_TAG = "trivialAndroid";
    
    public static enum ResultadoSubida {OK(0), MODIFIED(0), ERROR(-1);
    
		private int type;
		ResultadoSubida (int i) {
	        this.type = i;
	    }
	
	    public int getValue() {
	        return type;
	    }	
	};

}
