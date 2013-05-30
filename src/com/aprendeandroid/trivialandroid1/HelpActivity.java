package com.aprendeandroid.trivialandroid1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.app.Activity;
import android.view.Window;
import android.widget.TextView;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.help);

        InputStream iFile = getResources().openRawResource(R.raw.quizhelp);

        try {
            TextView helpText = (TextView) findViewById(R.id.TextView_HelpText);
            
            String strFile = inputStreamToString(iFile);
            helpText.setText(strFile);
        } 
        catch (Exception e) {

        }
	}

	public String inputStreamToString(InputStream is) throws IOException {
        StringBuffer sBuffer = new StringBuffer();

        BufferedReader dataIO = new BufferedReader(new InputStreamReader(is));        
        String strLine = null;

        while ((strLine = dataIO.readLine()) != null) {
            sBuffer.append(strLine + "\n");
        }

        dataIO.close();
        is.close();

        return sBuffer.toString();
    }

}
