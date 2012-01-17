/********************************* FilterY.java *********************************/
package my.fabian.webview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

public class FilterY extends AsyncTask<Void, String, File>
{
	Activity the_activity = null;
	String the_url = null;
	File the_file = null;
	WebView the_view = null;
	ProgressDialog the_dialog = null;
	
	Stripped hy = null;
	
	long timing_all = 0;
	long timing_write = 0;


	FilterY(final String url, final WebView view, final Activity activity) 
	{
		the_url = url; 
		the_view = view;
		the_activity = activity;
		
		hy = new Stripped();
		
		
		/*
		 * TOP: On http://forum.cockos.com/ we should only show non-empty links containing "forumdisplay"
		 * THREADS: On http://forum.cockos.com/forumdisplay.php?f=xx we should only show non-empty links containing "showthread"
		 * POSTS: On http://forum.cockos.com/showthread.php?t=xxxxx we should only show... what? Messages are embedded within <div id="post_message_xxxx"> </div> 
		 * 																				Poster name is found within <div id="postmenu_xxx"> </div>
		 */
		// If the url doesn't contain a FORUMTAG, THREADTAG or PROJECTTAG, then it is the TOP url (right?)
		Settings.forum_state = Settings.FORUM_STATE.TOP;	
		if(the_url.contains(Settings.FORUMTAG)) Settings.forum_state = Settings.FORUM_STATE.THREADS;
		else if(the_url.contains(Settings.THREADTAG)) Settings.forum_state = Settings.FORUM_STATE.POSTS;
		else if(the_url.contains(Settings.PROJECTTAG)) Settings.forum_state = Settings.FORUM_STATE.PROJECT;
		
		Log.i(Settings.TAG, "State: " + Settings.forum_state.toString());
	}

	//@Override
	protected void onPreExecute()
	{
		timing_all = System.currentTimeMillis();
		
		the_activity.setProgressBarIndeterminateVisibility(true);
		
		the_dialog = new ProgressDialog(the_activity);
		the_dialog.setMessage("Loading...");
		the_dialog.setIndeterminate(true);
		the_dialog.setCancelable(false);
		the_dialog.show();
	}

	//@Override
	protected File doInBackground(Void... v)
	{
		try
		{
			Document doc = Jsoup.connect(the_url).get();
			Log.i(Settings.TAG, "Title: " + doc.title());
			publishProgress(doc.title());	// clever(?) hack here to set the app title
			
			doc.outputSettings().prettyPrint(Settings.PRETTYPRINT);
			
			HtmlStringBuilder builder = new HtmlStringBuilder(Settings.ERROR);
			
			switch(Settings.forum_state)
			{
				case TOP:	// Cannot have Settings.FORUM_STATE.TOP here? amazing...
					builder = hy.manageTopState(doc);
					break;
				case THREADS:
					builder = hy.manageThreadState(doc);
					break;
				case POSTS:
					builder = hy.managePostState(doc);
					break;
				case PROJECT:
					builder = hy.manageProjectState(doc);
					break;
				default:
					Log.e(Settings.TAG, "Switch error that cannot occur!");
			}
			
			builder.insertTitle(doc.title());
			
			URI geller = new URI(the_url);	// using URI instead of URL because of hashCode (see http://www.eishay.com/2008/04/javas-url-little-secret.html)
			int hashcode = geller.hashCode();
	
//			int hashcode = doc.hashCode();
//			Document builder = doc;
			
			timing_write = System.currentTimeMillis();
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
        	return null;	// Should return file that says something about error
        }
	}

	//@Override
	protected void onPostExecute(File file)
	{
		the_view.loadUrl("file://" + file.getAbsolutePath());
		// the_bar.setVisibility(ProgressBar.GONE);
		the_dialog.hide();
		the_dialog.cancel();
		the_dialog = null;
		
		the_activity.setProgressBarIndeterminateVisibility(false);
		
		long time_now =  System.currentTimeMillis();
		timing_all = time_now - timing_all;
		timing_write = time_now - timing_write;
		Log.i(Settings.TAG, "Timing all: " + timing_all + " Timing write: " + timing_write + " (ms)");
	}
	//@Override
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
