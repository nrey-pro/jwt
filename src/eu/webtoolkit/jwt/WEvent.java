/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An application event.
 * <p>
 * 
 * The application is notified of an event (like a user interaction, a sesion
 * timeout, an internal keep-alive event, or other event) using
 * {@link WApplication#notify(WEvent e) WApplication#notify()}.
 * <p>
 * You can check for a particular event type using {@link WEvent#getEventType()
 * getEventType()}.
 */
public class WEvent {
	private static Logger logger = LoggerFactory.getLogger(WEvent.class);

	/**
	 * Returns the event type.
	 */
	public EventType getEventType() {
		if (!(this.impl_.handler != null)) {
			return EventType.OtherEvent;
		}
		return this.impl_.handler.getSession().getEventType(this);
	}

	WEvent(final WEvent.Impl impl) {
		this.impl_ = impl;
	}

	final WEvent.Impl impl_;

	static class Impl {
		private static Logger logger = LoggerFactory.getLogger(Impl.class);

		public WebSession.Handler handler;
		public Runnable function;
		public boolean renderOnly;

		Impl(WebSession.Handler aHandler, boolean doRenderOnly) {
			this.handler = aHandler;
			this.renderOnly = doRenderOnly;
		}

		public Impl(WebSession.Handler aHandler) {
			this(aHandler, false);
		}

		Impl(WebSession.Handler aHandler, final Runnable aFunction) {
			this.handler = aHandler;
			this.function = aFunction;
			this.renderOnly = false;
		}

		Impl(final WEvent.Impl other) {
			this.handler = other.handler;
			this.function = other.function;
			this.renderOnly = other.renderOnly;
		}

		Impl() {
			this.handler = null;
		}
	}

	static String concat(final String prefix, int prefixLength, String s2) {
		return prefix + s2;
	}

	static int asInt(final String v) {
		return Integer.parseInt(v);
	}

	static int asUInt(final String v) {
		return Integer.parseInt(v);
	}

	static int parseIntParameter(final WebRequest request, final String name,
			int ifMissing) {
		String p;
		if ((p = request.getParameter(name)) != null) {
			try {
				return asInt(p);
			} catch (final NumberFormatException ee) {
				logger.error(new StringWriter().append(
						"Could not cast event property '").append(name).append(
						": ").append(p).append("' to int").toString());
				return ifMissing;
			}
		} else {
			return ifMissing;
		}
	}

	static String getStringParameter(final WebRequest request, final String name) {
		String p;
		if ((p = request.getParameter(name)) != null) {
			return p;
		} else {
			return "";
		}
	}

	static void decodeTouches(String str, final List<Touch> result) {
		if (str.length() == 0) {
			return;
		}
		List<String> s = new ArrayList<String>();
		s = new ArrayList<String>(Arrays.asList(str.split(";")));
		if (s.size() % 9 != 0) {
			logger.error(new StringWriter().append(
					"Could not parse touches array '").append(str).append("'")
					.toString());
			return;
		}
		try {
			for (int i = 0; i < s.size(); i += 9) {
				result.add(new Touch(asUInt(s.get(i + 0)), asInt(s.get(i + 1)),
						asInt(s.get(i + 2)), asInt(s.get(i + 3)), asInt(s
								.get(i + 4)), asInt(s.get(i + 5)), asInt(s
								.get(i + 6)), asInt(s.get(i + 7)), asInt(s
								.get(i + 8))));
			}
		} catch (final NumberFormatException ee) {
			logger.error(new StringWriter().append(
					"Could not parse touches array '").append(str).append("'")
					.toString());
			return;
		}
	}
}
