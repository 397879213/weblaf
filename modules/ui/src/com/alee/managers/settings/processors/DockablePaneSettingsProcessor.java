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

package com.alee.managers.settings.processors;

import com.alee.api.data.CompassDirection;
import com.alee.extended.dock.DockableFrameListener;
import com.alee.extended.dock.DockableFrameState;
import com.alee.extended.dock.WebDockableFrame;
import com.alee.extended.dock.WebDockablePane;
import com.alee.extended.dock.data.DockableContainer;
import com.alee.managers.settings.SettingsProcessor;
import com.alee.managers.settings.SettingsProcessorData;

/**
 * {@link SettingsProcessor} for {@link WebDockablePane}.
 *
 * @author Mikle Garin
 * @see <a href="https://github.com/mgarin/weblaf/wiki/How-to-use-WebDockablePane">How to use WebDockablePane</a>
 * @see WebDockablePane
 * @see <a href="https://github.com/mgarin/weblaf/wiki/How-to-use-SettingsManager">How to use SettingsManager</a>
 * @see com.alee.managers.settings.SettingsManager
 * @see com.alee.managers.settings.SettingsProcessor
 */

public class DockablePaneSettingsProcessor extends SettingsProcessor<WebDockablePane, DockableContainer> implements DockableFrameListener
{
    /**
     * Constructs new {@link DockablePaneSettingsProcessor}.
     *
     * @param data {@link SettingsProcessorData}
     */
    public DockablePaneSettingsProcessor ( final SettingsProcessorData data )
    {
        super ( data );
    }

    @Override
    protected void doInit ( final WebDockablePane component )
    {
        component.addFrameListener ( this );
    }

    @Override
    protected void doDestroy ( final WebDockablePane component )
    {
        component.removeFrameListener ( this );
    }

    @Override
    public void frameAdded ( final WebDockableFrame frame, final WebDockablePane dockablePane )
    {
        // This event is tracked within state change
    }

    @Override
    public void frameStateChanged ( final WebDockableFrame frame, final DockableFrameState oldState, final DockableFrameState newState )
    {
        save ();
    }

    @Override
    public void frameMoved ( final WebDockableFrame frame, final CompassDirection position )
    {
        save ();
    }

    @Override
    public void frameRemoved ( final WebDockableFrame frame, final WebDockablePane dockablePane )
    {
        // This event is tracked within state change
    }

    @Override
    protected void doLoad ( final WebDockablePane component )
    {
        final DockableContainer state = loadValue ();
        if ( state != null )
        {
            component.setState ( state );
        }
    }

    @Override
    protected void doSave ( final WebDockablePane component )
    {
        saveValue ( component.getState () );
    }
}