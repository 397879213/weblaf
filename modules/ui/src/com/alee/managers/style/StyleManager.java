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

package com.alee.managers.style;

import com.alee.extended.canvas.Gripper;
import com.alee.extended.checkbox.MixedIcon;
import com.alee.extended.label.AbstractStyledTextContent;
import com.alee.extended.label.HotkeyLabelBackground;
import com.alee.extended.label.StyledLabelText;
import com.alee.extended.panel.SelectablePanelPainter;
import com.alee.extended.statusbar.MemoryBarBackground;
import com.alee.extended.syntax.SyntaxPanelPainter;
import com.alee.extended.window.PopOverPainter;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.*;
import com.alee.laf.checkbox.CheckIcon;
import com.alee.laf.label.LabelIcon;
import com.alee.laf.label.LabelLayout;
import com.alee.laf.label.LabelText;
import com.alee.laf.menu.AcceleratorText;
import com.alee.laf.menu.MenuItemLayout;
import com.alee.laf.progressbar.ProgressBarText;
import com.alee.laf.radiobutton.RadioIcon;
import com.alee.laf.scroll.layout.WebScrollPaneLayout;
import com.alee.laf.separator.SeparatorStripes;
import com.alee.laf.tooltip.StyledToolTipText;
import com.alee.laf.tooltip.ToolTipText;
import com.alee.managers.style.data.ComponentStyle;
import com.alee.managers.style.data.SkinInfo;
import com.alee.painter.Painter;
import com.alee.painter.common.TextureType;
import com.alee.painter.decoration.AbstractDecoration;
import com.alee.painter.decoration.Decorations;
import com.alee.painter.decoration.NinePatchDecoration;
import com.alee.painter.decoration.WebDecoration;
import com.alee.painter.decoration.background.*;
import com.alee.painter.decoration.border.AbstractBorder;
import com.alee.painter.decoration.border.LineBorder;
import com.alee.painter.decoration.content.*;
import com.alee.painter.decoration.layout.AbstractContentLayout;
import com.alee.painter.decoration.layout.BorderLayout;
import com.alee.painter.decoration.layout.IconTextLayout;
import com.alee.painter.decoration.shadow.AbstractShadow;
import com.alee.painter.decoration.shadow.ExpandingShadow;
import com.alee.painter.decoration.shadow.WebShadow;
import com.alee.painter.decoration.shape.ArrowShape;
import com.alee.painter.decoration.shape.BoundsShape;
import com.alee.painter.decoration.shape.EllipseShape;
import com.alee.painter.decoration.shape.WebShape;
import com.alee.skin.web.WebSkin;
import com.alee.utils.*;
import com.alee.utils.ninepatch.NinePatchIcon;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.util.*;

/**
 * This manager handles all default Swing and custom WebLaF component styles.
 * It provides API to manage global {@link Skin}s in runtime, customize
 *
 * @author Mikle Garin
 * @see <a href="https://github.com/mgarin/weblaf/wiki/How-to-use-StyleManager">How to use StyleManager</a>
 * @see com.alee.managers.style.Skin
 * @see com.alee.managers.style.data.SkinInfo
 * @see com.alee.managers.style.StyleId
 * @see com.alee.managers.style.StyleData
 */

public final class StyleManager
{
    /**
     * List of listeners for various style events.
     */
    private static final EventListenerList listenerList = new EventListenerList ();

    /**
     * Various component style related data which includes:
     *
     * 1. Skins applied for each specific skinnable component
     * Used to determine skinnable components, update them properly and detect their current skin.
     *
     * 2. Style IDs set for each specific component
     * They are all collected and stored in StyleManager to determine their changes correctly.
     *
     * 3. Style children each styled component has
     * Those children are generally collected here for convenient changes tracking.
     */
    private static final Map<JComponent, StyleData> styleData = new WeakHashMap<JComponent, StyleData> ();

    /**
     * Installed skin extensions.
     * They provide additional styles for active skin.
     * Whether or not each extension will be applied to currently active skin is decided based on the extension description.
     *
     * Note that there are a few important limitations:
     * 1. Extensions can be attached to skins only, not to other extensions
     * 2. You cannot put any style overrides into extensions
     */
    private static final List<SkinExtension> extensions = new ArrayList<SkinExtension> ();

    /**
     * Default WebLaF skin class.
     * Class of the skin used by default when no other skins provided.
     * This skin can be set before WebLaF initialization to avoid unnecessary UI updates afterwards.
     *
     * Every skin set as default must have an empty constructor that properly initializes that skin.
     * Otherwise you have to set that skin manually through one of the methods in this manager.
     */
    private static Class<? extends Skin> defaultSkinClass = null;

    /**
     * Currently used skin.
     * This skin is applied to all newly created components styled by WebLaF except customized ones.
     */
    private static Skin currentSkin = null;

    /**
     * Whether strict style checks are enabled or not.
     * In case strict checks are enabled any incorrect properties or painters getter and setter calls will cause exceptions.
     * These exceptions will not cause UI to halt but they will properly inform about missing styles, incorrect settings etc.
     * It is highly recommended to keep this property enabled to see and fix all problems right away.
     */
    private static boolean strictStyleChecks = true;

    /**
     * Whether {@link StyleManager} is initialized or not.
     */
    private static boolean initialized = false;

    /**
     * Initializes {@link StyleManager} settings.
     */
    public static synchronized void initialize ()
    {
        if ( !initialized )
        {
            // Class aliases
            XmlUtils.processAnnotations ( SkinInfo.class );
            XmlUtils.processAnnotations ( ComponentStyle.class );
            XmlUtils.processAnnotations ( NinePatchIcon.class );
            XmlUtils.processAnnotations ( AbstractDecoration.class );
            XmlUtils.processAnnotations ( Decorations.class );
            XmlUtils.processAnnotations ( WebDecoration.class );
            XmlUtils.processAnnotations ( NinePatchDecoration.class );
            XmlUtils.processAnnotations ( AbstractShadow.class );
            XmlUtils.processAnnotations ( BoundsShape.class );
            XmlUtils.processAnnotations ( WebShape.class );
            XmlUtils.processAnnotations ( EllipseShape.class );
            XmlUtils.processAnnotations ( ArrowShape.class );
            XmlUtils.processAnnotations ( WebShadow.class );
            XmlUtils.processAnnotations ( ExpandingShadow.class );
            XmlUtils.processAnnotations ( AbstractBorder.class );
            XmlUtils.processAnnotations ( LineBorder.class );
            XmlUtils.processAnnotations ( AbstractBackground.class );
            XmlUtils.processAnnotations ( ColorBackground.class );
            XmlUtils.processAnnotations ( GradientBackground.class );
            XmlUtils.processAnnotations ( PresetTextureBackground.class );
            XmlUtils.processAnnotations ( AlphaLayerBackground.class );
            XmlUtils.processAnnotations ( MovingHighlightBackground.class );
            XmlUtils.processAnnotations ( TextureType.class );
            XmlUtils.processAnnotations ( GradientType.class );
            XmlUtils.processAnnotations ( GradientColor.class );
            XmlUtils.processAnnotations ( Stripes.class );
            XmlUtils.processAnnotations ( Stripe.class );
            XmlUtils.processAnnotations ( Gripper.class );
            XmlUtils.processAnnotations ( BorderLayout.class );
            XmlUtils.processAnnotations ( AbstractContentLayout.class );
            XmlUtils.processAnnotations ( IconTextLayout.class );
            XmlUtils.processAnnotations ( AbstractContent.class );
            XmlUtils.processAnnotations ( CheckIcon.class );
            XmlUtils.processAnnotations ( RadioIcon.class );
            XmlUtils.processAnnotations ( MixedIcon.class );
            XmlUtils.processAnnotations ( SeparatorStripes.class );
            XmlUtils.processAnnotations ( RoundRectangle.class );
            XmlUtils.processAnnotations ( DashFocus.class );
            XmlUtils.processAnnotations ( AbstractIconContent.class );
            XmlUtils.processAnnotations ( AbstractTextContent.class );
            XmlUtils.processAnnotations ( AbstractStyledTextContent.class );
            XmlUtils.processAnnotations ( ToolTipText.class );
            XmlUtils.processAnnotations ( StyledToolTipText.class );
            XmlUtils.processAnnotations ( ButtonLayout.class );
            XmlUtils.processAnnotations ( ButtonIcon.class );
            XmlUtils.processAnnotations ( ButtonText.class );
            XmlUtils.processAnnotations ( SimpleButtonIcon.class );
            XmlUtils.processAnnotations ( StyledButtonText.class );
            XmlUtils.processAnnotations ( LabelLayout.class );
            XmlUtils.processAnnotations ( LabelIcon.class );
            XmlUtils.processAnnotations ( LabelText.class );
            XmlUtils.processAnnotations ( StyledLabelText.class );
            XmlUtils.processAnnotations ( MenuItemLayout.class );
            XmlUtils.processAnnotations ( AcceleratorText.class );
            XmlUtils.processAnnotations ( ProgressBarText.class );
            XmlUtils.processAnnotations ( HotkeyLabelBackground.class );
            XmlUtils.processAnnotations ( MemoryBarBackground.class );

            // Painter aliases
            XmlUtils.processAnnotations ( PopOverPainter.class );
            XmlUtils.processAnnotations ( SelectablePanelPainter.class );
            XmlUtils.processAnnotations ( SyntaxPanelPainter.class );

            // Layout aliases
            XmlUtils.processAnnotations ( WebScrollPaneLayout.class );
            XmlUtils.processAnnotations ( WebScrollPaneLayout.UIResource.class );

            // Workaround for ScrollPaneLayout due to neccessity of its usage
            XmlUtils.omitField ( ScrollPaneLayout.class, "viewport" );
            XmlUtils.omitField ( ScrollPaneLayout.class, "vsb" );
            XmlUtils.omitField ( ScrollPaneLayout.class, "hsb" );
            XmlUtils.omitField ( ScrollPaneLayout.class, "rowHead" );
            XmlUtils.omitField ( ScrollPaneLayout.class, "colHead" );
            XmlUtils.omitField ( ScrollPaneLayout.class, "lowerLeft" );
            XmlUtils.omitField ( ScrollPaneLayout.class, "lowerRight" );
            XmlUtils.omitField ( ScrollPaneLayout.class, "upperLeft" );
            XmlUtils.omitField ( ScrollPaneLayout.class, "upperRight" );
            XmlUtils.omitField ( ScrollPaneLayout.class, "vsbPolicy" );
            XmlUtils.omitField ( ScrollPaneLayout.class, "hsbPolicy" );

            // Updating initialization mark
            initialized = true;

            // Applying default skin as current skin
            setSkin ( getDefaultSkin () );
        }
    }

    /**
     * Throws runtime exception if manager was not initialized yet.
     */
    private static void checkInitialization ()
    {
        if ( !initialized )
        {
            throw new StyleException ( "StyleManager must be initialized" );
        }
    }

    /**
     * Returns whether strict style checks are enabled or not.
     *
     * @return true if strict style checks are enabled, false otherwise
     */
    public static boolean isStrictStyleChecks ()
    {
        return strictStyleChecks;
    }

    /**
     * Sets whether strict style checks are enabled or not.
     *
     * @param strict whether strict style checks are enabled or not
     */
    public static void setStrictStyleChecks ( final boolean strict )
    {
        StyleManager.strictStyleChecks = strict;
    }

    /**
     * Returns default skin.
     *
     * @return default skin
     */
    public static Class<? extends Skin> getDefaultSkin ()
    {
        return defaultSkinClass != null ? defaultSkinClass : WebSkin.class;
    }

    /**
     * Sets default skin.
     *
     * @param skinClassName default skin class name
     * @return previous default skin
     */
    public static Class<? extends Skin> setDefaultSkin ( final String skinClassName )
    {
        final Class<? extends Skin> skinClass = ReflectUtils.getClassSafely ( skinClassName );
        return setDefaultSkin ( skinClass );
    }

    /**
     * Sets default skin.
     *
     * @param skinClass default skin class
     * @return previous default skin
     */
    public static Class<? extends Skin> setDefaultSkin ( final Class<? extends Skin> skinClass )
    {
        final Class<? extends Skin> oldSkin = StyleManager.defaultSkinClass;
        StyleManager.defaultSkinClass = skinClass;
        return oldSkin;
    }

    /**
     * Returns currently applied skin.
     *
     * @return currently applied skin
     */
    public static Skin getSkin ()
    {
        return currentSkin;
    }

    /**
     * Applies specified skin to all existing skinnable components.
     * This skin will also be applied to all skinnable components created afterwards.
     *
     * @param skinClassName class name of the skin to be applied
     * @return previously applied skin
     */
    public static Skin setSkin ( final String skinClassName )
    {
        return setSkin ( ReflectUtils.getClassSafely ( skinClassName ) );
    }

    /**
     * Applies specified skin to all existing skinnable components.
     * This skin will also be applied to all skinnable components created afterwards.
     *
     * @param skinClass class of the skin to be applied
     * @return previously applied skin
     */
    public static Skin setSkin ( final Class skinClass )
    {
        return setSkin ( createSkin ( skinClass ) );
    }

    /**
     * Applies specified skin to all existing skinnable components.
     * This skin will also be applied to all skinnable components created afterwards.
     *
     * @param skin skin to be applied
     * @return previously applied skin
     */
    public static synchronized Skin setSkin ( final Skin skin )
    {
        // Event Dispatch Thread check
        WebLookAndFeel.checkEventDispatchThread ();

        // Checking manager initialization
        checkInitialization ();

        // Checking skin support
        checkSupport ( skin );

        // Saving previously applied skin
        final Skin previousSkin = currentSkin;

        // Uninstalling previous skin
        if ( previousSkin != null )
        {
            previousSkin.uninstall ();
        }

        // Updating currently applied skin
        currentSkin = skin;

        // Installing new skin
        if ( skin != null )
        {
            skin.install ();
        }

        // Applying new skin to all existing skinnable components
        final HashMap<JComponent, StyleData> skins = MapUtils.copyMap ( styleData );
        for ( final Map.Entry<JComponent, StyleData> entry : skins.entrySet () )
        {
            final JComponent component = entry.getKey ();
            final StyleData data = getData ( component );
            if ( !data.isPinnedSkin () && data.getSkin () == previousSkin )
            {
                // There is no need to update child style components here as we will reach them anyway
                // So we simply update each single component skin separately
                data.applySkin ( skin, false );
            }
        }

        // Informing about skin change
        fireSkinChanged ( previousSkin, skin );

        return previousSkin;
    }

    /**
     * Adds skin change listener.
     *
     * @param listener skin change listener to add
     */
    public static void addSkinListener ( final SkinListener listener )
    {
        listenerList.add ( SkinListener.class, listener );
    }

    /**
     * Removes skin change listener.
     *
     * @param listener skin change listener to remove
     */
    public static void removeSkinListener ( final SkinListener listener )
    {
        listenerList.remove ( SkinListener.class, listener );
    }

    /**
     * Informs listeners about skin change.
     *
     * @param previous previously used skin
     * @param current  currently used skin
     */
    public static void fireSkinChanged ( final Skin previous, final Skin current )
    {
        for ( final SkinListener listener : listenerList.getListeners ( SkinListener.class ) )
        {
            listener.skinChanged ( previous, current );
        }
    }

    /**
     * Adds new skin extensions.
     * These extensions are loaded after manager initialization.
     * If it was already initialized before they will be loaded right away.
     *
     * @param extensions skin extensions to add
     */
    public static void addExtensions ( final SkinExtension... extensions )
    {
        // Event Dispatch Thread check
        WebLookAndFeel.checkEventDispatchThread ();

        // Iterating through added extensions
        for ( final SkinExtension extension : extensions )
        {
            // Saving extension
            StyleManager.extensions.add ( extension );

            // Performing additional actions if manager was already initialized
            // It is allowed to add extensions before L&F initialization to speedup startup
            if ( initialized )
            {
                // Installing extension onto the current skin
                // Components are not updated when extension is added because extension styles should not be used at this point yet
                // If they are used it is an issue of components/extension initialization order and it should be fixed in application
                getSkin ().applyExtension ( extension );
            }
        }
    }

    /**
     * Returns list of installed skin extensions.
     *
     * @return list of installed skin extensions
     */
    public static List<SkinExtension> getExtensions ()
    {
        return CollectionUtils.copy ( extensions );
    }

    /**
     * Returns component style ID.
     *
     * @param component component to retrieve style ID for
     * @return component style ID
     */
    public static StyleId getStyleId ( final JComponent component )
    {
        return getData ( component ).getStyleId ();
    }

    /**
     * Sets new component style ID.
     *
     * @param component component to set style ID for
     * @param id        new style ID
     * @return previously used style ID
     */
    public static StyleId setStyleId ( final JComponent component, final StyleId id )
    {
        return getData ( component ).setStyleId ( id );
    }

    /**
     * Resets style ID to default value.
     * This method forces component to instantly apply style with the specified ID to itself.
     *
     * @param component component to reset style ID for
     * @return previously used style ID
     */
    public static StyleId resetStyleId ( final JComponent component )
    {
        return getData ( component ).resetStyleId ( true );
    }

    /**
     * Applies current skin to the skinnable component.
     * <p>
     * This method is used only to setup style data into UI on install.
     * It is not recommended to use it outside of that install behavior.
     *
     * @param component component to apply skin to
     * @return previously applied skin
     */
    public static Skin installSkin ( final JComponent component )
    {
        return getData ( component ).applySkin ( getSkin (), false );
    }

    /**
     * Updates current skin in the skinnable component.
     * This method is used only to properly update skin on various changes.
     * It is not recommended to use it outside of style manager behavior.
     *
     * @param component component to update skin for
     */
    public static void updateSkin ( final JComponent component )
    {
        getData ( component ).updateSkin ( true );
    }

    /**
     * Removes skin applied to the specified component and returns it.
     * <p>
     * This method is used only to cleanup style data from UI on uninstall.
     * It is not recommended to use it outside of that uninstall behavior.
     *
     * @param component component to remove skin from
     * @return previously applied skin
     */
    public static Skin uninstallSkin ( final JComponent component )
    {
        return getData ( component ).removeSkin ();
    }

    /**
     * Returns skin currently applied to the specified component.
     *
     * @param component component to retrieve applied skin from
     * @return skin currently applied to the specified component
     */
    public static Skin getSkin ( final JComponent component )
    {
        return getData ( component ).getSkin ();
    }

    /**
     * Applies specified custom skin to the skinnable component and all of its children linked via {@link com.alee.managers.style.StyleId}.
     * Actual linked children information is stored within {@link com.alee.managers.style.StyleData} data objects.
     * Custom skin provided using this method will not be replaced if application skin changes.
     *
     * @param component component to apply skin to
     * @param skin      skin to be applied
     * @return previously applied skin
     */
    public static Skin setSkin ( final JComponent component, final Skin skin )
    {
        return setSkin ( component, skin, false );
    }

    /**
     * Applies specified custom skin to the skinnable component and all of its children linked via {@link com.alee.managers.style.StyleId}.
     * Actual linked children information is stored within {@link com.alee.managers.style.StyleData} data objects.
     * Custom skin provided using this method will not be replaced if application skin changes.
     *
     * @param component   component to apply skin to
     * @param skin        skin to be applied
     * @param recursively whether or not should apply skin to child components
     * @return previously applied skin
     */
    public static Skin setSkin ( final JComponent component, final Skin skin, final boolean recursively )
    {
        return getData ( component ).applyCustomSkin ( skin, recursively );
    }

    /**
     * Resets skin for the specified component and all of its children linked via {@link com.alee.managers.style.StyleId}.
     * Actual linked children information is stored within {@link com.alee.managers.style.StyleData} data objects.
     * Resetting component skin will also include it back into the skin update cycle in case global skin will be changed.
     *
     * @param component component to reset skin for
     * @return skin applied to the specified component after reset
     */
    public static Skin resetSkin ( final JComponent component )
    {
        return getData ( component ).resetSkin ();
    }

    /**
     * Adds skin change listener.
     *
     * @param component component to listen skin changes on
     * @param listener  skin change listener to add
     */
    public static void addStyleListener ( final JComponent component, final StyleListener listener )
    {
        getData ( component ).addStyleListener ( listener );
    }

    /**
     * Removes skin change listener.
     *
     * @param component component to listen skin changes on
     * @param listener  skin change listener to remove
     */
    public static void removeStyleListener ( final JComponent component, final StyleListener listener )
    {
        getData ( component ).removeStyleListener ( listener );
    }

    /**
     * Returns all custom painters for the specified component.
     *
     * @param component component to retrieve custom painters for
     * @return all custom painters for the specified component
     */
    @Deprecated
    public static Map<String, Painter> getCustomPainters ( final JComponent component )
    {
        return getData ( component ).getPainters ();
    }

    /**
     * Returns custom base painter for the specified component.
     *
     * @param component component to retrieve custom base painter for
     * @return custom base painter for the specified component
     */
    public static Painter getCustomPainter ( final JComponent component )
    {
        return getCustomPainter ( component, ComponentStyle.BASE_PAINTER_ID );
    }

    /**
     * Returns custom painter for the specified component.
     *
     * @param component component to retrieve custom painter for
     * @param id        painter ID
     * @return custom painter for the specified component
     */
    @Deprecated
    public static Painter getCustomPainter ( final JComponent component, final String id )
    {
        final Map<String, Painter> customPainters = getCustomPainters ( component );
        return customPainters != null ? customPainters.get ( id ) : null;
    }

    /**
     * Sets custom base painter for the specified component.
     * You should call this method when setting painter outside of the UI.
     *
     * @param component component to set painter for
     * @param painter   painter
     * @param <T>       painter type
     * @return old custom painter
     */
    public static <T extends Painter> T setCustomPainter ( final JComponent component, final T painter )
    {
        return setCustomPainter ( component, ComponentStyle.BASE_PAINTER_ID, painter );
    }

    /**
     * Sets custom painter for the specified component under the specified painter ID.
     * You should call this method when setting painter outside of the UI.
     *
     * @param component component to set painter for
     * @param id        painter ID
     * @param painter   painter
     * @param <T>       painter type
     * @return old custom painter
     */
    @Deprecated
    public static <T extends Painter> T setCustomPainter ( final JComponent component, final String id, final T painter )
    {
        // todo Move this into StyleData?

        // Saving custom painter
        final StyleData data = getData ( component );
        Map<String, Painter> painters = data.getPainters ();
        if ( painters == null )
        {
            painters = new HashMap<String, Painter> ( 1 );
            data.setPainters ( painters );
        }
        final T oldValue = ( T ) painters.put ( id, painter );

        // Forcing component update
        // todo Some methods in skin instead of full reinstall?
        // todo This also incorrectly resets custom skin!
        installSkin ( component );

        return oldValue;
    }

    /**
     * Resets painter for the specified component to default one.
     *
     * @param component component to reset painter for
     * @return true if painter was successfully resetted, false otherwise
     */
    public static boolean resetPainter ( final JComponent component )
    {
        // todo Move this into StyleData?

        final Map<String, Painter> painters = getData ( component ).getPainters ();
        if ( painters != null && painters.size () > 0 )
        {
            // Removing all custom painters
            painters.clear ();

            // Forcing component skin update
            // todo Some methods in skin instead of full reinstall?
            // todo This also incorrectly resets custom skin!
            installSkin ( component );

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns component style data.
     *
     * @param component component to retrieve style data for
     * @return component style data
     */
    protected static StyleData getData ( final JComponent component )
    {
        // Event Dispatch Thread check
        WebLookAndFeel.checkEventDispatchThread ();

        // Checking manager initialization
        checkInitialization ();

        // Checking component support
        checkSupport ( component );

        // Retrieving component style data
        StyleData data = styleData.get ( component );
        if ( data == null )
        {
            data = new StyleData ( component );
            styleData.put ( component, data );
        }
        return data;
    }

    /**
     * Performs component styling support check and throws an exception if it is not supported.
     *
     * @param component component to check
     * @throws com.alee.managers.style.StyleException in case component is not specified or not supported
     */
    private static void checkSupport ( final JComponent component )
    {
        if ( component == null )
        {
            throw new StyleException ( "Component is not specified" );
        }
        if ( !StyleableComponent.isSupported ( component ) )
        {
            final String msg = "Component '%s' is not supported";
            throw new StyleException ( String.format ( msg, component ) );
        }
    }

    /**
     * Performs skin support check and throws an exception if skin is not supported.
     *
     * @param skin skin to check
     * @throws com.alee.managers.style.StyleException in case skin is not specified or not supported
     */
    private static void checkSupport ( final Skin skin )
    {
        if ( skin == null )
        {
            throw new StyleException ( "Skin is not provided or failed to load" );
        }
        if ( !skin.isSupported () )
        {
            final String msg = "Skin '%s' is not supported in this system";
            throw new StyleException ( String.format ( msg, skin.getTitle () ) );
        }
    }

    /**
     * Returns newly created skin class instance.
     *
     * @param skinClass skin class
     * @return newly created skin class instance
     */
    private static Skin createSkin ( final Class skinClass )
    {
        try
        {
            return ( Skin ) ReflectUtils.createInstance ( skinClass );
        }
        catch ( final Throwable e )
        {
            final String msg = "Unable to initialize skin for class '%s'";
            throw new StyleException ( String.format ( msg, skinClass ), e );
        }
    }
}