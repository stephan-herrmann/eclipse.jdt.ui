/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.workingsets;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionGroup;

import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;

/**
 * An action group to provide access to the working sets.
 */
public class ViewActionGroup extends ActionGroup {

	public static final int SHOW_PROJECTS= 1;
	public static final int SHOW_WORKING_SETS= 2;
	public static final String MODE_CHANGED= ViewActionGroup.class.getName() + ".mode_changed"; //$NON-NLS-1$
	
	private static final Integer INT_SHOW_PROJECTS= new Integer(SHOW_PROJECTS);
	private static final Integer INT_SHOW_WORKING_SETS= new Integer(SHOW_WORKING_SETS);
	
	private final IPropertyChangeListener fChangeListener;
	
	private int fMode;
	private IMenuManager fMenuManager;
	private IWorkingSetActionGroup fActiveActionGroup;
	private WorkingSetShowActionGroup fShowActionGroup;
	private WorkingSetFilterActionGroup fFilterActionGroup;

	public ViewActionGroup(WorkingSetModel workingSetModel, int mode, IPropertyChangeListener changeListener, Shell shell) {
		fChangeListener= changeListener;
		fFilterActionGroup= new WorkingSetFilterActionGroup(shell, changeListener);
		fShowActionGroup= new WorkingSetShowActionGroup(workingSetModel, shell);
		fMode= mode;
		if (showWorkingSets())
			fActiveActionGroup= fShowActionGroup;
		else
			fActiveActionGroup= fFilterActionGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		fMenuManager= actionBars.getMenuManager();
		if (PackageExplorerPart.ENABLE_WORKING_SET_MODE) {
			IMenuManager showMenu= new MenuManager(WorkingSetMessages.getString("ViewActionGroup.show.label")); //$NON-NLS-1$
			fillShowMenu(showMenu);
			fMenuManager.add(showMenu);
		}
		fMenuManager.add(new Separator(IWorkingSetActionGroup.ACTION_GROUP));
		if (fActiveActionGroup == null)
			fActiveActionGroup= fFilterActionGroup;
		((ActionGroup)fActiveActionGroup).fillActionBars(actionBars);
	}
	
	private void fillShowMenu(IMenuManager menu) {
		ViewAction projects= new ViewAction(this, SHOW_PROJECTS);
		projects.setText(WorkingSetMessages.getString("ViewActionGroup.projects.label")); //$NON-NLS-1$
		menu.add(projects);
		ViewAction workingSets= new ViewAction(this, SHOW_WORKING_SETS);
		workingSets.setText(WorkingSetMessages.getString("ViewActionGroup.workingSets.label")); //$NON-NLS-1$
		menu.add(workingSets);
		if (fMode == SHOW_PROJECTS) {
			projects.setChecked(true);
		} else {
			workingSets.setChecked(true);
		}
	}

	public void fillFilters(StructuredViewer viewer) {
		if (fMode == SHOW_PROJECTS)
			viewer.addFilter(fFilterActionGroup.getWorkingSetFilter());
	}
	
	public void setMode(int mode) {
		fMode= mode;
		fActiveActionGroup.cleanViewMenu(fMenuManager);
		PropertyChangeEvent event;
		if (mode == SHOW_PROJECTS) {
			fActiveActionGroup= fFilterActionGroup;
			event= new PropertyChangeEvent(this, MODE_CHANGED, INT_SHOW_WORKING_SETS, INT_SHOW_PROJECTS);
		} else {
			fActiveActionGroup= fShowActionGroup;
			event= new PropertyChangeEvent(this, MODE_CHANGED, INT_SHOW_PROJECTS, INT_SHOW_WORKING_SETS);
		}
		fActiveActionGroup.fillViewMenu(fMenuManager);
		fMenuManager.updateAll(true);
		fChangeListener.propertyChange(event);
	}
	
	public WorkingSetFilterActionGroup getFilterGroup() {
		return fFilterActionGroup;
	}

	public void restoreState(IMemento memento) {
		fFilterActionGroup.restoreState(memento);
	}

	public void saveState(IMemento memento) {
		fFilterActionGroup.saveState(memento);
	}
	
	public boolean showProjects() {
		return fMode == SHOW_PROJECTS;
	}
	
	public boolean showWorkingSets() {
		return fMode == SHOW_WORKING_SETS;
	}
}
