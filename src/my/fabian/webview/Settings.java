/************************** Settings.java ****************************/
/*
 * Collects the settings
 */
package my.fabian.webview;

import java.net.URL;

public class Settings
{
	public static final String FORUM = "http://forum.cockos.com";
	public static final int[] BANNED = {6, 8, 12, 20, 38, };
	
	public static enum FORUM_STATE {TOP, THREAD, POST};
	public static FORUM_STATE forum_state = FORUM_STATE.TOP;
	
	public static final String FORUMTAG = "forumdisplay";
	public static final String THREADTAG = "showthread";
	public static final String POSTTAG = "<div id=\"post_message_";
	
	public static final String FONTSIZE = "8pt";
	public static final String DECORATION = "none";
	
	public static final int TEXTWIDTH = 35;	// governs the truncation length of too long link tags
	
	// These are taken directly from view-source:http://forum.cockos.com/
	public static final String BACKGROUND = "#ABB8B8";	
	public static final int BGCOLOR = 0xFFABB8B8;
	public static final String BODY = "body { background: #ABB8B8; color: #000000; font-size:" + FONTSIZE + "; }";	
	public static final String ALINK = "a:link, body_alink { color: #000000; text-decoration:" + DECORATION + "; }";
	public static final String TDTHPLI = "td, th, p, li { font:" + FONTSIZE + "verdana, geneva, lucida, 'lucida grande', arial, helvetica, sans-serif; }";
	public static final String LOGO = "http://www.cockos.com/reaper/siteimages/forum-head-r.jpg";
	
	public static final String TD = "td { font-size:8pt; font-family:Arial;}"; 	// my own, seems TDTHPLI doesn't work...
	public static final String TAG = "MF";
}
