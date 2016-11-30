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

package com.alee.painter;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.style.Bounds;
import com.alee.managers.style.BoundsType;
import com.alee.managers.style.PainterShapeProvider;
import com.alee.managers.style.StyleManager;
import com.alee.managers.style.data.ComponentStyle;
import com.alee.painter.decoration.AbstractDecorationPainter;
import com.alee.painter.decoration.AbstractSectionDecorationPainter;
import com.alee.utils.*;
import com.alee.utils.swing.DataRunnable;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This special class provides basic methods to link painter with components.
 *
 * @author Mikle Garin
 */

public final class PainterSupport
{
    /**
     * Installed painters map.
     */
    private static final Map<JComponent, Map<Painter, PainterListener>> installedPainters =
            new WeakHashMap<JComponent, Map<Painter, PainterListener>> ();

    /**
     * Returns the specified painter if it can be assigned to proper painter type.
     * Otherwise returns newly created adapter painter that wraps the specified painter.
     * Used by component UIs to adapt general-type painters for their specific-type needs.
     *
     * @param painter      processed painter
     * @param properClass  proper painter class
     * @param adapterClass adapter painter class
     * @param <T>          proper painter type
     * @return specified painter if it can be assigned to proper painter type, new painter adapter if it cannot be assigned
     */
    public static <T extends SpecificPainter> T getProperPainter ( final Painter painter, final Class<T> properClass,
                                                                   final Class<? extends T> adapterClass )
    {
        return painter == null ? null : ReflectUtils.isAssignable ( properClass, painter.getClass () ) ? ( T ) painter :
                ( T ) ReflectUtils.createInstanceSafely ( adapterClass, painter );
    }

    /**
     * Returns either the specified painter if it is not an adapted painter or the adapted painter.
     * Used by component UIs to retrieve painters adapted for their specific needs.
     *
     * @param painter painter to process
     * @param <T>     desired painter type
     * @return either the specified painter if it is not an adapted painter or the adapted painter
     */
    public static <T extends Painter> T getAdaptedPainter ( final Painter painter )
    {
        return ( T ) ( painter != null && painter instanceof AdaptivePainter ? ( ( AdaptivePainter ) painter ).getPainter () : painter );
    }

    /**
     * Sets component painter.
     * {@code null} can be provided to uninstall painter.
     *
     * @param component            component painter should be installed into
     * @param setter               runnable that updates actual painter field
     * @param oldPainter           previously installed painter
     * @param painter              painter to install
     * @param specificClass        specific painter class
     * @param specificAdapterClass specific painter adapter class
     * @param <P>                  specific painter class type
     */
    public static <P extends SpecificPainter> void setPainter ( final JComponent component, final DataRunnable<P> setter,
                                                                final P oldPainter, final Painter painter, final Class<P> specificClass,
                                                                final Class<? extends P> specificAdapterClass )
    {
        // Creating adaptive painter if required
        final P properPainter = getProperPainter ( painter, specificClass, specificAdapterClass );

        // Properly updating painter
        uninstallPainter ( component, oldPainter );
        setter.run ( properPainter );
        installPainter ( component, properPainter );

        // Firing painter change event
        SwingUtils.firePropertyChanged ( component, WebLookAndFeel.PAINTER_PROPERTY, oldPainter, properPainter );
    }

    /**
     * Installs painter into the specified component.
     * It is highly recommended to call this method only from EDT.
     *
     * @param component component painter is applied to
     * @param painter   painter to install
     */
    public static void installPainter ( final JComponent component, final Painter painter )
    {
        // Simply ignore this call if empty painter is set or component doesn't exist
        if ( component == null || painter == null )
        {
            return;
        }

        // Installing painter
        Map<Painter, PainterListener> listeners = installedPainters.get ( component );
        if ( listeners == null )
        {
            listeners = new WeakHashMap<Painter, PainterListener> ( 1 );
            installedPainters.put ( component, listeners );
        }
        if ( !installedPainters.containsKey ( painter ) )
        {
            // Installing painter
            painter.install ( component, LafUtils.getUI ( component ) );

            // Applying initial component settings
            final Boolean opaque = painter.isOpaque ();
            if ( opaque != null )
            {
                LookAndFeel.installProperty ( component, WebLookAndFeel.OPAQUE_PROPERTY, opaque ? Boolean.TRUE : Boolean.FALSE );
            }

            // Creating weak references to use them inside the listener
            // Otherwise we will force it to keep strong reference to component and painter if we use them directly
            final WeakReference<JComponent> c = new WeakReference<JComponent> ( component );
            final WeakReference<Painter> p = new WeakReference<Painter> ( painter );

            // Adding painter listener
            final PainterListener listener = new PainterListener ()
            {
                @Override
                public void repaint ()
                {
                    // Forcing component to be repainted
                    c.get ().repaint ();
                }

                @Override
                public void repaint ( final int x, final int y, final int width, final int height )
                {
                    // Forcing component to be repainted
                    c.get ().repaint ( x, y, width, height );
                }

                @Override
                public void revalidate ()
                {
                    // Forcing layout updates
                    c.get ().revalidate ();
                }

                @Override
                public void updateOpacity ()
                {
                    // Updating component opacity according to painter
                    final Painter painter = p.get ();
                    if ( painter != null )
                    {
                        final Boolean opaque = painter.isOpaque ();
                        if ( opaque != null )
                        {
                            c.get ().setOpaque ( opaque );
                        }
                    }
                }
            };
            painter.addPainterListener ( listener );
            listeners.put ( painter, listener );
        }
    }

    /**
     * Uninstalls painter from the specified component.
     * It is highly recommended to call this method only from EDT.
     *
     * @param component component painter is uninstalled from
     * @param painter   painter to uninstall
     */
    public static void uninstallPainter ( final JComponent component, final Painter painter )
    {
        // Simply ignore this call if painter or component doesn't exist
        if ( component == null || painter == null )
        {
            return;
        }

        // Uninstalling painter
        final Map<Painter, PainterListener> listeners = installedPainters.get ( component );
        if ( listeners != null )
        {
            // Uninstalling painter
            painter.uninstall ( component, LafUtils.getUI ( component ) );

            // Removing painter listener
            listeners.remove ( painter );
        }
    }

    /**
     * Installs section painter into the specified component.
     * It is highly recommended to call this method only from EDT.
     *
     * @param origin    origin painter
     * @param painter   section painter to install
     * @param old       previously installed section painter
     * @param component component painter should be installed into
     * @param ui        component UI
     * @param <T>       section painter type
     * @return installed sub-painter
     */
    public static <T extends SectionPainter> T installSectionPainter ( final Painter origin, final T painter, final Painter old,
                                                                       final JComponent component, final ComponentUI ui )
    {
        if ( component != null && ui != null )
        {
            if ( old != null )
            {
                old.uninstall ( component, ui );
                if ( old instanceof AbstractSectionDecorationPainter )
                {
                    ( ( AbstractSectionDecorationPainter ) old ).setOrigin ( null );
                }
            }
            if ( painter != null )
            {
                if ( painter instanceof AbstractSectionDecorationPainter )
                {
                    ( ( AbstractSectionDecorationPainter ) painter ).setOrigin ( origin );
                }
                painter.install ( component, ui );
            }
        }
        return painter;
    }

    /**
     * Uninstalls section painter from the specified component.
     * It is highly recommended to call this method only from EDT.
     *
     * @param painter   section painter to uninstall
     * @param component component painter should be uninstalled from
     * @param ui        component UI
     * @param <T>       section painter type
     * @return {@code null}
     */
    public static <T extends SectionPainter> T uninstallSectionPainter ( final T painter, final JComponent component, final ComponentUI ui )
    {
        if ( component != null && ui != null )
        {
            if ( painter != null )
            {
                painter.uninstall ( component, ui );
                if ( painter instanceof AbstractSectionDecorationPainter )
                {
                    ( ( AbstractSectionDecorationPainter ) painter ).clearOrigin ();
                }
            }
        }
        return null;
    }

    /**
     * Paints {@link com.alee.painter.SectionPainter} at the specified bounds.
     * This method was introduced as one of the measures to fix #401 issue appearing on Linux systems.
     *
     * @param painter   {@link com.alee.painter.SectionPainter}
     * @param g2d       graphics context
     * @param component section component
     * @param ui        section component ui
     * @param bounds    section bounds relative to component coordinates system
     */
    public static void paintSection ( final SectionPainter painter, final Graphics2D g2d, final JComponent component, final ComponentUI ui,
                                      final Rectangle bounds )
    {
        if ( SystemUtils.isUnix () )
        {
            // todo This part of code is only here until #401 issue fix for Unix systems
            // todo The problem with this workaround is that it provides bounds which are only relevant within paint run
            // todo In general we want to have bounds which are relevant related

            // Translating to section coordinates
            g2d.translate ( bounds.x, bounds.y );

            // Clipping area
            final Rectangle section = new Rectangle ( 0, 0, bounds.width, bounds.height );
            final Shape oc = GraphicsUtils.intersectClip ( g2d, section );

            // Creating appropriate bounds for painter
            final Bounds componentBounds = new Bounds ( component, -bounds.x, -bounds.y );
            final Bounds sectionBounds = new Bounds ( componentBounds, section );

            // Painting section
            painter.paint ( g2d, component, ui, sectionBounds );

            // Restoring old clip
            GraphicsUtils.restoreClip ( g2d, oc );

            // Translating back
            g2d.translate ( -bounds.x, -bounds.y );
        }
        else
        {
            // Clipping area
            final Shape oc = GraphicsUtils.intersectClip ( g2d, bounds );

            // Creating appropriate bounds for painter
            final Bounds componentBounds = new Bounds ( component );
            final Bounds sectionBounds = new Bounds ( componentBounds, bounds );

            // Painting section
            painter.paint ( g2d, component, ui, sectionBounds );

            // Restoring old clip
            GraphicsUtils.restoreClip ( g2d, oc );
        }
    }

    /**
     * Force painter to update border of the component it is attached to.
     *
     * @param painter painter to ask for border update
     */
    public static void updateBorder ( final Painter painter )
    {
        if ( painter instanceof AbstractPainter )
        {
            ( ( AbstractPainter ) painter ).updateBorder ();
        }
    }

    /**
     * Returns component shape according to its painter.
     *
     * @param component component painter is applied to
     * @param painter   component painter
     * @return component shape according to its painter
     */
    public static Shape getShape ( final JComponent component, final Painter painter )
    {
        if ( painter != null && painter instanceof PainterShapeProvider )
        {
            return ( ( PainterShapeProvider ) painter ).provideShape ( component, BoundsType.margin.bounds ( component ) );
        }
        else
        {
            return BoundsType.margin.bounds ( component );
        }
    }

    /**
     * Returns component baseline for the specified component size, measured from the top of the component bounds.
     * A return value less than {@code 0} indicates this component does not have a reasonable baseline.
     * This method is primarily meant for {@code java.awt.LayoutManager}s to align components along their baseline.
     *
     * @param component aligned component
     * @param ui        aligned component UI
     * @param painter   aligned component painter
     * @param width     approximate component width
     * @param height    approximate component height
     * @return component baseline within the specified bounds, measured from the top of the bounds
     */
    public static int getBaseline ( final JComponent component, final ComponentUI ui,
                                    final Painter painter, final int width, final int height )
    {
        // Default baseline
        int baseline = -1;

        // Painter baseline support
        if ( painter != null )
        {
            // Creating appropriate bounds for painter
            final Bounds componentBounds = new Bounds ( new Dimension ( width, height ) );

            // Retrieving baseline provided by painter
            baseline = painter.getBaseline ( component, ui, componentBounds );
        }

        // Border baseline support
        // Taken from JPanel baseline implementation
        if ( baseline == -1 )
        {
            final Border border = component.getBorder ();
            if ( border instanceof AbstractBorder )
            {
                baseline = ( ( AbstractBorder ) border ).getBaseline ( component, width, height );
            }
        }

        return baseline;
    }

    /**
     * Returns enum indicating how the baseline of the component changes as the size changes.
     *
     * @param component aligned component
     * @param ui        aligned component UI
     * @param painter   aligned component painter
     * @return enum indicating how the baseline of the component changes as the size changes
     */
    public static Component.BaselineResizeBehavior getBaselineResizeBehavior ( final JComponent component, final ComponentUI ui,
                                                                               final Painter painter )
    {
        // Default behavior
        Component.BaselineResizeBehavior behavior = Component.BaselineResizeBehavior.OTHER;

        // Painter baseline behavior support
        if ( painter != null )
        {
            // Retrieving baseline behavior provided by painter
            return painter.getBaselineResizeBehavior ( component, ui );
        }

        // Border baseline behavior support
        // Taken from JPanel baseline implementation
        if ( behavior == Component.BaselineResizeBehavior.OTHER )
        {
            final Border border = component.getBorder ();
            if ( border instanceof AbstractBorder )
            {
                behavior = ( ( AbstractBorder ) border ).getBaselineResizeBehavior ( component );
            }
        }

        return behavior;
    }

    /**
     * Returns component preferred size or {@code null} if there is no preferred size.
     *
     * @param component component painter is applied to
     * @param painter   component painter
     * @return component preferred size or {@code null} if there is no preferred size
     */
    public static Dimension getPreferredSize ( final JComponent component, final Painter painter )
    {
        return getPreferredSize ( component, null, painter );
    }

    /**
     * Returns component preferred size or {@code null} if there is no preferred size.
     * todo Probably get rid of this method and force painters to determine full preferred size?
     *
     * @param component component painter is applied to
     * @param preferred component preferred size
     * @param painter   component painter
     * @return component preferred size or {@code null} if there is no preferred size
     */
    public static Dimension getPreferredSize ( final JComponent component, final Dimension preferred, final Painter painter )
    {
        return getPreferredSize ( component, preferred, painter, false );
    }

    /**
     * Returns component preferred size or {@code null} if there is no preferred size.
     *
     * @param component        component painter is applied to
     * @param preferred        component preferred size
     * @param painter          component painter
     * @param ignoreLayoutSize whether or not layout preferred size should be ignored
     * @return component preferred size or {@code null} if there is no preferred size
     */
    public static Dimension getPreferredSize ( final JComponent component, final Dimension preferred, final Painter painter,
                                               final boolean ignoreLayoutSize )
    {
        // Painter's preferred size
        Dimension ps = SwingUtils.max ( preferred, painter != null ? painter.getPreferredSize () : null );

        // Layout preferred size
        if ( !ignoreLayoutSize )
        {
            synchronized ( component.getTreeLock () )
            {
                final LayoutManager layout = component.getLayout ();
                if ( layout != null )
                {
                    ps = SwingUtils.max ( ps, layout.preferredLayoutSize ( component ) );
                }
            }
        }

        return ps;
    }

    /**
     * Returns whether or not component uses decoratable painter.
     *
     * @param component component to process
     * @return true if component uses decoratable painter, false otherwise
     */
    public static boolean isDecoratable ( final Component component )
    {
        if ( component instanceof JComponent )
        {
            final JComponent jComponent = ( JComponent ) component;
            final ComponentStyle style = StyleManager.getSkin ( jComponent ).getStyle ( jComponent );
            final Painter painter = style != null ? style.getPainter ( jComponent ) : null;
            return painter != null && painter instanceof AbstractDecorationPainter;
        }
        else
        {
            return false;
        }
    }
}