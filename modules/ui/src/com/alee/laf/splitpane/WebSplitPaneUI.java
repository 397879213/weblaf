/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alee.laf.splitpane;

import com.alee.api.jdk.Consumer;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebButton;
import com.alee.managers.icon.Icons;
import com.alee.managers.style.*;
import com.alee.painter.DefaultPainter;
import com.alee.painter.Painter;
import com.alee.painter.PainterSupport;
import com.alee.utils.ColorUtils;
import com.alee.utils.GraphicsUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.awt.*;
import java.beans.PropertyChangeEvent;

/**
 * Custom UI for {@link JSplitPane} component.
 *
 * @author Mikle Garin
 * @author Alexandr Zernov
 */

public class WebSplitPaneUI extends WSplitPaneUI implements ShapeSupport, MarginSupport, PaddingSupport
{
    /**
     * Component painter.
     */
    @DefaultPainter ( SplitPanePainter.class )
    protected ISplitPanePainter painter;

    /**
     * Returns an instance of the {@link WebSplitPaneUI} for the specified component.
     * This tricky method is used by {@link UIManager} to create component UIs when needed.
     *
     * @param c component that will use UI instance
     * @return instance of the {@link WebSplitPaneUI}
     */
    @SuppressWarnings ( "UnusedParameters" )
    public static ComponentUI createUI ( final JComponent c )
    {
        return new WebSplitPaneUI ();
    }

    @Override
    public void installUI ( final JComponent c )
    {
        super.installUI ( c );

        // Applying skin
        StyleManager.installSkin ( splitPane );

        // Default settings
        // todo Remove this after proper painters implementation
        LookAndFeel.installProperty ( splitPane, WebLookAndFeel.OPAQUE_PROPERTY, Boolean.FALSE );
        splitPane.setDividerSize ( 6 );
    }

    @Override
    public void uninstallUI ( final JComponent c )
    {
        // Uninstalling applied skin
        StyleManager.uninstallSkin ( splitPane );

        super.uninstallUI ( c );
    }

    @Override
    public Shape getShape ()
    {
        return PainterSupport.getShape ( splitPane, painter );
    }

    @Override
    public Insets getMargin ()
    {
        return PainterSupport.getMargin ( splitPane );
    }

    @Override
    public void setMargin ( final Insets margin )
    {
        PainterSupport.setMargin ( splitPane, margin );
    }

    @Override
    public Insets getPadding ()
    {
        return PainterSupport.getPadding ( splitPane );
    }

    @Override
    public void setPadding ( final Insets padding )
    {
        PainterSupport.setPadding ( splitPane, padding );
    }

    /**
     * Returns split pane painter.
     *
     * @return split pane painter
     */
    public Painter getPainter ()
    {
        return PainterSupport.getPainter ( painter );
    }

    /**
     * Sets split pane painter.
     * Pass null to remove split pane painter.
     *
     * @param painter new split pane painter
     */
    public void setPainter ( final Painter painter )
    {
        PainterSupport.setPainter ( splitPane, new Consumer<ISplitPanePainter> ()
        {
            @Override
            public void accept ( final ISplitPanePainter newPainter )
            {
                WebSplitPaneUI.this.painter = newPainter;
            }
        }, this.painter, painter, ISplitPanePainter.class, AdaptiveSplitPanePainter.class );
    }

    @Override
    public BasicSplitPaneDivider createDefaultDivider ()
    {
        return new BasicSplitPaneDivider ( this )
        {
            protected final Border border = BorderFactory.createEmptyBorder ( 0, 0, 0, 0 );
            protected final Color color = new Color ( 158, 158, 158 );
            protected final Color[] gradient = new Color[]{ ColorUtils.transparent (), color, color, ColorUtils.transparent () };

            @Override
            public Border getBorder ()
            {
                return border;
            }

            @Override
            protected JButton createLeftOneTouchButton ()
            {
                final boolean hor = orientation == JSplitPane.HORIZONTAL_SPLIT;
                final Icon icon = getOneTouchIcon ( true, hor );
                final WebButton iconWebButton = new WebButton ( StyleId.splitpaneOneTouchLeftButton.at ( splitPane ), icon );
                iconWebButton.setCursor ( Cursor.getDefaultCursor () );
                iconWebButton.setPreferredSize ( getOneTouchButtonSize ( hor ) );
                return iconWebButton;
            }

            @Override
            protected JButton createRightOneTouchButton ()
            {
                final boolean hor = orientation == JSplitPane.HORIZONTAL_SPLIT;
                final Icon icon = getOneTouchIcon ( false, hor );
                final WebButton iconWebButton = new WebButton ( StyleId.splitpaneOneTouchRightButton.at ( splitPane ), icon );
                iconWebButton.setCursor ( Cursor.getDefaultCursor () );
                iconWebButton.setPreferredSize ( getOneTouchButtonSize ( hor ) );
                return iconWebButton;
            }

            /**
             * todo Replace with paintComponent?
             */
            @Override
            public void paint ( final Graphics g )
            {
                final Graphics2D g2d = ( Graphics2D ) g;
                final Object aa = GraphicsUtils.setupAntialias ( g2d );
                final boolean drawDividerBorder = false;

                if ( orientation == JSplitPane.HORIZONTAL_SPLIT )
                {
                    final int startY = getHeight () / 2 - 35;
                    final int endY = getHeight () / 2 + 35;
                    g2d.setPaint ( new LinearGradientPaint ( 0, startY, 0, endY, new float[]{ 0f, 0.25f, 0.75f, 1f }, gradient ) );
                    for ( int i = startY; i < endY; i += 5 )
                    {
                        g2d.fillRect ( getWidth () / 2 - 1, i - 1, 2, 2 );
                    }

                    if ( drawDividerBorder )
                    {
                        g2d.setPaint ( Color.GRAY );
                        g2d.drawLine ( 0, 0, 0, getHeight () - 1 );
                        g2d.drawLine ( getWidth () - 1, 0, getWidth () - 1, getHeight () - 1 );
                    }
                }
                else
                {
                    final int startX = getWidth () / 2 - 35;
                    final int endX = getWidth () / 2 + 35;
                    g2d.setPaint ( new LinearGradientPaint ( startX, 0, endX, 0, new float[]{ 0f, 0.25f, 0.75f, 1f }, gradient ) );
                    for ( int i = startX; i < endX; i += 5 )
                    {
                        g2d.fillRect ( i - 1, getHeight () / 2 - 1, 2, 2 );
                    }

                    if ( drawDividerBorder )
                    {
                        g2d.setPaint ( Color.GRAY );
                        g2d.drawLine ( 0, 0, getWidth () - 1, 0 );
                        g2d.drawLine ( 0, getHeight () - 1, getWidth () - 1, getHeight () - 1 );
                    }
                }

                super.paint ( g );

                GraphicsUtils.restoreAntialias ( g2d, aa );
            }

            /**
             * Property change event, presumably from the JSplitPane, will message
             * updateOrientation if necessary.
             */
            @Override
            public void propertyChange ( final PropertyChangeEvent e )
            {
                super.propertyChange ( e );

                // Listening to split orientation changes
                if ( e.getSource () == splitPane && e.getPropertyName ().equals ( JSplitPane.ORIENTATION_PROPERTY ) )
                {
                    // Updating one-touch-button icons according to new orientation
                    final boolean hor = orientation == JSplitPane.HORIZONTAL_SPLIT;
                    if ( leftButton != null )
                    {
                        leftButton.setIcon ( getOneTouchIcon ( true, hor ) );
                        leftButton.setPreferredSize ( getOneTouchButtonSize ( hor ) );
                    }
                    if ( rightButton != null )
                    {
                        rightButton.setIcon ( getOneTouchIcon ( false, hor ) );
                        rightButton.setPreferredSize ( getOneTouchButtonSize ( hor ) );
                    }
                }
            }
        };
    }

    /**
     * Returns cached one-touch-button icon.
     *
     * @param leading    whether it should be leading button icon or not
     * @param horizontal whether split is horizontal or not
     * @return cached one-touch-button icon
     */
    protected Icon getOneTouchIcon ( final boolean leading, final boolean horizontal )
    {
        return horizontal ? leading ? Icons.left : Icons.right : leading ? Icons.up : Icons.down;
    }

    /**
     * Returns one-touch-button size.
     *
     * @param horizontal whether split is horizontal or not
     * @return one-touch-button size
     */
    protected Dimension getOneTouchButtonSize ( final boolean horizontal )
    {
        return new Dimension ( horizontal ? 6 : 7, horizontal ? 7 : 6 );
    }

    @Override
    protected Component createDefaultNonContinuousLayoutDivider ()
    {
        return new Canvas ()
        {
            @Override
            public void paint ( final Graphics g )
            {
                if ( !isContinuousLayout () && getLastDragLocation () != -1 )
                {
                    final Dimension size = splitPane.getSize ();
                    g.setColor ( Color.LIGHT_GRAY );
                    if ( getOrientation () == JSplitPane.HORIZONTAL_SPLIT )
                    {
                        g.fillRect ( 0, 0, dividerSize - 1, size.height - 1 );
                    }
                    else
                    {
                        g.fillRect ( 0, 0, size.width - 1, dividerSize - 1 );
                    }
                }
            }
        };
    }

    @Override
    public void finishedPaintingChildren ( final JSplitPane jc, final Graphics g )
    {
        if ( jc == splitPane && getLastDragLocation () != -1 && !isContinuousLayout () &&
                !draggingHW )
        {
            final Dimension size = splitPane.getSize ();

            g.setColor ( Color.LIGHT_GRAY );
            if ( getOrientation () == JSplitPane.HORIZONTAL_SPLIT )
            {
                g.fillRect ( getLastDragLocation (), 0, dividerSize - 1, size.height - 1 );
            }
            else
            {
                g.fillRect ( 0, getLastDragLocation (), size.width - 1, dividerSize - 1 );
            }
        }
    }

    @Override
    public int getBaseline ( final JComponent c, final int width, final int height )
    {
        return PainterSupport.getBaseline ( c, this, painter, width, height );
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior ( final JComponent c )
    {
        return PainterSupport.getBaselineResizeBehavior ( c, this, painter );
    }

    @Override
    public void paint ( final Graphics g, final JComponent c )
    {
        if ( painter != null )
        {
            // Call superclass to set internal flags
            // It doesn't paint anything so we don't need to worry about that
            super.paint ( g, c );

            // Painting split pane
            painter.paint ( ( Graphics2D ) g, c, this, new Bounds ( c ) );
        }
    }

    @Override
    public Dimension getPreferredSize ( final JComponent c )
    {
        return PainterSupport.getPreferredSize ( c, painter );
    }
}