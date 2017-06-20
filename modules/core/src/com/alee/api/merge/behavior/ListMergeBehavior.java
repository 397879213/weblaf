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

package com.alee.api.merge.behavior;

import com.alee.api.Identifiable;
import com.alee.api.merge.Merge;
import com.alee.api.merge.GlobalMergeBehavior;
import com.alee.utils.CompareUtils;

import java.util.List;

/**
 * Smart {@link List} merge behavior that tracks `Identifiable` elements
 * It will attempt to find identifiable elements in list and merge them.
 * Other elements will simply be added to the end of the list in provided order.
 * This is the best way we can handle list elements merge without any additional information on the elements
 *
 * @param <T> {@link List} type
 * @author Mikle Garin
 * @see <a href="https://github.com/mgarin/weblaf/wiki/How-to-use-Merge">How to use Merge</a>
 * @see Merge
 */

public final class ListMergeBehavior<T extends List> implements GlobalMergeBehavior<T, T, T>
{
    /**
     * todo 1. Merging two lists of Identifiable elements gives unexpected results (https://github.com/mgarin/weblaf/issues/448)
     * todo 2. Provide a simple lists merge behavior similar to {@link IndexArrayMergeBehavior}
     * todo 3. Provide an appropriate support for {@link com.alee.utils.collection.ImmutableList}
     */

    @Override
    public boolean supports ( final Merge merge, final Object object, final Object merged )
    {
        return object instanceof List && merged instanceof List;
    }

    @Override
    public T merge ( final Merge merge, final T object, final T merged )
    {
        for ( final Object mergedObject : merged )
        {
            // We only merge identifiable objects as there is no other way to ensure we really need to merge them
            // We don't really want to have two different objects of the same type with the same ID in one list
            if ( mergedObject != null && mergedObject instanceof Identifiable )
            {
                // Looking for object of the same type which is also identifiable in the existing list
                // Then we compare their IDs and merge them using the same algorithm if IDs are equal
                final String mid = ( ( Identifiable ) mergedObject ).getId ();
                boolean found = false;
                for ( int j = 0; j < object.size (); j++ )
                {
                    final Object existingObject = object.get ( j );
                    if ( existingObject != null )
                    {
                        final String eid = ( ( Identifiable ) existingObject ).getId ();
                        if ( CompareUtils.equals ( eid, mid ) )
                        {
                            object.set ( j, merge.merge ( existingObject, mergedObject ) );
                            found = true;
                            break;
                        }
                    }
                }
                if ( !found )
                {
                    // Simply adding object to the end of the list
                    object.add ( mergedObject );
                }
            }
            else
            {
                // Simply adding non-identifiable object to the end of the list
                object.add ( mergedObject );
            }
        }
        return object;
    }
}