/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.jeview.views;


/**
 *
 */
public abstract class JEAttribute {
	
	protected static final JEAttribute[] EMPTY= new JEAttribute[0];

	public abstract JEAttribute getParent();
	public abstract JEAttribute[] getChildren();
	public abstract String getLabel();
		
}
