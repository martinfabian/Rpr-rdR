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
package my.fabian.rprrdr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

public class FilterX extends AsyncTask<Void, String, String>
{
	Activity the_activity = null;
	String the_url = null;
	File the_file = null;
	WebView the_view = null;
	ProgressDialog the_dialog = null;
	
	FilterHelper helper = null;
	
	long timing_all = 0;
	long timing_write = 0;
	
	FilterX(final String url, final WebView view, final Activity activity) 
	{
		the_url = url; 
		the_view = view;
		the_activity = activity;
		helper = Settings.USE_XY ? new Minimalistic() : new Stripped();
		
		/*
		 * TOP: On http://forum.cockos.com/ we should only show non-empty links containing "forumdisplay"
		 * THREADS: On http://forum.cockos.com/forumdisplay.php?f=xx we should only show non-empty links containing "showthread"
		 * POSTS: On http://forum.cockos.com/showthread.php?t=xxxxx we should only show... what? Messages are embedded within <div id="post_message_xxxx"> </div> 
		 * 																				Poster name is found within <div id="postmenu_xxx"> </div>
		 */		
		if(Settings.forum_state != Settings.FORUM_STATE.BYPASS)
		{
			// If the url doesn't contain a FORUMTAG, THREADTAG or PROJECTTAG, then it is the TOP url (right?)
			Settings.forum_state = Settings.FORUM_STATE.TOP;	
			if(the_url.contains(Settings.FORUMTAG)) Settings.forum_state = Settings.FORUM_STATE.THREADS;
			else if(the_url.contains(Settings.THREADTAG)) Settings.forum_state = Settings.FORUM_STATE.POSTS;
			else if(the_url.contains(Settings.PROJECTTAG)) Settings.forum_state = Settings.FORUM_STATE.PROJECT;
		}
		Log.i(Settings.TAG, "State: " + Settings.forum_state.toString());
	}
	
	//@Override
	protected void onPreExecute()
	{
		new Measure("Timing all").Start();
		
		the_activity.setProgressBarIndeterminateVisibility(true);
		
		the_dialog = new ProgressDialog(the_activity);
		the_dialog.setMessage("Loading...");
		the_dialog.setIndeterminate(true);
		the_dialog.setCancelable(false);
		the_dialog.show();
	}
	
	//@Override
	protected String doInBackground(Void... v)
	{
		Thread.currentThread().setName("FilterX");
		// Log.i(Settings.TAG, "Thread priority: " + Thread.currentThread().getPriority());	// default seems to be 5
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		try
		{
			Document doc = Jsoup.connect(the_url).get();
			Log.i(Settings.TAG, "Title: " + doc.title());
			// publishProgress(doc.title());	// clever(?) hack here to set the app title

			doc.outputSettings().prettyPrint(Settings.PRETTYPRINT);
			
			HtmlStringBuilder builder = new HtmlStringBuilder(Settings.ERROR);
			
			switch(Settings.forum_state)
			{
				case TOP:	// Cannot have Settings.FORUM_STATE.TOP here? amazing...
					builder = helper.manageTopState(doc);
					break;
				case THREADS:
					builder = helper.manageThreadState(doc);
					break;
				case POSTS:
					builder = helper.managePostState(doc);
					break;
				case PROJECT:
					builder = helper.manageProjectState(doc);
					break;
				case BYPASS:
					builder = helper.bypassState(doc);
					break;
				default:
					Log.e(Settings.TAG, "Switch error that cannot occur!");
			}
			
			builder.insertTitle(doc.title());
			
			URI geller = new URI(the_url);	// using URI instead of URL because of hashCode (see http://www.eishay.com/2008/04/javas-url-little-secret.html)
			int hashcode = geller.hashCode();
	
			// timing_write = System.currentTimeMillis();
			new Measure("Timing write").Start();
			the_file = new File(the_activity.getExternalFilesDir(null), hashcode + ".html");
			Log.i(Settings.TAG, "File name: " + the_file.getAbsolutePath());
			
			OutputStream outStream = new FileOutputStream(the_file.getAbsolutePath());
			outStream.write(builder.toString().getBytes());	        
			outStream.close();
			
			return builder.toString();
		}
        catch(Exception excp)
        {
        	Log.e(Settings.TAG, "Exception!", excp);
        	return null;	// Should return file that says something about error
        }
	}


	//@Override
	protected void onPostExecute(final String string)
	{
		the_view.loadUrl("file://" + the_file.getAbsolutePath());
		// the_view.loadDataWithBaseURL(Settings.FORUM, string, "text/html", "utf-8", "");
		
		the_dialog.hide();
		the_dialog.cancel();
		the_dialog = null;
		
		the_activity.setProgressBarIndeterminateVisibility(false);
		
		// long time_now =  System.currentTimeMillis();
		// timing_all = time_now - timing_all;
		// timing_write = time_now - timing_write;
		Measure.StopAll();
		StringBuilder b = Measure.getAll();
		Log.i(Settings.TAG, b.append("(ms)").toString());
		Measure.Clear();
	}
	
	//@Override
	protected void onProgressUpdate(String... strings)
	{
		the_activity.setTitle(strings[0]);	// clever(?) hack here to set the app title (to change size etc http://labs.makemachine.net/2010/03/custom-android-window-title/ and 
											// http://stackoverflow.com/questions/3438276/change-title-bar-text-in-android)
	}
	
}
