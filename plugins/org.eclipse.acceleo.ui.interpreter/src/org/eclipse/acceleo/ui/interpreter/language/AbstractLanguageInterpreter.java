/*******************************************************************************
 * Copyright (c) 2010, 2011 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.acceleo.ui.interpreter.language;

import java.util.concurrent.Callable;

import org.eclipse.acceleo.ui.interpreter.view.InterpreterView;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;

/**
 * This class describes the necessary contract for a language to be considered usable in the interpreter view.
 * <p>
 * A Language Interpreter will need to be able to provide the necessary tooling for the language edition
 * (completion, syntax highlighting...), a parser that can check for syntax errors and return them for
 * display, and an evaluation engine that can be called on given EObjects for a given expression.
 * </p>
 * 
 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 */
@SuppressWarnings("unused")
public abstract class AbstractLanguageInterpreter {
	/**
	 * If this editor reacts to the "link with editor" action of the interpreter view, this should return
	 * <code>true</code>. Will return <code>false</code> by default.
	 * 
	 * @return <code>true</code> if this interpreter cares for the "link with editor" action,
	 *         <code>false</code> otherwise.
	 */
	public boolean acceptLinkWithEditorContext() {
		return false;
	}

	/**
	 * If the language interpreter needs to add custom actions to the interpreter form, do it from here.
	 * <p>
	 * Do note that this will be called each time the interpreter is set. Clients are not expected to dispose
	 * of their actions, but are expected to create them each time this is called.
	 * </p>
	 * 
	 * @param interpreterView
	 *            The interpreter view
	 * @param toolBarManager
	 *            The manager for the form tool bar.
	 */
	public void addToolBarActions(InterpreterView interpreterView, IToolBarManager toolBarManager) {
		// Do nothing
	}

	/**
	 * This can be used to configure the source viewer to fit the language needs. Document scanner, document
	 * partitioner, source viewer configuration... can be set accordingly.
	 * 
	 * @param viewer
	 *            The viewer that will be displayed to the user for the edition of expressions in this
	 *            language.
	 */
	public void configureSourceViewer(SourceViewer viewer) {
		// Do nothing
	}

	/**
	 * If this language interpreter requires a specific form for the "result" viewer, this method can be used
	 * to instantiate it.
	 * 
	 * @param parent
	 *            The parent composite for this result viewer.
	 * @return The viewer that is to be used to display the result of this language's evaluations. Can be
	 *         <code>null</code>, in which case a default {@link TreeViewer} will be instantiated.
	 */
	public Viewer createResultViewer(Composite parent) {
		return null;
	}

	/**
	 * If editing this language needs a custom implementation of a SourceViewer, this method can be used to
	 * instantiate it.
	 * 
	 * @param parent
	 *            The parent composite for this source viewer.
	 * @return The source viewer that is to be used for this language. Can be <code>null</code>, in which case
	 *         a default {@link SourceViewer} will be instantiated.
	 */
	public SourceViewer createSourceViewer(Composite parent) {
		return null;
	}

	/**
	 * This will be called when the user has selected a new language from the interpreter view. If this
	 * interpreter has registered listeners or keeps references to one of the Viewers it has created, they
	 * should be disposed of here.
	 */
	public void dispose() {
		// Do nothing
	}

	/**
	 * This will be called when the interpreter view needs the current expression to be compiled. It will be
	 * called whenever the evaluation is needed : explicit call to the evaluate action, real time
	 * evaluation...
	 * <p>
	 * The returned task must be cancellable; real time compilations might not be run to the end before the
	 * user changes the expression. Do note that this task must be thread-safe as it will be called
	 * asynchronously and we do not guarantee that each task will be canceled before the subsequent is
	 * started.
	 * </p>
	 * <p>
	 * The {@link CompilationResult} object returned by this Callable should hold both the compiled expression
	 * (if any) and the problem(s) encountered during the compilation (if any). This(These) problem(s) will be
	 * displayed on the interpreter UI.
	 * </p>
	 * 
	 * @param context
	 *            The current interpreter context.
	 * @return Cancellable task that can be run by the interpreter view to know whether the expression is
	 *         well-formed. Can be <code>null</code> if this language cannot (or does not need to) be
	 *         compiled.
	 * @see org.eclipse.acceleo.ui.interpreter.language.CompilationResult
	 */
	public Callable<CompilationResult> getCompilationTask(InterpreterContext context) {
		return null;
	}

	/**
	 * This will be called when the user requests that his expression be evaluated against the currently
	 * selected EObjects. It will be called whenever the evaluation is needed : explicit call to the evaluate
	 * action, real time evaluation...
	 * <p>
	 * The returned task must be cancellable; real time evaluations might not be run to the end before the
	 * user changes the expression. Do note that this task must be thread-safe as it will be called
	 * asynchronously and we do not guarantee that each task will be canceled before the subsequent is
	 * started.
	 * </p>
	 * <p>
	 * The {@link EvaluationResult} object returned by this Callable should hold both the actual result of the
	 * evaluation (if any) that will be displayed in the "result" part of the interpreter view, and the
	 * problem(s) encountered during the evaluation (if any). This(These) problem(s) will be displayed on the
	 * interpreter UI.
	 * </p>
	 * 
	 * @param context
	 *            The current interpreter context.
	 * @return Cancellable task that can be run by the interpreter view to compute the results of a given
	 *         evaluation. Cannot be <code>null</code>.
	 */
	public abstract Callable<EvaluationResult> getEvaluationTask(EvaluationContext context);

	/**
	 * This will be called whenever a new editor is given focus while the "link with editor" toggle is
	 * enabled. The language interpreter can react to these changes, or ignore the event altogether.
	 * <p>
	 * When the "link with editor" toggle is disabled, this will be called with <code>null</code> as the
	 * <code>editorPart</code>.
	 * </p>
	 * 
	 * @param editorPart
	 *            The editor part that has been given focus. Can be <code>null</code> when the last editor is
	 *            closed or when the toggle is disabled.
	 */
	public void linkWithEditor(IEditorPart editorPart) {
		// Do nothing
	}
}