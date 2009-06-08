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

enum DomElementType {
	DomElement_A, DomElement_BR, DomElement_BUTTON, DomElement_COL, DomElement_DIV, DomElement_FIELDSET, DomElement_FORM, DomElement_H1, DomElement_H2, DomElement_H3, DomElement_H4, DomElement_H5, DomElement_H6, DomElement_IFRAME, DomElement_IMG, DomElement_INPUT, DomElement_LABEL, DomElement_LEGEND, DomElement_LI, DomElement_OL, DomElement_OPTION, DomElement_UL, DomElement_SCRIPT, DomElement_SELECT, DomElement_SPAN, DomElement_TABLE, DomElement_TBODY, DomElement_THEAD, DomElement_TH, DomElement_TD, DomElement_TEXTAREA, DomElement_TR, DomElement_P, DomElement_CANVAS, DomElement_MAP, DomElement_AREA;

	public int getValue() {
		return ordinal();
	}
}