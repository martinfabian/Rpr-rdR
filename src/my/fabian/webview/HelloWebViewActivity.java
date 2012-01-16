package my.fabian.webview;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class HelloWebViewActivity extends Activity
{	
	WebView webview;
	ProgressBar progressbar;
	
	@Override
	public File getExternalFilesDir(final String type)
	{
		File path = super.getExternalFilesDir(type);
		// Should extend the path with "RprrdR" + File.separator -- but I don't know how just yet
		// The reason is to be able to remove all files when the app is stopped, by just removing the RprrdR folder
		return path;
		
	}
	private class HelloWebViewClient extends WebViewClient 
	{
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) 
	    {
	    	HelloWebViewActivity.this.fetch(view, url);
	        return true;
	    }
	    
	    @Override
	    public void onLoadResource (WebView view, String url)
	    {
	    	Log.i(Settings.TAG, "onLoadResource: " + url);
	    }
	}
	
	public void fetch(WebView view, String url)
	{
    	Log.i(Settings.TAG, "URL: " + url);
    	
    	AsyncTask<Void, String, File> fx = Settings.USE_XY ? new FilterX(url, view, this) : new FilterY(url, view, this);
        if(url.startsWith(Settings.FORUM) && Settings.STRIP)
        {
        	fx.execute();	// takes roughly ten times longer than just plain load!
        }
        else
        {
        	view.loadUrl(url);
        }
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); 
        setProgressBarIndeterminateVisibility(false);
        
        setContentView(R.layout.main);
        
    	// This is ugly hack for now, should use AsyncThread
//    	if (android.os.Build.VERSION.SDK_INT > 9) // see http://stackoverflow.com/questions/8706464/defaulthttpclient-to-androidhttpclient
//    	{
//    		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//    		StrictMode.setThreadPolicy(policy);
//    	}
//    	 
        webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new HelloWebViewClient());
        webview.setBackgroundColor(Settings.BGCOLOR);
        webview.loadUrl(Settings.LOGO);
        
        fetch(webview, Settings.FORUM);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {	
        	if(webview.canGoBack()) 
	        {
        		// TODO: don't forget to change the app title!
        		
	            webview.goBack();
	            return true;
	        }
	        else
	        {
	            //Ask the user if they want to quit
	            new AlertDialog.Builder(this)
	                .setIcon(android.R.drawable.ic_dialog_alert)
	                .setTitle(R.string.quit)
	                .setMessage(R.string.really_quit)
	                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() 
	                {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) 
	                    {
	                        //Stop the activity
	                        finish();    
	                    }

	                })
	                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
	                {
	                	@Override
	                	public void onClick(DialogInterface dialog, int which)
	                	{
	                		// Go home, i.e. to the FORUM
	                		fetch(webview, Settings.FORUM);
	                	}
	                })
	                .show();

	                return true;
	        }
         }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.setGroupVisible(R.id.layout_group, true);
        menu.setGroupEnabled(R.id.layout_group, true);
        menu.setGroupCheckable(R.id.layout_group, true, true);
        if(Settings.USE_XY)
        {
        	menu.findItem(R.id.minimalistic).setChecked(true);	// DO NOT use getItem here, index an did are very different things!
        }														// Some stupid f*** at google apparently had too much to think...
        else
        {
        	menu.findItem(R.id.stripped).setChecked(true);
        }
        menu.findItem(R.id.to_strip).setCheckable(true);
        menu.findItem(R.id.to_strip).setChecked(Settings.STRIP);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        	case R.id.minimalistic:	Toast.makeText(this, R.string.minimalistic, Toast.LENGTH_SHORT).show();
        							Settings.USE_XY = true;
        							item.setChecked(true);
        							break;
        	case R.id.stripped:		Toast.makeText(this, R.string.stripped, Toast.LENGTH_SHORT).show();
        							Settings.USE_XY = false;
        							item.setChecked(true);
        							break;
        	case R.id.to_strip:		Settings.STRIP = !Settings.STRIP;
        							item.setChecked(Settings.STRIP);
        							break;
        	case R.id.forum_home:	fetch(webview, Settings.FORUM);
        							break;
        }
		
        return true;
    }
 
}