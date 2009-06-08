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

class ExtremesIterator extends SeriesIterator {
	public ExtremesIterator(Axis axis) {
		super();
		this.axis_ = axis;
		this.minimum_ = WAxis.AUTO_MINIMUM;
		this.maximum_ = WAxis.AUTO_MAXIMUM;
	}

	public boolean startSeries(WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		return series.getAxis() == this.axis_;
	}

	public void newValue(WDataSeries series, double x, double y, double stackY) {
		if (!myisnan(y)) {
			this.maximum_ = Math.max(y, this.maximum_);
			this.minimum_ = Math.min(y, this.minimum_);
		}
	}

	public double getMinimum() {
		return this.minimum_;
	}

	public double getMaximum() {
		return this.maximum_;
	}

	private Axis axis_;
	private double minimum_;
	private double maximum_;
}