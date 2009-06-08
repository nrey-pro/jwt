package eu.webtoolkit.jwt.chart;

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
 * Enumeration that specifies options for the labels.
 * 
 * @see WPieChart#setDisplayLabels(EnumSet options)
 */
public enum LabelOption {
	/**
	 * Do not display labels (default).
	 */
	NoLabels,
	/**
	 * Display labels inside each segment.
	 */
	Inside,
	/**
	 * Display labels outside each segment.
	 */
	Outside,
	/**
	 * Display the label text.
	 */
	TextLabel,
	/**
	 * Display the value (as percentage).
	 */
	TextPercentage;

	public int getValue() {
		return ordinal();
	}
}