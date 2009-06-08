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
 * A class that represents a selection for a WAbstractItemView.
 * 
 * 
 * This model is currently only used by {@link WTreeView}, and plays only a role
 * in drag &amp; drop of an item selection.
 * <p>
 * When an item selection is dragged from a view widget, the generated drop
 * events will have as source object (see {@link WDropEvent#getSource()}) this
 * selection model.
 * <p>
 * Although this class does not (yet) allow you to modify the selection, note
 * that manipulations to the model may modify the selection (row insertions and
 * removals may shift the selection, and row deletions may shrink the
 * selection).
 * <p>
 * <p>
 * <i><b>Note:</b>Currently this class cannot be shared between multiple
 * views.</i>
 * </p>
 * 
 * @see WTreeView
 * @see WAbstractItemModel
 */
public class WItemSelectionModel extends WObject {
	public WAbstractItemModel getModel() {
		return this.model_;
	}

	/**
	 * Returns the set of selected items.
	 * 
	 * The model indexes are returned as a set, topologically ordered (in the
	 * order they appear in the view).
	 */
	public SortedSet<WModelIndex> getSelectedIndexes() {
		return this.selection_;
	}

	/**
	 * Returns wheter an item is selected.
	 * 
	 * @see WItemSelectionModel#getSelectedIndexes()
	 */
	public boolean isSelected(WModelIndex index) {
		return this.selection_.contains(index) != false;
	}

	/**
	 * Change the selection behaviour.
	 * 
	 * By default, the selection contains rows (
	 * {@link SelectionBehavior#SelectRows SelectRows}), in which case model
	 * indexes will always be have column 0, but represent the whole row.
	 * <p>
	 * Alternatively, you can allow selection for individual items (
	 * {@link SelectionBehavior#SelectItems SelectItems}).
	 */
	public void setSelectionBehavior(SelectionBehavior behavior) {
		this.selectionBehavior_ = behavior;
	}

	/**
	 * Returns the selection behaviour.
	 * 
	 * @see WItemSelectionModel#setSelectionBehavior(SelectionBehavior behavior)
	 */
	public SelectionBehavior getSelectionBehavior() {
		return this.selectionBehavior_;
	}

	SortedSet<WModelIndex> selection_;
	private WAbstractItemModel model_;
	private SelectionBehavior selectionBehavior_;

	WItemSelectionModel(WAbstractItemModel model, WObject parent) {
		super(parent);
		this.selection_ = new TreeSet<WModelIndex>();
		this.model_ = model;
		this.selectionBehavior_ = SelectionBehavior.SelectRows;
	}

	WItemSelectionModel(WAbstractItemModel model) {
		this(model, (WObject) null);
	}
}