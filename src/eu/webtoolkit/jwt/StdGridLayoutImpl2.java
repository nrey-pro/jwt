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

class StdGridLayoutImpl2 extends StdLayoutImpl {
	private static Logger logger = LoggerFactory
			.getLogger(StdGridLayoutImpl2.class);

	public StdGridLayoutImpl2(WLayout layout, final Grid grid) {
		super(layout);
		this.grid_ = grid;
		this.needAdjust_ = false;
		this.needRemeasure_ = false;
		this.needConfigUpdate_ = false;
		this.addedItems_ = new ArrayList<WLayoutItem>();
		this.removedItems_ = new ArrayList<String>();
		String THIS_JS = "js/StdGridLayoutImpl2.js";
		WApplication app = WApplication.getInstance();
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.getStyleSheet().addRule("table.Wt-hcenter",
					"margin: 0px auto;position: relative");
			app.loadJavaScript(THIS_JS, wtjs1());
			app.loadJavaScript(THIS_JS, appjs1());
			app.doJavaScript(app.getJavaScriptClass()
					+ ".layouts2.scheduleAdjust();");
			app.doJavaScript("$(window).load(function() { "
					+ app.getJavaScriptClass() + ".layouts2.scheduleAdjust();"
					+ "});");
			WApplication.getInstance().addAutoJavaScript(
					app.getJavaScriptClass() + ".layouts2.adjustNow();");
		}
	}

	public int getMinimumWidth() {
		final int colCount = this.grid_.columns_.size();
		int total = 0;
		for (int i = 0; i < colCount; ++i) {
			total += this.minimumWidthForColumn(i);
		}
		return total + (colCount - 1) * this.grid_.horizontalSpacing_;
	}

	public int getMinimumHeight() {
		final int rowCount = this.grid_.rows_.size();
		int total = 0;
		for (int i = 0; i < rowCount; ++i) {
			total += this.minimumHeightForRow(i);
		}
		return total + (rowCount - 1) * this.grid_.verticalSpacing_;
	}

	public void updateAddItem(WLayoutItem item) {
		super.updateAddItem(item);
		this.addedItems_.add(item);
	}

	public void updateRemoveItem(WLayoutItem item) {
		super.updateRemoveItem(item);
		this.addedItems_.remove(item);
		this.removedItems_.add(getImpl(item).getId());
	}

	public void update(WLayoutItem item) {
		WContainerWidget c = this.getContainer();
		if (c != null) {
			c.layoutChanged(false, false);
		}
		this.needConfigUpdate_ = true;
	}

	public DomElement createDomElement(boolean fitWidth, boolean fitHeight,
			WApplication app) {
		this.needAdjust_ = this.needConfigUpdate_ = this.needRemeasure_ = false;
		this.addedItems_.clear();
		this.removedItems_.clear();
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		int[] margin = { 0, 0, 0, 0 };
		int maxWidth = 0;
		int maxHeight = 0;
		if (this.getLayout().getParentLayout() == null) {
			margin[3] = this.getLayout().getContentsMargin(Side.Left);
			margin[0] = this.getLayout().getContentsMargin(Side.Top);
			margin[1] = this.getLayout().getContentsMargin(Side.Right);
			margin[2] = this.getLayout().getContentsMargin(Side.Bottom);
			maxWidth = pixelSize(this.getContainer().getMaximumWidth());
			maxHeight = pixelSize(this.getContainer().getMaximumHeight());
		}
		StringBuilder js = new StringBuilder();
		js.append(app.getJavaScriptClass()).append(
				".layouts2.add(new Wt3_3_2.StdLayout2(").append(
				app.getJavaScriptClass()).append(",'").append(this.getId())
				.append("',");
		if (this.getLayout().getParentLayout() != null) {
			js.append("'").append(
					getImpl(this.getLayout().getParentLayout()).getId())
					.append("',");
		} else {
			js.append("null,");
		}
		boolean progressive = !app.getEnvironment().hasAjax();
		js.append(fitWidth ? '1' : '0').append(",").append(
				fitHeight ? '1' : '0').append(",").append(
				progressive ? '1' : '0').append(",");
		js.append(maxWidth).append(",").append(maxHeight).append(",[").append(
				this.grid_.horizontalSpacing_).append(",").append(margin[3])
				.append(",").append(margin[1]).append("],[").append(
						this.grid_.verticalSpacing_).append(",").append(
						margin[0]).append(",").append(margin[2]).append("],");
		this.streamConfig(js, app);
		DomElement div = DomElement.createNew(DomElementType.DomElement_DIV);
		div.setId(this.getId());
		div.setProperty(Property.PropertyStylePosition, "relative");
		DomElement table = null;
		DomElement tbody = null;
		DomElement tr = null;
		if (progressive) {
			table = DomElement.createNew(DomElementType.DomElement_TABLE);
			StringBuilder style = new StringBuilder();
			if (maxWidth != 0) {
				style.append("max-width: ").append(maxWidth).append("px;");
			}
			if (maxHeight != 0) {
				style.append("max-height: ").append(maxHeight).append("px;");
			}
			style.append("width: 100%;");
			table.setProperty(Property.PropertyStyle, style.toString());
			int totalColStretch = 0;
			for (int col = 0; col < colCount; ++col) {
				totalColStretch += Math.max(0,
						this.grid_.columns_.get(col).stretch_);
			}
			for (int col = 0; col < colCount; ++col) {
				DomElement c = DomElement
						.createNew(DomElementType.DomElement_COL);
				int stretch = Math
						.max(0, this.grid_.columns_.get(col).stretch_);
				if (stretch != 0 || totalColStretch == 0) {
					char[] buf = new char[30];
					double pct = totalColStretch == 0 ? 100.0 / colCount
							: 100.0 * stretch / totalColStretch;
					StringBuilder ss = new StringBuilder();
					ss.append("width:").append(MathUtils.roundCss(pct, 2))
							.append("%;");
					c.setProperty(Property.PropertyStyle, ss.toString());
				}
				table.addChild(c);
			}
			tbody = DomElement.createNew(DomElementType.DomElement_TBODY);
		}
		List<Boolean> overSpanned = new ArrayList<Boolean>();
		{
			int insertPos = 0;
			for (int ii = 0; ii < colCount * rowCount; ++ii)
				overSpanned.add(insertPos + ii, false);
		}
		;
		int prevRowWithItem = -1;
		for (int row = 0; row < rowCount; ++row) {
			if (table != null) {
				tr = DomElement.createNew(DomElementType.DomElement_TR);
			}
			boolean rowVisible = false;
			int prevColumnWithItem = -1;
			for (int col = 0; col < colCount; ++col) {
				final Grid.Item item = this.grid_.items_.get(row).get(col);
				if (!overSpanned.get(row * colCount + col)) {
					for (int i = 0; i < item.rowSpan_; ++i) {
						for (int j = 0; j < item.colSpan_; ++j) {
							if (i + j > 0) {
								overSpanned.set((row + i) * colCount + col + j,
										true);
							}
						}
					}
					AlignmentFlag hAlign = EnumUtils.enumFromSet(EnumUtils
							.mask(item.alignment_,
									AlignmentFlag.AlignHorizontalMask));
					AlignmentFlag vAlign = EnumUtils.enumFromSet(EnumUtils
							.mask(item.alignment_,
									AlignmentFlag.AlignVerticalMask));
					DomElement td = null;
					if (table != null) {
						boolean itemVisible = this.hasItem(row, col);
						rowVisible = rowVisible || itemVisible;
						td = DomElement.createNew(DomElementType.DomElement_TD);
						if (itemVisible) {
							int[] padding = { 0, 0, 0, 0 };
							int nextRow = this.nextRowWithItem(row, col);
							int prevRow = prevRowWithItem;
							int nextCol = this.nextColumnWithItem(row, col);
							int prevCol = prevColumnWithItem;
							if (prevRow == -1) {
								padding[0] = margin[0];
							} else {
								padding[0] = (this.grid_.verticalSpacing_ + 1) / 2;
							}
							if (nextRow == (int) rowCount) {
								padding[2] = margin[2];
							} else {
								padding[2] = this.grid_.verticalSpacing_ / 2;
							}
							if (prevCol == -1) {
								padding[3] = margin[3];
							} else {
								padding[3] = (this.grid_.horizontalSpacing_ + 1) / 2;
							}
							if (nextCol == (int) colCount) {
								padding[1] = margin[1];
							} else {
								padding[1] = this.grid_.horizontalSpacing_ / 2;
							}
							StringBuilder style = new StringBuilder();
							if (app.getLayoutDirection() == LayoutDirection.RightToLeft) {
								int tmp = padding[1];
								padding[1] = padding[3];
								padding[3] = tmp;
							}
							if (padding[0] == padding[1]
									&& padding[0] == padding[2]
									&& padding[0] == padding[3]) {
								if (padding[0] != 0) {
									style.append("padding:").append(padding[0])
											.append("px;");
								}
							} else {
								style.append("padding:").append(padding[0])
										.append("px ").append(padding[1])
										.append("px ").append(padding[2])
										.append("px ").append(padding[3])
										.append("px;");
							}
							if (vAlign != null) {
								switch (vAlign) {
								case AlignTop:
									style.append("vertical-align:top;");
									break;
								case AlignMiddle:
									style.append("vertical-align:middle;");
									break;
								case AlignBottom:
									style.append("vertical-align:bottom;");
								default:
									break;
								}
							}
							td.setProperty(Property.PropertyStyle, style
									.toString());
							if (item.rowSpan_ != 1) {
								td.setProperty(Property.PropertyRowSpan, String
										.valueOf(item.rowSpan_));
							}
							if (item.colSpan_ != 1) {
								td.setProperty(Property.PropertyColSpan, String
										.valueOf(item.colSpan_));
							}
							prevColumnWithItem = col;
						}
					}
					DomElement c = null;
					if (!(table != null)) {
						if (item.item_ != null) {
							c = this.createElement(item.item_, app);
							div.addChild(c);
						}
					} else {
						if (item.item_ != null) {
							c = getImpl(item.item_).createDomElement(true,
									true, app);
						}
					}
					if (table != null) {
						if (c != null) {
							if (!app.getEnvironment().agentIsIElt(9)) {
								c.setProperty(Property.PropertyStyleBoxSizing,
										"border-box");
							}
							if (hAlign == null) {
								hAlign = AlignmentFlag.AlignJustify;
							}
							switch (hAlign) {
							case AlignCenter: {
								DomElement itable = DomElement
										.createNew(DomElementType.DomElement_TABLE);
								itable.setProperty(Property.PropertyClass,
										"Wt-hcenter");
								if (vAlign == null) {
									itable.setProperty(Property.PropertyStyle,
											"height:100%;");
								}
								DomElement irow = DomElement
										.createNew(DomElementType.DomElement_TR);
								DomElement itd = DomElement
										.createNew(DomElementType.DomElement_TD);
								if (vAlign == null) {
									itd.setProperty(Property.PropertyStyle,
											"height:100%;");
								}
								boolean haveMinWidth = c.getProperty(
										Property.PropertyStyleMinWidth)
										.length() != 0;
								itd.addChild(c);
								if (app.getEnvironment().agentIsIElt(9)) {
									if (haveMinWidth) {
										DomElement spacer = DomElement
												.createNew(DomElementType.DomElement_DIV);
										spacer
												.setProperty(
														Property.PropertyStyleWidth,
														c
																.getProperty(Property.PropertyStyleMinWidth));
										spacer.setProperty(
												Property.PropertyStyleHeight,
												"1px");
										itd.addChild(spacer);
									}
								}
								irow.addChild(itd);
								itable.addChild(irow);
								c = itable;
								break;
							}
							case AlignRight:
								if (!c.isDefaultInline()) {
									c.setProperty(Property.PropertyStyleFloat,
											"right");
								} else {
									td.setProperty(
											Property.PropertyStyleTextAlign,
											"right");
								}
								break;
							case AlignLeft:
								if (!c.isDefaultInline()) {
									c.setProperty(Property.PropertyStyleFloat,
											"left");
								} else {
									td.setProperty(
											Property.PropertyStyleTextAlign,
											"left");
								}
								break;
							default:
								break;
							}
							boolean haveMinWidth = c.getProperty(
									Property.PropertyStyleMinWidth).length() != 0;
							td.addChild(c);
							if (app.getEnvironment().agentIsIElt(9)) {
								if (haveMinWidth) {
									DomElement spacer = DomElement
											.createNew(DomElementType.DomElement_DIV);
									spacer
											.setProperty(
													Property.PropertyStyleWidth,
													c
															.getProperty(Property.PropertyStyleMinWidth));
									spacer
											.setProperty(
													Property.PropertyStyleHeight,
													"1px");
									td.addChild(spacer);
								}
							}
						}
						tr.addChild(td);
					}
				}
			}
			if (tr != null) {
				if (!rowVisible) {
					tr.setProperty(Property.PropertyStyleDisplay, "hidden");
				} else {
					prevRowWithItem = row;
				}
				tbody.addChild(tr);
			}
		}
		js.append("));");
		if (table != null) {
			table.addChild(tbody);
			div.addChild(table);
		}
		div.callJavaScript(js.toString());
		return div;
	}

	public void updateDom(final DomElement parent) {
		WApplication app = WApplication.getInstance();
		if (this.needConfigUpdate_) {
			this.needConfigUpdate_ = false;
			DomElement div = DomElement.getForUpdate(this,
					DomElementType.DomElement_DIV);
			for (int i = 0; i < this.addedItems_.size(); ++i) {
				WLayoutItem item = this.addedItems_.get(i);
				DomElement c = this.createElement(item, app);
				div.addChild(c);
			}
			this.addedItems_.clear();
			for (int i = 0; i < this.removedItems_.size(); ++i) {
				parent.callJavaScript("Wt3_3_2.remove('"
						+ this.removedItems_.get(i) + "');", true);
			}
			this.removedItems_.clear();
			parent.addChild(div);
			StringBuilder js = new StringBuilder();
			js.append(app.getJavaScriptClass()).append(
					".layouts2.updateConfig('").append(this.getId()).append(
					"',");
			this.streamConfig(js, app);
			js.append(");");
			app.doJavaScript(js.toString());
			this.needRemeasure_ = false;
			this.needAdjust_ = false;
		}
		if (this.needRemeasure_) {
			this.needRemeasure_ = false;
			StringBuilder js = new StringBuilder();
			js.append(app.getJavaScriptClass()).append(".layouts2.setDirty('")
					.append(this.getId()).append("');");
			app.doJavaScript(js.toString());
		}
		if (this.needAdjust_) {
			this.needAdjust_ = false;
			StringBuilder js = new StringBuilder();
			js.append(app.getJavaScriptClass()).append(".layouts2.adjust('")
					.append(this.getId()).append("', [");
			boolean first = true;
			final int colCount = this.grid_.columns_.size();
			final int rowCount = this.grid_.rows_.size();
			for (int row = 0; row < rowCount; ++row) {
				for (int col = 0; col < colCount; ++col) {
					if (this.grid_.items_.get(row).get(col).update_) {
						this.grid_.items_.get(row).get(col).update_ = false;
						if (!first) {
							js.append(",");
						}
						first = false;
						js.append("[").append((int) row).append(",").append(
								(int) col).append("]");
					}
				}
			}
			js.append("]);");
			app.doJavaScript(js.toString());
		}
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		for (int i = 0; i < rowCount; ++i) {
			for (int j = 0; j < colCount; ++j) {
				WLayoutItem item = this.grid_.items_.get(i).get(j).item_;
				if (item != null) {
					WLayout nested = item.getLayout();
					if (nested != null) {
						(((nested.getImpl()) instanceof StdLayoutImpl ? (StdLayoutImpl) (nested
								.getImpl())
								: null)).updateDom(parent);
					}
				}
			}
		}
	}

	public void setHint(final String name, final String value) {
		logger.error(new StringWriter().append("unrecognized hint '").append(
				name).append("'").toString());
	}

	public boolean itemResized(WLayoutItem item) {
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		for (int row = 0; row < rowCount; ++row) {
			for (int col = 0; col < colCount; ++col) {
				if (this.grid_.items_.get(row).get(col).item_ == item
						&& !this.grid_.items_.get(row).get(col).update_) {
					this.grid_.items_.get(row).get(col).update_ = true;
					this.needAdjust_ = true;
					return true;
				}
			}
		}
		return false;
	}

	public boolean isParentResized() {
		if (!this.needRemeasure_) {
			this.needRemeasure_ = true;
			return true;
		} else {
			return false;
		}
	}

	void containerAddWidgets(WContainerWidget container) {
		super.containerAddWidgets(container);
		if (!(container != null)) {
			return;
		}
		WApplication app = WApplication.getInstance();
		if (this.getParentLayoutImpl() == null) {
			if (container == app.getRoot()) {
				app.setBodyClass(app.getBodyClass() + " Wt-layout");
				app.setHtmlClass(app.getHtmlClass() + " Wt-layout");
			}
		}
	}

	private final Grid grid_;
	private boolean needAdjust_;
	private boolean needRemeasure_;
	private boolean needConfigUpdate_;
	private List<WLayoutItem> addedItems_;
	private List<String> removedItems_;

	private int nextRowWithItem(int row, int c) {
		for (row += this.grid_.items_.get(row).get(c).rowSpan_; row < (int) this.grid_.rows_
				.size(); ++row) {
			for (int col = 0; col < this.grid_.columns_.size(); col += this.grid_.items_
					.get(row).get(col).colSpan_) {
				if (this.hasItem(row, col)) {
					return row;
				}
			}
		}
		return this.grid_.rows_.size();
	}

	private int nextColumnWithItem(int row, int col) {
		for (;;) {
			col = col + this.grid_.items_.get(row).get(col).colSpan_;
			if (col < (int) this.grid_.columns_.size()) {
				for (int i = 0; i < this.grid_.rows_.size(); ++i) {
					if (this.hasItem(i, col)) {
						return col;
					}
				}
			} else {
				return this.grid_.columns_.size();
			}
		}
	}

	private boolean hasItem(int row, int col) {
		WLayoutItem item = this.grid_.items_.get(row).get(col).item_;
		if (item != null) {
			WWidget w = item.getWidget();
			return !(w != null) || !w.isHidden();
		} else {
			return false;
		}
	}

	private int minimumHeightForRow(int row) {
		int minHeight = 0;
		final int colCount = this.grid_.columns_.size();
		for (int j = 0; j < colCount; ++j) {
			WLayoutItem item = this.grid_.items_.get(row).get(j).item_;
			if (item != null) {
				minHeight = Math.max(minHeight, getImpl(item)
						.getMinimumHeight());
			}
		}
		return minHeight;
	}

	private int minimumWidthForColumn(int col) {
		int minWidth = 0;
		final int rowCount = this.grid_.rows_.size();
		for (int i = 0; i < rowCount; ++i) {
			WLayoutItem item = this.grid_.items_.get(i).get(col).item_;
			if (item != null) {
				minWidth = Math.max(minWidth, getImpl(item).getMinimumWidth());
			}
		}
		return minWidth;
	}

	private static int pixelSize(final WLength size) {
		if (size.getUnit() == WLength.Unit.Percentage) {
			return 0;
		} else {
			return (int) size.toPixels();
		}
	}

	private void streamConfig(final StringBuilder js,
			final List<Grid.Section> sections, boolean rows, WApplication app) {
		js.append("[");
		for (int i = 0; i < sections.size(); ++i) {
			if (i != 0) {
				js.append(",");
			}
			js.append("[").append(sections.get(i).stretch_).append(",");
			if (sections.get(i).resizable_) {
				SizeHandle.loadJavaScript(app);
				js.append("[");
				final WLength size = sections.get(i).initialSize_;
				if (size.isAuto()) {
					js.append("-1");
				} else {
					if (size.getUnit() == WLength.Unit.Percentage) {
						js.append(size.getValue()).append(",1");
					} else {
						js.append(size.toPixels());
					}
				}
				js.append("],");
			} else {
				js.append("0,");
			}
			if (rows) {
				js.append(this.minimumHeightForRow(i));
			} else {
				js.append(this.minimumWidthForColumn(i));
			}
			js.append("]");
		}
		js.append("]");
	}

	private void streamConfig(final StringBuilder js, WApplication app) {
		js.append("{ rows:");
		this.streamConfig(js, this.grid_.rows_, true, app);
		js.append(", cols:");
		this.streamConfig(js, this.grid_.columns_, false, app);
		js.append(", items: [");
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		for (int row = 0; row < rowCount; ++row) {
			for (int col = 0; col < colCount; ++col) {
				final Grid.Item item = this.grid_.items_.get(row).get(col);
				AlignmentFlag hAlign = EnumUtils.enumFromSet(EnumUtils.mask(
						item.alignment_, AlignmentFlag.AlignHorizontalMask));
				AlignmentFlag vAlign = EnumUtils.enumFromSet(EnumUtils.mask(
						item.alignment_, AlignmentFlag.AlignVerticalMask));
				if (row + col != 0) {
					js.append(",");
				}
				if (item.item_ != null) {
					String id = getImpl(item.item_).getId();
					js.append("{");
					if (item.colSpan_ != 1 || item.rowSpan_ != 1) {
						js.append("span: [").append(item.colSpan_).append(",")
								.append(item.rowSpan_).append("],");
					}
					if (!item.alignment_.isEmpty()) {
						int align = 0;
						if (hAlign != null) {
							switch (hAlign) {
							case AlignLeft:
								align |= 0x1;
								break;
							case AlignRight:
								align |= 0x2;
								break;
							case AlignCenter:
								align |= 0x4;
								break;
							default:
								break;
							}
						}
						if (vAlign != null) {
							switch (vAlign) {
							case AlignTop:
								align |= 0x10;
								break;
							case AlignBottom:
								align |= 0x20;
								break;
							case AlignMiddle:
								align |= 0x40;
								break;
							default:
								break;
							}
						}
						js.append("align:").append((int) align).append(",");
					}
					js
							.append("dirty:")
							.append(
									this.grid_.items_.get(row).get(col).update_ ? 2
											: 0).append(",id:'").append(id)
							.append("'").append("}");
					this.grid_.items_.get(row).get(col).update_ = 0 != 0;
				} else {
					js.append("null");
				}
			}
		}
		js.append("]}");
	}

	private DomElement createElement(WLayoutItem item, WApplication app) {
		DomElement c = getImpl(item).createDomElement(true, true, app);
		c.setProperty(Property.PropertyStyleVisibility, "hidden");
		return c;
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"StdLayout2",
				"function(F,D,K,V,W,X,aa,q,E,z,A){function x(a){var b,c;b=0;for(c=C.items.length;b<c;++b){var e=C.items[b];if(e&&e.id==a)return e}return null}function B(a,b,c,e){function i(h){return h==\"visible\"||h==\"none\"}var l=s[b],k=b?a.scrollHeight:a.scrollWidth,u,d;if(b==0){var j=f.pxself(a,l.left);if(k+j>e.clientWidth||k+j==e.clientWidth&&f.isGecko&&e.parentNode.parentNode.style.visibility===\"hidden\"){u=a.style[l.left];v(a,l.left,\"-1000000px\");k=b?a.scrollHeight: a.scrollWidth}}e=b?a.clientHeight:a.clientWidth;if(f.isGecko&&!a.style[l.size]&&b==0&&i(f.css(a,\"overflow\"))){d=a.style[l.size];v(a,l.size,\"\")}j=b?a.offsetHeight:a.offsetWidth;u&&v(a,l.left,u);d&&v(a,l.size,d);if(e>=1E6)e-=1E6;if(k>=1E6)k-=1E6;if(j>=1E6)j-=1E6;if(k===0){k=f.pxself(a,l.size);if(k!==0&&!f.isOpera&&!f.isGecko)k-=f.px(a,\"border\"+l.Left+\"Width\")+f.px(a,\"border\"+l.Right+\"Width\")}if(f.isIE&&(f.hasTag(a,\"BUTTON\")||f.hasTag(a,\"TEXTAREA\")||f.hasTag(a,\"INPUT\")||f.hasTag(a,\"SELECT\")))k=e;if(k> j)if(f.pxself(a,l.size)==0)k=e;else{var m=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!==\"none\")m=true});if(m)k=e}d=f.px(a,\"border\"+l.Left+\"Width\")+f.px(a,\"border\"+l.Right+\"Width\");u=j-(e+d)!=0;if(c)return[k,scrollBar];if(f.isGecko&&b==0&&a.getBoundingClientRect().width!=Math.ceil(a.getBoundingClientRect().width))k+=1;if(!f.boxSizing(a)&&!f.isOpera)k+=d;k+=f.px(a,\"margin\"+l.Left)+f.px(a,\"margin\"+l.Right);if(!f.boxSizing(a)&&!f.isIE)k+=f.px(a,\"padding\"+l.Left)+f.px(a,\"padding\"+ l.Right);k+=j-(e+d);if(k<j)k=j;a=f.px(a,\"max\"+l.Size);if(a>0)k=Math.min(a,k);return[Math.round(k),u]}function y(a,b){b=s[b];if(a.style.display===\"none\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=f.px(a,\"min\"+b.Size);f.boxSizing(a)||(c+=f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right));return c}}function O(a,b){b=s[b];var c=f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right);if(!f.boxSizing(a)&&!(f.isIE&&!f.isIElt9&&f.hasTag(a,\"BUTTON\")))c+=f.px(a,\"border\"+b.Left+ \"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right);return c}function M(a,b){b=s[b];return f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}function fa(a,b){if(f.boxSizing(a)){b=s[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}else return 0}function ba(a,b){b=s[b];return Math.round(f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"margin\"+b.Left)+f.px(a, \"margin\"+b.Right)+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right))}function Y(a,b,c){a.dirty=Math.max(a.dirty,b);P=true;c&&F.layouts2.scheduleAdjust()}function v(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function ka(a){return a.style.display===\"none\"&&!a.ed||$(a).hasClass(\"Wt-hidden\")}function ta(a,b,c){var e=s[a],i=e.config.length,l=s[a^1].config.length,k=e.measures.slice();if(k.length==5){k[0]=k[0].slice();k[1]=k[1].slice()}if(P){if(c&&typeof e.minSize==\"undefined\"){e.minSize= f.px(c,\"min\"+e.Size);if(e.minSize>0)e.minSize-=fa(c,a)}var u=[],d=[],j=0,m=0,h,G,r=false;for(h=0;h<i;++h){var p=0,t=e.config[h][2],o=true;for(G=0;G<l;++G){var g=e.getItem(h,G);if(g){if(!g.w||a==0&&g.dirty>1){var n=$(\"#\"+g.id),w=n.get(0);if(!w){e.setItem(h,G,null);continue}if(w!=g.w){g.w=w;n.find(\"img\").add(n.filter(\"img\")).bind(\"load\",{item:g},function(S){Y(S.data.item,1,true)})}}if(!X&&g.w.style.position!=\"absolute\"){g.w.style.position=\"absolute\";g.w.style.visibility=\"hidden\"}if(!g.ps)g.ps=[];if(!g.sc)g.sc= [];if(!g.ms)g.ms=[];if(!g.size)g.size=[];if(!g.psize)g.psize=[];if(!g.fs)g.fs=[];if(!g.margin)g.margin=[];if(ka(g.w))g.ps[a]=g.ms[a]=0;else{n=!g.set;if(!g.set)g.set=[false,false];if(g.w){if(f.isIE)g.w.style.visibility=\"\";if(g.dirty){if(g.dirty>1){w=y(g.w,a);g.ms[a]=w}else w=g.ms[a];if(w>t)t=w;if(g.dirty>1)g.margin[a]=O(g.w,a);if(!g.set[a])if(a==0||!n){n=f.pxself(g.w,e.size);g.fs[a]=n?n+g.margin[a]:0}else{n=Math.round(f.px(g.w,e.size));g.fs[a]=n>Math.max(fa(g.w,a),w)?n+g.margin[a]:0}n=g.fs[a];if(g.layout){if(n== 0)n=g.ps[a];g.ps[a]=n}else{if(g.wasLayout){g.wasLayout=false;g.set=[false,false];g.ps=[];g.w.wtResize&&g.w.wtResize(g.w,-1,-1,true);v(g.w,s[1].size,\"\")}w=B(g.w,a,false,b);var J=w[0],T=g.set[a];if(T)if(g.psize[a]>8)T=J>=g.psize[a]-4&&J<=g.psize[a]+4;var la=typeof g.ps[a]!==\"undefined\"&&e.config[h][0]>0&&g.set[a];n=T||la?Math.max(n,g.ps[a]):Math.max(n,J);g.ps[a]=n;g.sc[a]=w[1]}if(!g.span||g.span[a]==1){if(n>p)p=n}else r=true}else if(!g.span||g.span[a]==1){if(g.ps[a]>p)p=g.ps[a];if(g.ms[a]>t)t=g.ms[a]}else r= true;if(!ka(g.w)&&(!g.span||g.span[a]==1))o=false}}}}if(o)t=p=-1;else if(t>p)p=t;u[h]=p;d[h]=t;if(t>-1){j+=p;m+=t}}if(r){function pa(S,ga){for(h=0;h<i;++h)for(G=0;G<l;++G){var Z=e.getItem(h,G);if(Z&&Z.span&&Z.span[a]>1){var ca=S(Z),ha=0,ia=0,H;for(H=0;H<Z.span[a];++H){var Q=ga[h+H];if(Q!=-1){ca-=Q;++ha;if(e.config[h+H][0]>0)ia+=e.config[h+H][0]}}if(ca>=0)if(ha>0){if(ia>0)ha=ia;for(H=0;H<Z.span[a];++H){Q=ga[h+H];if(Q!=-1){Q=ia>0?e.config[h+H][0]:1;if(Q>0){var qa=Math.round(ca/Q);ca-=qa;ha-=Q;ga[h+ H]+=qa}}}}else ga[h]=ca}}}pa(function(S){return S.ps[a]},u);pa(function(S){return S.ms[a]},d)}b=0;n=true;r=false;for(h=0;h<i;++h)if(d[h]>-1){if(n){b+=e.margins[1];n=false}else{b+=e.margins[0];if(r)b+=4}r=e.config[h][1]!==0}n||(b+=e.margins[2]);j+=b;m+=b;e.measures=[u,d,j,m,b]}if(da||k[2]!=e.measures[2])ra.updateSizeInParent(a);c&&e.minSize==0&&k[3]!=e.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&&v(c,\"min\"+e.Size,e.measures[3]+\"px\");c&&a==0&&c&&f.hasTag(c,\"TD\")&&v(c,e.size,e.measures[2]+\"px\")} function ua(a,b,c){a=s[a];if(ea)c=-c;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;c=-c}a.fixedSize[b]=a.sizes[b]+c;F.layouts2.scheduleAdjust()}function va(a,b,c){var e=b.di,i=s[a],l=s[a^1],k,u=f.getElement(D),d;for(d=e-1;d>=0;--d)if(i.sizes[d]>=0){k=-(i.sizes[d]-i.measures[1][d]);break}e=i.sizes[e]-i.measures[1][e];if(ea){var j=k;k=-e;e=-j}new f.SizeHandle(f,i.resizeDir,f.pxself(b,i.size),f.pxself(b,l.size),k,e,i.resizerClass,function(m){ua(a,d,m)},b,u,c,0,0)}function wa(a,b){return b==0?a[b][1]!== 0:a[b-1][1]!==0||a[b][1]!==0}function xa(a,b){var c=s[a],e=s[a^1],i=c.measures,l=0,k=false,u=false,d=false,j=ja?b.parentNode:null;if(c.maxSize===0)if(j){var m=f.css(j,\"position\");if(m===\"absolute\")l=f.pxself(j,c.size);if(l===0){if(!c.initialized){if(a===0&&(m===\"absolute\"||m===\"fixed\")){j.style.display=\"none\";l=j.clientWidth;j.style.display=\"\"}l=a?j.clientHeight:j.clientWidth;k=true;if(a==0&&l==0&&f.isIElt9){l=j.offsetWidth;k=false}var h;if((f.hasTag(j,\"TD\")||f.hasTag(j,\"TH\"))&&!(f.isIE&&!f.isIElt9)){d= 0;h=1}else{d=c.minSize?c.minSize:i[3];h=0}function G(T,la){return T-la<=1}if(f.isIElt9&&G(l,h)||G(l,d+M(j,a)))c.maxSize=999999}if(l===0&&c.maxSize===0){l=a?j.clientHeight:j.clientWidth;k=true}}}else{l=f.pxself(b,c.size);u=true}else if(c.sizeSet){l=f.pxself(j,c.size);u=true}var r=0;if(j&&j.wtGetPS&&a==1)r=j.wtGetPS(j,b,a,0);d=i[2];if(d<c.minSize)d=c.minSize;if(c.maxSize&&!c.sizeSet){l=Math.min(d,c.maxSize)+r;v(j,c.size,l+fa(j,a)+\"px\")&&N&&N.setElDirty(I);l=l;d=u=true}c.cSize=l;if(a==1&&j&&j.wtResize){h= e.cSize;d=c.cSize;j.wtResize(j,Math.round(h),Math.round(d),true)}l-=r;if(!u){u=0;if(typeof c.cPadding===\"undefined\"){u=k?M(j,a):fa(j,a);c.cPadding=u}else u=c.cPadding;l-=u}c.initialized=true;if(!(j&&l<=0)){if(l<i[3]-r)l=i[3]-r;j=[];k=c.config.length;u=e.config.length;for(d=0;d<k;++d)c.stretched[d]=false;if(l>=i[3]-r){r=l-i[4];h=[];var p=[0,0],t=[0,0],o=0;for(d=0;d<k;++d)if(i[1][d]>-1){m=-1;if(typeof c.fixedSize[d]!==\"undefined\"&&!wa(c.config,d))c.fixedSize[d]=undefined;if(typeof c.fixedSize[d]!== \"undefined\"&&(d+1==k||i[1][d+1]>-1))m=c.fixedSize[d];else if(c.config[d][1]!==0&&c.config[d][1][0]>=0){m=c.config[d][1][0];if(c.config[d][1][1])m=(l-i[4])*m/100}if(m>=0){h[d]=-1;j[d]=m;r-=j[d]}else{if(c.config[d][0]>0){m=1;h[d]=c.config[d][0];o+=h[d]}else{m=0;h[d]=0}p[m]+=i[1][d];t[m]+=i[0][d];j[d]=i[0][d]}}else{h[d]=-2;j[d]=-1}if(c.fixedSize.length>k)c.fixedSize.length=k;if(o==0){for(d=0;d<k;++d)if(h[d]==0){h[d]=1;++o}t[1]=t[0];p[1]=p[0];t[0]=0;p[0]=0}if(r>t[0]+p[1]){r-=t[0];if(r>t[1]){if(c.fitSize){r-= t[1];r=r/o;for(d=0;d<k;++d)if(h[d]>0){j[d]+=Math.round(h[d]*r);c.stretched[d]=true}}}else{m=1;if(r<p[m])r=p[m];r=t[m]-p[m]>0?(r-p[m])/(t[m]-p[m]):0;for(d=0;d<k;++d)if(h[d]>0){p=i[0][d]-i[1][d];j[d]=i[1][d]+Math.round(p*r)}}}else{for(d=0;d<k;++d)if(h[d]>0)j[d]=i[1][d];r-=p[1];m=0;if(r<p[m])r=p[m];r=t[m]-p[m]>0?(r-p[m])/(t[m]-p[m]):0;for(d=0;d<k;++d)if(h[d]==0){p=i[0][d]-i[1][d];j[d]=i[1][d]+Math.round(p*r)}}}else j=i[1];c.sizes=j;i=c.margins[1];r=true;p=false;for(d=0;d<k;++d){if(j[d]>-1){var g=p;if(p){p= D+\"-rs\"+a+\"-\"+d;h=f.getElement(p);if(!h){c.resizeHandles[d]=p;h=document.createElement(\"div\");h.setAttribute(\"id\",p);h.di=d;h.style.position=\"absolute\";h.style[e.left]=e.margins[1]+\"px\";h.style[c.size]=c.margins[0]+\"px\";if(e.cSize)h.style[e.size]=e.cSize-e.margins[2]-e.margins[1]+\"px\";h.className=c.handleClass;b.insertBefore(h,b.firstChild);h.onmousedown=h.ontouchstart=function(T){va(a,this,T||window.event)}}i+=2;v(h,c.left,i+\"px\");i+=2}else if(c.resizeHandles[d]){h=f.getElement(c.resizeHandles[d]); h.parentNode.removeChild(h);c.resizeHandles[d]=undefined}p=c.config[d][1]!==0;if(r)r=false;else i+=c.margins[0]}for(t=0;t<u;++t)if((o=c.getItem(d,t))&&o.w){h=o.w;m=Math.max(j[d],0);if(o.span){var n,w=p;for(n=1;n<o.span[a];++n){if(d+n>=j.length)break;if(w)m+=4;w=c.config[d+n][1]!==0;if(j[d+n-1]>-1&&j[d+n]>-1)m+=c.margins[0];m+=j[d+n]}}var J;v(h,\"visibility\",\"\");w=o.align>>c.alignBits&15;n=o.ps[a];if(m<n)w=0;if(w){switch(w){case 1:J=i;break;case 4:J=i+(m-n)/2;break;case 2:J=i+(m-n);break}n-=o.margin[a]; if(o.layout){v(h,c.size,n+\"px\")&&Y(o,1);o.set[a]=true}else if(m>=n&&o.set[a]){v(h,c.size,n+\"px\")&&Y(o,1);o.set[a]=false}o.size[a]=n;o.psize[a]=n}else{w=Math.max(0,m-o.margin[a]);J=a==0&&o.sc[a];if(!ka(h)&&(J||m!=n||o.layout)){if(v(h,c.size,w+\"px\")){if(!f.isIE&&(f.hasTag(h,\"TEXTAREA\")||f.hasTag(h,\"INPUT\"))){v(h,\"margin-\"+c.left,o.margin[a]/2+\"px\");v(h,\"margin-\"+e.left,o.margin[!a]/2+\"px\")}Y(o,1);o.set[a]=true}}else if(o.fs[a])a==0&&v(h,c.size,o.fs[a]+\"px\");else{v(h,c.size,\"\")&&Y(o,1);if(o.set)o.set[a]= false}J=i;o.size[a]=w;o.psize[a]=m}if(X)if(g){v(h,c.left,\"4px\");m=f.css(h,\"position\");if(m!==\"absolute\")h.style.position=\"relative\"}else v(h,c.left,\"0px\");else v(h,c.left,J+\"px\");if(a==1){if(h.wtResize)h.wtResize(h,o.set[0]?Math.round(o.size[0]):-1,o.set[1]?Math.round(o.size[1]):-1,true);o.dirty=0}}if(j[d]>-1)i+=j[d]}if(c.resizeHandles.length>k){for(g=k;g<c.resizeHandles.length;g++)if(c.resizeHandles[g]){h=f.getElement(c.resizeHandles[g]);h.parentNode.removeChild(h)}c.resizeHandles.length=k}$(b).children(\".\"+ e.handleClass).css(c.size,l-c.margins[2]-c.margins[1]+\"px\")}}function ma(){var a=f.getElement(D);ja=K==null;I=N=null;sa=na=true;U=[];oa=false;if(ja){var b=a;b=b.parentNode;for(U=[0,0];b!=document;){U[0]+=ba(b,0);U[1]+=ba(b,1);if(b.wtGetPS)oa=true;var c=jQuery.data(b.parentNode,\"layout\");if(c){I=b;N=c;break}b=b;b=b.parentNode;if(b.childNodes.length!=1&&!b.wtGetPS)na=false}a=a.parentNode;for(b=0;b<2;++b)s[b].sizeSet=f.pxself(a,s[b].size)!=0}else{N=jQuery.data(document.getElementById(K),\"layout\");I= a;U[0]=ba(I,0);U[1]=ba(I,1)}}var f=F.WT;this.descendants=[];var ra=this,C=A,P=true,da=true,ja,N,I,na,sa=false,U,oa,ea=$(document.body).hasClass(\"Wt-rtl\"),s=[{initialized:false,config:C.cols,margins:E,maxSize:aa,measures:[],sizes:[],stretched:[],fixedSize:[],Left:ea?\"Right\":\"Left\",left:ea?\"right\":\"left\",Right:ea?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return C.items[b*s[0].config.length+a]},setItem:function(a,b,c){C.items[b*s[0].config.length+a]=c},handleClass:\"Wt-vrh2\", resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:V,resizeHandles:[]},{initialized:false,config:C.rows,margins:z,maxSize:q,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,b){return C.items[a*s[0].config.length+b]},setItem:function(a,b,c){C.items[a*s[0].config.length+b]=c},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:W,resizeHandles:[]}];jQuery.data(document.getElementById(D),\"layout\",this); this.updateSizeInParent=function(a){if(N&&I.id){var b=f.$(I.id);if(b){if(I!=b)if(N=jQuery.data(b.parentNode,\"layout\"))I=b;else ma()}else ma()}if(N)if(na){var c=s[a];b=c.measures[2];if(c.maxSize>0)b=Math.min(c.maxSize,b);if(oa){c=f.getElement(D);if(!c)return;for(var e=c,i=e.parentNode;;){if(i.wtGetPS)b=i.wtGetPS(i,e,a,b);b+=ba(i,a);if(i==I)break;if(a==1&&i==c.parentNode&&!i.lh&&i.offsetHeight>b)b=i.offsetHeight;e=i;i=e.parentNode}}else b+=U[a];N.setChildSize(I,a,b)}};this.setConfig=function(a){var b= C;C=a;s[0].config=C.cols;s[1].config=C.rows;s[0].stretched=[];s[1].stretched=[];var c;a=0;for(c=b.items.length;a<c;++a){var e=b.items[a];if(e){var i=x(e.id);if(i){i.ps=e.ps;i.sc=e.sc;i.ms=e.ms;i.size=e.size;i.psize=e.psize;i.fs=e.fs;i.margin=e.margin;i.set=e.set}else if(e.set){e.set[0]&&v(e.w,s[0].size,\"\");e.set[1]&&v(e.w,s[1].size,\"\")}}}P=da=true;F.layouts2.scheduleAdjust()};this.getId=function(){return D};this.setElDirty=function(a){if(a=x(a.id)){a.dirty=2;P=true;F.layouts2.scheduleAdjust()}};this.setItemsDirty= function(a){var b,c,e=s[0].config.length;b=0;for(c=a.length;b<c;++b){var i=C.items[a[b][0]*e+a[b][1]];i.dirty=2;if(i.layout){i.layout=false;i.wasLayout=true;F.layouts2.setChildLayoutsDirty(ra,i.w)}}P=true};this.setDirty=function(){da=true};this.setAllDirty=function(){var a,b;a=0;for(b=C.items.length;a<b;++a){var c=C.items[a];if(c)c.dirty=2}P=true};this.setChildSize=function(a,b,c){var e=s[0].config.length,i=s[b],l;if(a=x(a.id)){e=b===0?l%e:l/e;if(a.align>>i.alignBits&15||!i.stretched[e]){if(!a.ps)a.ps= [];a.ps[b]=c}a.layout=true;Y(a,1)}};this.measure=function(a){var b=f.getElement(D);if(b)if(!f.isHidden(b)){sa||ma();if(P||da)ta(a,b,ja?b.parentNode:null);if(a==1)P=da=false}};this.setMaxSize=function(a,b){s[0].maxSize=a;s[1].maxSize=b};this.apply=function(a){var b=f.getElement(D);if(!b)return false;if(f.isHidden(b))return true;xa(a,b);return true};this.contains=function(a){var b=f.getElement(D);a=f.getElement(a.getId());return b&&a?f.contains(b,a):false};this.WT=f}");
	}

	static WJavaScriptPreamble appjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.ApplicationScope,
				JavaScriptObjectType.JavaScriptObject,
				"layouts2",
				"new (function(){var F=[],D=false,K=this,V=false;this.find=function(q){return(q=document.getElementById(q))?jQuery.data(q,\"layout\"):null};this.setDirty=function(q){if(q=this.find(q)){q.setDirty();K.scheduleAdjust()}};this.setElementDirty=function(q){var E=q;for(q=q.parentNode;q&&q!=document.body;){var z=jQuery.data(q,\"layout\");z&&z.setElDirty(E);E=q;q=q.parentNode}};this.setChildLayoutsDirty=function(q,E){var z,A;z=0;for(A=q.descendants.length;z< A;++z){var x=q.descendants[z];if(E){var B=q.WT.getElement(x.getId());if(B&&!q.WT.contains(E,B))continue}x.setDirty()}};this.add=function(q){function E(z,A){var x,B;x=0;for(B=z.length;x<B;++x){var y=z[x];if(y.getId()==A.getId()){z[x]=A;A.descendants=y.descendants;return}else if(y.contains(A)){E(y.descendants,A);return}else if(A.contains(y)){A.descendants.push(y);z.splice(x,1);--x;--B}}z.push(A)}E(F,q);K.scheduleAdjust()};var W=false,X=0;this.scheduleAdjust=function(q){if(q)V=true;if(!W){if(D)++X;else X= 0;if(!(X>=6)){W=true;setTimeout(function(){K.adjust()},0)}}};this.adjust=function(q,E){function z(x,B){var y,O;y=0;for(O=x.length;y<O;++y){var M=x[y];z(M.descendants,B);if(B==1&&V)M.setDirty();else B==0&&M.setAllDirty();M.measure(B)}}function A(x,B){var y,O;y=0;for(O=x.length;y<O;++y){var M=x[y];if(M.apply(B))A(M.descendants,B);else{x.splice(y,1);--y;--O}}}if(q){(q=this.find(q))&&q.setItemsDirty(E);K.scheduleAdjust()}else{W=false;if(!D){D=true;z(F,0);A(F,0);z(F,1);A(F,1);V=D=false}}};this.updateConfig= function(q,E){(q=this.find(q))&&q.setConfig(E)};this.adjustNow=function(){W&&K.adjust()};var aa=null;window.onresize=function(){clearTimeout(aa);aa=setTimeout(function(){aa=null;K.scheduleAdjust(true)},20)};window.onshow=function(){V=true;K.adjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(a,f,c,e){function i(j){var k=b.px(j,\"marginTop\");k+=b.px(j,\"marginBottom\");if(!b.boxSizing(j)){k+=b.px(j,\"borderTopWidth\");k+=b.px(j,\"borderBottomWidth\");k+=b.px(j,\"paddingTop\");k+=b.px(j,\"paddingBottom\")}return k}var b=this,h=c>=0;a.lh=h&&e;a.style.height=h?c+\"px\":\"\";if(b.boxSizing(a)){c-=b.px(a,\"marginTop\");c-=b.px(a,\"marginBottom\");c-=b.px(a,\"borderTopWidth\");c-=b.px(a,\"borderBottomWidth\");c-=b.px(a,\"paddingTop\");c-=b.px(a,\"paddingBottom\"); f-=b.px(a,\"marginLeft\");f-=b.px(a,\"marginRight\");f-=b.px(a,\"borderLeftWidth\");f-=b.px(a,\"borderRightWidth\");f-=b.px(a,\"paddingLeft\");f-=b.px(a,\"paddingRight\")}var g,m,d;g=0;for(m=a.childNodes.length;g<m;++g){d=a.childNodes[g];if(d.nodeType==1)if(h){var l=c-i(d);if(l>0){if(d.offsetTop>0){var n=b.css(d,\"overflow\");if(n===\"visible\"||n===\"\")d.style.overflow=\"auto\"}if(d.wtResize)d.wtResize(d,f,l,e);else{l=l+\"px\";if(d.style.height!=l){d.style.height=l;d.lh=e}}}}else if(d.wtResize)d.wtResize(d,f,-1);else{d.style.height= \"\";d.lh=false}}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "ChildrenGetPS",
				"function(a,f,c,e){return e}");
	}

	static WJavaScriptPreamble wtjs12() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastResize",
				"function(a,f,c,e){var i=this,b=c>=0;a.lh=b&&e;a.style.height=b?c+\"px\":\"\";a=a.lastChild;var h=a.previousSibling;if(b){c-=h.offsetHeight+i.px(h,\"marginTop\")+i.px(h,\"marginBottom\");if(c>0)if(a.wtResize)a.wtResize(a,f,c,e);else{a.style.height=c+\"px\";a.lh=e}}else if(a.wtResize)a.wtResize(a,-1,-1);else{a.style.height=\"\";a.lh=false}}");
	}

	static WJavaScriptPreamble wtjs13() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastGetPS",
				"function(a,f,c,e){var i=this,b,h;b=0;for(h=a.childNodes.length;b<h;++b){var g=a.childNodes[b];if(g!=f){var m=i.css(g,\"position\");if(m!=\"absolute\"&&m!=\"fixed\")if(c===0)e=Math.max(e,g.offsetWidth);else e+=g.offsetHeight+i.px(g,\"marginTop\")+i.px(g,\"marginBottom\")}}return e}");
	}
}
