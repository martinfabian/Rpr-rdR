/*********************************** HtmlStringBuilder.java ***************************/
package my.fabian.rprrdr;

import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

public class HtmlStringBuilder
{
	StringBuilder builder = null;
	
	public HtmlStringBuilder(final String str)
	{
		builder = new StringBuilder(str);
	}
	
	public HtmlStringBuilder()
	{
		builder = new StringBuilder("<html><head><style type=\"text/css\">\n" + 
				Settings.BODY + Settings.ALINK + Settings.AVISITED + Settings.TDTHPLI + Settings.ALT2 +
				Settings.SMALLFONT + Settings.ALT1ACTIVE + Settings.TBORDER + Settings.THEAD +
				"</style></head>\n" + "<body><table width=\"100%\" frame=\"box\" rules=\"rows\"" + 
				Settings.CELLPADDING + Settings.CELLSPACING + "bgcolor=\"white\" bordercolor=\"black\">");
	}
	
	public HtmlStringBuilder(Document doc)
	{
		Element style = new Element(Tag.valueOf("style"), doc.baseUri());
		style.attr("type", "text/css");
		style.append(Settings.BODY + Settings.ALINK + Settings.AVISITED + Settings.TDTHPLI + Settings.ALT2 +
				Settings.SMALLFONT + Settings.ALT1ACTIVE + Settings.TBORDER + Settings.THEAD);
		
		Element head = doc.select("head").first();
		head.select("style").remove();
		head.appendChild(style);	
		builder = new StringBuilder(doc.toString());
	}
	public HtmlStringBuilder append(final String str)
	{
		builder.append(str);
		return this;
	}
	
	public HtmlStringBuilder insertTitle(final String title)
	{
		// find </head> and insert <title> before it
		int index = builder .indexOf("</head>");
		if(index != -1) // we found it
		{
			final StringBuilder t = new StringBuilder("<title>").append(title).append("</title>");
			builder.insert(index, t);
		}
		return this;
	}
	
	public HtmlStringBuilder appendNavigator(Document doc)
	{
		/*
		Elements firsts = doc.select("a[rel=start]");
		Elements prevs = doc.select("a[rel=prev]");
		Elements nexts = doc.select("a[rel=next]");
		Elements lasts = doc.select("a[title*=Last]");	// what? No "rel=last"??
		
		builder.append("<tr bgcolor=\"grey\" vlink=\"black\"><td>");

		if(!firsts.isEmpty()) 
		{
			builder.append("<A HREF=\"").append(firsts.first().attr("abs:href")).append("\">First</A> | ");
		}
		else
			builder.append("First | ");
		
		if(!prevs.isEmpty())
		{
			builder.append("<A HREF=\"").append(prevs.first().attr("abs:href")).append("\">Prev</A> | ");
		}
		else
			builder.append("Prev | ");
		
		if(!nexts.isEmpty())
		{
			builder.append("<A HREF=\"").append(nexts.first().attr("abs:href")).append("\">Next</A> | ");
		}
		else
			builder.append("Next | ");
		
		if(!lasts.isEmpty())
		{
			builder.append("<A HREF=\"").append(lasts.first().attr("abs:href")).append("\">Last</A>");
		}
		else
			builder.append("Last");
		
		builder.append("</td></tr>");
		*/
		Element nav = getNavigator(doc);
		builder.append(nav.toString());
		return this;
	}
	
	public static Element getNavigator(Document doc)
	{
		Element tr = new Element(Tag.valueOf("tr"), doc.baseUri());
		Element td = new Element(Tag.valueOf("td"), doc.baseUri());
		td.attr("colspan", "2");
		td.attr("class", "alt2");
		
		Elements firsts = doc.select("a[rel=start]");
		Elements prevs = doc.select("a[rel=prev]");
		Elements nexts = doc.select("a[rel=next]");
		Elements lasts = doc.select("a[title*=Last]");	// what? No "rel=last"??
		
		if(!firsts.isEmpty()) 
		{
			td.appendChild(new Element(Tag.valueOf("a"), doc.baseUri()).attr("href", firsts.first().attr("abs:href")).text("First | "));
		}
		else
			td.appendChild(new TextNode("First | ", doc.baseUri()));
		
		if(!prevs.isEmpty())
		{
			td.appendChild(new Element(Tag.valueOf("a"), doc.baseUri()).attr("href", prevs.first().attr("abs:href")).text("Prev | "));
		}
		else
			td.appendChild(new TextNode("Prev | ", doc.baseUri()));
		
		if(!nexts.isEmpty())
		{
			td.appendChild(new Element(Tag.valueOf("a"), doc.baseUri()).attr("href", nexts.first().attr("abs:href")).text("Next | "));
		}
		else
			td.appendChild(new TextNode("Next | ", doc.baseUri()));
		
		if(!lasts.isEmpty())
		{
			td.appendChild(new Element(Tag.valueOf("a"), doc.baseUri()).attr("href", lasts.first().attr("abs:href")).text("Last "));
		}
		else
			td.appendChild(new TextNode("Last", doc.baseUri()));
		
		tr.appendChild(td);		
		return tr;
	}
	
	@Override
	public String toString()
	{
		return builder.toString();
	}
}
