/*
 * Copyright (C) 2012 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.htmlhifive.tools.wizard.ui.page.tree;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.htmlhifive.tools.wizard.PluginConstant;
import com.htmlhifive.tools.wizard.library.model.LibraryState;

/**
 * <H3>ツリーラベルプロバイダ.</H3>
 * 
 * @author fkubo
 */
public class LibraryTreeLabelProvider extends LabelProvider implements ITableLabelProvider, ITableFontProvider,
ITableColorProvider {

	@Override
	public String getColumnText(Object element, int columnIndex) {

		if (columnIndex == 0) {
			if (element instanceof LibraryTreeNode) {
				return ((LibraryTreeNode) element).getLabel();
			}
			return super.getText(element);
		}

		if (columnIndex == 1) {
			if (element instanceof CategoryNode) {
				CategoryNode categoryNode = (CategoryNode) element;
				return categoryNode.getPathLable();
			}
			if (element instanceof LibraryNode) {
				LibraryNode libraryNode = (LibraryNode) element;
				if (libraryNode.getFileList() != null) {
					return "    " + StringUtils.join(libraryNode.getFileList(), ", ");
				}
			}
		}
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {

		if (columnIndex == 0) {
			if (element instanceof CategoryNode) {
				// カテゴリ.
				return PluginConstant.IMG_CATEGORY;
			}

			if (element instanceof LibraryNode) {
				LibraryNode libraryNode = (LibraryNode) element;

				if (libraryNode.getState() == LibraryState.DEFAULT && libraryNode.isRecommended()) {
					return PluginConstant.IMG_QUICK_ASSIST;
				} else {
					return libraryNode.getState().getImage();
				}
			}
		}
		return null;
	}

	@Override
	public Font getFont(Object element, int columnIndex) {

		// return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		return null;
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {

		if (columnIndex == 1 && element instanceof CategoryNode) {
			CategoryNode categoryNode = (CategoryNode) element;
			boolean installed = true;
			for (TreeNode node : categoryNode.getChildren()) {
				if (!((LibraryNode) node).isExists()) {
					installed = false;
					break;
				}
			}
			if (!installed) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
			}
		}
		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex) {

		return null;
	}
}