package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A default loading indicator
 * 
 * 
 * Wt/WDefaultLoadingIndicator The default loading indicator displays the text
 * message <span>Loading...</span> in the right top corner of the window.
 * <p>
 * 
 * @see WApplication#setLoadingIndicator(WLoadingIndicator indicator)
 */
public class WDefaultLoadingIndicator extends WText implements
		WLoadingIndicator {
	public WDefaultLoadingIndicator() {
		super("Loading...");
		this.setInline(false);
		this.setStyleClass("Wt-loading");
		WApplication app = WApplication.instance();
		app
				.getStyleSheet()
				.addRule(
						"div.Wt-loading",
						"background-color: red; color: white;font-family: Arial,Helvetica,sans-serif;font-size: small;position: absolute; right: 0px; top: 0px;");
		app.getStyleSheet().addRule("body div > div.Wt-loading",
				"position: fixed;");
		if (app.getEnvironment().getUserAgent().indexOf("MSIE 5.5") != -1
				|| app.getEnvironment().getUserAgent().indexOf("MSIE 6") != -1) {
			app
					.getStyleSheet()
					.addRule(
							"div.Wt-loading",
							"right: expression(((ignoreMe2 = document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft )) + 'px' );top: expression(((ignoreMe = document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop)) + 'px' );");
		}
	}

	public WWidget getWidget() {
		return this;
	}

	public void setMessage(CharSequence text) {
		this.setText(text);
	}
}