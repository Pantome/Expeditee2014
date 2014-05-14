package org.expeditee.items;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.expeditee.actions.Actions;
import org.expeditee.actions.IncorrectUseOfStatementException;
import org.expeditee.actions.Javascript;
import org.expeditee.actions.Misc;
import org.expeditee.actions.Simple;
import org.expeditee.gui.AttributeUtils;
import org.expeditee.gui.AttributeValuePair;
import org.expeditee.gui.DisplayIO;
import org.expeditee.gui.Frame;
import org.expeditee.gui.FrameGraphics;
import org.expeditee.gui.FrameIO;
import org.expeditee.gui.FrameKeyboardActions;
import org.expeditee.gui.FrameMouseActions;
import org.expeditee.gui.FrameUtils;
import org.expeditee.gui.FreeItems;
import org.expeditee.gui.MessageBay;
import org.expeditee.gui.Overlay;
import org.expeditee.gui.Vector;
import org.expeditee.io.Conversion;
import org.expeditee.settings.UserSettings;
import org.expeditee.simple.Context;
import org.expeditee.stats.AgentStats;
import org.expeditee.stats.Formatter;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Represents everything that can be drawn on the screen (text, lines, dots,
 * images). Each specific type is a subclass of Item.
 * 
 * @author jdm18
 * 
 */
public abstract class Item implements Comparable<Item>, Runnable {

	public static final Float DEFAULT_THICKNESS = 2f;

	public static final Float MINIMUM_THICKNESS = 0f;

	public static final Float MINIMUM_PAINT_THICKNESS = 1f;

	protected final int JOIN = BasicStroke.JOIN_ROUND;

	protected final int CAP = BasicStroke.CAP_BUTT;

	protected final Stroke DOT_STROKE = new BasicStroke(DEFAULT_THICKNESS,
			CAP, JOIN, 4.0F);

	protected final Stroke HIGHLIGHT_STROKE = new BasicStroke(
			MINIMUM_THICKNESS, CAP, JOIN, 4.0F);

	// contains all dots (including this one) that form an enclosure
	// if this dot is part of an enclosing shape
	private Collection<Item> _enclosure = null;

	public static final int LEFT_MARGIN = 13;

	// indicates which end the arrowhead should be drawn at
	protected Polygon _poly = null;

	protected boolean _connectedToAnnotation = false;

	protected boolean _save = true;

	private int _gradientAngle = 0;

	public static final int NEAR_DISTANCE = 15;

	/**
	 * The default Color to draw highlighting in
	 */
	public static final int DEFAULT_HIGHLIGHT_THICKNESS = 2;

	public static final Color DEFAULT_HIGHLIGHT = Color.RED;

	public static final Color DEPRESSED_HIGHLIGHT = Color.GREEN;

	public static final Color ALTERNATE_HIGHLIGHT = Color.BLUE;

	public static final Color LINK_COLOR = Color.BLACK;

	public static final Color ACTION_COLOR = Color.BLACK;

	public static final Color LINK_ACTION_COLOR = Color.RED;

	public static final Color DEFAULT_FOREGROUND = Color.BLACK;

	public static final Color DEFAULT_BACKGROUND = Color.white;

	public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	/**
	 * The number of pixels highlighting should extend around Items.
	 */
	public static final int XGRAVITY = 3;

	public static final int MARGIN_RIGHT = 2;

	public static final int MARGIN_LEFT = 15;

	protected static final double DEFAULT_ARROWHEAD_RATIO     = 0.3; // used to be 0.5

	public static final double DEFAULT_ARROWHEAD_NIB_PERC  = 0.75; 
	
	public static final Color GREEN = Color.GREEN.darker();

	public static final int UNCHANGED_CURSOR = -100;

	public static final int DEFAULT_CURSOR = Cursor.DEFAULT_CURSOR;

	public static final int HIDDEN_CURSOR = Cursor.CUSTOM_CURSOR;

	public static final int TEXT_CURSOR = Cursor.TEXT_CURSOR;

	public static final int CROP_CURSOR = Cursor.CROSSHAIR_CURSOR;

	// The default value for integer attributes
	public static final int DEFAULT_INTEGER = -1;

	protected DotType _type = DotType.square;

	protected boolean _filled = true;
	
	private List<String> _tooltip = null;
	private static Text _tooltipItem = null;
	private static Item _tooltipOwner = null;

	public enum AnchorEdgeType {
		None, Left, Right, Top, Bottom
	}

	public static void DuplicateItem(Item source, Item dest) {
		dest.setX(source.getX());
		dest.setY(source.getY());

		dest.setActions(source.getAction());
		dest.setActionCursorEnter(source.getActionCursorEnter());
		dest.setActionCursorLeave(source.getActionCursorLeave());
		dest.setActionEnterFrame(source.getActionEnterFrame());
		dest.setActionLeaveFrame(source.getActionLeaveFrame());
		dest.setActionMark(source.getActionMark());

		dest.setBackgroundColor(source.getBackgroundColor());
		dest.setBottomShadowColor(source.getBottomShadowColor());
		dest.setColor(source.getColor());
		dest.setBorderColor(source.getBorderColor());

		dest.setTooltips(source.getTooltip());
		dest.setData(source.getData());
		dest.setTag(source.getTag());
		dest.setFillColor(source.getFillColor());
		dest.setGradientColor(source.getGradientColor());
		dest.setGradientAngle(source.getGradientAngle());
		dest.setFillPattern(source.getFillPattern());

		dest.setHighlight(source.getHighlight());
		dest.setLink(source.getLink());
		dest.setLinkFrameset(source.getLinkFrameset());
		dest.setLinkMark(source.getLinkMark());
		dest.setLinkTemplate(source.getLinkTemplate());

		// dest.setMaxWidth(source.getMaxWidth());

		dest.setOffset(source.getOffset());
		// dest.setOwner(source.getOwner());
		dest.setThickness(source.getThickness());
		dest.setSize(source.getSize());
		dest.setTopShadowColor(source.getTopShadowColor());
		dest.setLinePattern(source.getLinePattern());

		dest.setFloating(source.isFloating());
		dest.setArrow(source.getArrowheadLength(), source.getArrowheadRatio(), source.getArrowheadNibPerc());

		dest.setDotType(source.getDotType());
		dest.setFilled(source.getFilled());
		/*
		 * Calling the methods will move the item... This messes things up when
		 * the user uses backspace to delete a text line end
		 */
		// dest._anchorLeft = source._anchorLeft;
		// dest._anchorRight = source._anchorRight;
		// dest._anchorTop = source._anchorTop;
		// dest._anchorBottom = source._anchorBottom;
		dest.setFormula(source.getFormula());
		dest._overlay = source._overlay;
		dest._mode = source._mode;// SelectedMode.None;
		// dest._highlightColor = source._highlightColor;
		// dest.setHighlight(source.getHighlight());

		dest._visible = source._visible;

		Frame parent = DisplayIO.getCurrentFrame();
		if (parent == null)
			parent = source.getParentOrCurrentFrame();
		dest.setParent(parent);

		/*
		 * TODO MIKE says maybe we could tighten up and only give items ID's if
		 * their current ID is negative?
		 */
		if (parent != null) {
			dest.setID(parent.getNextItemID());
		}

		if (parent != null && !UserSettings.UserName.equals(parent.getOwner())) {
			dest.setOwner(UserSettings.UserName.get());
		}
	}

	public void setGradientAngle(int gradientAngle) {
		_gradientAngle = gradientAngle;

		for (Line line : _lines) {
			Item other = line.getOppositeEnd(this);
			if (other.getGradientAngle() != gradientAngle)
				other.setGradientAngle(gradientAngle);
		}

		invalidateCommonTrait(ItemAppearence.GradientColor);
		invalidateFill();
	}

	public int getGradientAngle() {
		return _gradientAngle;
	}

	public int getGravity() {
		if (isVectorItem()) {
			return 2;
		}
		return UserSettings.Gravity.get();
	}

	public static boolean showLineHighlight() {
		return UserSettings.LineHighlight.get();
	}

	public enum HighlightMode {
		None, Enclosed, Connected, Disconnect, Normal
	}

	public void setHighlightMode(HighlightMode mode) {
		setHighlightMode(mode, DEFAULT_HIGHLIGHT);
	}

	protected Float _anchorLeft = null;
	protected Float _anchorRight = null;

	protected Float _anchorTop = null;
	protected Float _anchorBottom = null;

	protected HighlightMode _mode = HighlightMode.None;

	private Point _offset = new Point(0, 0);

	protected float _x;
	protected float _y;

	private int _id;

	private Item _editTarget = this;

	private String _creationDate = null;

	private boolean _linkMark = true;

	private boolean _actionMark = true;

	private boolean _highlight = true;

	// private int _maxWidth = -1;

	private String _owner = null;

	private String _link = null;
	
	private boolean _linkHistory = true;

	private StringBuffer _tag = new StringBuffer();

	private List<String> _actionCursorEnter = null;

	private List<String> _actionCursorLeave = null;

	private List<String> _actionEnterFrame = null;

	private List<String> _actionLeaveFrame = null;

	private PermissionPair _permissionPair = null;
	
	private UserAppliedPermission _overlayPermission = null;
	
	public void setOverlayPermission(UserAppliedPermission overlayPermission) {
		_overlayPermission = overlayPermission;
	}

	public void setPermission(PermissionPair permissionPair) {
		_permissionPair = permissionPair;
	}
	
	public PermissionPair getPermission() {
		return _permissionPair;
	}

	public UserAppliedPermission getUserAppliedPermission() {
		String owner = _owner != null ? _owner : _parent != null ? _parent.getOwner() : null;
		if(_permissionPair != null) return _permissionPair.getPermission(owner);
		if(_overlayPermission != null) return _overlayPermission;
		if(_parent != null) return _parent.getUserAppliedPermission();
		return UserAppliedPermission.full;
	}
	
	public boolean hasPermission(UserAppliedPermission permission) {
		return getUserAppliedPermission().ordinal() >= permission.ordinal();
	}

	// A fill color of null represents transparent
	private Color _colorFill = null;

	// A gradient color of null represents NO gradient
	private Color _colorGradient = null;

	// A fore color of null represents the default color
	private Color _color = null;

	protected Color _highlightColor = DEFAULT_HIGHLIGHT;

	private Color _colorBackground = null;

	private Color _colorBorder = null;

	private Color _colorTopShadow = null;

	private Color _colorBottomShadow = null;

	// the link\action circle
	private Polygon _circle = null;

	// the invalid link cross
	private Polygon _circleCross = null;

	private Frame _parent = null;
	private Frame _oldParent = null;

	protected int _highlightThickness = 2;

	protected int _vectorHighlightThickness = 1;

	// arrowhead parameters
	private float  _arrowheadLength  = 0;
	private double _arrowheadRatio   = DEFAULT_ARROWHEAD_RATIO;
	private double _arrowheadNibPerc = DEFAULT_ARROWHEAD_NIB_PERC;
	
	private Polygon _arrowhead = null;

	// the list of lines that this point is part of.
	private List<Line> _lines = new ArrayList<Line>();

	private int[] _linePattern = null;

	private boolean _floating = false;

	// list of points constrained with this point
	private List<Constraint> _constraints = new ArrayList<Constraint>();

	private List<String> _actions = null;

	private List<String> _data = null;

	private String _formula = null;

	private String _link_frameset = null;

	private String _link_template = null;

	private String _fillPattern = null;

	private boolean _visible = true;

	private float _thickness = -1.0F;

	protected Item() {
		_creationDate = Formatter.getLongDateTime();
	}

	/**
	 * Adds an action to this Item.
	 * 
	 * @param action
	 *            The action to add to this Item
	 */
	public void addAction(String action) {
		if (action == null || action.equals("")) {
			return;
		}

		if (_actions == null) {
			_actions = new LinkedList<String>();
		}
		_actions.add(action);
		if (_actions.size() == 1) {
			_poly = null;
			invalidateCommonTrait(ItemAppearence.LinkChanged);
		}
	}

	public void addAllConnected(Collection<Item> connected) {
		if (!connected.contains(this))
			connected.add(this);

		for (Item item : getConnected()) {
			if (!connected.contains(item))
				item.addAllConnected(connected);
		}
	}

	/**
	 * Adds the given Constraint to this Dot
	 * 
	 * @param c
	 *            The Constraint to set this Dot as a member of.
	 */
	public void addConstraint(Constraint c) {
		// do not add duplicate constraint
		if (_constraints.contains(c))
			return;

		_constraints.add(c);
	}

	/**
	 * Adds a given line to the list of lines that this Point is an end for.
	 * 
	 * @param line
	 *            The Line that this Point is an end of.
	 */
	public void addLine(Line line) {
		if (_lines.contains(line)) {
			return;
		}

		_lines.add(line);
	}

	/**
	 * Items are sorted by their Y coordinate on the screen.
	 * 
	 * @param i
	 *            The Item to compare this Item to
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Item i) {
		return getY() - i.getY();
	}

	/**
	 * Every Item has an area around it defined by a Shape (typically a
	 * rectangle), this method returns true if the given x,y pair lies within
	 * the area and false otherwise.
	 * 
	 * @param x
	 *            The x coordinate to check
	 * @param y
	 *            The y coordinate to check
	 * @return True if the Shape around this Item contains the given x,y pair,
	 *         false otherwise.
	 */
	public boolean contains(int x, int y) {
		return getPolygon().contains(x, y);
	}

	/**
	 * Returns a deep copy of this Item, note: it is up to the receiver to
	 * change the Item ID etc as necessary.
	 * 
	 * @return A deep copy of this Item.
	 */
	public abstract Item copy();

	public void delete() {
		_deleted = true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (getClass().equals(o.getClass())) {
			Item i = (Item) o;
			return i.getID() == getID()
					&& ((i.getParent() == _parent) || (i.getParent() != null && i
							.getParent().equals(_parent)));
		} else
			return false;
	}

	/**
	 * Returns a list of any action code that is currently associated with this
	 * Item
	 * 
	 * @return A List of action code associated with this Item, or null if none
	 *         has been assigned.
	 */
	public List<String> getAction() {
		return _actions;
	}

	public List<String> getData() {
		return _data;
	}

	public List<String> getActionCursorEnter() {
		return _actionCursorEnter;
	}

	public List<String> getActionCursorLeave() {
		return _actionCursorLeave;
	}

	public List<String> getActionEnterFrame() {
		return _actionEnterFrame;
	}

	public List<String> getActionLeaveFrame() {
		return _actionLeaveFrame;
	};

	public boolean getActionMark() {
		return _actionMark;
	}

	/**
	 * Gets all the items connected to this item. Uses a recursive approach to
	 * search connected points.
	 * 
	 * @return
	 */
	public Collection<Item> getAllConnected() {
		Collection<Item> list = new LinkedHashSet<Item>();
		addAllConnected(list);
		return list;
	}

	public Area getArea() {
		return new Area(getPolygon());
	}

	public String getArrow() {
		if (!hasVisibleArrow())
			return null;

		String ratio = "" + getArrowheadRatio();
		if (ratio.length() - ratio.indexOf(".") > 2)
			ratio = ratio.substring(0, ratio.indexOf(".") + 3);

		return getArrowheadLength() + " " + ratio + " " + getArrowheadNibPerc();
	}

	public Polygon getArrowhead() {
		return _arrowhead;
	}

	public float getArrowheadLength() {
		return _arrowheadLength;
	}

	public double getArrowheadRatio() {
		return _arrowheadRatio;
	}

	public double getArrowheadNibPerc() {
		return _arrowheadNibPerc;
	}
	
	public Color getBackgroundColor() {
		return _colorBackground;
	}

	public Color getBorderColor() {
		return _colorBorder;
	}

	/**
	 * Returns the Color being used to shade the bottom half of this Item's
	 * border. This can be NULL if no Color is being used
	 * 
	 * @return The Color displayed on the bottom\right half of this Item's
	 *         border.
	 */
	public Color getBottomShadowColor() {
		return _colorBottomShadow;
	}

	/**
	 * Returns the height (in pixels) of this Item's surrounding area.
	 * 
	 * @return The height (in pixels) of this Item's surrounding area as
	 *         returned by getArea().
	 */
	public int getBoundsHeight() {
		return getPolygon().getBounds().height;
	}

	/**
	 * Returns the width (in pixels) of this Item's surrounding area.
	 * 
	 * @return The width (in pixels) of this Item's surrounding area as returned
	 *         by getArea().
	 */
	public int getBoundsWidth() {
		return getPolygon().getBounds().width;
	}

	// TODO draw the link with a circle rather than a polygon!!
	public Polygon getLinkPoly() {
		if (_circle == null) {
			int points = 16;

			double radians = 0.0;
			int xPoints[] = new int[points];
			int yPoints[] = new int[xPoints.length];

			for (int i = 0; i < xPoints.length; i++) {
				// circle looks best if these values are not related to gravity
				xPoints[i] = (int) (3.5 * Math.cos(radians)) + 6;// (2 *
				// GRAVITY);
				yPoints[i] = (int) (3.5 * Math.sin(radians)) + 3;// GRAVITY;
				radians += (2.0 * Math.PI) / xPoints.length;
			}

			_circle = new Polygon(xPoints, yPoints, xPoints.length);
		}

		return _circle;
	}

	protected Polygon getCircleCross() {

		if (_circleCross == null) {
			_circleCross = new Polygon();

			Rectangle bounds = getLinkPoly().getBounds();
			int x1 = (int) bounds.getMinX();
			int x2 = (int) bounds.getMaxX();
			int y1 = (int) bounds.getMinY();
			int y2 = (int) bounds.getMaxY();
			int midX = ((x2 - x1) / 2) + x1;
			int midY = ((y2 - y1) / 2) + y1;

			_circleCross.addPoint(x1, y1);
			_circleCross.addPoint(x2, y2);
			_circleCross.addPoint(midX, midY);
			_circleCross.addPoint(x1, y2);
			_circleCross.addPoint(x2, y1);
			_circleCross.addPoint(midX, midY);
		}

		return _circleCross;
	}

	public Color getColor() {
		return _color;
	}

	public Collection<Item> getConnected() {
		List<Item> conn = new LinkedList<Item>();
		conn.add(this);
		conn.addAll(getEnclosures());
		conn.addAll(getLines());
		return conn;
	}

	public String getConstraintIDs() {
		if (_constraints == null || _constraints.size() == 0)
			return null;

		String cons = "";

		for (Constraint c : _constraints)
			cons += c.getID() + " ";

		return cons.trim();
	}

	/*
	 * public void setLinkValid(boolean val) { _isValidLink = val; }
	 */

	/**
	 * Returns a List of any Constraints that this Dot is a memeber of.
	 * 
	 * @return a List of Constraints that this Dot is a member of.
	 */
	public List<Constraint> getConstraints() {
		return _constraints;
	}

	public String getTag() {
		if (_tag != null && _tag.length() > 0)
			return _tag.toString();
		return null;
	}

	public String getDateCreated() {
		return _creationDate;
	}

	public Color getFillColor() {
		return _colorFill;
	}

	public String getFillPattern() {
		return _fillPattern;
	}

	public String getFirstAction() {
		if (_actions == null || _actions.size() == 0)
			return null;
		return _actions.get(0);
	}

	public boolean getHighlight() {
		return _highlight;
	}

	public Color getHighlightColor() {
		if (_highlightColor.equals(getPaintColor()))
			return getAlternateHighlightColor();
		return getDefaultHighlightColor();
	}

	/**
	 * Returns the ID of this Item, which must be unique for the Frame.
	 * 
	 * @return The ID of this Item.
	 */
	public int getID() {
		return _id;
	}

	/**
	 * Returns the list of IDs of the Lines that this Dot is an end of.
	 * 
	 * @return The list of Line IDs that this point is part of.
	 */
	public String getLineIDs() {
		String lineID = null;

		if (_lines.size() > 0) {
			lineID = "" + _lines.get(0).getID();

			for (int i = 1; i < _lines.size(); i++)
				lineID += " " + _lines.get(i).getID();
		}

		return lineID;
	}

	public int[] getLinePattern() {
		return _linePattern;
	}

	/**
	 * Returns a list of Lines where this Dot is an end.
	 * 
	 * @return A list of the Lines that this Dot is an end for or null if no
	 *         Lines have been added.
	 */
	public List<Line> getLines() {
		return _lines;
	}

	/**
	 * Returns the name of a Frame that this Item links to, or null if this Item
	 * has no link.
	 * 
	 * @return The name of a Frame that this Item links to (if any) or null if
	 *         this Item does not link to anything.
	 */
	public String getLink() {
		return _link;
	}

	public String getFormula() {
		return _formula;
	}

	public boolean hasFormula() {
		return _formula != null;
	}

	public boolean hasAttributeValuePair() {
		return _attributeValuePair != null && _attributeValuePair.hasPair();
	}

	public void setFormula(String formula) {
		_formula = formula;
	}

	public boolean calculate(String formula) {
		setFormula(formula);
		return true;
	}

	public String getLinkFrameset() {
		return _link_frameset;
	}

	public boolean getLinkMark() {
		return _linkMark;
	}

	public String getLinkTemplate() {
		return _link_template;
	}

	// public int getMaxWidth() {
	// return _maxWidth;
	// }

	public Point getOffset() {
		return _offset;
	}

	public String getOwner() {
		return _owner;
	}

	public Color getPaintBackgroundColor() {
		Color colorBackground = getBackgroundColor();
		if (colorBackground == null) {
			if (getParent() != null && getParent().getBackgroundColor() != null)
				return getParent().getBackgroundColor();

			return DEFAULT_BACKGROUND;
		}

		return colorBackground;
	}

	/**
	 * Returns the foreground Color of this Item.
	 * 
	 * @return The Color of this item (foreground)
	 */
	public final Color getPaintColor() {
		// If color is null then get the paint foregroundColor for the frame the
		// item is on which is a color adjusted to suit the background
		Color color = getColor();

		if (color == null) {
			if (getParent() != null)
				return getParent().getPaintForegroundColor();

			Frame current = DisplayIO.getCurrentFrame();
			if (current == null) {
				return DEFAULT_FOREGROUND;
			}
			return current.getPaintForegroundColor();
		}

		return color;
	}

	public final Color getPaintBorderColor() {
		// If color is null then get the paint foregroundColor for the frame the
		// item is on which is a color adjusted to suit the background
		Color color = getBorderColor();

		if (color == null) {
			if (getParent() != null)
				return getParent().getPaintForegroundColor();

			Frame current = DisplayIO.getCurrentFrame();
			if (current == null) {
				return DEFAULT_FOREGROUND;
			}
			return current.getPaintForegroundColor();
		}

		return color;
	}

	protected Color getPaintHighlightColor() {
		Color highlightColor = getDefaultHighlightColor();
		if (hasVisibleBorder()) {
			if (getPaintBorderColor().equals(highlightColor)) {
				highlightColor = getDefaultHighlightColor();
			}
		} else if (getPaintBackgroundColor().equals(highlightColor)) {
			highlightColor = getDefaultHighlightColor();
		}
		if (getParent() != null
				&& getParent().getPaintBackgroundColor().equals(highlightColor))
			highlightColor = getParent().getPaintForegroundColor();

		if (hasVisibleBorder()) {
			if (highlightColor.equals(getBorderColor())
					&& getThickness() == getHighlightThickness()) {
				highlightColor = new Color(highlightColor.getRed(),
						highlightColor.getGreen(), highlightColor.getBlue(),
						150);
			}
		}

		return highlightColor;
	}

	static final int BRIGHTNESS = 185;

	protected Color getDefaultHighlightColor() {
		if (isVectorItem()
				&& !this.contains(FrameMouseActions.getX(), FrameMouseActions
						.getY())) {
			return new Color(255, BRIGHTNESS, BRIGHTNESS);
		}
		return _highlightColor;
	}

	protected Color getAlternateHighlightColor() {
		if (isVectorItem()
				&& !this.contains(FrameMouseActions.getX(), FrameMouseActions
						.getY())) {
			return new Color(BRIGHTNESS, BRIGHTNESS, 255);
		}
		return ALTERNATE_HIGHLIGHT;
	}

	protected int getHighlightThickness() {
		if (isVectorItem())
			return _vectorHighlightThickness;
		return _highlightThickness;
	}

	public final Frame getParent() {
		return _parent;
	}

	public final Point getPosition() {
		return new Point(getX(), getY());
	}

	/**
	 * Returns the size of this Item. For Text this is the Font size, for Lines
	 * and Dots this is the thickness.
	 * 
	 * @return The size of this Item.
	 */
	public float getSize() {
		return -1.0F;
	}

	/**
	 * Returns the Color being used to shade the top half of this Item's border.
	 * This can be NULL if no Color is being used
	 * 
	 * @return The Color displayed on the top\left half of this Item's border.
	 */
	public Color getTopShadowColor() {
		return _colorTopShadow;
	}

	public String getTypeAndID() {
		return "T " + getID();
	}

	public Integer getWidthToSave() {
		return getWidth();
	}

	public Integer getWidth() {
		return null;
	}

	public int getHeight() {
		return 0;
	}

	/**
	 * Returns the X coordinate of this Item on the screen
	 * 
	 * @return The X coordinate of this Item on the screen
	 */
	public int getX() {
		return Math.round(_x);
	}

	/**
	 * Returns the Y coordinate of this Item on the screen
	 * 
	 * @return The Y coordinate of this Item on the screen
	 */
	public int getY() {
		return Math.round(_y);
	}

	public boolean hasVisibleArrow() {
		return isLineEnd() && getArrowheadRatio() != 0 && getArrowheadLength() != 0;
	}

	/**
	 * Checks if the given Shape intersects with the Shape around this Item.
	 * 
	 * @param s
	 *            The Shape to check.
	 * @return True if the two Shapes overlap, False otherwise.
	 */
	public boolean intersects(Polygon p) {
		if (p == null)
			return false;

		Area a = new Area(p);
		Area thisArea = this.getArea();
		// Need to do this check for circles
		if (a.equals(thisArea))
			return true;

		a.intersect(thisArea);

		// Need to check the second equality so that we dont pick up circles
		// inside other circles
		return !a.isEmpty() && !a.equals(new Area(p));
	}

	/**
	 * Note: Pictures always return False, as they should be drawn even when no
	 * other annotation Items are.
	 * 
	 * @return True if this Item is an annotation, False otherwise.
	 */
	public boolean isAnnotation() {
		return false;
	}

	public boolean isFloating() {
		return _floating;
	}

	public boolean isFrameName() {
		if (this.getParent() == null || this.getParent().getNameItem() != this)
			return false;
		return true;
	}

	public boolean isFrameTitle() {
		if (this.getParent() == null || this.getParent().getTitleItem() != this)
			return false;
		return true;
	}

	/**
	 * Returns True if this Item is currently highlighted.
	 * 
	 * @return True if this Item is currently highlighted on the screen, False
	 *         otherwise.
	 */
	public boolean isHighlighted() {
		if (isFloating())
			return false;
		return _mode != HighlightMode.None;
	}

	/**
	 * Tests if the item link is a valid framename, that is, the String must
	 * begin with a character, end with a number with 0 or more letters and
	 * numbers in between. If there is a dot in the framename all the chars
	 * after it must be digits.
	 * 
	 * @return True if the given framename is proper, false otherwise.
	 */
	public boolean isLinkValid() {
		if (FrameIO.isPositiveInteger(getLink()))
			return true;

		if (FrameIO.isValidFrameName(getLink()))
			return true;
		return false;
	}

	public boolean isNear(int x, int y) {

		int xLeft = getPolygon().getBounds().x;
		int yTop = getPolygon().getBounds().y;

		return (x > xLeft - NEAR_DISTANCE && y > yTop - NEAR_DISTANCE
				&& x < xLeft + getBoundsWidth() + NEAR_DISTANCE && y < yTop
				+ getBoundsHeight() + NEAR_DISTANCE);
	}

	public boolean isOldTag() {
		if (this instanceof Text)
			if (((Text) this).getTextList().get(0).toLowerCase().equals("@old"))
				return true;
		return false;
	}

	/**
	 * Merges this Item with the given Item. The merger Item should be left
	 * unchanged after this method. The merger may or may not be the same class
	 * as this Item, exact behaviour depends on the subclass, No-op is allowed.
	 * 
	 * @param merger
	 *            The Item to merge with
	 * @return any Item that should remain on the cursor
	 */
	public abstract Item merge(Item merger, int mouseX, int mouseY);

	/**
	 * Displays this item directly on the screen. Note: All Items are
	 * responsible for their own drawing, buffering, etc.
	 * 
	 * @param g
	 *            The Graphics to draw this Item on.
	 */
	public abstract void paint(Graphics2D g);
	
	public void setTooltips(List<String> tooltips) {
		if (tooltips == null || tooltips.size() == 0) {
			_tooltip = null;
		} else {
			_tooltip = new LinkedList<String>(tooltips);
		}
	}
	
	public void setTooltip(String tooltip) {
		if(_tooltip == null || _tooltip.size() == 0) {
			_tooltip = new LinkedList<String>();
		}
		if(tooltip != null && tooltip.trim().length() > 0) {
			_tooltip.add(tooltip);
		}
	}
	
	public List<String> getTooltip() {
		return _tooltip;
	}
	
	public static void clearTooltipOwner() {
		_tooltipOwner = null;
	}
	
	public void paintTooltip(Graphics2D g) {
		if(_tooltipOwner != this) {
			_tooltipItem = null;
			_tooltipOwner = this;
		}
		// generate tooltip item
		if(_tooltipItem == null) {
			if(_tooltip != null && _tooltip.size() > 0) {
				StringBuffer tooltip = new StringBuffer();
				for(String t : _tooltip) {
					tooltip.append(t).append("\n");
				}
				if(tooltip.length() > 0) {
					tooltip.deleteCharAt(tooltip.length() - 1);
				}
				_tooltipItem = (Text) getParentOrCurrentFrame().getTooltipTextItem("");
				for(String s : _tooltip) {
					// set text
					if(s.trim().toLowerCase().startsWith("text") && s.contains(":")) {
						_tooltipItem.appendLine(s.substring(s.indexOf(':') + 1).trim());
					} else {
						AttributeUtils.setAttribute(_tooltipItem, new Text(s));
					}
				}
			} else {
				return;
			}
		}
		Rectangle bounds = getPolygon().getBounds();
		_tooltipItem.invalidateAll();
		int x = bounds.x + bounds.width;
		if(x + _tooltipItem.getPolygon().getBounds().width > FrameGraphics.getMaxFrameSize().width) {
			x -= x + _tooltipItem.getPolygon().getBounds().width - FrameGraphics.getMaxFrameSize().width;
		}
		int y = bounds.y + bounds.height + bounds.height / 2;
		if(y + _tooltipItem.getPolygon().getBounds().height > FrameGraphics.getMaxFrameSize().height) {
			y = bounds.y + bounds.height / 2 - _tooltipItem.getPolygon().getBounds().height;
		}
		_tooltipItem.setPosition(x, y);
		_tooltipItem.paint(g);
	}
	
	public void paintFill(Graphics2D g) {
		Color fillColor = getFillColor();
		if (fillColor != null && getEnclosingDots() != null) {
			setFillPaint(g);
			g.fillPolygon(getEnclosedShape());
		}
	}

	protected void setFillPaint(Graphics2D g) {
		Color fillColor = getFillColor();
		if (isFloating()) {
			// TODO experiment with adding alpha when picking up filled
			// items... Slows things down quite alot!!
			fillColor = new Color(fillColor.getRed(), fillColor.getGreen(),
					fillColor.getBlue(), fillColor.getAlpha());
		}
		g.setColor(fillColor);
		Color gradientColor = getGradientColor();
		if (gradientColor != null) {
			/*
			 * It is slow when painting gradients... modify so this is only done
			 * once unless it is resized...
			 */
			Shape s = getEnclosedShape();
			if (s != null) {
				Rectangle b = s.getBounds();
				double rads = getGradientAngle() * Math.PI / 180;
				double cos = Math.cos(rads);
				double sin = Math.sin(rads);

				GradientPaint gp = new GradientPaint((int) (b.x + b.width
						* (0.2 * cos + 0.5)), (int) (b.y + b.height
						* (0.2 * sin + 0.5)), fillColor, (int) (b.x + b.width
						* (-0.8 * cos + 0.5)), (int) (b.y + b.height
						* (-0.8 * sin + 0.5)), gradientColor);
				g.setPaint(gp);
			}
		}
	}

	/**
	 * This method performs all the actions in an items list. If it contains a
	 * link as well the link is used as the source frame for all acitons.
	 */
	public void performActions() {
		Frame sourceFrame = null;
		Item sourceItem = FreeItems.getItemAttachedToCursor();

		if (sourceItem == null) {
			sourceItem = this;
		} else {
			for (Item i : sourceItem.getAllConnected()) {
				if (i instanceof Text) {
					sourceItem = i;
					break;
				}
			}
		}

		// TODO decide whether to have items or
		// if a link exists make it the source frame for this action
		if (getLink() != null) {
			sourceFrame = FrameUtils.getFrame(getAbsoluteLink());
		}
		// if no link exists or the link is bad then use the
		// currently displayed frame as the source frame for the
		// action
		if (sourceFrame == null) {
			// For actions like format they rely on this being set to the
			// current frame incase the item being activated is on an overlay
			sourceFrame = DisplayIO.getCurrentFrame();
		}

		for (String s : getAction()) {
			Object returnValue = Actions.PerformActionCatchErrors(sourceFrame,
					sourceItem, s);
			if (returnValue != null) {
				FreeItems.getInstance().clear();
				if (returnValue instanceof Item) {
					Misc.attachToCursor(((Item) returnValue).getAllConnected());
				} else if (returnValue instanceof Collection) {
					try {
						Misc.attachToCursor((Collection) returnValue);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Misc.attachStatsToCursor(returnValue.toString());
				}
			}
		}
	}

	/**
	 * Removes all constraints that this item has.
	 * 
	 */
	public void removeAllConstraints() {
		while (_constraints.size() > 0) {
			Constraint c = _constraints.get(0);
			c.getEnd().removeConstraint(c);
			c.getStart().removeConstraint(c);
		}
	}

	/**
	 * Clears the list of Lines that this Dot is an end of. Note: This only
	 * clears this Dot's list and does not have any affect on the Lines or other
	 * Dots.
	 */
	public void removeAllLines() {
		for (Line l : _lines) {
			l.invalidateAll();
		}
		_lines.clear();
	}

	/**
	 * Removes the given Constraint from the list of constraints that this Dot
	 * is a part of.
	 * 
	 * @param c
	 *            The Constraint that this Dot is no longer a part of.
	 */
	public void removeConstraint(Constraint c) {
		_constraints.remove(c);
	}

	/**
	 * Removes the given Line from the list of lines that this Dot is an end
	 * for.
	 * 
	 * @param line
	 *            The Line that this Dot is no longer an end of.
	 */
	public void removeLine(Line line) {
		if (_lines.remove(line))
			line.invalidateAll();
	}

	public void run() {
		try {
			
			List<String> action = this.getAction();
			if (action != null) {
				String action_name = action.get(0);
				if (action_name.equalsIgnoreCase("RunJavascriptFrame")){
				    // Associate a new Context with this thread
			        org.mozilla.javascript.Context javascript_context = org.mozilla.javascript.Context.enter();
			        try {
			        	Scriptable javascript_scope = javascript_context.initStandardObjects();
			        	Context simple_context = new Context();
			        	
			        	
			        	//Object jsDisplayIO = org.mozilla.javascript.Context.javaToJS(org.expeditee.gui.DisplayIO, javascript_scope);
				        //ScriptableObject.putProperty(javascript_scope, "displayIO", jsDisplayIO);
				        
			        	
		        		Object jsSimpleContext = org.mozilla.javascript.Context.javaToJS(simple_context, javascript_scope);
				        ScriptableObject.putProperty(javascript_scope, "simpleContext", jsSimpleContext);
				           
				        Object jsErr = org.mozilla.javascript.Context.javaToJS(System.err, javascript_scope);
				        ScriptableObject.putProperty(javascript_scope, "err", jsErr);
			           
				        Object jsOut = org.mozilla.javascript.Context.javaToJS(System.out, javascript_scope);
				        ScriptableObject.putProperty(javascript_scope, "out", jsOut);
				           
			        	Javascript.ProgramStarted();
			        	Javascript.RunFrameAndReportError(this, javascript_context,javascript_scope);
			        	MessageBay.displayMessage(AgentStats.getStats(), GREEN);
			        }
			        finally {
			        	org.mozilla.javascript.Context.exit();
			        }
				}
			}
			else {
				
				// assume it is a simple program that is to be run
				Simple.ProgramStarted();
				Context simple_context = new Context();
				Simple.RunFrameAndReportError(this, simple_context);
				MessageBay.displayMessage(AgentStats.getStats(), GREEN);
			}
		} catch (ConcurrentModificationException ce) {
			ce.printStackTrace();
		} catch (IncorrectUseOfStatementException ise) {
			MessageBay.linkedErrorMessage(ise.getMessage());
			MessageBay.displayMessage("See SIMPLE doc for ["
					+ ise.getStatement() + "] statement", ise.getStatement()
					+ "1", Color.CYAN.darker(), true, null);
		} catch (Exception e) {
			MessageBay.linkedErrorMessage(e.getMessage());
		}
		Simple.ProgramFinished();
		// Need to repaint any highlights etc
		FrameGraphics.requestRefresh(true);
	}

	/**
	 * Check if it has a relative link if so make it absolute.
	 * 
	 */
	public void setAbsoluteLink() {
		String link = getLink();
		if (link == null)
			return;
		// Check if all the characters are digits and hence it is a relative
		// link
		if (!FrameIO.isPositiveInteger(link))
			return;

		// Make it an absolute link
		String framesetName;

		if (_parent == null)
			framesetName = DisplayIO.getCurrentFrame().getFramesetName();
		else
			framesetName = _parent.getFramesetName();

		setLink(framesetName + link);
	}

	/**
	 * Sets any action code that should be associated with this Item Each entry
	 * in the list is one line of code
	 * 
	 * @param actions
	 *            The lines of code to associate with this Item
	 */
	public void setActions(List<String> actions) {
		if (actions == null || actions.size() == 0) {
			invalidateCommonTrait(ItemAppearence.LinkChanged);
			_actions = null;
		} else
			_actions = new LinkedList<String>(actions);

		// Want to resize the highlight box for text items if actions have been
		// added
		_poly = null;
		invalidateCommonTrait(ItemAppearence.LinkChanged);
	}

	public void setData(List<String> data) {
		if (data == null || data.size() == 0)
			_data = null;
		else
			_data = new LinkedList<String>(data);
	}

	public void setData(String data) {
		if (data == null || data.length() == 0)
			_data = null;
		else {
			_data = new LinkedList<String>();
			_data.add(data);
		}
	}

	public void addToData(String dataItem) {
		if (dataItem != null) {
			if (_data == null)
				_data = new LinkedList<String>();
			_data.add(dataItem);
		}
	}

	public void setActionCursorEnter(List<String> enter) {
		_actionCursorEnter = enter;
	}

	public void setActionCursorLeave(List<String> leave) {
		_actionCursorLeave = leave;
	}

	public void setActionEnterFrame(List<String> enter) {
		_actionEnterFrame = enter;
	}

	public void setActionLeaveFrame(List<String> leave) {
		_actionLeaveFrame = leave;
	}

	public void setActionMark(boolean val) {
		if (!val)
			invalidateCommonTrait(ItemAppearence.LinkChanged);
		_poly = null;
		_actionMark = val;
		if (val)
			invalidateCommonTrait(ItemAppearence.LinkChanged);
	}

	/**
	 * Sets whether this Item is an Annotation.
	 * 
	 * @param val
	 *            True if this Item is an Annotation, False otherwise.
	 */
	public abstract void setAnnotation(boolean val);

	/**
	 * Used to set this Line as an Arrow. If length and ratio are 0, no arrow is
	 * shown.
	 * 
	 * @param length
	 *            The how far down the shaft of the line the arrowhead should
	 *            come.
	 * @param ratio
	 *            The ratio of the arrow's length to its width.
	 */
	public void setArrow(float length, double ratio, double nib_perc) {
		_arrowheadLength = length;
		_arrowheadRatio = ratio;
		_arrowheadNibPerc = nib_perc;
		updateArrowPolygon();
	}

	public void setArrow(float length, double ratio) {
		setArrow(length,ratio,DEFAULT_ARROWHEAD_NIB_PERC);
	}
	
	public void setArrowhead(Polygon arrow) {
		_arrowhead = arrow;
	}

	public void setArrowheadLength(float length) {
		_arrowheadLength = length;
		updateArrowPolygon();
	}

	public void setArrowheadRatio(double ratio) {
		_arrowheadRatio = ratio;
		updateArrowPolygon();
	}
	
	public void setArrowheadNibPerc(double perc) {
		_arrowheadNibPerc = perc;
		updateArrowPolygon();
	}

	public void setBackgroundColor(Color c) {
		if (c != _colorBackground) {
			_colorBackground = c;
			invalidateCommonTrait(ItemAppearence.BackgroundColorChanged);
		}
	}

	public void setBorderColor(Color c) {
		if (c != _colorBorder) {
			_colorBorder = c;
			invalidateCommonTrait(ItemAppearence.BorderColorChanged);
		}
	}

	/**
	 * Sets the Color to use on the bottom and right sections of this Item's
	 * border. If top is NULL, then the Item's background Color will be used.
	 * 
	 * @param top
	 *            The Color to display in the bottom and right sections of this
	 *            Item's border.
	 */
	public void setBottomShadowColor(Color bottom) {
		_colorBottomShadow = bottom;
	}

	/**
	 * Sets the foreground Color of this Item to the given Color.
	 * 
	 * @param c
	 */
	public void setColor(Color c) {
		if (c != _color) {
			_color = c;
			invalidateCommonTrait(ItemAppearence.ForegroundColorChanged);
			if (hasVector()) {
				// TODO make this more efficient so it only repaints the items
				// for this vector
				FrameKeyboardActions.Refresh();
			}
		}
	}

	public void setConstraintIDs(String IDs) {
	}

	public void setConstraints(List<Constraint> constraints) {
		_constraints = constraints;
	}

	public void setTag(String newData) {
		if (newData != null)
			_tag = new StringBuffer(newData);
		else
			_tag = null;
	}

	/**
	 * Sets the created date of this Frame to the given String.
	 * 
	 * @param date
	 *            The date to use for this Frame.
	 */
	public void setDateCreated(String date) {
		_creationDate = date;
	}

	public void setFillColor(Color c) {

		_colorFill = c;

		for (Line line : _lines) {
			Item other = line.getOppositeEnd(this);
			if (other.getFillColor() != c) {
				other.setFillColor(c);
			}
		}

		invalidateCommonTrait(ItemAppearence.FillColor);
		invalidateFill();
	}

	public void setGradientColor(Color c) {
		_colorGradient = c;

		for (Line line : _lines) {
			Item other = line.getOppositeEnd(this);
			if (other.getGradientColor() != c)
				other.setGradientColor(c);
		}

		invalidateCommonTrait(ItemAppearence.GradientColor);
		invalidateFill();
	}

	public Color getGradientColor() {
		return _colorGradient;
	}

	public void setFillPattern(String patternLink) {
		_fillPattern = patternLink;
		invalidateCommonTrait(ItemAppearence.FillPattern);
		invalidateFill();
	}

	public void setFloating(boolean val) {
		_floating = val;
	}

	public void setHighlight(boolean val) {
		_highlight = val;
	}

	/**
	 * Sets the ID of this Item to the given Integer. Note: Items with ID's < 0
	 * are not saved
	 * 
	 * @param newID
	 *            The new ID to assign this Item.
	 */
	public void setID(int newID) {
		_id = newID;
	}

	/**
	 * Sets the list of lines that this point is part of (may be set to null).
	 * 
	 * @param lineID
	 *            A String of line ID numbers separated by spaces.
	 */
	public void setLineIDs(String lineID) {
	}

	public void setLinePattern(int[] pattern) {
		_linePattern = pattern;

		for (Line line : getLines())
			line.setLinePattern(pattern);
	}

	public void setLines(List<Line> lines) {
		_lines = lines;

		for (Line line : lines)
			line.setLinePattern(getLinePattern());

	}

	/**
	 * Links this item to the given Frame, this may be set to null to remove a
	 * link.
	 * 
	 * @param frameName
	 *            The name of the Frame to link this item to.
	 */
	public void setLink(String frameName) {
		if (frameName == null) {
			invalidateCommonTrait(ItemAppearence.LinkChanged);
		}

		// If a link is being removed or set then need to reset poly so the
		// highlighting is drawn with the correct width
		if (frameName == null || getLink() == null)
			_poly = null;

		if (FrameIO.isValidLink(frameName))
			_link = frameName;
		else
			MessageBay.errorMessage("[" + frameName
					+ "] is not a valid frame name");
		// TODO make this throw exceptions etc...

		invalidateCommonTrait(ItemAppearence.LinkChanged);
	}
	
	public void setLinkHistory(boolean value) {
		_linkHistory = value;
	}
	
	public boolean getLinkHistory() {
		return _linkHistory;
	}

	public void setLinkFrameset(String frameset) {
		if (frameset == null || FrameIO.isValidFramesetName(frameset))
			_link_frameset = frameset;
		else
			MessageBay.errorMessage("[" + frameset
					+ "] is not a valid frameset name");
		// TODO make this throw exceptions etc...
	}

	public void setLinkMark(boolean val) {
		if (!val)
			invalidateCommonTrait(ItemAppearence.LinkChanged);
		_poly = null;
		_linkMark = val;
		if (val)
			invalidateCommonTrait(ItemAppearence.LinkChanged);
	}

	public void setLinkTemplate(String template) {
		if (FrameIO.isValidLink(template))
			_link_template = template;
		else
			MessageBay.errorMessage("[" + template
					+ "] is not a valid frame name");
		// TODO make this throw exceptions etc...
	}

	// /**
	// * Sets the maximum coordinates on the screen that this item may occupy.
	// * This is used by Text items to compute word-wrapping lengths.
	// *
	// * @param d
	// * The Maximum size of the Frame containing this Item.
	// */
	// public void setMaxWidth(int width) {
	// if (width > 0) {
	// _maxWidth = width;
	// updatePolygon();
	// }
	// }

	public void setOffset(int x, int y) {
		_offset.setLocation(x, y);
	}

	public void setOffset(Point p) {
		_offset.setLocation(p);
	}

	public void setOwner(String own) {
		_owner = own;
	}

	public void setParent(Frame frame) {
		_oldParent = _parent;
		_parent = frame;

		if (_parent != null && UserSettings.UserName != null
				&& !UserSettings.UserName.equals(_parent.getOwner())) {
			setOwner(UserSettings.UserName.get());
		}
	}

	/**
	 * Invalidates this, connected lines and fill
	 * 
	 * @param trait
	 */
	private void invalidateCommonTraitForAll(ItemAppearence trait) {
		invalidateCommonTrait(trait);
		if (isLineEnd()) {
			boolean hasLinePattern = getLines().get(0).getLinePattern() != null;
			if (hasLinePattern) {
				for (Item i : getAllConnected()) {
					if (i instanceof Line) {
						((Line) i).invalidateCommonTrait(trait);
					}
				}
			} else {
				for (Line line : getLines()) {
					line.invalidateCommonTrait(trait);
				}
			}
		}
		if (_colorFill != null) {
			invalidateFill(); // only invalidates if has fill
		}
		for (XRayable x : getEnclosures()) {
			x.invalidateCommonTrait(trait);
		}

	}

	
	
	
	
	protected void anchorConstraints()
	{
		// update the position of any dots that are constrained by this one
		for (Constraint c : _constraints) {
			Item other = c.getOppositeEnd(this);

			// only set position if the other dot is still fixed to the
			// frame
			if (/* this.isFloating() && */!other.isFloating()) {
				if (c.getType() == Constraint.HORIZONTAL) {
					if (isAnchoredY()) {
						// Make the 'other' item have the same anchor top/bottom values as this
						other._anchorTop    = _anchorTop;
						other._anchorBottom = _anchorBottom;
					}
				} else if (c.getType() == Constraint.VERTICAL) {
					if (isAnchoredX()) {
						// Make the 'other' item have the same anchor left/right values as this
						other._anchorLeft  = _anchorLeft;
						other._anchorRight = _anchorRight;
					}
				} else if (c.isDiagonal()) {

					System.err.println("Warning: anchorConstraints() not implement for Diagonal setting");
				}
			}
		}
	}
	
	/**
	 * Sets the position of this item on the screen
	 * 
	 * @param x
	 *            The new X coordinate
	 * @param y
	 *            The new Y coordinate
	 */
	public void setPosition(float x, float y) {
		float deltaX = x - _x;
		float deltaY = y - _y;

		if (deltaX == 0 && deltaY == 0)
			return;

		invalidateCommonTraitForAll(ItemAppearence.PreMoved);

		_x = x;
		_y = y;

		for (Item i : getEnclosures()) {
			i.updatePolygon();
		}
		updatePolygon();

		// update the position of any dots that are constrained by this one
		for (Constraint c : _constraints) {
			Item other = c.getOppositeEnd(this);

			// only set position if the other dot is still fixed to the
			// frame
			if (/* this.isFloating() && */!other.isFloating()) {
				if (c.getType() == Constraint.HORIZONTAL) {
					if (other._y != y) {
						other.setY(y);
					}
				} else if (c.getType() == Constraint.VERTICAL) {
					if (other._x != x) {
						other.setX(x);
					}
				} else if (c.isDiagonal()) {
					if (Math.abs(other._x - x) != Math.abs(other._y - y)) {

						float m1 = c.getGradient();
						float c1 = y - m1 * x;
						// Now work out the equation for the second line
						// Get the first line the other end is attached to that
						// is not the diagonal line
						List<Line> lines = other.getLines();
						// If there is only one line...
						if (lines.size() == 1) {
							if (m1 != 0) {
								if (Math.abs(deltaX) > Math.abs(deltaY)) {
									other.setX((other._y - c1) / m1);
								} else {
									other.setY(m1 * other._x + c1);
								}
							}
						} else if (lines.size() > 1) {
							Line otherLine = lines.get(0);
							Item end = otherLine.getOppositeEnd(other);
							if (end.equals(this)) {
								otherLine = lines.get(1);
								end = otherLine.getOppositeEnd(other);
								assert (!end.equals(this));
							}

							float xDiff = end._x - other._x;
							float yDiff = end._y - other._y;
							if (xDiff == 0) {
								other.setY(m1 * other._x + c1);
							} else if (Math.abs(xDiff) == Math.abs(yDiff)
									&& !this.isFloating() && deltaX == 0
									&& deltaY == 0) {
								if (deltaX == 0) {
									_x = (_y - other._y) * m1 + other._x;
								} else {
									_y = (_x - other._x) * m1 + other._y;
								}
							} else {
								float m2 = yDiff / xDiff;
								float c2 = end._y - m2 * end._x;
								float mDiff = m1 - m2;
								if (Math.abs(mDiff) < 0.000001) {
									assert (false);
									// TODO how do I handle this case!!
								} else {
									float newX = (c2 - c1) / mDiff;
									float newY = m1 * newX + c1;
									if (other._x != newX
									/* && other._y != newY */) {
										other.setPosition(newX, newY);
									}
								}
							}
						}
						// Do simultaneous equations to get the new postion for
						// the other end of the diagonal line
					}
				}
			}
		}

		for (Line line : getLines()) {
			line.updatePolygon();
		}

		// for (Item item : getAllConnected()) {
		// item.updatePolygon();
		// }

		invalidateCommonTraitForAll(ItemAppearence.PostMoved);

	}

	public void setPosition(Point position) {
		setPosition(position.x, position.y);
	}

	public void setRelativeLink() {
		String link = getLink();
		if (link == null)
			return;
		assert (_parent != null);

		if (FrameIO.isPositiveInteger(link))
			return;

		// Check if the link is for the current frameset
		if (_parent.getFramesetName().equalsIgnoreCase(
				Conversion.getFramesetName(link))) {
			setLink("" + Conversion.getFrameNumber(link));
		}
	}

	/**
	 * Sets the size of this Item. For Text this is the Font size. For Lines and
	 * Dots this is the thickness.
	 */
	public void setSize(float size) {
	}

	/**
	 * Sets the thickness of the item.
	 * 
	 * @param thick
	 */
	public final void setThickness(float thick) {
		setThickness(thick, true);
	}

	/**
	 * Sets the thickness of this item.
	 * 
	 * @param thick
	 *            the new thickness for the item
	 * @param setConnectedThickness
	 *            true if all items connected to this item should also have
	 *            their thickness set
	 */
	public void setThickness(float thick, boolean setConnectedThickness) {
		if (thick == _thickness)
			return;
		boolean bigger = thick > _thickness;

		if (!bigger) {
			if (setConnectedThickness) {
				// TODO is there a more efficient way of doing this?
				for (Item i : getConnected())
					i.invalidateCommonTrait(ItemAppearence.Thickness);
			} else {
				invalidateCommonTrait(ItemAppearence.Thickness);
			}
		}

		_thickness = thick;
		// update the size of any lines
		/*
		 * TODO: Revise the way line thickness is set to make it more efficient
		 * etc...
		 */
		for (Line line : getLines())
			line.setThickness(thick, setConnectedThickness);

		if (setConnectedThickness)
			updatePolygon();

		if (bigger) {
			if (setConnectedThickness) {
				for (Item i : getConnected())
					i.invalidateCommonTrait(ItemAppearence.Thickness);
			} else {
				invalidateCommonTrait(ItemAppearence.Thickness);
			}
		}
	}

	/**
	 * Returns the thickness (in pixels) of this Dot.
	 * 
	 * @return The 'thickness' of this Dot. (returns -1 if the thickness is not
	 *         set).
	 */
	public float getThickness() {
		return _thickness;
	}

	/**
	 * Sets the Color to use on the top and left sections of this Item's border.
	 * If top is NULL, then the Item's background Color will be used.
	 * 
	 * @param top
	 *            The Color to display in the top and left sections of this
	 *            Item's border.
	 */
	public void setTopShadowColor(Color top) {
		_colorTopShadow = top;
	}

	public void setWidth(Integer width) throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Item type does not support width attribute!");
	}

	public void setRightMargin(int i, boolean fixWidth) {
		int newWidth = i - getX() - Item.MARGIN_LEFT;
		if (!fixWidth) {
			newWidth *= -1;
		}

		setWidth(newWidth);
	}

	/**
	 * Sets the position of this Item on the X axis
	 * 
	 * @param newX
	 *            The position on the X axis to assign to this Item
	 */
	public void setX(float newX) {
		setPosition(newX, getY());
	}

	/**
	 * Sets the position of this Item on the Y axis
	 * 
	 * @param newY
	 *            The position on the Y axis to assign to this Item
	 */
	public void setY(float newY) {
		setPosition(getX(), newY);
	}

	/**
	 * Paints any highlighting of this Item. This may include changing the
	 * thickness (lines) or painting a box around the item (Text, Images). If
	 * val is True then the Graphics Color is changed to the highlight Color, if
	 * False then the Graphics Color is left unchanged (for clearing of
	 * highlighting).
	 * 
	 * @param val
	 *            True if this Item should be highlighted, false if the
	 *            highlighting is being cleared.
	 * @return The desired mouse cursor when this Item is highlighted (negative
	 *         means no change)
	 */
	public int setHighlightColor() {
		return setHighlightColor(DEFAULT_HIGHLIGHT);
	}

	public int setHighlightColor(Color c) {
		if (!this.isVisible() && this.hasVector()) {
			for (Item i : this.getParentOrCurrentFrame().getVectorItems()) {
				if (i.getEditTarget() == this) {
					i.setHighlightColor(c);
				}
			}
		}

		_highlightThickness = DEFAULT_HIGHLIGHT_THICKNESS;

		Color selColor = (c != null) ? c : DEFAULT_HIGHLIGHT;
		if (_highlightColor != c) {
			_highlightColor = selColor;
			this.invalidateCommonTrait(ItemAppearence.HighlightColorChanged);
		}

		return Item.UNCHANGED_CURSOR;

	}

	private void updateArrowPolygon() {
		if (getArrowheadLength() < 0 || getArrowheadRatio() < 0 || getArrowheadNibPerc() < 0)
			_arrowhead = null;
		else {
			_arrowhead = Line.createArrowheadPolygon(getX(),getY(),getArrowheadLength(),getArrowheadRatio(),getArrowheadNibPerc()); 
		}
	}

	public abstract void updatePolygon();

	public void setHidden(boolean state) {
		this._visible = !state;
	}

	public void setVisible(boolean state) {
		this._visible = state;
	}

	public boolean isVisible() {
		return _visible && !_deleted;
	}

	/**
	 * Raised whenever the item is removed, added, no longer in view (That is,
	 * when it is not on any of the current frames, of overlays of the current
	 * frames) or has become visible. That is, when it is either on a current
	 * frame, or an overlay of a current frame.
	 * 
	 * @param e
	 *            The event
	 */
	public void onParentStateChanged(ItemParentStateChangedEvent e) {
	}

	public void setHighlightMode(HighlightMode mode, Color color) {
		setHighlightColor(color);
		if (hasPermission(UserAppliedPermission.followLinks)
				|| getEditTarget().hasPermission(UserAppliedPermission.followLinks)) {
			if (_mode != mode) {
				_mode = mode;
				this.invalidateCommonTrait(ItemAppearence.HighlightModeChanged);
			}
		}
	}

	public HighlightMode getHighlightMode() {
		return _mode;
	}

	public void anchor() {
		Frame current = getParentOrCurrentFrame();
		// only set the id if we've moved to a different frame, or if the frame already has an item with that id
		if(!current.equals(_oldParent) || current.getItemWithID(getID()) != null) {
			int id = _id;
			setID(current.getNextItemID());
			// System.out.println(this + " - Set ID to " + _id + " (was " + id + ")");
		} else {
			// System.out.println(this + " - Kept old ID of " + _id);
		}
		setOffset(0, 0);
		setParent(current);

		current.addItem(this, false);
		current.setResort(true);
		setRelativeLink();
		setFloating(false);

		// // If its an unconstrained line end check if we should add a
		// constraint
		// if (isLineEnd() && getLines().size() <= 2
		// && getConstraints().size() <= 1) {
		// Constraint existingConstraint = null;
		// List<Constraint> constraints = getConstraints();
		// // Get the existing constraint
		// if (constraints.size() > 0) {
		// existingConstraint = constraints.get(0);
		// }
		// for (Line line : getLines()) {
		// Integer constraintType = line.getPossibleConstraint();
		// if (constraintType != null) {
		// Item oppositeEnd = line.getOppositeEnd(this);
		// if (existingConstraint == null
		// || !existingConstraint.contains(oppositeEnd)) {
		// new Constraint(this, oppositeEnd,
		// getParentOrCurrentFrame().getNextItemID(),
		// constraintType);
		// }
		// }
		// }
		// }
	}

	/**
	 * Gets the parent frame if it is set or the current frame if this item does
	 * not have a parent set.
	 * 
	 * @return
	 */
	public Frame getParentOrCurrentFrame() {
		// if the item is from an overlay the parent will NOT be null
		if (getParent() == null) {
			return DisplayIO.getCurrentFrame();
		}
		return getParent();
	}

	/**
	 * Sets the list of Dots (including this one) that form a closed shape.
	 * Passing null sets this dot back to its normal (non-enclosed) state.
	 * 
	 * @param enclosed
	 *            The List of Dots including this one that form a closed shape,
	 *            or null.
	 */
	public void setEnclosedList(Collection<Item> enclosed) {

		boolean changed = (_enclosure == null && enclosed != null);

		if (_enclosure != null && enclosed == null) {
			invalidateFill();
		}

		_enclosure = enclosed;

		if (changed) {
			invalidateFill();
			;
		}
	}

	/**
	 * Returns the polygon that represents the shape created by all the Dots in
	 * this Dot's enclosed list. If the list is null, then null is returned.
	 * 
	 * @return A Polygon the same shape and position as created by the Dots in
	 *         the enclosed list.
	 */
	public Polygon getEnclosedShape() {
		if (_enclosure == null)
			return null;

		Polygon poly = new Polygon();
		for (Item d : _enclosure) {
			poly.addPoint(d.getX(), d.getY());
		}

		return poly;
	}

	/**
	 * Returns the list of Dots that, along with this Dot, form an enclosed
	 * polygon. If this Dot is not part of an enclosure null may be returned.
	 * 
	 * @return The List of Dots that form an enclosed shape with this Dot, or
	 *         null if this Dot is not part of an enclosure.
	 */
	public Collection<Item> getEnclosingDots() {
		return _enclosure;
	}

	/**
	 * Returns whether this Dot has an assigned enclosure list of other Dots.
	 * The result is the same as getEnclosedShape() != null.
	 * 
	 * @return True if this Dot has an enclosure list of other Dots, false
	 *         otherwise.
	 */
	public boolean isEnclosed() {
		return _enclosure != null;
	}

	/**
	 * True if this item is the end of a line.
	 * 
	 * @return
	 */
	public boolean isLineEnd() {
		// TODO this will need to be redone when enclosure class is added...
		// At the moment enclosures are only circles...we don't want circle
		// centres to be lineEnds
		return _lines.size() > 0;
	}

	public boolean hasEnclosures() {
		return _enclosures.size() > 0;
	}

	/**
	 * Method that is called to notify an item that is on the end of a line that
	 * its line has changed color.
	 * 
	 * @param c
	 *            the new color for the line
	 */
	protected void lineColorChanged(Color c) {
		for (Line l : getLines()) {
			if (l.getColor() != c)
				l.setColor(c);
		}
	}

	/**
	 * Checks if this item is off the left or top of the screen
	 * 
	 * @return
	 */
	public boolean offScreenTopOrLeft() {
		Rectangle itemRect = getArea().getBounds();
		// Check that the bottom right corner of this item is on the screen
		if (itemRect.x + itemRect.width >= 0
				&& itemRect.y + itemRect.height >= 0)
			return false;
		// Check if all the items it is connected to are offscreen
		for (Item i : getAllConnected()) {
			Rectangle iRect = i.getArea().getBounds();
			// Check that the bottom right corner of this item is on the screen
			if (iRect.x + iRect.width >= 0 && iRect.y + iRect.height >= 0) {
				return false;
			}
		}
		return true;
	}

	public void setConnectedToAnnotation(boolean val) {
		_connectedToAnnotation = val;
	}

	public boolean isConnectedToAnnotation() {
		return _connectedToAnnotation;
	}

	public boolean hasAction() {
		List<String> actions = getAction();
		return actions != null && actions.size() > 0;
	}

	public void setAction(String action) {
		// Want to resize the highlight box for text items if actions are been
		// added
		if (action == null || action.length() == 0) {
			invalidateCommonTrait(ItemAppearence.LinkChanged);
		}
		if (_actions == null || _actions.size() == 0) {
			_poly = null;
			_actions = new LinkedList<String>();
		} else {
			_actions.clear();
		}
		if (action != null && action.length() > 0)
			_actions.add(action);
		invalidateCommonTrait(ItemAppearence.LinkChanged);
	}

	protected int getLinkYOffset() {
		return 0;
	}

	protected Rectangle getLinkDrawArea() {
		return getLinkDrawArea(getX() - LEFT_MARGIN, getY() + getLinkYOffset());
	}

	/**
	 * TODO: Revise - it would be good to have a member that defines the link
	 * dimensions.
	 * 
	 * @param x
	 *            Left of graphic (i.e not centered)
	 * @param y
	 *            Above graphic (i.e not centered)
	 * 
	 * @return The drawing area of the link at the given coordinates.
	 */
	public Rectangle getLinkDrawArea(int x, int y) {
		return new Rectangle(x + 2, y - 1, 8, 8);
	}

	/**
	 * Paint the link symbol for the item if it is a
	 * 
	 * @param g
	 */
	protected void paintLink(Graphics2D g) {
		paintLinkGraphic(g, getX() - LEFT_MARGIN, getY() + getLinkYOffset());
	}

	/**
	 * Paint the link symbol for the item at a given position.
	 * 
	 * @see #paintLink
	 * 
	 * @param g
	 *            The graphics to paint with
	 * 
	 * @param x
	 *            The x position of the link. Left of graphic (i.e not centered)
	 * 
	 * @param y
	 *            The y position of the link. Above of graphic (i.e not
	 *            centered)
	 */
	public void paintLinkGraphic(Graphics2D g, int x, int y) {

		boolean hasLink = getLink() != null;
		boolean hasAction = hasAction();

		if (hasLink || hasAction) {
			g.setStroke(HIGHLIGHT_STROKE);
			if (hasLink && hasAction) {
				g.setColor(LINK_ACTION_COLOR);
			} else if (hasLink) {
				g.setColor(LINK_COLOR);
			} else if (hasAction) {
				g.setColor(ACTION_COLOR);
			}

			AffineTransform at = new AffineTransform();
			AffineTransform orig = g.getTransform();
			at.translate(x, y);
			g.setTransform(at);

			if (getLinkMark() && getLink() != null) {
				g.drawPolygon(getLinkPoly());

				// if the link is not valid, cross out the circle
				if (!isLinkValid())
					g.drawPolygon(getCircleCross());
			}

			if (getActionMark() && hasAction()) {
				g.drawPolygon(getLinkPoly());
				g.fillPolygon(getLinkPoly());

				// if the link is not valid, cross out the circle
				if (!isLinkValid() && getLink() != null) {
					g.setColor(getParent().getPaintBackgroundColor());
					g.drawPolygon(getCircleCross());
				}
			}

			// reset the graphics tranformation
			g.setTransform(orig);
		}
	}

	/**
	 * Gets the distance between the start of the text and the left border of
	 * the item. This distance changes depending on whether or not the item is
	 * linked or has an associated action.
	 * 
	 * @return the gap size in pixels
	 */
	protected int getLeftMargin() {
		return ((getLinkMark() && getLink() != null)
				|| (getActionMark() && getAction() != null) ? MARGIN_LEFT
				- MARGIN_RIGHT : MARGIN_RIGHT);
	}

	public String getName() {
		return getText();
	}

	final public String getAbsoluteLinkTemplate() {
		return getAbsoluteLink(getLinkTemplate());
	}

	final public String getAbsoluteLinkFrameset() {
		return getAbsoluteLink(getLinkFrameset());
	}

	final public String getAbsoluteLink() {
		return getAbsoluteLink(getLink());
	}

	/**
	 * @param link
	 * @return
	 */
	private String getAbsoluteLink(String link) {
		if (link == null)
			return null;
		// assert (_parent!= null);
		Frame parent = getParentOrCurrentFrame();
		if (_parent == null) {
			// if parent is null it is an item on the message box
			// so it must already be absolute
			// assert (!FrameIO.isPositiveInteger(link));
			// return link;

		}

		// if its a relative link then return absolute
		if (FrameIO.isPositiveInteger(link)) {
			return parent.getFramesetName() + link;
		}
		return link;
	}

	public static String convertToAbsoluteLink(String link) {
		if (link == null)
			return null;
		// assert (_parent!= null);
		Frame parent = DisplayIO.getCurrentFrame();
		assert (parent != null);

		// if its a relative link then return absolute
		if (FrameIO.isPositiveInteger(link)) {
			return parent.getFramesetName() + link;
		}
		return link;
	}

	/**
	 * Sets the x and y values of this item ignoring constraints.
	 * 
	 * @param x
	 *            new x position
	 * @param y
	 *            new y position
	 */
	public void setXY(float x, float y) {
		_x = x;
		_y = y;
	}

	/**
	 * Recursive function for getting the path around a shape. This is used to
	 * get the path that is painted on the screen.
	 * 
	 * @param visited
	 * @param points
	 * @param addToEnd
	 * @param toExplore
	 */
	public void appendPath(Collection<Line> visited, LinkedList<Point> points,
			boolean addToEnd, Collection<Line> toExplore) {

		if (addToEnd) {
			// put the start item points into our list
			points.addLast(getPosition());
		} else {
			points.addFirst(getPosition());
		}

		// Find the line that has not been added yet
		LinkedList<Line> lines = new LinkedList<Line>();
		lines.addAll(getLines());

		while (!lines.isEmpty()) {
			Line l = lines.remove();
			// if we havnt visited the line yet visit it
			if (!visited.contains(l)) {
				visited.add(l);
				Item otherEnd = l.getOppositeEnd(this);
				// Add all the enexplored lines to our list
				while (!lines.isEmpty()) {
					l = lines.remove();
					// Get the paths for the rest of the lines to be explored
					// later
					if (!toExplore.contains(l) && !visited.contains(l)) {
						toExplore.add(l);
					}
				}
				otherEnd.appendPath(visited, points, addToEnd, toExplore);
			}
		}
	}

	/**
	 * Gets the size of the enclosure that this item is part of. Used to
	 * determine the paint order of items, with smaller items being painted
	 * first.
	 * 
	 * @return the area of the box surrounding the enclosed shape that this item
	 *         is part of
	 */
	public double getEnclosedArea() {
		if (_enclosure == null)
			return 0.0;
		Rectangle2D box = getEnclosedShape().getBounds2D();
		return box.getWidth() * box.getHeight();
	}

	public Rectangle getEnclosedRectangle() {
		if (_enclosure == null)
			return null;
		return getEnclosedShape().getBounds();
	}

	public int getEnclosureID() {
		return _enclosure == null ? 0 : _enclosure.hashCode();
	}

	/**
	 * Returns the Shape that surrounds this Item representing this Item's
	 * 'gravity'.
	 * 
	 * @return The Shape (rectangle) surrounding this Item, which represents
	 *         this Items 'gravity'.
	 */
	public final Polygon getPolygon() {
		if (_poly == null)
			updatePolygon();

		return new Polygon(_poly.xpoints, _poly.ypoints, _poly.npoints);
	}

	/**
	 * Shifts the position of the item along the line between this items
	 * location and a specified point.
	 * 
	 * @param origin
	 * @param ratio
	 */
	public void translate(Point2D origin, double ratio) {

		invalidateCommonTraitForAll(ItemAppearence.PreMoved);

		_x = (float) (origin.getX() + ratio * (_x - origin.getX()));
		_y = (float) (origin.getY() + ratio * (_y - origin.getY()));
		updatePolygon();
		for (Line line : getLines())
			line.updatePolygon();

		invalidateCommonTraitForAll(ItemAppearence.PostMoved);

	}

	private static int[] LinePatterns = new int[] { 0, 10, 20 };

	/**
	 * The rotates through a wheel of dashed lines.
	 * 
	 * @param amount
	 *            number of rotations around the wheel to toggle by.
	 */
	public void toggleDashed(int amount) {
		// find the index of the current line pattern
		int[] currentPattern = getLinePattern();

		// Find the current pattern and move to the next pattern in the wheel
		for (int i = 0; i < LinePatterns.length; i++) {
			if (currentPattern == null || currentPattern[0] == LinePatterns[i]) {
				i += LinePatterns.length + amount;
				i %= LinePatterns.length;

				// if we are at the start of the wheel make it 'null' (solid
				// line)
				if (i == 0) {
					setLinePattern(null);
				} else {
					setLinePattern(new int[] { LinePatterns[i], LinePatterns[i] });
				}

				invalidateCommonTrait(ItemAppearence.ToggleDashed);
				return;
			}
		}

	}

	Collection<XRayable> _enclosures = new HashSet<XRayable>();

	private boolean _deleted = false;

	private Overlay _overlay = null;

	protected AttributeValuePair _attributeValuePair = null;

	private Float _autoStamp = null;

	/**
	 * For now there can only be one enclosure per item
	 * 
	 * @param enclosure
	 */
	public void addEnclosure(XRayable enclosure) {
		_enclosures.clear();
		_enclosures.add(enclosure);
	}

	/**
	 * Gets any XRayable items that have this item as a source.
	 * 
	 * @return the collection of items that are linked to this item as source.
	 *         Guaranteed not to be null.
	 */
	public Collection<? extends XRayable> getEnclosures() {
		return _enclosures;
	}

	public void removeEnclosure(Item i) {
		_enclosures.remove(i);

	}

	public boolean isDeleted() {
		return _deleted;
	}

	/**
	 * @return The full canvas that this item draws to. Must include
	 *         highlighting bounds
	 */
	public Rectangle[] getDrawingArea() {

		return new Rectangle[] { ItemUtils.expandRectangle(getPolygon()
				.getBounds(), (int) Math.ceil(Math.max(_highlightThickness,
				getThickness()))) };

	}

	/**
	 * 
	 * @param area
	 * @return True if area intersects with this items drawing area.
	 */
	public final boolean isInDrawingArea(Area area) {
		for (Rectangle r : getDrawingArea()) {
			if (area.intersects(r))
				return true;
		}
		return false;
	}

	/**
	 * Completetly invalidates the item - so that it should be redrawed. Note:
	 * This is handled internally, it should be reare to invoke this externally
	 */
	public final void invalidateAll() {
		invalidate(getDrawingArea());
	}

	/**
	 * Invalidates areas on the parent frame. Purpose: to be called on specific
	 * areas of the item that needs redrawing.
	 * 
	 * @param damagedAreas
	 */
	protected final void invalidate(Rectangle[] damagedAreas) {
		for (Rectangle r : damagedAreas)
			invalidate(r);
	}

	/**
	 * Invalidates areas on the parent frame. Purpose: to be called on specific
	 * areas of the item that needs redrawing.
	 * 
	 * @param damagedAreas
	 */
	protected final void invalidate(Rectangle damagedArea) {
		FrameGraphics.invalidateItem(this, damagedArea);
	}

	/**
	 * Used to invalidate visual traits commonly shared by all items.
	 * 
	 * @param trait
	 */
	public final void invalidateCommonTrait(ItemAppearence trait) {
		invalidate(getDamagedArea(trait));

		if (_colorFill != null
				&& (trait == ItemAppearence.Added || trait == ItemAppearence.Removed)) {
			invalidateFill();
		}
	}

	/**
	 * Invalidates fill if has one, even if no color is set.
	 */
	public void invalidateFill() {
		if (isLineEnd() && _enclosure != null) {
			invalidate(getEnclosedShape().getBounds());
		}
	}

	/**
	 * Default implementation always uses drawing area except for links, where
	 * the link drawing area is used. Override to make item drawing more
	 * efficient - defining only parts of the item that needs redrawing.
	 * 
	 * @see Item.getDrawingArea
	 * 
	 * @param trait
	 *            The visual trait that has changed.
	 * 
	 * @return The damaged area according to the visual trait that has changed.
	 */
	protected Rectangle[] getDamagedArea(ItemAppearence trait) {

		if (trait == ItemAppearence.LinkChanged)
			return new Rectangle[] { getLinkDrawArea() }; // Invalidate area
		// where link is
		// drawn

		return getDrawingArea();

	}

	public boolean hasVector() {
		return _overlay instanceof Vector;
	}

	public boolean hasOverlay() {
		return _overlay != null;
	}

	public Vector getVector() {
		if (_overlay instanceof Vector)
			return (Vector) _overlay;
		return null;
	}

	public void setOverlay(Overlay overlay) {
		_overlay = overlay;
	}

	public boolean dontSave() {
		/*
		 * TODO Mike says: checkout if the ID check is still needed- When will
		 * ID still be -1 when saving a frame? assert (i != null);
		 */
		// make it save stuff that's off the screen so stuff isn't deleted by panning - jts21
		return !_save || !isVisible() || getID() < 0; // || offScreenTopOrLeft();
	}

	public void setAnchorLeft(Float anchor) {
		this._anchorLeft = anchor;
		this._anchorRight = null;
		if (anchor != null) {
			anchorConstraints();
		    setX(anchor);
		}
	}

	public void setAnchorRight(Float anchor) {
		this._anchorRight = anchor;
		this._anchorLeft = null;
		if (anchor != null) {
			anchorConstraints();
			setX(FrameGraphics.getMaxFrameSize().width - anchor
					- getBoundsWidth());
		}
	}

	public void setAnchorTop(Float anchor) {
		this._anchorTop = anchor;
		this._anchorBottom = null;
		if (anchor != null) {
			anchorConstraints();
			setY(anchor);
		}
	}


	public void setAnchorBottom(Float anchor) {
		this._anchorBottom = anchor;
		this._anchorTop = null;
		if (anchor != null) {
			anchorConstraints();
			setY(FrameGraphics.getMaxFrameSize().height - anchor);
		}
	}


	public boolean isAnchored() {
	    return ((_anchorLeft != null) || (_anchorRight != null) 
		    || (_anchorTop != null) || (_anchorBottom != null));
	}

	public boolean isAnchoredX() {
	    return ((_anchorLeft != null) || (_anchorRight != null));
	}

	public boolean isAnchoredY() {
	    return ((_anchorTop != null) || (_anchorBottom != null));
	}

	public Float getAnchorLeft() {
		return _anchorLeft;
	}

	public Float getAnchorRight() {
		return _anchorRight;
	}

	public Float getAnchorTop() {
		return _anchorTop;
	}

	public Float getAnchorBottom() {
		return _anchorBottom;
	}

	public String getText() {
		return "@" + getClass().getSimpleName() + ":" + getID();
	}

	public void setText(String text) {
	}

	public boolean recalculateWhenChanged() {
		return false;
	}

	public boolean update() {
		return calculate(getText());
	}

	public Collection<Item> getEnclosedItems() {
		return FrameUtils.getItemsEnclosedBy(this.getParentOrCurrentFrame(),
				this.getEnclosedShape());
	}

	public Collection<Text> getEnclosedNonAnnotationText() {
		Collection<Text> items = new LinkedHashSet<Text>();
		for (Item t : getEnclosedItems()) {
			if (t instanceof Text && !t.isAnnotation())
				items.add((Text) t);
		}

		return items;
	}

	public void dispose() {
		setParent(null);
	}

	/**
	 * @return
	 */
	protected boolean hasVisibleBorder() {
		return getThickness() > 0 && !isLineEnd() && getBorderColor() != null;
	}

	public Frame getChild() {
		String childName = getAbsoluteLink();
		if (childName != null) {
			return FrameIO.LoadFrame(childName);
		}
		return null;
	}

	public boolean hasLink() {
		return _link != null;
	}

	protected void anchorConnectedOLD(AnchorEdgeType anchorEdgeType, Float delta) {
		// Check for a more efficient way to do this!!
		// Invalidate all the items
		for (Item i : this.getAllConnected()) {
			i.invalidateAll();
		}
		// Move the items
		for (Item i : this.getAllConnected()) {
			if (i.isLineEnd()) {
				if (delta != null) {
					if ((anchorEdgeType == AnchorEdgeType.Left) || (anchorEdgeType == AnchorEdgeType.Right)) {
						// 'delta' encodes a horizontal (x) move
						if (anchorEdgeType == AnchorEdgeType.Left) {
							i.setAnchorLeft(null);
						}
						else {
							// must be Right
							i.setAnchorRight(null);
						}
						
						i.setXY(i.getX() + delta, i.getY());
					}
					if ((anchorEdgeType == AnchorEdgeType.Top) || (anchorEdgeType == AnchorEdgeType.Bottom)) {
						// 'delta; encodes a vertical (y) move
						if (anchorEdgeType == AnchorEdgeType.Top) {
							i.setAnchorTop(null);
						}
						else {
							// must be Bottom
							i.setAnchorBottom(null);
						}
						i.setXY(i.getX(), i.getY() + delta);
					}
					
				}
			}
		}
		// Invalidate them again!!
		for (Item i : this.getAllConnected()) {
			i.updatePolygon();
			i.invalidateAll();
		}
	}

	
	protected void anchorConnected(AnchorEdgeType anchorEdgeType, Float delta) {
		
		// Check for a more efficient way to do this!!
		// Invalidate all the items
		for (Item i : this.getAllConnected()) {
			i.invalidateAll();
		}
		
		// Move the items
		for (Item i : this.getAllConnected()) {
			if (i.isLineEnd()) {
				if (delta != null) {
					if ((anchorEdgeType == AnchorEdgeType.Left) || (anchorEdgeType == AnchorEdgeType.Right)) {
						// 'delta' encodes a horizontal (x) move
						if (anchorEdgeType == AnchorEdgeType.Left) {
							// Processing a Left anchor
							// => Anything connected that is *not* anchored to the right should be moved by 'delta'
							if (i.getAnchorRight()==null) {
								i.setXY(i.getX() + delta, i.getY());
							}
						}
						else {
							// Processing a Right anchor
							// => Anything connected that is *not* anchored to the left should be moved by 'delta'
							if (i.getAnchorLeft()==null) {
								i.setXY(i.getX() + delta, i.getY());
							}
						}

					}
					if ((anchorEdgeType == AnchorEdgeType.Top) || (anchorEdgeType == AnchorEdgeType.Bottom)) {
						// 'delta; encodes a vertical (y) move
						if (anchorEdgeType == AnchorEdgeType.Top) {
							// Processing a Top anchor
							// => Anything connected that is *not* anchored to the bottom should be moved by 'delta'
							if (i.getAnchorBottom()==null) {
								i.setXY(i.getX(), i.getY() + delta);
							}
						}
						else {
							// Processing a Bottom anchor
							// => Anything connected that is *not* anchored to the top should be moved by 'delta'
							if (i.getAnchorTop()==null) {
								// must be Bottom
								//i.setAnchorBottom(null);
								i.setXY(i.getX(), i.getY() + delta);
							}
						}
					}		
				}
			}
		}
		
		anchorConstraints();
		
		// Invalidate them again!!
		for (Item i : this.getAllConnected()) {
			i.updatePolygon();
			i.invalidateAll();
		}
	}
	/**
	 * Sets the item to pickup when the user attempts to pick this item up.
	 * EditTarget has a value of 'this' by default but may be set to other
	 * values if this item is on a vector.
	 * 
	 * @param target
	 *            the item to be copied or picked up when the user attempts to
	 *            edit this item.
	 */
	public void setEditTarget(Item target) {
		_editTarget = target;
	}

	/**
	 * Gets the item to pickup when the user attempts to pick this item up.
	 * EditTarget has a value of 'this' by default but may be set to other
	 * values if this item is on a vector.
	 */
	public Item getEditTarget() {
		return _editTarget;
	}

	public void scale(Float scale, int originX, int originY) {
		setXY((getX() - originX) * scale + originX, (getY() - originY) * scale + originY);
		setArrowheadLength(getArrowheadLength() * scale);

		float thickness = getThickness();
		if (thickness > 0)
			setThickness(thickness * scale, false);

		// DONT PUT SIZE IN HERE CAUSE IT STUFFS UP CIRCLES

		updatePolygon();
	}

	protected boolean isVectorItem() {
		return _editTarget != this;
	}

	public AttributeValuePair getAttributeValuePair() {
		if (_attributeValuePair == null) {
			_attributeValuePair = new AttributeValuePair(getText());
		}
		return _attributeValuePair;
	}

	/*
	 * private static Set<Object> _locks = new HashSet<Object>();
	 * 
	 * public static void lock(Object itemToLock) { _locks.add(itemToLock); }
	 * 
	 * public static void unlock(Object itemToUnlock) {
	 * _locks.remove(itemToUnlock); }
	 * 
	 * public static boolean isLocked(Object item) { return
	 * _locks.contains(item); }
	 */

	public void setSave(boolean state) {
		_save = state;
	}

	public boolean getSave() {
		return _save;
	}

	public void setAutoStamp(Float rate) {
		_autoStamp = rate;
	}

	public Float getAutoStamp() {
		return _autoStamp;
	}

	public boolean isAutoStamp() {
		return _autoStamp != null && _autoStamp >= 0.0;
	}

	public void setDotType(DotType type) {
		invalidateAll();
		_type = type;
		invalidateAll();
	}

	public DotType getDotType() {
		return _type;
	}

	public void setFilled(boolean filled) {
		invalidateAll();
		_filled = filled;
		invalidateAll();
	}

	public boolean getFilled() {
		return _filled;
	}
}
