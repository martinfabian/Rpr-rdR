package my.fabian.webview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HelloWebViewActivity extends Activity
{
	private static final String TAG = "MF";
	
	private static String trim(String s, int width) 
	{
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
	}
	
	private class FileFix
	{
		File the_file = null;
		
		FileFix()
		{
			// File f = getExternalFilesDir(null);
			// String fstr = f.toString();
			the_file = new File(getExternalFilesDir(null), "MFtest.html");
			Log.i(TAG, "FileFix: " + getTheFile().getAbsolutePath());
		}
		
		File getTheFile()
		{
			return the_file;
		}
	}
	
	WebView webview;
	FileFix filefix;
	
	private class ListLinks // see http://jsoup.org/cookbook/extracting-data/example-list-links
	{
		String the_url = null;
		
		ListLinks(final String url) 
		{
			the_url = url; 
			try
			{
				Document doc = Jsoup.connect(the_url).get();
				Log.i(TAG, "Title: " + doc.title());
				Elements links = doc.select("a[href]");
				StringBuilder builder = new StringBuilder("<html><head><style type=\"text/css\"><!-- TD{font-family:Arial; font-size:8pt;} ---></style></head><body><table width=\"100%\" frame=\"box\" rules=\"rows\" bordercolor=\"grey\">");
				for (org.jsoup.nodes.Element link : links) 
				{
					builder.append(String.format("<tr><td><A HREF=\"%s\">%s</A></td></tr>", link.attr("abs:href"), trim(link.text(), 35)));
				}
				builder.append("</table></body></html>");
				
				OutputStream outStream = new FileOutputStream(filefix.getTheFile().getAbsolutePath());
				Log.i(TAG, "File: " + filefix.getTheFile().getAbsolutePath());
				outStream.write(builder.toString().getBytes());	        
				outStream.close();
			}
	        catch(Exception excp)
	        {
	        	Log.e(TAG, "Exception!", excp);
	        	excp.printStackTrace();
	        	// return;
	        }
		}
	}
	
	private class HelloWebViewClient extends WebViewClient 
	{
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) 
	    {
	    	ListLinks lister = new ListLinks(url);
	        view.loadUrl("file://" + filefix.getTheFile().getAbsolutePath());
	        return true;
	    }
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        try
        {	
        	filefix = new FileFix();
        	
        	// This is ugly hack for now, should use AsyncThread
        	if (android.os.Build.VERSION.SDK_INT > 9) // see http://stackoverflow.com/questions/8706464/defaulthttpclient-to-androidhttpclient
        	{
        		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        		StrictMode.setThreadPolicy(policy);
        	}
        	 
        	 /* see http://stackoverflow.com/questions/5290384/jsoup-connecturl-always-throw-exception
        	 String url = "http://forum.cockos.com";
             Document doc = Jsoup.connect(url).get();
             String title = doc.title();
             Log.i(TAG, "Title: " + title);
             */
        	 
         	ListLinks links = new ListLinks("http://forum.cockos.com");
        }
        catch(Exception excp)
        {
        	Log.e(TAG, "Exception!", excp);
        	excp.printStackTrace();
        	// return;
        }
        
        webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new HelloWebViewClient());
        // webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("file://" + filefix.getTheFile().getAbsolutePath());
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) 
        {
            webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}