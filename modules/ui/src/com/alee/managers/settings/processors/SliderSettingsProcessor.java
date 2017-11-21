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

import com.alee.managers.settings.SettingsProcessor;
import com.alee.managers.settings.SettingsProcessorData;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Custom SettingsProcessor for {@link javax.swing.JSlider} component.
 *
 * @author Mikle Garin
 * @see <a href="https://github.com/mgarin/weblaf/wiki/How-to-use-SettingsManager">How to use SettingsManager</a>
 * @see com.alee.managers.settings.SettingsManager
 * @see com.alee.managers.settings.SettingsProcessor
 */

public class SliderSettingsProcessor extends SettingsProcessor<JSlider, Integer> implements ChangeListener
{
    /**
     * Constructs SettingsProcessor using the specified SettingsProcessorData.
     *
     * @param data SettingsProcessorData
     */
    public SliderSettingsProcessor ( final SettingsProcessorData data )
    {
        super ( data );
    }

    @Override
    public Integer getDefaultValue ()
    {
        Integer defaultValue = super.getDefaultValue ();
        if ( defaultValue == null )
        {
            defaultValue = getComponent ().getMinimum ();
        }
        return defaultValue;
    }

    @Override
    protected void doInit ( final JSlider slider )
    {
        slider.addChangeListener ( this );
    }

    @Override
    protected void doDestroy ( final JSlider slider )
    {
        slider.removeChangeListener ( this );
    }

    @Override
    public void stateChanged ( final ChangeEvent e )
    {
        save ();
    }

    @Override
    protected void doLoad ( final JSlider slider )
    {
        slider.setValue ( loadValue () );
    }

    @Override
    protected void doSave ( final JSlider slider )
    {
        saveValue ( slider.getValue () );
    }
}