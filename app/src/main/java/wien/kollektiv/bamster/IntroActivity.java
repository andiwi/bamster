package wien.kollektiv.bamster;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.VideoView;

/**
 * startet das Video, bei beenden des Videos wird die MenuActivity automatisch aufgerufen
 * @author Andreas
 *
 */
public class IntroActivity extends Activity {

	private VideoView mVideoView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
		//Displays a video file. 
        mVideoView = (VideoView)findViewById(R.id.videoView);
        
        mVideoView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// do nothing		
			}
		});
        
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
					
			@Override
			public void onCompletion(MediaPlayer mp) {
				goToMenuActivity();
			}
		});
        
        String uriPath = "android.resource://wien.kollektiv.bamster/"+R.raw.intro;
        Uri uri = Uri.parse(uriPath);
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        mVideoView.start();
        
        
	}
	
	/**
	 * tracked Button-Aktivität
	 * @param v aktuelle button-view
	 */
	public void skipClicked(View v) {
		mVideoView.stopPlayback();
		goToMenuActivity();
	}
	
	/**
	 * wechselt zum Menü
	 */
	private void goToMenuActivity() {
		Intent intent = new Intent(this, MenuActivity.class);
		startActivity(intent);
	}
}
