/********************* FilterX.java ***********************/
/*
 * HTML filtering class for Rpr rdR
 */
package my.fabian.webview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class FilterX extends AsyncTask<Void, Integer, File>
{
	Context the_context = null;
	String the_url = null;
	File the_file = null;
	WebView the_view = null;
	ProgressDialog the_dialog = null;
	
	private static String trim(String s, int width) 
	{
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
	}
	
	FilterX(final String url, final WebView view, final Context context) 
	{
		the_url = url; 
		the_view = view;
		the_context = context;
	}
	
	protected File doInBackground(Void... v)
	{
		try
		{
			Document doc = Jsoup.connect(the_url).get();
			Log.i(Settings.TAG, "Title: " + doc.title());
			Elements links = doc.select("a[href]");
			
			StringBuilder builder = new StringBuilder("<html><head><style type=\"text/css\"><!-- a:link{text-decoration:none;} TD{font-family:Arial; font-size:8pt;} ---></style></head><body><table width=\"100%\" frame=\"box\" rules=\"rows\" bordercolor=\"grey\">");
			for (org.jsoup.nodes.Element link : links) 
			{
				builder.append(String.format("<tr><td><A HREF=\"%s\">%s</A></td></tr>", link.attr("abs:href"), trim(link.text(), 35)));
			}
			builder.append("</table></body></html>");
			
			URI geller = new URI(the_url);	// using URI instead of URL because of hashCode (see http://www.eishay.com/2008/04/javas-url-little-secret.html)
			the_file = new File(the_context.getExternalFilesDir(null), geller.hashCode() + ".html");
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
	
	protected void onPreExecute()
	{
		// the_bar.setIndeterminate(true);
		// the_bar.setVisibility(ProgressBar.VISIBLE);
		the_dialog = new ProgressDialog(the_context);
		the_dialog.setMessage("Loading...");
		the_dialog.setIndeterminate(true);
		the_dialog.setCancelable(false);
		the_dialog.show();
	}
	protected void onProgressUpdate(Integer... prog)
	{
		// the_bar.setProgress(prog[0]);
	}
	protected void onPostExecute(File file)
	{
		the_view.loadUrl("file://" + file.getAbsolutePath());
		// the_bar.setVisibility(ProgressBar.GONE);
		the_dialog.hide();
		the_dialog.cancel();
		the_dialog = null;
	}
	
	File getTheFile()
	{
		return the_file;
	}
}
