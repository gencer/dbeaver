/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.controls.grid;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public interface GridContentProvider {

	/**
	 * Returns the background color at the given column index in the receiver.
	 *
	 * @param index
	 *            the column index
	 * @return the background color
	 */
	public Color getBackground(int row, int column);

    /**
     * Returns the foreground color at the given column index in the receiver.
     *
     * @param index
     *            the column index
     * @return the foreground color
     */
    public Color getForeground(int row, int column);

	/**
	 * Returns the font that the receiver will use to paint textual information
	 * for the specified cell in this item.
	 *
	 * @param index
	 *            the column index
	 * @return the receiver's font
	 */
	public Font getFont(int row, int column);

	/**
	 * Returns the image stored at the given column index in the receiver, or
	 * null if the image has not been set or if the column does not exist.
	 *
	 * @param index
	 *            the column index
	 * @return the image stored at the given column index in the receiver
	 */
	public Image getImage(int row, int column);

	/**
	 * Returns the text stored at the given column index in the receiver, or
	 * empty string if the text has not been set.
	 *
	 * @param index
	 *            the column index
	 * @return the text stored at the given column index in the receiver
	 */
	public String getText(int row, int column);

    /**
     * Returns the tooltip for the given cell.
     *
     * @param index
     *            the column index
     * @return the tooltip
     */
    public String getToolTipText(int row, int column);

	/**
	 * Returns the receiver's row header text. If the text is <code>null</code>
	 * the row header will display the row number.
	 *
	 * @return the text stored for the row header or code <code>null</code> if
	 *         the default has to be displayed
	 */
	public String getHeaderText(int row);

	/**
	 * Returns the receiver's row header image.
	 *
	 * @return the image stored for the header or <code>null</code> if none has
	 *         to be displayed
	 */
	public Image getHeaderImage(int row);
	/**
	 * Returns the receiver's row header background color
	 *
	 * @return the color or <code>null</code> if none
	 */
	public Color getHeaderBackground(int row);

	/**
	 * Returns the receiver's row header foreground color
	 *
	 * @return the color or <code>null</code> if none
	 */
	public Color getHeaderForeground(int row);

}
