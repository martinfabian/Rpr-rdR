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

public abstract class FilterHelper
{
	public FilterHelper()
	{
		
	}
	
	public HtmlStringBuilder manageProjectState(Document doc)
	{
		return new HtmlStringBuilder("Issue Tracker not supported yet, sorry...");
	}
	
	public static String trim(String s, int width) 
	{
		/* Just testing what happens if trim does nothing...
        if (s.length() > width)
            return s.substring(0, width-1) + "...";
        else */
            return s;
	}
	
	public abstract HtmlStringBuilder manageTopState(Document doc);
	public abstract HtmlStringBuilder manageThreadState(Document doc);
	public abstract HtmlStringBuilder managePostState(Document doc);
	
}

class Minimalistic extends FilterHelper
{
	public Minimalistic()
	{
		super();
	}
	
	public HtmlStringBuilder manageTopState(Document doc)
	{
		HtmlStringBuilder builder = new HtmlStringBuilder();
		
		Elements tds = doc.select("td[class=alt1Active]");	// select all elements like <td class="alt1Active"
		for(org.jsoup.nodes.Element td : tds) 
		{
			Elements forumlink = td.select("a[href]");
			Elements undertext = td.select("div[class=smallfont]");
			
			builder.append(String.format("<tr><td><A HREF=\"%s\">%s<br>", forumlink.attr("abs:href"), trim(forumlink.first().text(), Settings.TEXTWIDTH)));
																		// Note Jsoup anomaly here, Elements::attr retruns for the first, Elements::text returns for *all*
			builder.append(String.format("<small>%s</small></A></td></tr>", undertext.text()));

		}
		builder.append("</table></body></html>");
		
		return builder;
	}
	
	public HtmlStringBuilder manageThreadState(Document doc)
	{	
		HtmlStringBuilder builder = new HtmlStringBuilder();

		Elements threads = doc.select("a[id*=thread_title]");
		// Log.i(Settings.TAG, threads.first().parent().parent().parent().outerHtml()); // from here we can get statusicon (tells us if the thread is hot or not)
																						// and the number of replies
		for(org.jsoup.nodes.Element threadlink : threads)
		{
			// Log.i(Settings.TAG, threadlink.outerHtml());	// Why is the style="..." stripped?
			org.jsoup.nodes.Element gg_parent = threadlink.parent().parent().parent();
			org.jsoup.nodes.Element staticon = gg_parent.select("img[id*=statusicon").first();	// thread_hot_new.gif and thread_new.gif make style bold (workaround for Jsoup not recognizing the style attribute
			Log.i(Settings.TAG, staticon.attr("src"));
			
			builder.append("<tr><td width=\"90%\"><A HREF=\"").append(threadlink.attr("abs:href")).append("\" ");
			
			// builder.append("style=\"").append(threadlink.attr("style")).append("\"");	// does not work Jsoup strips the style attr
			if(staticon.attr("src").contains("hot"))	// instead we have to do this
			{	
				builder.append("style=\"font-weight:bold\"");
			}
			builder.append(">");
			builder.append(trim(threadlink.text(), Settings.TEXTWIDTH));
			builder.append("</A></td>");
			builder.append("<td class=\"alt2\">").append(gg_parent.select("a[href*=whoposted]").first().text()).append("</td>"); // number of replies
			builder.append("</tr>");
		}
		builder.appendNavigator(doc);
		builder.append("</table></body></html>");
		
		return builder;
	}
	
	public Elements makeAbsolute(Elements elems)
	{
		for(org.jsoup.nodes.Element elem : elems)
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
	
	public HtmlStringBuilder managePostState(Document doc)
	{
		HtmlStringBuilder builder = new HtmlStringBuilder();
		
		Elements posts = doc.select("table[id*=post]");	// each post is a table of its own
		for(org.jsoup.nodes.Element post : posts)
		{
			org.jsoup.nodes.Element datetd = post.select("td").first();	// the first td holds teh date
			Elements user = post.select("a[class=bigusername]");	// the href here points to the user
			Elements td = post.select("td[id*=td_post");			// this holds the title and the posted text
			Elements title = td.first().select("div[class=smallfont]");	// this gets the title
			Elements words = post.select("div[id*=post_message]");	// this is the posted text, could as well used td.first().select
			
			builder.append("<tr><td>");
			if(!title.first().text().isEmpty())
				builder.append("<strong>").append(trim(title.first().text(), Settings.TEXTWIDTH)).append("</strong><br>");
			builder.append("<small><strong><A HREF=\"");
			builder.append(user.attr("abs:href")).append("\">");
			builder.append(trim(user.text(), Settings.TEXTWIDTH)).append("</A></strong> - ");
			builder.append(datetd.text()).append("</small></p><p><small>");
			
			builder.append(makeAbsolute(words).first().html()).append("</small></p></td></tr>");
		}
		
		builder.appendNavigator(doc);
		builder.append("</table></body></html>");
		
		return builder;
	}
}

class Stripped extends FilterHelper
{
	public Stripped()
	{
		super();
	}
	
	public Elements makeAbsolute(Elements elems)
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
		
	public Element removeCols(Element elem)	// Takes a TR Element with five TDs and removes TD number 0, 2, 4
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
	
	public HtmlStringBuilder manageTopState(Document doc)
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
	public HtmlStringBuilder manageThreadState(Document doc)	// forumdisplay
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
	
	public Element createNavTable(Document doc)
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

	public HtmlStringBuilder managePostState(Document doc)	// showthread
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
}