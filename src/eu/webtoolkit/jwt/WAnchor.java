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
 * A widget that represents an HTML anchor (to link to other documents)
 * 
 * 
 * Use an anchor to link to another web page, document, internal application
 * path or a resource. The anchor may contain a label text, an image, or any
 * other widget (as it inherits from {@link WContainerWidget}). If you do not
 * want the application to terminate when the user follows the anchor, you must
 * use setTarget(TargetNewWindow). Even for non-HTML documents, this may be
 * important since pending AJAX requests are cancelled even if documents are not
 * served within the browser window in certain browsers.
 * <p>
 * Usage example:
 * <p>
 * The widget corresponds to the HTML <code>&lt;a&gt;</code> tag.
 * <p>
 * WAnchor is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <p>
 * <i><b>Note:</b>If you set a text or image using one of the API methods like
 * {@link WAnchor#setText(CharSequence text)} or
 * {@link WAnchor#setImage(WImage image)} or a constructor, you should not
 * attempt to remove all contents (using {@link WContainerWidget#clear()}, or
 * provide a layout (using {@link WContainerWidget#setLayout(WLayout layout)}),
 * as this will result in undefined behaviour. </i>
 * </p>
 */
public class WAnchor extends WContainerWidget {
	/**
	 * Create an anchor.
	 */
	public WAnchor(WContainerWidget parent) {
		super(parent);
		this.ref_ = "";
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
	}

	public WAnchor() {
		this((WContainerWidget) null);
	}

	/**
	 * Create an anchor referring to a URL.
	 */
	public WAnchor(String ref, WContainerWidget parent) {
		super(parent);
		this.ref_ = ref;
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
	}

	public WAnchor(String ref) {
		this(ref, (WContainerWidget) null);
	}

	/**
	 * Create an anchor referring to a resource.
	 * 
	 * A resource specifies application-dependent content, which may be
	 * generated by your application on demand.
	 * <p>
	 * The anchor does not assume ownership of the resource.
	 */
	public WAnchor(WResource resource, WContainerWidget parent) {
		super(parent);
		this.ref_ = "";
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.setResource(resource);
	}

	public WAnchor(WResource resource) {
		this(resource, (WContainerWidget) null);
	}

	/**
	 * Create an anchor referring to a URL, using a text message.
	 */
	public WAnchor(String ref, CharSequence text, WContainerWidget parent) {
		super(parent);
		this.ref_ = ref;
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.text_ = new WText(text, this);
	}

	public WAnchor(String ref, CharSequence text) {
		this(ref, text, (WContainerWidget) null);
	}

	/**
	 * Create an anchor reffering to a resource, using a text message.
	 * 
	 * A resource specifies application-dependent content, which may be
	 * generated by your application on demand.
	 * <p>
	 * The anchor does not assume ownership of the resource.
	 */
	public WAnchor(WResource resource, CharSequence text,
			WContainerWidget parent) {
		super(parent);
		this.ref_ = "";
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.text_ = new WText(text, this);
		this.setResource(resource);
	}

	public WAnchor(WResource resource, CharSequence text) {
		this(resource, text, (WContainerWidget) null);
	}

	/**
	 * Create an anchor reffering to a URL, using an image.
	 * 
	 * Ownership of the image is transferred to the anchor.
	 */
	public WAnchor(String ref, WImage image, WContainerWidget parent) {
		super(parent);
		this.ref_ = ref;
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.image_ = image;
		if (this.image_ != null) {
			this.addWidget(this.image_);
		}
	}

	public WAnchor(String ref, WImage image) {
		this(ref, image, (WContainerWidget) null);
	}

	/**
	 * Create an anchor reffering to a resource, using an image.
	 * 
	 * A resource specifies application-dependent content, which may be
	 * generated by your application on demand.
	 * <p>
	 * The anchor does not assume ownership of the resource.
	 * <p>
	 * Ownership of the image is transferred to the anchor.
	 */
	public WAnchor(WResource resource, WImage image, WContainerWidget parent) {
		super(parent);
		this.ref_ = "";
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.image_ = image;
		if (this.image_ != null) {
			this.addWidget(this.image_);
		}
		this.setResource(resource);
	}

	public WAnchor(WResource resource, WImage image) {
		this(resource, image, (WContainerWidget) null);
	}

	public void remove() {
		/* delete this.changeInternalPathJS_ */;
		super.remove();
	}

	/**
	 * Set the destination URL.
	 * 
	 * This method should not be used when the anchor has been pointed to a
	 * dynamically generated resource using
	 * {@link WAnchor#setResource(WResource resource)}.
	 * <p>
	 * 
	 * @see WAnchor#setResource(WResource resource)
	 * @see WAnchor#setRefInternalPath(String path)
	 */
	public void setRef(String ref) {
		if (!this.ref_.equals(ref)) {
			this.ref_ = ref;
			this.flags_.set(BIT_REF_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
		}
	}

	/**
	 * Set the destination URL to an internal path.
	 * 
	 * Sets the anchor to point to the internal path <i>path</i>. When the
	 * anchor is activated, the internal path is set to <i>path</i>, and the
	 * {@link WApplication#internalPathChanged()} signal is emitted.
	 * <p>
	 * This is the easiest way to let the application participate in browser
	 * history, and generate URLs that are bookmarkable and search engine
	 * friendly.
	 * <p>
	 * Internally, this method sets the destination URL using:
	 * <p>
	 * The {@link WInteractWidget#clicked()} signal is connected to a slot that
	 * changes the internal path by calling
	 * <p>
	 * 
	 * @see WAnchor#setRef(String ref)
	 * @see WAnchor#setResource(WResource resource)
	 * @see WApplication#getBookmarkUrl()
	 * @see WApplication#setInternalPath(String path, boolean emitChange)
	 */
	public void setRefInternalPath(String path) {
		WApplication app = WApplication.instance();
		String r = app.getBookmarkUrl(path);
		if (r.equals(this.ref_)) {
			return;
		}
		this.ref_ = r;
		if (app.getEnvironment().hasAjax()) {
			if (!(this.changeInternalPathJS_ != null)) {
				this.changeInternalPathJS_ = new JSlot();
				this.clicked().addListener(this.changeInternalPathJS_);
				this.clicked().setPreventDefault(true);
			}
			this.changeInternalPathJS_
					.setJavaScript("function(obj, event){window.location.hash='#"
							+ DomElement.urlEncodeS(path) + "';}");
			this.clicked().senderRepaint();
		}
		this.flags_.set(BIT_REF_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
	}

	/**
	 * Returns the destination URL.
	 * 
	 * When the anchor refers to a resource, the current resource URL is
	 * returned. When the anchor refers to an internal path, the internal path
	 * is returned. Otherwise, the URL is returned that was set using
	 * {@link WAnchor#setRef(String ref)}.
	 * <p>
	 * 
	 * @see WAnchor#setRef(String ref)
	 * @see WResource#generateUrl()
	 */
	public String getRef() {
		return this.ref_;
	}

	/**
	 * Set a destination resource.
	 * 
	 * A resource specifies application-dependent content, which may be
	 * generated by your application on demand.
	 * <p>
	 * This sets the <i>resource</i> as the destination of the anchor, and is an
	 * alternative to {@link WAnchor#setRef(String ref)}. The resource may be
	 * cleared by passing <i>resource</i> = 0.
	 * <p>
	 * The anchor does not assume ownership of the resource.
	 * <p>
	 * 
	 * @see WAnchor#setRef(String ref)
	 */
	public void setResource(WResource resource) {
		this.resource_ = resource;
		if (this.resource_ != null) {
			this.resource_.dataChanged().addListener(this,
					new Signal.Listener() {
						public void trigger() {
							WAnchor.this.resourceChanged();
						}
					});
			this.setRef(this.resource_.generateUrl());
		}
	}

	/**
	 * Returns the destination resource.
	 * 
	 * Returns 0 if no resource has been set.
	 * <p>
	 * 
	 * @see WAnchor#setResource(WResource resource)
	 */
	public WResource getResource() {
		return this.resource_;
	}

	/**
	 * Sets a text label.
	 * 
	 * If no text was previously set, a new {@link WText} widget is added using
	 * {@link WContainerWidget#addWidget(WWidget widget)}.
	 */
	public void setText(CharSequence text) {
		if (!(this.text_ != null)) {
			this.text_ = new WText(text, this);
		} else {
			if (!(text.length() == 0)) {
				this.text_.setText(text);
			} else {
				if (this.text_ != null)
					this.text_.remove();
				this.text_ = null;
			}
		}
	}

	/**
	 * Returns the label text.
	 * 
	 * Returns an empty string if no label was set.
	 * <p>
	 * 
	 * @see WAnchor#setText(CharSequence text)
	 */
	public WString getText() {
		if (this.text_ != null) {
			return this.text_.getText();
		} else {
			return empty;
		}
	}

	/**
	 * Configure text word wrapping.
	 * 
	 * When <i>on</i> is true, the text set with
	 * {@link WAnchor#setText(CharSequence text)} may be broken up over multiple
	 * lines. When <i>on</i> is false, the text will displayed on a single line,
	 * unless the text contains &lt;br /&gt; tags or other block-level tags.
	 * <p>
	 * The default value is true.
	 * <p>
	 * 
	 * @see WAnchor#hasWordWrap()
	 */
	public void setWordWrap(boolean on) {
		if (!(this.text_ != null)) {
			this.text_ = new WText(this);
		}
		this.text_.setWordWrap(on);
	}

	/**
	 * Returns whether the widget may break lines.
	 * 
	 * @see WAnchor#setWordWrap(boolean on)
	 */
	public boolean hasWordWrap() {
		return this.text_ != null ? this.text_.isWordWrap() : true;
	}

	/**
	 * Set an image.
	 * 
	 * If an image was previously set, it is deleted. The <i>image</i> is added
	 * using {@link WContainerWidget#addWidget(WWidget widget)}.
	 * <p>
	 * Ownership of the image is transferred to the anchor.
	 */
	public void setImage(WImage image) {
		if (this.image_ != null) {
			if (this.image_ != null)
				this.image_.remove();
		}
		this.image_ = image;
		if (this.image_ != null) {
			this.addWidget(this.image_);
		}
	}

	/**
	 * Returns the image.
	 * 
	 * Returns 0 if no image is set.
	 * <p>
	 * 
	 * @see WAnchor#setImage(WImage image)
	 */
	public WImage getImage() {
		return this.image_;
	}

	/**
	 * Set the location where the referred content should be displayed.
	 * 
	 * By default, the referred content is displayed in the application (
	 * {@link AnchorTarget#TargetSelf}). When the destination is an HTML
	 * document, the application is replaced with the new document. When the
	 * reference is a document that cannot be displayed in the browser, it is
	 * offered for download or opened using an external program, depending on
	 * browser settings.
	 * <p>
	 * By setting <i>target</i> to {@link AnchorTarget#TargetNewWindow}, the
	 * destination is displayed in a new browser window or tab.
	 * <p>
	 * 
	 * @see WAnchor#getTarget()
	 */
	public void setTarget(AnchorTarget target) {
		if (this.target_ != target) {
			this.target_ = target;
			this.flags_.set(BIT_TARGET_CHANGED);
		}
	}

	/**
	 * Returns the location where the referred content should be displayed.
	 * 
	 * @see WAnchor#setTarget(AnchorTarget target)
	 */
	public AnchorTarget getTarget() {
		return this.target_;
	}

	private static final int BIT_REF_CHANGED = 0;
	private static final int BIT_TARGET_CHANGED = 1;
	private String ref_;
	private WResource resource_;
	private WText text_;
	private WImage image_;
	private AnchorTarget target_;
	private BitSet flags_;
	private JSlot changeInternalPathJS_;

	private void resourceChanged() {
		this.setRef(this.resource_.generateUrl());
	}

	protected void updateDom(DomElement element, boolean all) {
		if (this.flags_.get(BIT_REF_CHANGED) || all) {
			String uri = this.ref_;
			element.setAttribute("href", fixRelativeUrl(uri));
			this.flags_.clear(BIT_REF_CHANGED);
		}
		if (this.flags_.get(BIT_TARGET_CHANGED) || all) {
			switch (this.target_) {
			case TargetSelf:
				if (!all) {
					element.setAttribute("target", "_self");
				}
				break;
			case TargetThisWindow:
				element.setAttribute("target", "_top");
				break;
			case TargetNewWindow:
				element.setAttribute("target", "_blank");
			}
			this.flags_.clear(BIT_TARGET_CHANGED);
		}
		super.updateDom(element, all);
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_A;
	}

	protected void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_REF_CHANGED);
		this.flags_.clear(BIT_TARGET_CHANGED);
		super.propagateRenderOk(deep);
	}

	protected static WString empty = new WString("");
}