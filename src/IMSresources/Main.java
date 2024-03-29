package IMSresources;

import IMSresources.BaseScreen;
import IMSresources.IBaseScreen;
import IMSresources.ScreenAV;
//import IMSresources.ScreenAVQueue;
//import IMSresources.ScreenChatQueue;
//import IMSresources.ScreenFileTransferQueue;
//import IMSresources.ScreenHome;
//import IMSresources.ScreenSplash;
//import IMSresources.ScreenTabMessages;
import IMSresources.BaseScreen.SCREEN_TYPE;
import IMSresources.IScreenService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class Main extends ActivityGroup {
	private static String TAG = Main.class.getCanonicalName();
	
	public static final int ACTION_NONE = 0;
	public static final int ACTION_RESTORE_LAST_STATE = 1;
	public static final int ACTION_SHOW_AVSCREEN = 2;
	public static final int ACTION_SHOW_CONTSHARE_SCREEN = 3;
	public static final int ACTION_SHOW_SMS = 4;
	public static final int ACTION_SHOW_CHAT_SCREEN = 5;
	
	private static final int RC_SPLASH = 0;
	
	private Handler mHanler;
	private final Engine mEngine;
	private final IScreenService mScreenService;
	
	public Main(){
		super();
		
		// Sets main activity (should be done before starting services)
		mEngine = (Engine)Engine.getInstance();
		mEngine.setMainActivity(this);
    	mScreenService = ((Engine)Engine.getInstance()).getScreenService();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(tesis.sos2.R.layout.main);
        
        mHanler = new Handler();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        
        if(!Engine.getInstance().isStarted()){
        	//startActivityForResult(new Intent(this, ScreenSplash.class), Main.RC_SPLASH);
        	return;
        }
        
        Bundle bundle = savedInstanceState;
        if(bundle == null){
	        Intent intent = getIntent();
	        bundle = intent == null ? null : intent.getExtras();
        }
        if(bundle != null && bundle.getInt("action", Main.ACTION_NONE) != Main.ACTION_NONE){
        	handleAction(bundle);
        }
        else if(mScreenService != null){
        	//mScreenService.show(ScreenHome.class);
        }
    }
    
    @Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Bundle bundle = intent.getExtras();
		if(bundle != null){
			handleAction(bundle);
		}
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(mScreenService.getCurrentScreen().hasMenu()){
			return mScreenService.getCurrentScreen().createOptionsMenu(menu);
		}
		
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if(mScreenService.getCurrentScreen().hasMenu()){
			menu.clear();
			return mScreenService.getCurrentScreen().createOptionsMenu(menu);
		}
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		IBaseScreen baseScreen = mScreenService.getCurrentScreen();
		if(baseScreen instanceof Activity){
			return ((Activity)baseScreen).onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(mScreenService == null){
			super.onSaveInstanceState(outState);
			return;
		}
		
		IBaseScreen screen = mScreenService.getCurrentScreen();
		if(screen != null){
			outState.putInt("action", Main.ACTION_RESTORE_LAST_STATE);
			outState.putString("screen-id", screen.getId());
			outState.putString("screen-type", screen.getType().toString());
		}
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		this.handleAction(savedInstanceState);
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.d(TAG, "onActivityResult("+requestCode+","+resultCode+")");
		if(resultCode == RESULT_OK){
			if(requestCode == Main.RC_SPLASH){
				Log.d(TAG, "Result from splash screen");
			}
		}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(!BaseScreen.processKeyDown(keyCode, event)){
    		return super.onKeyDown(keyCode, event);
    	}
    	return true;
	}
    
    public void exit(){
    	mHanler.post(new Runnable() {
			public void run() {
				if (!Engine.getInstance().stop()) {
					Log.e(TAG, "Failed to stop engine");
				}				
				finish();
			}
		});
	}
    
    private void handleAction(Bundle bundle){
		final String id;
		switch(bundle.getInt("action", Main.ACTION_NONE)){
			// Default or ACTION_RESTORE_LAST_STATE
			default:
			case ACTION_RESTORE_LAST_STATE:
				id = bundle.getString("screen-id");
				final String screenTypeStr = bundle.getString("screen-type");
				final SCREEN_TYPE screenType = NgnStringUtils.isNullOrEmpty(screenTypeStr) ? SCREEN_TYPE.HOME_T :
						SCREEN_TYPE.valueOf(screenTypeStr);
				switch(screenType){
					case AV_T:
						mScreenService.show(ScreenAV.class, id);
						break;
					default:
						if(!mScreenService.show(id)){
							//mScreenService.show(ScreenHome.class);
						}
						break;
				}
				break;
				
			// Notify for new SMSs
			case ACTION_SHOW_SMS:
                //mScreenService.show(ScreenTabMessages.class);
                break;
               
			// Show Audio/Video Calls
			case ACTION_SHOW_AVSCREEN:
				Log.d(TAG, "Main.ACTION_SHOW_AVSCREEN");
				
				final int activeSessionsCount = NgnAVSession.getSize(new NgnPredicate<NgnAVSession>() {
					@Override
					public boolean apply(NgnAVSession session) {
						return session != null && session.isActive();
					}
				});
				if(activeSessionsCount > 1){
					//mScreenService.show(ScreenAVQueue.class);
				}
				else{
					NgnAVSession avSession = NgnAVSession.getSession(new NgnPredicate<NgnAVSession>() {
						@Override
						public boolean apply(NgnAVSession session) {
							return session != null && session.isActive() && !session.isLocalHeld() && !session.isRemoteHeld();
						}
					});
					if(avSession == null){
						avSession = NgnAVSession.getSession(new NgnPredicate<NgnAVSession>() {
							@Override
							public boolean apply(NgnAVSession session) {
								return session != null && session.isActive();
							}
						});
					}
					if(avSession != null){
						if(!mScreenService.show(ScreenAV.class, Long.toString(avSession.getId()))){
							//mScreenService.show(ScreenHome.class);
						}
					}
					else{
						Log.e(TAG,"Failed to find associated audio/video session");
						//mScreenService.show(ScreenHome.class);
						//mEngine.refreshAVCallNotif(R.drawable.phone_call_25);
					}
				}
				break;
				
			// Show Content Share Queue
			case ACTION_SHOW_CONTSHARE_SCREEN:
				//mScreenService.show(ScreenFileTransferQueue.class);
				break;
				
			// Show Chat Queue
			case ACTION_SHOW_CHAT_SCREEN:
				//mScreenService.show(ScreenChatQueue.class);
				break;
		}
	}
}