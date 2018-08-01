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

package com.alee.demo.content.data.tree;

import com.alee.demo.api.example.*;
import com.alee.extended.tree.*;
import com.alee.extended.tree.sample.SampleExDataProvider;
import com.alee.extended.tree.sample.SampleNode;
import com.alee.extended.tree.sample.SampleObjectType;
import com.alee.extended.tree.sample.SampleTreeCellEditor;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.managers.style.StyleId;
import com.alee.utils.CollectionUtils;

import javax.swing.*;
import java.util.List;

/**
 * @author Mikle Garin
 */
public class WebExTreeExample extends AbstractStylePreviewExample
{
    @Override
    public String getId ()
    {
        return "extree";
    }

    @Override
    protected String getStyleFileName ()
    {
        return "extree";
    }

    @Override
    public FeatureType getFeatureType ()
    {
        return FeatureType.extended;
    }

    @Override
    protected List<Preview> createPreviews ()
    {
        return CollectionUtils.<Preview>asList (
                new BasicTree ( StyleId.extree ),
                new EditableTree ( StyleId.extree ),
                new DragAndDropTree ( StyleId.extree )
        );
    }

    /**
     * Basic extended tree preview.
     */
    protected class BasicTree extends AbstractStylePreview
    {
        /**
         * Constructs new style preview.
         *
         * @param styleId preview style ID
         */
        public BasicTree ( final StyleId styleId )
        {
            super ( WebExTreeExample.this, "basic", FeatureState.updated, styleId );
        }

        @Override
        protected List<? extends JComponent> createPreviewElements ()
        {
            final WebExTree tree = new WebExTree ( getStyleId (), new SampleExDataProvider () );
            tree.setVisibleRowCount ( 8 );
            return CollectionUtils.asList ( new WebScrollPane ( tree ).setPreferredWidth ( 200 ) );
        }
    }

    /**
     * Editable extended tree preview.
     */
    protected class EditableTree extends AbstractStylePreview
    {
        /**
         * Constructs new style preview.
         *
         * @param styleId preview style ID
         */
        public EditableTree ( final StyleId styleId )
        {
            super ( WebExTreeExample.this, "editable", FeatureState.updated, styleId );
        }

        @Override
        protected List<? extends JComponent> createPreviewElements ()
        {
            final WebExTree tree = new WebExTree ( getStyleId (), new SampleExDataProvider (), new SampleTreeCellEditor () );
            tree.setVisibleRowCount ( 8 );
            return CollectionUtils.asList ( new WebScrollPane ( tree ).setPreferredWidth ( 200 ) );
        }
    }

    /**
     * Drag and drop tree preview.
     */
    protected class DragAndDropTree extends AbstractStylePreview
    {
        /**
         * Constructs new style preview.
         *
         * @param styleId preview style ID
         */
        public DragAndDropTree ( final StyleId styleId )
        {
            super ( WebExTreeExample.this, "dragndrop", FeatureState.updated, styleId );
        }

        @Override
        protected List<? extends JComponent> createPreviewElements ()
        {
            final WebExTree left = new WebExTree ( getStyleId (), new SampleExDataProvider (), new SampleTreeCellEditor () );
            left.setVisibleRowCount ( 8 );
            left.setDragEnabled ( true );
            left.setDropMode ( DropMode.ON_OR_INSERT );
            left.setTransferHandler ( createTransferHandler () );
            final WebScrollPane leftScroll = new WebScrollPane ( left ).setPreferredWidth ( 200 );

            final WebExTree right = new WebExTree ( getStyleId (), new SampleExDataProvider (), new SampleTreeCellEditor () );
            right.setVisibleRowCount ( 8 );
            right.setDragEnabled ( true );
            right.setDropMode ( DropMode.ON_OR_INSERT );
            right.setTransferHandler ( createTransferHandler () );
            final WebScrollPane rightScroll = new WebScrollPane ( right ).setPreferredWidth ( 200 );

            return CollectionUtils.asList ( leftScroll, rightScroll );
        }
    }

    /**
     * Returns sample tree transfer handler.
     * It will provide base functionality of DnD for our sample tree.
     *
     * @return sample extended tree transfer handler
     */
    protected static ExTreeTransferHandler<SampleNode, WebExTree<SampleNode>, ExTreeModel<SampleNode>> createTransferHandler ()
    {
        return new ExTreeTransferHandler<SampleNode, WebExTree<SampleNode>, ExTreeModel<SampleNode>> ()
        {
            /**
             * Forcing this {@link TransferHandler} to move nodes.
             */
            @Override
            public int getSourceActions ( final JComponent c )
            {
                return MOVE;
            }

            /**
             * Blocks drop on {@link SampleObjectType#leaf} nodes.
             */
            @Override
            protected List<? extends TreeDropHandler> createDropHandlers ()
            {
                return CollectionUtils.asList ( new NodesDropHandler<SampleNode, WebExTree<SampleNode>, ExTreeModel<SampleNode>> ()
                {
                    @Override
                    protected boolean canDrop ( final TransferSupport support, final WebExTree<SampleNode> tree,
                                                final ExTreeModel<SampleNode> model, final SampleNode dropLocation, final int dropIndex,
                                                final List<SampleNode> nodes )
                    {
                        return dropLocation.getUserObject ().getType () != SampleObjectType.leaf;
                    }
                } );
            }

            /**
             * We do not need to copy children as {@link ExTreeDataProvider} will do that instead.
             * We only need to provide a copy of the specified node here.
             */
            @Override
            protected SampleNode copy ( final WebExTree<SampleNode> tree, final ExTreeModel<SampleNode> model, final SampleNode node )
            {
                return node.clone ();
            }

            /**
             * Blocks root element drag.
             */
            @Override
            protected boolean canBeDragged ( final WebExTree<SampleNode> tree, final ExTreeModel<SampleNode> model,
                                             final List<SampleNode> nodes )
            {
                boolean allowed = true;
                for ( final SampleNode node : nodes )
                {
                    if ( node.getUserObject ().getType () == SampleObjectType.root )
                    {
                        allowed = false;
                        break;
                    }
                }
                return allowed;
            }
        };
    }
}