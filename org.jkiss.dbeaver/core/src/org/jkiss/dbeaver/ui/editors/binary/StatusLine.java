/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */
package org.jkiss.dbeaver.ui.editors.binary;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Status line component of the editor. Displays the current position and the insert/overwrite status.
 */
public class StatusLine extends Composite {


    static final String textInsert = "Insert";
    static final String textOverwrite = "Overwrite";

    private Label position = null;
    private Label value = null;
    private Label insertMode = null;


    /**
     * Create a status line part
     *
     * @param parent            parent in the widget hierarchy
     * @param style             not used
     * @param withLeftSeparator so it can be put besides other status items (for plugin)
     */
    public StatusLine(Composite parent, int style, boolean withLeftSeparator)
    {
        super(parent, style);
        initialize(withLeftSeparator);
    }


    private void initialize(boolean withSeparator)
    {
        GridLayout statusLayout = new GridLayout();
        statusLayout.numColumns = withSeparator ? 6 : 5;
        statusLayout.marginHeight = 0;
        setLayout(statusLayout);

        if (withSeparator) {
            GridData separator1GridData = new GridData();
            separator1GridData.grabExcessVerticalSpace = true;
            separator1GridData.verticalAlignment = SWT.FILL;
            Label separator1 = new Label(this, SWT.SEPARATOR);
            separator1.setLayoutData(separator1GridData);
        }

        GC gc = new GC(this);
        FontMetrics fontMetrics = gc.getFontMetrics();

        position = new Label(this, SWT.SHADOW_NONE);
        GridData gridData1 = new GridData(/*SWT.DEFAULT*/
                                          (11 + 10 + 12 + 3 + 10 + 12) * fontMetrics.getAverageCharWidth(),
                                          SWT.DEFAULT);
        position.setLayoutData(gridData1);

        GridData separator23GridData = new GridData();
        separator23GridData.grabExcessVerticalSpace = true;
        separator23GridData.verticalAlignment = SWT.FILL;
        Label separator2 = new Label(this, SWT.SEPARATOR);
        separator2.setLayoutData(separator23GridData);

        value = new Label(this, SWT.SHADOW_NONE);
        GridData gridData2 = new GridData(/*SWT.DEFAULT*/
                                          (7 + 3 + 9 + 2 + 9 + 8 + 6) * fontMetrics.getAverageCharWidth(), SWT.DEFAULT);
        value.setLayoutData(gridData2);

        // From Eclipse 3.1's GridData javadoc:
        // NOTE: Do not reuse GridData objects. Every control in a Composite that is managed by a
        // GridLayout must have a unique GridData
        GridData separator3GridData = new GridData();
        separator3GridData.grabExcessVerticalSpace = true;
        separator3GridData.verticalAlignment = SWT.FILL;
        Label separator3 = new Label(this, SWT.SEPARATOR);
        separator3.setLayoutData(separator3GridData);

        insertMode = new Label(this, SWT.SHADOW_NONE);
        GridData gridData3 = new GridData(/*SWT.DEFAULT*/
                                          (textOverwrite.length() + 2) * fontMetrics.getAverageCharWidth(),
                                          SWT.DEFAULT);
        insertMode.setLayoutData(gridData3);
        gc.dispose();
    }


    /**
     * Update the insert mode status. Can be "Insert" or "Overwrite"
     *
     * @param insert true will display "Insert"
     */
    public void updateInsertModeText(boolean insert)
    {
        if (isDisposed() || insertMode.isDisposed()) return;

        insertMode.setText(insert ? textInsert : textOverwrite);
    }

    /**
     * Update the position status and value.
     */
    public void updatePositionValueText(long pos, byte val)
    {
        updatePositionText(pos);
        updateValueText(val);
    }

    /**
     * Update the selection status and value.
     */
    public void updateSelectionValueText(long[] sel, byte val)
    {
        updateSelectionText(sel);
        updateValueText(val);
    }

    /**
     * Update the position status. Displays its decimal and hex value.
     */
    public void updatePositionText(long pos)
    {
        if (isDisposed() || position.isDisposed()) return;

        String posText = "Offset: " + pos + " (dec) = " + Long.toHexString(pos) + " (binary)";
//	String posText = String.format("Offset: %1$d (dec) = %1$X (binary)", pos);
        position.setText(posText);
        //position.pack(true);
    }

    /**
     * Update the value. Displays its decimal, hex and binary value
     *
     * @param val value to display
     */
    public void updateValueText(byte val)
    {
        if (isDisposed() || position.isDisposed()) return;

        String valBinText = "0000000" + Long.toBinaryString(val);
        String valText = "Value: " + val + " (dec) = " + Integer.toHexString(0x0ff & val) + " (binary) = " +
            valBinText.substring(valBinText.length() - 8) + " (bin)";
//	String valText = String.format("Value: %1$d (dec) = %1$X (binary) = %2$s (bin)", val, valBinText.substring(valBinText.length()-8));
        value.setText(valText);
        //value.pack(true);
    }

    /**
     * Update the selection status. Displays its decimal and hex values for start and end selection
     *
     * @param sel selection array to display: [0] = start, [1] = end
     */
    public void updateSelectionText(long[] sel)
    {
        if (isDisposed() || position.isDisposed()) return;

        String selText = "Selection: " + sel[0] + " (0x" + Long.toHexString(sel[0]) + ") - " + sel[1] +
            " (0x" + Long.toHexString(sel[1]) + ")";
//	String selText = String.format("Selection: %1$d (0x%1$X) - %2$d (0x%2$X)", sel[0], sel[1]);
        position.setText(selText);
        //position.pack(true);
    }

}
