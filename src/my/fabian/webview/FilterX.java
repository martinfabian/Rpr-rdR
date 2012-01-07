/********************* FilterX.java ***********************/
/*
 * HTML filtering class for Rpr rdR
 * 
 * Jsoup.connect.get  Jsoup.connect.get()
 * write file	      parse, build
 * read into webview  write file
 * (ms)		          read into webview
 * 46198		   33725
 * 37070		   33619
 * 36164		   32428
 * 36344		   32095
		   
 * Seems faster to parse and build, 
 * probably because smaller file is
 * written and then parsed by webview

 */
package my.fabian.webview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

public class FilterX extends AsyncTask<Void, String, File>
{
	Activity the_activity = null;
	String the_url = null;
	File the_file = null;
	WebView the_view = null;
	ProgressDialog the_dialog = null;
	long timing = 0;
	
	private static String trim(String s, int width) 
	{
        if (s.length() > width)
            return s.substring(0, width-1) + "...";
        else
            return s;
	}
	
	FilterX(final String url, final WebView view, final Activity activity) 
	{
		the_url = url; 
		the_view = view;
		the_activity = activity;
		
		// If the url doesn't contain a FORUMTAG or a THREADTAG, then it is the TOP url (right?)
		Settings.forum_state = Settings.FORUM_STATE.TOP;	
		if(the_url.contains(Settings.FORUMTAG)) Settings.forum_state = Settings.FORUM_STATE.THREAD;
		else if(the_url.contains(Settings.THREADTAG)) Settings.forum_state = Settings.FORUM_STATE.POST;
		
		Log.i(Settings.TAG, "State: " + Settings.forum_state.toString());
	}
	

	/*
	 * TOP: On http://forum.cockos.com/ we should only show non-empty links containing "forumdisplay"
	 * THREAD: On http://forum.cockos.com/forumdisplay.php?f=xx we should only show non-empty links containing "showthread"
	 * POST: On http://forum.cockos.com/showthread.php?t=xxxxx we should only show... what? Messages are embedded within <div id="post_message_xxxx"> </div> 
	 * 																				Poster name is found within <div id="postmenu_xxx"> </div>
	 */
	private StringBuilder manageTopState(Document doc)
	{
		Elements links = doc.select("a[href]");	// select all links like <A HREF=
		StringBuilder builder = new StringBuilder("<html><head><style type=\"text/css\"><!--" + Settings.BODY + Settings.ALINK + Settings.TD + "---></style></head><body><table width=\"100%\" frame=\"box\" rules=\"rows\" bordercolor=\"grey\">");
		for (org.jsoup.nodes.Element link : links) 
		{
			if(!link.text().isEmpty() && link.attr("href").contains(Settings.FORUMTAG))
				builder.append(String.format("<tr><td><A HREF=\"%s\">%s</A></td></tr>", link.attr("abs:href"), trim(link.text(), Settings.TEXTWIDTH)));
		}
		builder.append("</table></body></html>");
		
		return builder;
	}
	private StringBuilder manageThreadState(Document doc)
	{
		return manageTopState(doc);	//*** for now
	}
	
	private StringBuilder managePostState(Document doc)
	{
		return manageTopState(doc);	//*** for now
	}
	
	@Override
	protected File doInBackground(Void... v)
	{
		try
		{
			Document doc = Jsoup.connect(the_url).get();
			Log.i(Settings.TAG, "Title: " + doc.title());
			publishProgress(doc.title());	// clever(?) hack here to set the app title
			
			StringBuilder builder = new StringBuilder("<html><body><h3>404 Not found</h3><BR>or some other silly error, sorry...</body></html>");
			
			switch(Settings.forum_state)
			{
				case TOP:	// Cannot have Settings.FORUM_STATE.TOP here? amazing...
					builder = manageTopState(doc);
					break;
				case THREAD:
					builder = manageThreadState(doc);
					break;
				case POST:
					builder = managePostState(doc);
					break;
				default:
					Log.e(Settings.TAG, "Switch error that cannot occur!");
			}
			
			URI geller = new URI(the_url);	// using URI instead of URL because of hashCode (see http://www.eishay.com/2008/04/javas-url-little-secret.html)
			int hashcode = geller.hashCode();
	
//			int hashcode = doc.hashCode();
//			Document builder = doc;
			
			the_file = new File(the_activity.getExternalFilesDir(null), hashcode + ".html");
			Log.i(Settings.TAG, "File name: " + getTheFile().getAbsolutePath());
			
			OutputStream outStream = new FileOutputStream(the_file.getAbsolutePath());
			outStream.write(builder.toString().getBytes());	        
			outStream.close();
			
			return the_file;
		}
        catch(Exception excp)
        {
        	Log.e(Settings.TAG, "Exception!", excp);
        	excp.printStackTrace();
        	return null;	// Should return file that says something about error
        }
	}
	
	@Override
	protected void onPreExecute()
	{
		timing = System.currentTimeMillis();
		
		the_activity.setProgressBarIndeterminateVisibility(true);
		
		// the_bar.setIndeterminate(true);
		// the_bar.setVisibility(ProgressBar.VISIBLE);
		the_dialog = new ProgressDialog(the_activity);
		the_dialog.setMessage("Loading...");
		the_dialog.setIndeterminate(true);
		the_dialog.setCancelable(false);
		the_dialog.show();
	}

	@Override
	protected void onPostExecute(File file)
	{
		the_view.loadUrl("file://" + file.getAbsolutePath());
		// the_bar.setVisibility(ProgressBar.GONE);
		the_dialog.hide();
		the_dialog.cancel();
		the_dialog = null;
		
		the_activity.setProgressBarIndeterminateVisibility(false);
		
		timing = System.currentTimeMillis() - timing;
		Log.i(Settings.TAG, "Timing: " + timing + " ms");
	}
	
	@Override
	protected void onProgressUpdate(String... strings)
	{
		the_activity.setTitle(strings[0]);	// clever(?) hack here to set the app title (to change size etc http://labs.makemachine.net/2010/03/custom-android-window-title/ and 
											// http://stackoverflow.com/questions/3438276/change-title-bar-text-in-android)
	}
	
	File getTheFile()
	{
		return the_file;
	}
}
