package my.fabian.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class HelloWebViewActivity extends Activity
{	
	WebView webview;
	ProgressBar progressbar;
	
	private class HelloWebViewClient extends WebViewClient 
	{
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) 
	    {
	    	HelloWebViewActivity.this.fetch(view, url);
	        return true;
	    }
	}
	
	public void fetch(WebView view, String url)
	{
    	Log.i(Settings.TAG, "URL: " + url);
    	
    	FilterX fx = new FilterX(url, view, this);
        // progressbar.setVisibility(ProgressBar.VISIBLE);
    	fx.execute();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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

        fetch(webview, Settings.FORUM);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {	
        	if(webview.canGoBack()) 
	        {
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
	                .setNegativeButton(R.string.no, null)
	                .show();

	                return true;
	        }
         }
        return super.onKeyDown(keyCode, event);
    }
}