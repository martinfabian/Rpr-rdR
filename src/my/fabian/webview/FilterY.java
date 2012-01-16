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
	long timing = 0;

	private static String trim(String s, int width) 
	{
		/* Just testing what happens if trim does nothing...
        if (s.length() > width)
            return s.substring(0, width-1) + "...";
        else */
            return s;
	}

//	FilterY(final String url)
//	{
//		this(url, new WebView(), new Activity());
//		
//		/* Just a test *//*
//		final String html = "<html><head></head><body><div></div></body></html>";
//		Document doc = Jsoup.parse(html);
//		Element body = doc.body();
//		Element div = body.select("div").first();
//		body.empty();	// gives exception
//		// body.children().remove();	// does not give exception
//		body.appendChild(div);	// Exception here when body.empty is used
//		*/
//	}
	FilterY(final String url, final WebView view, final Activity activity) 
	{
		the_url = url; 
		the_view = view;
		the_activity = activity;
		
		// If the url doesn't contain a FORUMTAG, THREADTAG or PROJECTTAG, then it is the TOP url (right?)
		Settings.forum_state = Settings.FORUM_STATE.TOP;	
		if(the_url.contains(Settings.FORUMTAG)) Settings.forum_state = Settings.FORUM_STATE.THREADS;
		else if(the_url.contains(Settings.THREADTAG)) Settings.forum_state = Settings.FORUM_STATE.POSTS;
		else if(the_url.contains(Settings.PROJECTTAG)) Settings.forum_state = Settings.FORUM_STATE.PROJECT;
		
		Log.i(Settings.TAG, "State: " + Settings.forum_state.toString());
	}
	
	/*
	 * TOP: On http://forum.cockos.com/ we should only show non-empty links containing "forumdisplay"
	 * THREADS: On http://forum.cockos.com/forumdisplay.php?f=xx we should only show non-empty links containing "showthread"
	 * POSTS: On http://forum.cockos.com/showthread.php?t=xxxxx we should only show... what? Messages are embedded within <div id="post_message_xxxx"> </div> 
	 * 																				Poster name is found within <div id="postmenu_xxx"> </div>
	 */
	private Elements makeAbsolute(Elements elems)
	{
		for(Element elem : elems)
		{
			// Need a better way of making absolute, this is seriously inefficient!
			Elements srcs = elem.select("img[src]");
			for(org.jsoup.nodes.Element src : srcs)
			{
				src.attr("src", src.attr("abs:src"));
			}
			Elements hrefs = elem.select("a[href]");
			for(org.jsoup.nodes.Element href : hrefs)
			{
				href.attr("href", href.attr("abs:href"));
			}
		}
		return elems;
	}
		
	private Element removeCols(Element elem)	// Takes a TR Element with five TDs and removes TD number 0, 2, 4
	{
		assert(elem.tagName().equals("tr"));
		
		Elements elems = elem.children();
		// assert(elems.size() == 5);	// The "Issue Tracker" entry has only four columns
		
		Element forum = elem.child(1);
		Element threads = elem.child(3);
		threads.removeClass("alt1");
		threads.addClass("alt2");
				
		elems.remove();	// Remove all of them, then put the two wanted ones back
		elem.appendChild(forum);
		elem.appendChild(threads);
		
		forum.removeAttr("title");
		threads.removeAttr("title");
		return elem;
	}
	
	private HtmlStringBuilder manageTopState(Document doc)
	{
		Element body = doc.body();		// get the body
		body.select("map").remove();	// remove all maps from the body
		body.select("form").remove();	// remove all forms from teh body
		body.select("script").remove();// remove all scripts from the body

		Element table2 = body.select("table table").first();	// find the first table within this table and remove it
		table2.remove();
		
		Element div = body.select("table tr>td>div>div>div").first();	// should remove everything under here, except the fourth element
		Element table = div.child(Settings.TOPMAINTABLE);	// magic number is the zero-based index of the table we want to keep
		div.children().remove(); // remove all, but then put table back Note: DO NOT use div.empty() here, it throws exception! (see above)
		div.appendChild(table);	
		div.attr("style", Settings.DIVSTYLEPADDING);
		table.attr("cellpadding", Settings.TABLECELLPADDING);

		Elements to_keep = table.select("tr[align=center]");
		table.empty();
		
		for(Element child : to_keep)
		{
			removeCols(child);
			table.appendChild(child);
		}
		Element thead = to_keep.first();
		thead.child(1).removeClass("alt2");
		thead.child(1).addClass("thead");
		
		makeAbsolute(body.children());
		
		HtmlStringBuilder builder = new HtmlStringBuilder(doc);
		return builder;
	}
	private HtmlStringBuilder manageThreadState(Document doc)	// forumdisplay
	{		
		Element body = doc.body();
		Element div = body.select("table tr>td>div>div>div").first();	// should remove everything under here, except the fourth element
		Element form = div.select("form[action*=inlinemod]").first();	// NOTE: Need a from here, so cannot remove all forms at start!
		Element table = form.select("table").get(Settings.THREADMAINTABLE);	// magic number for the main table
		
		Elements trs = table.select("tr");	
		for(Element tr : trs)
		{
			removeCols(tr);
		}
		
		Elements head = table.select("td[class*=thead");	// remove links in table head
		assert(head.size() == 2);
		head.first().empty();
		head.first().text("Thread/Thread Starter");
		head.last().empty();
		head.last().text("Replies");
		
		table.appendChild(HtmlStringBuilder.getNavigator(doc));
		
		div.empty(); // remove all, but then put table back
		div.appendChild(table);		
		div.attr("style", Settings.DIVSTYLEPADDING);
		table.attr("cellpadding", Settings.TABLECELLPADDING);
		
		body.select("table tr > td > table").remove();
		body.select("map").remove();	// remove all maps from the body
		body.select("form").remove();	// remove all forms from the body
		body.select("script").remove();// remove all scripts from the body
		
		makeAbsolute(body.children());
		
		HtmlStringBuilder builder = new HtmlStringBuilder(doc);		
		return builder;
	}
	
	private Element createNavTable(Document doc)
	{
		Element table = new Element(Tag.valueOf("table"), doc.baseUri());
		table.addClass("tborder");
		table.attr("id", "nav_table");
		table.attr("cellpadding", Settings.CELLPADDING);
		table.attr("cellspacing", Settings.CELLSPACING);
		table.attr("width", "98%");
		table.attr("align", "center");
		Element nav = HtmlStringBuilder.getNavigator(doc);
		Element td = nav.getElementsByTag("td").first();
		td.addClass("thead");
		td.attr("style", "font-weigth:bold;");
		table.appendChild(nav);
		return table;
	}

	private HtmlStringBuilder managePostState(Document doc)	// showthread
	{
		Element body = doc.body();
		Element div_posts = body.getElementById("posts"); // select("div[id=posts]").first(); // 
	
		// Under div_posts, there's one div_align_center for every post
		Elements posts = div_posts.select("div[align=center]").first().siblingElements();
		posts.remove(posts.size()-1);
		Log.i(Settings.TAG, "siblings: " + posts.size());
		for(Element post : posts)
		{
			post.select("div[class^=vbmenu]").remove();
			// Now there's a single table with three tr siblings
			Element table = post.select("table[id^=post]").first();
			Log.i(Settings.TAG, table.id());
			Elements trs = table.select("tr").first().siblingElements();
			assert(trs.size() == 3);
			Element tr1 = trs.first();	// the head
			Element tr2 = trs.get(1);	// the body
			Element tr3 = trs.last();	// the foot (with on/off-line img and Quote button)
			
			// fiddle with the head
			Elements td1s = tr1.select("td");
			assert(td1s.size() == 2);
			Element td11 = td1s.first();	// date & time
			Element td12 = td1s.last();		// post #

			Element a_href = td12.select("a[href]").first();
			StringBuilder str = new StringBuilder("<span style=\"float:left;\">");
			str.append(td11.html());
			str.append("</span><span style=\"float:right;\">");
			str.append("#"). append(a_href.html()).append("</span>");
			td12.remove();
			td11.attr("colspan", "2");
			td11.html(str.toString());
			
			// remove avatars and adjust size of the user info
			Element td21 = tr2.select("td").first();
			td21.select("img").remove();
			td21.attr("width", "10%");
			// If no title text, remove the separator line
			if(!tr2.select("div[class=smallfont]").last().hasText())
			{
				tr2.select("hr").remove();
			}
			// Remove the title icon
			tr2.getElementsByClass("inlineimg").remove();
			
			// Remove the foot
			tr3.remove();
			
			table.attr("cellpadding", Settings.TABLECELLPADDING);
		}
		
		posts.select("div[style=padding").attr("style", Settings.DIVSTYLEPADDING);
		
		// Remove everything and then put the posts back
		div_posts.getElementsByClass("vbmenu_popup").remove();
		Element td = body.select("table tr>td").first();
		td.children().remove();
		td.appendChild(div_posts);

		// Add the navigator bar at the bottom
		Element nav_table = createNavTable(doc);
		div_posts.appendChild(nav_table);
		
		makeAbsolute(body.children());
		
		HtmlStringBuilder builder = new HtmlStringBuilder(doc);		
		return builder;
	}
	private HtmlStringBuilder manageProjectState(Document doc)
	{
		return new HtmlStringBuilder("Issue Tracker not supported yet, sorry...");
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
					builder = manageTopState(doc);
					break;
				case THREADS:
					builder = manageThreadState(doc);
					break;
				case POSTS:
					builder = managePostState(doc);
					break;
				case PROJECT:
					builder = manageProjectState(doc);
					break;
				default:
					Log.e(Settings.TAG, "Switch error that cannot occur!");
			}
			
			builder.insertTitle(doc.title());
			
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
        	return null;	// Should return file that says something about error
        }
	}
	
	//@Override
	protected void onPreExecute()
	{
		timing = System.currentTimeMillis();
		
		the_activity.setProgressBarIndeterminateVisibility(true);
		
		the_dialog = new ProgressDialog(the_activity);
		the_dialog.setMessage("Loading...");
		the_dialog.setIndeterminate(true);
		the_dialog.setCancelable(false);
		the_dialog.show();
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
		
		timing = System.currentTimeMillis() - timing;
		Log.i(Settings.TAG, "Timing: " + timing + " ms");
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
