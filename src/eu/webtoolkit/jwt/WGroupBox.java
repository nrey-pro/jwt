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
 * A widget which group widgets into a frame with a title
 * 
 * 
 * This is typically used in a form to group certain form elements together.
 * <p>
 * Usage example:
 * <p>
 * <p>
 * The widget corresponds to the HTML <code>&lt;fieldset&gt;</code> tag, and the
 * title in a nested <code>&lt;legend&gt;</code> tag.
 * <p>
 * Like {@link WContainerWidget}, WGroupBox is by default a block level widget.
 * <p>
 */
public class WGroupBox extends WContainerWidget {
	/**
	 * Create a groupbox with empty title.
	 */
	public WGroupBox(WContainerWidget parent) {
		super(parent);
		this.title_ = new WString();
		this.titleChanged_ = false;
	}

	public WGroupBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a groupbox with given title message.
	 */
	public WGroupBox(CharSequence title, WContainerWidget parent) {
		super(parent);
		this.title_ = new WString(title);
		this.titleChanged_ = false;
	}

	public WGroupBox(CharSequence title) {
		this(title, (WContainerWidget) null);
	}

	/**
	 * Get the title.
	 */
	public WString getTitle() {
		return this.title_;
	}

	/**
	 * Set the title.
	 */
	public void setTitle(CharSequence title) {
		this.title_ = WString.toWString(title);
		this.titleChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	public void refresh() {
		if (this.title_.refresh()) {
			this.titleChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		super.refresh();
	}

	private WString title_;
	private boolean titleChanged_;

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_FIELDSET;
	}

	protected void updateDom(DomElement element, boolean all) {
		if (all) {
			DomElement legend = DomElement
					.createNew(DomElementType.DomElement_LEGEND);
			legend.setId(this.getFormName() + "l");
			legend.setProperty(Property.PropertyInnerHTML, escapeText(
					this.title_).toString());
			element.addChild(legend);
			this.titleChanged_ = false;
		}
		super.updateDom(element, all);
	}

	protected void getDomChanges(List<DomElement> result, WApplication app) {
		DomElement e = DomElement.getForUpdate(this, this.getDomElementType());
		this.updateDom(e, false);
		result.add(e);
		if (this.titleChanged_) {
			DomElement legend = DomElement.getForUpdate(this.getFormName()
					+ "l", DomElementType.DomElement_LEGEND);
			legend.setProperty(Property.PropertyInnerHTML, escapeText(
					this.title_).toString());
			this.titleChanged_ = false;
			result.add(legend);
		}
	}

	protected void propagateRenderOk(boolean deep) {
		this.titleChanged_ = false;
		super.propagateRenderOk(deep);
	}

	protected int getFirstChildIndex() {
		return 1;
	}
}