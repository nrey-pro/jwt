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

class XSSUtils {
	static boolean isBadTag(String name) {
		return name.equalsIgnoreCase("script")
				|| name.equalsIgnoreCase("applet")
				|| name.equalsIgnoreCase("object")
				|| name.equalsIgnoreCase("iframe")
				|| name.equalsIgnoreCase("frame")
				|| name.equalsIgnoreCase("layer")
				|| name.equalsIgnoreCase("ilayer")
				|| name.equalsIgnoreCase("frameset")
				|| name.equalsIgnoreCase("link")
				|| name.equalsIgnoreCase("meta")
				|| name.equalsIgnoreCase("title")
				|| name.equalsIgnoreCase("base")
				|| name.equalsIgnoreCase("basefont")
				|| name.equalsIgnoreCase("bgsound")
				|| name.equalsIgnoreCase("head")
				|| name.equalsIgnoreCase("body")
				|| name.equalsIgnoreCase("embed")
				|| name.equalsIgnoreCase("style")
				|| name.equalsIgnoreCase("blink");
	}

	static boolean isBadAttribute(String name) {
		return StringUtils.startsWithIgnoreCase(name, "on")
				|| StringUtils.startsWithIgnoreCase(name, "data")
				|| name.equalsIgnoreCase("dynsrc")
				|| name.equalsIgnoreCase("id") || name.equalsIgnoreCase("name");
	}

	static boolean isBadAttributeValue(String name, String value) {
		if (name.equalsIgnoreCase("action")
				|| name.equalsIgnoreCase("background")
				|| name.equalsIgnoreCase("codebase")
				|| name.equalsIgnoreCase("dynsrc")
				|| name.equalsIgnoreCase("href")
				|| name.equalsIgnoreCase("src")) {
			return StringUtils.startsWithIgnoreCase(value, "javascript:")
					|| StringUtils.startsWithIgnoreCase(value, "vbscript:")
					|| StringUtils.startsWithIgnoreCase(value, "about:")
					|| StringUtils.startsWithIgnoreCase(value, "chrome:")
					|| StringUtils.startsWithIgnoreCase(value, "data:")
					|| StringUtils.startsWithIgnoreCase(value, "disk:")
					|| StringUtils.startsWithIgnoreCase(value, "hcp:")
					|| StringUtils.startsWithIgnoreCase(value, "help:")
					|| StringUtils.startsWithIgnoreCase(value, "livescript")
					|| StringUtils.startsWithIgnoreCase(value, "lynxcgi:")
					|| StringUtils.startsWithIgnoreCase(value, "lynxexec:")
					|| StringUtils.startsWithIgnoreCase(value, "ms-help:")
					|| StringUtils.startsWithIgnoreCase(value, "ms-its:")
					|| StringUtils.startsWithIgnoreCase(value, "mhtml:")
					|| StringUtils.startsWithIgnoreCase(value, "mocha:")
					|| StringUtils.startsWithIgnoreCase(value, "opera:")
					|| StringUtils.startsWithIgnoreCase(value, "res:")
					|| StringUtils.startsWithIgnoreCase(value, "resource:")
					|| StringUtils.startsWithIgnoreCase(value, "shell:")
					|| StringUtils.startsWithIgnoreCase(value, "view-source:")
					|| StringUtils.startsWithIgnoreCase(value, "vnd.ms.radio:")
					|| StringUtils.startsWithIgnoreCase(value, "wysiwyg:");
		} else {
			if (name.equalsIgnoreCase("style")) {
				return StringUtils.containsIgnoreCase(value, "absolute")
						|| StringUtils.containsIgnoreCase(value, "behaviour")
						|| StringUtils.containsIgnoreCase(value, "content")
						|| StringUtils.containsIgnoreCase(value, "expression")
						|| StringUtils.containsIgnoreCase(value, "fixed")
						|| StringUtils.containsIgnoreCase(value,
								"include-source")
						|| StringUtils.containsIgnoreCase(value, "moz-binding")
						|| StringUtils.containsIgnoreCase(value, "javascript");
			} else {
				return false;
			}
		}
	}
}