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

package com.alee.laf.window;

import com.alee.laf.rootpane.WRootPaneUI;
import com.alee.laf.rootpane.WebRootPane;
import com.alee.laf.rootpane.WebRootPaneUI;
import com.alee.managers.focus.DefaultFocusTracker;
import com.alee.managers.focus.FocusManager;
import com.alee.managers.language.DictionaryListener;
import com.alee.managers.language.LanguageEventMethods;
import com.alee.managers.language.LanguageListener;
import com.alee.managers.language.WebLanguageManager;
import com.alee.managers.settings.DefaultValue;
import com.alee.managers.settings.SettingsManager;
import com.alee.managers.settings.SettingsMethods;
import com.alee.managers.settings.SettingsProcessor;
import com.alee.managers.style.*;
import com.alee.painter.Paintable;
import com.alee.painter.Painter;
import com.alee.utils.ProprietaryUtils;
import com.alee.utils.SwingUtils;
import com.alee.utils.swing.extensions.ComponentEventRunnable;
import com.alee.utils.swing.extensions.WindowCloseAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.util.List;
import java.util.Map;

/**
 * {@link JWindow} extension class.
 * It contains various useful methods to simplify core component usage.
 * <p/>
 * This component should never be used with a non-Web UIs as it might cause an unexpected behavior.
 * You could still use that component even if WebLaF is not your application L&amp;F as this component will use Web-UI in any case.
 *
 * @author Mikle Garin
 * @see JWindow
 * @see WebRootPaneUI
 * @see com.alee.laf.rootpane.RootPanePainter
 */

public class WebWindow<T extends WebWindow<T>> extends JWindow
        implements Styleable, Paintable, PaddingMethods, WindowEventMethods, LanguageEventMethods, SettingsMethods, WindowMethods<T>
{
    /**
     * Whether should close window on focus loss or not.
     */
    protected boolean closeOnFocusLoss = false;

    /**
     * Window focus tracker.
     */
    protected DefaultFocusTracker focusTracker;

    /**
     * Creates a window with no specified owner. This window will not be focusable.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     */
    public WebWindow ()
    {
        this ( StyleId.auto );
    }

    /**
     * Creates a window with the specified {@code GraphicsConfiguration} of a screen device. This window will not be focusable.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param gc the {@code GraphicsConfiguration} that is used to construct the new window with; if gc is {@code null},
     *           the system default {@code GraphicsConfiguration} is assumed
     */
    public WebWindow ( final GraphicsConfiguration gc )
    {
        this ( StyleId.auto, gc );
    }

    /**
     * Creates a window with the owner window from the specified component.
     * This window will not be focusable unless its owner is showing on the screen.
     * If {@code owner} is {@code null}, the shared owner will be used and this window will not be focusable.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param owner the componnt from which parent window this window is displayed
     */
    public WebWindow ( final Component owner )
    {
        this ( StyleId.auto, owner );
    }

    /**
     * Creates a window with the specified owner window. This window will not be focusable unless its owner is showing on the screen.
     * If {@code owner} is {@code null}, the shared owner will be used and this window will not be focusable.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param owner the window from which the window is displayed
     */
    public WebWindow ( final Window owner )
    {
        this ( StyleId.auto, owner );
    }

    /**
     * Creates a window with the specified owner window and {@code GraphicsConfiguration} of a screen device.
     * If {@code owner} is {@code null}, the shared owner will be used and this window will not be focusable.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param owner the window from which the window is displayed
     * @param gc    the {@code GraphicsConfiguration} that is used to construct the new window with; if gc is {@code null},
     *              the system default {@code GraphicsConfiguration} is assumed, unless {@code owner} is also null, in which
     *              case the {@code GraphicsConfiguration} from the shared owner frame will be used
     */
    public WebWindow ( final Window owner, final GraphicsConfiguration gc )
    {
        this ( StyleId.auto, owner, gc );
    }

    /**
     * Creates a window with the specified owner frame.
     * If {@code owner} is {@code null}, the shared owner will be used and this window will not be focusable.
     * Also, this window will not be focusable unless its owner is showing on the screen.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param owner the frame from which the window is displayed
     */
    public WebWindow ( final Frame owner )
    {
        this ( StyleId.auto, owner );
    }

    /**
     * Creates a window with no specified owner. This window will not be focusable.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param id style ID
     */
    public WebWindow ( final StyleId id )
    {
        this ( id, ( Frame ) null );
    }

    /**
     * Creates a window with the specified {@code GraphicsConfiguration} of a screen device. This window will not be focusable.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param id style ID
     * @param gc the {@code GraphicsConfiguration} that is used to construct the new window with; if gc is {@code null},
     *           the system default {@code GraphicsConfiguration} is assumed
     */
    public WebWindow ( final StyleId id, final GraphicsConfiguration gc )
    {
        this ( id, null, gc );
    }

    /**
     * Creates a window with the owner window from the specified component.
     * This window will not be focusable unless its owner is showing on the screen.
     * If {@code owner} is {@code null}, the shared owner will be used and this window will not be focusable.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param id    style ID
     * @param owner the componnt from which parent window this window is displayed
     */
    public WebWindow ( final StyleId id, final Component owner )
    {
        this ( id, SwingUtils.getWindowAncestor ( owner ) );
    }

    /**
     * Creates a window with the specified owner window. This window will not be focusable unless its owner is showing on the screen.
     * If {@code owner} is {@code null}, the shared owner will be used and this window will not be focusable.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param id    style ID
     * @param owner the window from which the window is displayed
     */
    public WebWindow ( final StyleId id, final Window owner )
    {
        this ( id, owner, owner != null ? owner.getGraphicsConfiguration () : null );
    }

    /**
     * Creates a window with the specified owner window and {@code GraphicsConfiguration} of a screen device.
     * If {@code owner} is {@code null}, the shared owner will be used and this window will not be focusable.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param id    style ID
     * @param owner the window from which the window is displayed
     * @param gc    the {@code GraphicsConfiguration} that is used to construct the new window with; if gc is {@code null},
     *              the system default {@code GraphicsConfiguration} is assumed, unless {@code owner} is also null, in which
     *              case the {@code GraphicsConfiguration} from the shared owner frame will be used
     */
    public WebWindow ( final StyleId id, final Window owner, final GraphicsConfiguration gc )
    {
        super ( owner, gc );
        initialize ( id );
    }

    /**
     * Creates a window with the specified owner frame.
     * If {@code owner} is {@code null}, the shared owner will be used and this window will not be focusable.
     * Also, this window will not be focusable unless its owner is showing on the screen.
     * <p>
     * This constructor sets the component's locale property to the value returned by {@code JComponent.getDefaultLocale}.
     *
     * @param id    style ID
     * @param owner the frame from which the window is displayed
     */
    public WebWindow ( final StyleId id, final Frame owner )
    {
        super ( owner );
        initialize ( id );
    }

    @Override
    protected void windowInit ()
    {
        // Disabling default initialization to optimize startup performance
    }

    /**
     * Additional initializtion of WebWindow settings.
     *
     * @param id initial style ID
     */
    protected void initialize ( final StyleId id )
    {
        // Default window initialization
        setLocale ( JComponent.getDefaultLocale () );
        setRootPane ( createRootPane () );
        setRootPaneCheckingEnabled ( true );
        ProprietaryUtils.checkAndSetPolicy ( this );

        // Updating base settings
        setFocusable ( true );
        setFocusableWindowState ( true );
        SwingUtils.setOrientation ( this );

        // Installing root pane style
        setStyleId ( id );

        // Adding focus tracker for this window
        // It is stored into a separate field to avoid its disposal from memory
        focusTracker = new DefaultFocusTracker ( true )
        {
            @Override
            public boolean isTrackingEnabled ()
            {
                return isShowing () && isCloseOnFocusLoss ();
            }

            @Override
            public void focusChanged ( final boolean focused )
            {
                if ( isCloseOnFocusLoss () && isShowing () && !focused )
                {
                    dispose ();
                }
            }
        };
        FocusManager.addFocusTracker ( this, focusTracker );
    }

    @Override
    protected JRootPane createRootPane ()
    {
        return new WebWindowRootPane ();
    }

    /**
     * Returns whether should close window on focus loss or not.
     *
     * @return true if should close window on focus loss, false otherwise
     */
    public boolean isCloseOnFocusLoss ()
    {
        return closeOnFocusLoss;
    }

    /**
     * Sets whether should close window on focus loss or not.
     *
     * @param closeOnFocusLoss whether should close window on focus loss or not
     */
    public void setCloseOnFocusLoss ( final boolean closeOnFocusLoss )
    {
        this.closeOnFocusLoss = closeOnFocusLoss;
    }

    /**
     * Returns focusable children that don't force window to close even if it set to close on focus loss.
     *
     * @return focusable children that don't force window to close even if it set to close on focus loss
     */
    public List<Component> getFocusableChildren ()
    {
        return focusTracker.getCustomChildren ();
    }

    /**
     * Adds focusable child that won't force window to close even if it set to close on focus loss.
     *
     * @param child focusable child that won't force window to close even if it set to close on focus loss
     */
    public void addFocusableChild ( final Component child )
    {
        focusTracker.addCustomChild ( child );
    }

    /**
     * Removes focusable child that doesn't force window to close even if it set to close on focus loss.
     *
     * @param child focusable child that doesn't force window to close even if it set to close on focus loss
     */
    public void removeFocusableChild ( final Component child )
    {
        focusTracker.removeCustomChild ( child );
    }

    @Override
    public StyleId getDefaultStyleId ()
    {
        return StyleId.window;
    }

    @Override
    public StyleId getStyleId ()
    {
        return StyleManager.getStyleId ( getRootPane () );
    }

    @Override
    public StyleId setStyleId ( final StyleId id )
    {
        return StyleManager.setStyleId ( getRootPane (), id );
    }

    @Override
    public StyleId resetStyleId ()
    {
        return StyleManager.resetStyleId ( getRootPane () );
    }

    @Override
    public Skin getSkin ()
    {
        return StyleManager.getSkin ( getRootPane () );
    }

    @Override
    public Skin setSkin ( final Skin skin )
    {
        return StyleManager.setSkin ( getRootPane (), skin );
    }

    @Override
    public Skin setSkin ( final Skin skin, final boolean recursively )
    {
        return StyleManager.setSkin ( getRootPane (), skin, recursively );
    }

    @Override
    public Skin resetSkin ()
    {
        return StyleManager.resetSkin ( getRootPane () );
    }

    @Override
    public void addStyleListener ( final StyleListener listener )
    {
        StyleManager.addStyleListener ( getRootPane (), listener );
    }

    @Override
    public void removeStyleListener ( final StyleListener listener )
    {
        StyleManager.removeStyleListener ( getRootPane (), listener );
    }

    @Override
    public Map<String, Painter> getCustomPainters ()
    {
        return StyleManager.getCustomPainters ( getRootPane () );
    }

    @Override
    public Painter getCustomPainter ()
    {
        return StyleManager.getCustomPainter ( getRootPane () );
    }

    @Override
    public Painter getCustomPainter ( final String id )
    {
        return StyleManager.getCustomPainter ( getRootPane (), id );
    }

    @Override
    public Painter setCustomPainter ( final Painter painter )
    {
        return StyleManager.setCustomPainter ( getRootPane (), painter );
    }

    @Override
    public Painter setCustomPainter ( final String id, final Painter painter )
    {
        return StyleManager.setCustomPainter ( getRootPane (), id, painter );
    }

    @Override
    public boolean resetPainter ()
    {
        return StyleManager.resetPainter ( getRootPane () );
    }

    @Override
    public Insets getPadding ()
    {
        return PaddingMethodsImpl.getPadding ( getRootPane () );
    }

    @Override
    public void setPadding ( final int padding )
    {
        PaddingMethodsImpl.setPadding ( getRootPane (), padding );
    }

    @Override
    public void setPadding ( final int top, final int left, final int bottom, final int right )
    {
        PaddingMethodsImpl.setPadding ( getRootPane (), top, left, bottom, right );
    }

    @Override
    public void setPadding ( final Insets padding )
    {
        PaddingMethodsImpl.setPadding ( getRootPane (), padding );
    }

    /**
     * Returns the look and feel (L&amp;F) object that renders this component.
     *
     * @return the {@link com.alee.laf.rootpane.WRootPaneUI} object that renders this component
     */
    public WRootPaneUI getUI ()
    {
        return ( WRootPaneUI ) getRootPane ().getUI ();
    }

    /**
     * Sets the L&amp;F object that renders this component.
     *
     * @param ui {@link WRootPaneUI}
     */
    public void setUI ( final WRootPaneUI ui )
    {
        getRootPane ().setUI ( ui );
    }

    @Override
    public WindowAdapter onClosing ( final WindowEventRunnable runnable )
    {
        return WindowEventMethodsImpl.onClosing ( this, runnable );
    }

    @Override
    public WindowCloseAdapter onClose ( final ComponentEventRunnable runnable )
    {
        return WindowEventMethodsImpl.onClose ( this, runnable );
    }

    @Override
    public void addLanguageListener ( final LanguageListener listener )
    {
        WebLanguageManager.addLanguageListener ( getRootPane (), listener );
    }

    @Override
    public void removeLanguageListener ( final LanguageListener listener )
    {
        WebLanguageManager.removeLanguageListener ( getRootPane (), listener );
    }

    @Override
    public void removeLanguageListeners ()
    {
        WebLanguageManager.removeLanguageListeners ( getRootPane () );
    }

    @Override
    public void addDictionaryListener ( final DictionaryListener listener )
    {
        WebLanguageManager.addDictionaryListener ( getRootPane (), listener );
    }

    @Override
    public void removeDictionaryListener ( final DictionaryListener listener )
    {
        WebLanguageManager.removeDictionaryListener ( getRootPane (), listener );
    }

    @Override
    public void removeDictionaryListeners ()
    {
        WebLanguageManager.removeDictionaryListeners ( getRootPane () );
    }

    @Override
    public void registerSettings ( final String key )
    {
        SettingsManager.registerComponent ( getRootPane (), key );
    }

    @Override
    public <V extends DefaultValue> void registerSettings ( final String key, final Class<V> defaultValueClass )
    {
        SettingsManager.registerComponent ( getRootPane (), key, defaultValueClass );
    }

    @Override
    public void registerSettings ( final String key, final Object defaultValue )
    {
        SettingsManager.registerComponent ( getRootPane (), key, defaultValue );
    }

    @Override
    public void registerSettings ( final String group, final String key )
    {
        SettingsManager.registerComponent ( getRootPane (), group, key );
    }

    @Override
    public <V extends DefaultValue> void registerSettings ( final String group, final String key, final Class<V> defaultValueClass )
    {
        SettingsManager.registerComponent ( getRootPane (), group, key, defaultValueClass );
    }

    @Override
    public void registerSettings ( final String group, final String key, final Object defaultValue )
    {
        SettingsManager.registerComponent ( getRootPane (), group, key, defaultValue );
    }

    @Override
    public void registerSettings ( final String key, final boolean loadInitialSettings, final boolean applySettingsChanges )
    {
        SettingsManager.registerComponent ( getRootPane (), key, loadInitialSettings, applySettingsChanges );
    }

    @Override
    public <V extends DefaultValue> void registerSettings ( final String key, final Class<V> defaultValueClass,
                                                            final boolean loadInitialSettings, final boolean applySettingsChanges )
    {
        SettingsManager.registerComponent ( getRootPane (), key, defaultValueClass, loadInitialSettings, applySettingsChanges );
    }

    @Override
    public void registerSettings ( final String key, final Object defaultValue, final boolean loadInitialSettings,
                                   final boolean applySettingsChanges )
    {
        SettingsManager.registerComponent ( getRootPane (), key, defaultValue, loadInitialSettings, applySettingsChanges );
    }

    @Override
    public <V extends DefaultValue> void registerSettings ( final String group, final String key, final Class<V> defaultValueClass,
                                                            final boolean loadInitialSettings, final boolean applySettingsChanges )
    {
        SettingsManager.registerComponent ( getRootPane (), group, key, defaultValueClass, loadInitialSettings, applySettingsChanges );
    }

    @Override
    public void registerSettings ( final String group, final String key, final Object defaultValue, final boolean loadInitialSettings,
                                   final boolean applySettingsChanges )
    {
        SettingsManager.registerComponent ( getRootPane (), group, key, defaultValue, loadInitialSettings, applySettingsChanges );
    }

    @Override
    public void registerSettings ( final SettingsProcessor settingsProcessor )
    {
        SettingsManager.registerComponent ( getRootPane (), settingsProcessor );
    }

    @Override
    public void unregisterSettings ()
    {
        SettingsManager.unregisterComponent ( getRootPane () );
    }

    @Override
    public void loadSettings ()
    {
        SettingsManager.loadComponentSettings ( getRootPane () );
    }

    @Override
    public void saveSettings ()
    {
        SettingsManager.saveComponentSettings ( getRootPane () );
    }

    @Override
    public boolean isWindowOpaque ()
    {
        return WindowMethodsImpl.isWindowOpaque ( this );
    }

    @Override
    public T setWindowOpaque ( final boolean opaque )
    {
        return WindowMethodsImpl.setWindowOpaque ( this, opaque );
    }

    @Override
    public float getWindowOpacity ()
    {
        return WindowMethodsImpl.getWindowOpacity ( this );
    }

    @Override
    public T setWindowOpacity ( final float opacity )
    {
        return WindowMethodsImpl.setWindowOpacity ( this, opacity );
    }

    @Override
    public T center ()
    {
        return WindowMethodsImpl.center ( this );
    }

    @Override
    public T center ( final Component relativeTo )
    {
        return WindowMethodsImpl.center ( this, relativeTo );
    }

    @Override
    public T center ( final int width, final int height )
    {
        return WindowMethodsImpl.center ( this, width, height );
    }

    @Override
    public T center ( final Component relativeTo, final int width, final int height )
    {
        return WindowMethodsImpl.center ( this, relativeTo, width, height );
    }

    @Override
    public T packToWidth ( final int width )
    {
        return WindowMethodsImpl.packToWidth ( this, width );
    }

    @Override
    public T packToHeight ( final int height )
    {
        return WindowMethodsImpl.packToHeight ( this, height );
    }

    /**
     * Custom root pane for this {@link WebWindow}.
     * It is required to provide undecorated root pane style ID to avoid issues with further style updates.
     * It also provides default window style ID instead of default root pane style ID.
     */
    public class WebWindowRootPane extends WebRootPane
    {
        /**
         * Constructs new root pane for this {@link WebWindow}.
         */
        public WebWindowRootPane ()
        {
            super ( StyleId.rootpane );
        }

        @Override
        public StyleId getDefaultStyleId ()
        {
            return WebWindow.this.getDefaultStyleId ();
        }
    }
}