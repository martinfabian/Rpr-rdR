/************************** Settings.java ****************************/
/*
 * Collects the settings
 */
package my.fabian.webview;

import java.net.URL;

public class Settings
{
	public static boolean USE_XY = false;	// false means use Y "stripped", true mens X "minimalistic"
	
	public static final String FORUM = "http://forum.cockos.com";
	
	public static enum FORUM_STATE {TOP, THREADS, POSTS, PROJECT};
	public static FORUM_STATE forum_state = FORUM_STATE.TOP;
	
	public static final String FORUMTAG = "forumdisplay";
	public static final String THREADTAG = "showthread";
	public static final String PROJECTTAG = "project";
	public static final String POSTTAG = "<div id=\"post_message_";
	
	public static final String ERROR = "<html><body><h3>404 Not found</h3><BR>or some other silly error, sorry...<p style=\"font-size:60%\"><small><b>Rpr-rdR</B> by M Fabian 2011</p></body></html>";
	
	public static final boolean PRETTYPRINT = true;	// for document, set this to false for production code
	public static final int TOPMAINTABLE = 6; // magic number 6 is the zero based index of the main table under the div, shoudl be kept
	public static final int THREADMAINTABLE = 3; // magic number 3 is the index for the main thread table
	
	public static final String FONTSIZE = "9pt";
	public static final String DECORATION = "none";
	public static final String LINKCOLOR = "DarkSlateBlue";
	public static final String VISITEDCOLOR = "gray";
	public static final String CELLPADDING = "cellpadding=\"2\"";
	public static final String CELLSPACING = "cellspacing=\"0\"";
	public static final String TABLECELLPADDING = "2";
	public static final String DIVSTYLEPADDING = "padding:0px 0px 0px 0px";
	
	public static final int TEXTWIDTH = 50;	// governs the truncation length of too long link tags
	
	// These are taken directly from view-source:http://forum.cockos.com/
	public static final String BACKGROUND = "#ABB8B8";	
	public static final int BGCOLOR = 0xFFABB8B8;
	public static final String BODY = "body { background: #ABB8B8; color: #000000; font-size:" + FONTSIZE + ";}";	
	public static final String ALINK = "a:link, body_alink { color: " + LINKCOLOR + "; text-decoration:" + DECORATION + ";}";
	public static final String AVISITED = "a:visited, body_avisited { color:" + VISITEDCOLOR + "; text-decoration:" + DECORATION + ";}";
	public static final String TDTHPLI = "td, th, p, li { font:" + FONTSIZE + " verdana, geneva, lucida, 'lucida grande', arial, helvetica, sans-serif;}";
	public static final String LOGO = "http://www.cockos.com/reaper/siteimages/forum-head-r.jpg";
	public static final String ALT2 = ".alt2, .alt2Active{background: #D6DFDF;color: #000000; font-size:" + FONTSIZE + ";}";
	public static final String ALT1ACTIVE = ".alt1, .alt1Active { background: #FFFFFF; color: #000000;}";
	public static final String SMALLFONT = ".smallfont { font-size:" + FONTSIZE + ";}";
	public static final String TBORDER = ".tborder { background: #33454B;color: #FFFFFF;border: 1px solid #000000;}";
	public static final String THEAD = ".thead{	background: #FFFFFF url(http://www.cockos.com/reaper/siteimages/forum-bg-grad.jpg) "
								+ "repeat-x top left;color: #FFFFFF;font-size:" + FONTSIZE + ";font-weight: bold;}";

	public static final String TAG = "MF";
}
