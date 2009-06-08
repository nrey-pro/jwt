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
 * A layout manager which arranges widgets vertically
 * 
 * 
 * This convenience class creates a vertical box layout, laying contained
 * widgets out from top to bottom.
 * <p>
 * See {@link WBoxLayout} for available member methods and more information.
 * <p>
 * Usage example:
 * <p>
 * <p>
 * <i><b>Note:</b>First consider if you can achieve your layout using CSS !</i>
 * </p>
 * 
 * @see WHBoxLayout
 */
public class WVBoxLayout extends WBoxLayout {
	/**
	 * Create a new vertical box layout.
	 * 
	 * Use <i>parent</i>=0 to created a layout manager that can be nested inside
	 * other layout managers.
	 */
	public WVBoxLayout(WWidget parent) {
		super(WBoxLayout.Direction.TopToBottom, parent);
	}

	public WVBoxLayout() {
		this((WWidget) null);
	}
}