package tesis.sos2;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import IMSresources.CustomDialog;
import android.content.DialogInterface;

public class LogInActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log_in, menu);
		return true;
	}
	/*
	public void customDialogTest(View view){
		CustomDialog dialog;
		CustomDialog.show(this, 1, "Title", "Message", null, null, "Negative Text", 
				new android.content.DialogInterface.OnClickListener() {@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}});
	}
	*/
}
