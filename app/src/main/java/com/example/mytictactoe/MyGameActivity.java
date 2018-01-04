package com.example.mytictactoe;



import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MyGameActivity extends Activity {

	public final String TAG = "MyGameActivity";
	private Handler mHandler = new Handler(new MyHandlerCallback());
	private TextView mTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_game);
		mTextView = (TextView) findViewById(R.id.textView);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_game, menu);
		return true;
	}
	
	public void OnButtonClick(View view)
	{
		Button button = (Button) findViewById(view.getId());
		MyGameView myGameView = (MyGameView) findViewById(R.id.game_view);
		switch(view.getId()) {
		case R.id.Computer_First:
			myGameView.startGame(MyGameView.Player.COMPUTER, mTextView);
			break;
		case R.id.Human_First:
			myGameView.startGame(MyGameView.Player.HUMAN, mTextView);
			break;
		default:
			break;
		}
	}
	
	private class MyHandlerCallback implements Callback {
        public boolean handleMessage(Message msg) {
            Log.v(TAG, msg.toString());
            return false;
        }
    }
	

}
