/*******************************************************************************
 * Copyright (c) 2000, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IWorkbenchSite;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.refactoring.sef.SelfEncapsulateFieldRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.util.WorkingCopyUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.internal.ui.refactoring.sef.SelfEncapsulateFieldWizard;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;

/**
 * Action to run the self encapsulate field refactoring. The action works
 * for both view parts that present Java elements and compilation unit
 * editors. 
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class SelfEncapsulateFieldAction extends SelectionDispatchAction {
	
	private CompilationUnitEditor fEditor;
	
	/**
	 * Creates a new <code>SelfEncapsulateFieldAction</code>.
	 * 
	 * @param site the site providing context information for this action
	 */
	public SelfEncapsulateFieldAction(IWorkbenchSite site) {
		super(site);
		setText(ActionMessages.getString("SelfEncapsulateFieldAction.label")); //$NON-NLS-1$
	}
	
	/**
	 * Creates a new <code>SelfEncapsulateFieldAction</code>.
	 * <p>
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 * </p>
	 */
	public SelfEncapsulateFieldAction(CompilationUnitEditor editor) {
		this(editor.getEditorSite());
		fEditor= editor;
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	protected void selectionChanged(ITextSelection selection) {
		setEnabled(fEditor != null);
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	protected void selectionChanged(IStructuredSelection selection) {
		setEnabled(checkEnabled(selection));
	}
	
	private boolean checkEnabled(IStructuredSelection selection) {
		if (selection.size() != 1)
			return false;
		Object element= selection.getFirstElement();
		if (!(element instanceof IField))
			return false;
		return !((IField)element).isBinary();
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	protected void run(ITextSelection selection) {
		IJavaElement[] elements= SelectionConverter.codeResolveHandled(fEditor, getShell(), getDialogTitle());
		if (elements.length != 1 || !(elements[0] instanceof IField)) {
			MessageDialog.openInformation(getShell(), getDialogTitle(), ActionMessages.getString("SelfEncapsulateFieldAction.dialog.unavailable")); //$NON-NLS-1$
			return;
		}
		IField field= (IField)elements[0];
		if (field.isBinary())
			return;
		run(field);
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	protected void run(IStructuredSelection selection) {
		if (!checkEnabled(selection))
			return;
		run((IField)selection.getFirstElement());
	}
	
	private void run(IField selectedField) {
		IField field= null;
		try {
			field= (IField)WorkingCopyUtil.getWorkingCopyIfExists(selectedField);
		} catch (JavaModelException e) {
		}
		if (field == null) {
			MessageDialog.openInformation(
				JavaPlugin.getActiveWorkbenchShell(), getDialogTitle(),
				ActionMessages.getFormattedString("SelfEncapsulateFieldAction.dialog.field_doesnot_exit", selectedField.getElementName()));  //$NON-NLS-1$
			return;
		}
			
		SelfEncapsulateFieldRefactoring refactoring= new SelfEncapsulateFieldRefactoring(field, JavaPreferencesSettings.getCodeGenerationSettings());
		try  {	
			new RefactoringStarter().activate(
				refactoring, 
				new SelfEncapsulateFieldWizard(refactoring), getDialogTitle(), true);
		} catch (JavaModelException e) {
			ExceptionHandler.handle(e, getDialogTitle(),
				ActionMessages.getString("SelfEncapsulateFieldAction.dialog.cannot_perform")); //$NON-NLS-1$
		}
	}
		
	private String getDialogTitle() {
		return ActionMessages.getString("SelfEncapsulateFieldAction.dialog.title"); //$NON-NLS-1$
	}	
}
