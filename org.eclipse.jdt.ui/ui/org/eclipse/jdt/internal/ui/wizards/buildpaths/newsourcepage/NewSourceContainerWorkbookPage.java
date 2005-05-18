/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.ui.wizards.buildpaths.newsourcepage;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.buildpath.ClasspathModifier;
import org.eclipse.jdt.internal.corext.buildpath.ResetAllOutputFoldersOperation;
import org.eclipse.jdt.internal.corext.buildpath.ClasspathModifier.IClasspathModifierListener;
import org.eclipse.jdt.internal.corext.util.Messages;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.ScrolledPageContent;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.util.ViewerPane;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.newsourcepage.DialogPackageExplorerActionGroup.DialogExplorerActionContext;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;

public class NewSourceContainerWorkbookPage extends BuildPathBasePage implements IClasspathModifierListener {
    
    public static final String OPEN_SETTING= "org.eclipse.jdt.internal.ui.wizards.buildpaths.NewSourceContainerPage.openSetting";  //$NON-NLS-1$
    
    private ListDialogField fClassPathList;
    private HintTextGroup fHintTextGroup;
    private DialogPackageExplorer fPackageExplorer;
    private SelectionButtonDialogField fUseFolderOutputs;

	private IJavaProject fJavaProject;

    /**
     * Constructor of the <code>NewSourceContainerWorkbookPage</code> which consists of 
     * a tree representing the project, a toolbar with the available actions, an area 
     * containing hyperlinks that perform the same actions as those in the toolbar but 
     * additionally with some short description.
     * 
     * @param classPathList
     * @param outputLocationField
     * @param context a runnable context, can be <code>null</code>
     */
    public NewSourceContainerWorkbookPage(ListDialogField classPathList, StringDialogField outputLocationField, IRunnableContext context) {
        fClassPathList= classPathList;
    
        fUseFolderOutputs= new SelectionButtonDialogField(SWT.CHECK);
        fUseFolderOutputs.setSelection(false);
        fUseFolderOutputs.setLabelText(NewWizardMessages.SourceContainerWorkbookPage_folders_check); 
        
		fPackageExplorer= new DialogPackageExplorer();
		fHintTextGroup= new HintTextGroup(fPackageExplorer, outputLocationField, fUseFolderOutputs, context);

     }
    
    /**
     * Initialize the controls displaying
     * the content of the java project and saving 
     * the '.classpath' and '.project' file.
     * 
     * Must be called before initializing the 
     * controls using <code>getControl(Composite)</code>.
     * 
     * @param javaProject the current java project
     */
    public void init(IJavaProject javaProject) {
		fJavaProject= javaProject;
        fHintTextGroup.setJavaProject(javaProject);
        
        fPackageExplorer.setInput(javaProject);
		
		boolean useFolderOutputs= false;
		List cpelements= fClassPathList.getElements();
		for (int i= 0; i < cpelements.size() && !useFolderOutputs; i++) {
			CPListElement cpe= (CPListElement) cpelements.get(i);
			if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				if (cpe.getAttribute(CPListElement.OUTPUT) != null) {
					useFolderOutputs= true;
				}
			}
		}
		fUseFolderOutputs.setSelection(useFolderOutputs);
    }
    
     
    /**
     * Initializes controls and return composite containing
     * these controls.
     * 
     * Before calling this method, make sure to have 
     * initialized this instance with a java project 
     * using <code>init(IJavaProject)</code>.
     * 
     * @param parent the parent composite
     * @return composite containing controls
     * 
     * @see #init(IJavaProject)
     */
    public Control getControl(Composite parent) {
        final int[] sashWeight= {60};
        final IPreferenceStore preferenceStore= JavaPlugin.getDefault().getPreferenceStore();
        preferenceStore.setDefault(OPEN_SETTING, true);
        
        // ScrolledPageContent is needed for resizing on expand the expandable composite
        ScrolledPageContent scrolledContent = new ScrolledPageContent(parent);
        Composite body= scrolledContent.getBody();
        body.setLayout(new GridLayout());
        
        final SashForm sashForm= new SashForm(body, SWT.VERTICAL | SWT.NONE);
        sashForm.setFont(sashForm.getFont());
        
        ViewerPane pane= new ViewerPane(sashForm, SWT.BORDER | SWT.FLAT);
        pane.setContent(fPackageExplorer.createControl(pane));
		fPackageExplorer.setContentProvider();
        
        final ExpandableComposite excomposite= new ExpandableComposite(sashForm, SWT.NONE, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
        excomposite.setFont(sashForm.getFont());
        excomposite.setText(NewWizardMessages.NewSourceContainerWorkbookPage_HintTextGroup_title);
        final boolean isExpanded= preferenceStore.getBoolean(OPEN_SETTING);
        excomposite.setExpanded(isExpanded);
        excomposite.addExpansionListener(new ExpansionAdapter() {
                       public void expansionStateChanged(ExpansionEvent e) {
                           ScrolledPageContent parentScrolledComposite= getParentScrolledComposite(excomposite);
                           if (parentScrolledComposite != null) {
                              boolean expanded= excomposite.isExpanded();
                              parentScrolledComposite.reflow(true);
                              adjustSashForm(sashWeight, sashForm, expanded);
                              preferenceStore.setValue(OPEN_SETTING, expanded);
                           }
                       }
                 });
        
        excomposite.setClient(fHintTextGroup.createControl(excomposite));
        fUseFolderOutputs.doFillIntoGrid(body, 1);
		
	    final DialogPackageExplorerActionGroup actionGroup= new DialogPackageExplorerActionGroup(fHintTextGroup, this);
		   
		
        fUseFolderOutputs.setDialogFieldListener(new IDialogFieldListener() {
            public void dialogFieldChanged(DialogField field) {
                boolean isUseFolders= fUseFolderOutputs.isSelected();
                if (isUseFolders) {
                    ResetAllOutputFoldersOperation op= new ResetAllOutputFoldersOperation(NewSourceContainerWorkbookPage.this, fHintTextGroup);
                    try {
                        op.run(null);
                    } catch (InvocationTargetException e) {
                        ExceptionHandler.handle(e, getShell(),
                                Messages.format(NewWizardMessages.NewSourceContainerWorkbookPage_Exception_Title, op.getName()), e.getMessage()); 
                    }
                }
				fPackageExplorer.showOutputFolders(isUseFolders);
                try {
                    ISelection selection= fPackageExplorer.getSelection();
                    actionGroup.refresh(new DialogExplorerActionContext(selection, fJavaProject));
                } catch (JavaModelException e) {
                    ExceptionHandler.handle(e, getShell(),
                            NewWizardMessages.NewSourceContainerWorkbookPage_Exception_refresh, e.getMessage()); 
                }
            }
        });
        
        // Create toolbar with actions on the left
        ToolBarManager tbm= actionGroup.createLeftToolBarManager(pane);
        pane.setTopCenter(null);
        pane.setTopLeft(tbm.getControl());
        
        // Create toolbar with help on the right
        tbm= actionGroup.createLeftToolBar(pane);
        pane.setTopRight(tbm.getControl());
        
        fHintTextGroup.setActionGroup(actionGroup);
        fPackageExplorer.setActionGroup(actionGroup);
        actionGroup.addListener(fHintTextGroup);
        
		sashForm.setWeights(new int[] {60, 40});
		adjustSashForm(sashWeight, sashForm, excomposite.isExpanded());
		GridData gd= new GridData(GridData.FILL_BOTH);
		PixelConverter converter= new PixelConverter(parent);
		gd.heightHint= converter.convertHeightInCharsToPixels(20);
		sashForm.setLayoutData(gd);
        
        fUseFolderOutputs.dialogFieldChanged();
        
        parent.layout(true);

        return scrolledContent;
    }
    
    /**
     * Adjust the size of the sash form.
     * 
     * @param sashWeight the weight to be read or written
     * @param sashForm the sash form to apply the new weights to
     * @param isExpanded <code>true</code> if the expandable composite is 
     * expanded, <code>false</code> otherwise
     */
    private void adjustSashForm(int[] sashWeight, SashForm sashForm, boolean isExpanded) {
        if (isExpanded) {
            int upperWeight= sashWeight[0];
            sashForm.setWeights(new int[]{upperWeight, 100 - upperWeight});
        }
        else {
            // TODO Dividing by 10 because of https://bugs.eclipse.org/bugs/show_bug.cgi?id=81939
            sashWeight[0]= sashForm.getWeights()[0] / 10;
            sashForm.setWeights(new int[]{95, 5});
        }
        sashForm.layout(true);
    }
    
    /**
     * Get the scrolled page content of the given control by 
     * traversing the parents.
     * 
     * @param control the control to get the scrolled page content for 
     * @return the scrolled page content or <code>null</code> if none found
     */
    private ScrolledPageContent getParentScrolledComposite(Control control) {
       Control parent= control.getParent();
       while (!(parent instanceof ScrolledPageContent)) {
           parent= parent.getParent();
       }
       if (parent instanceof ScrolledPageContent) {
           return (ScrolledPageContent) parent;
       }
       return null;
   }
    
    /**
     * Get the active shell.
     * 
     * @return the active shell
     */
    private Shell getShell() {
        return JavaPlugin.getActiveWorkbenchShell();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage#getSelection()
     */
    public List getSelection() {
        List selectedList= new ArrayList();
        
        IJavaProject project= fHintTextGroup.getJavaProject();
        try {
            List list= fHintTextGroup.getSelection().toList();
            List existingEntries= ClasspathModifier.getExistingEntries(project);
        
            for(int i= 0; i < list.size(); i++) {
                Object obj= list.get(i);
                if (obj instanceof IPackageFragmentRoot) {
                    IPackageFragmentRoot element= (IPackageFragmentRoot)obj;
                    CPListElement cpElement= ClasspathModifier.getClasspathEntry(existingEntries, element); 
                    selectedList.add(cpElement);
                }
                else if (obj instanceof IJavaProject) {
                    IClasspathEntry entry= ClasspathModifier.getClasspathEntryFor(project.getPath(), project, IClasspathEntry.CPE_SOURCE);
                    if (entry == null)
                        continue;
                    CPListElement cpElement= CPListElement.createFromExisting(entry, project);
                    selectedList.add(cpElement);
                }
            }
        } catch (JavaModelException e) {
            return new ArrayList();
        }
        return selectedList;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage#setSelection(java.util.List)
     */
    public void setSelection(List selection) {
		// page switch
		
        if (selection.size() == 0)
            return;
	    
		List cpEntries= new ArrayList();
		
		for (int i= 0; i < selection.size(); i++) {
			CPListElement element= (CPListElement) selection.get(i);
			if (element.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				cpEntries.add(element);
			}
		}
		
        // refresh classpath
        List list= fClassPathList.getElements();
        IClasspathEntry[] entries= new IClasspathEntry[list.size()];
        for(int i= 0; i < list.size(); i++) {
            CPListElement entry= (CPListElement) list.get(i);
            entries[i]= entry.getClasspathEntry(); 
        }
        try {
			fJavaProject.setRawClasspath(entries, null);
            fPackageExplorer.refresh();
        } catch (JavaModelException e) {
            JavaPlugin.log(e);
        }
        
        fPackageExplorer.setSelection(cpEntries);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage#isEntryKind(int)
     */
    public boolean isEntryKind(int kind) {
        return kind == IClasspathEntry.CPE_SOURCE;
    }
    
    /**
     * Update <code>fClassPathList</code>.
     */
    public void classpathEntryChanged(List newEntries) {
        fClassPathList.setElements(newEntries);
    }
}
