package com.laboki.eclipse.plugin.fastopen.listeners;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class OpenerResourceChangeListener extends AbstractOpenerListener
	implements
		IResourceChangeListener {

	private static final TaskMutexRule RULE = new TaskMutexRule();
	private static final String FAMILY =
		"Eclipse Fast Open Plugin: find changed resource task";
	private final IResourceDeltaVisitor handler;
	private final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private final static Logger LOGGER = Logger
		.getLogger(OpenerResourceChangeListener.class.getName());

	public OpenerResourceChangeListener(final IResourceDeltaVisitor handler) {
		this.handler = handler;
	}

	@Override
	public void
	add() {
		this.workspace
			.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void
	remove() {
		this.workspace.removeResourceChangeListener(this);
	}

	@Override
	public void
	resourceChanged(final IResourceChangeEvent event) {
		EditorContext.cancelJobsBelongingTo(OpenerResourceChangeListener.FAMILY);
		this.findResources(event);
	}

	private void
	findResources(final IResourceChangeEvent event) {
		new Task() {

			@Override
			public void
			execute() {
				if (event.getType() != IResourceChangeEvent.POST_CHANGE) return;
				this.findResourceDeltaChanges(event.getDelta());
			}

			private void
			findResourceDeltaChanges(final IResourceDelta delta) {
				try {
					delta.accept(OpenerResourceChangeListener.this.handler);
				}
				catch (final CoreException e) {
					OpenerResourceChangeListener.LOGGER
						.log(Level.WARNING, "Failed to find resource delta changes", e);
				}
			}
		}.setRule(OpenerResourceChangeListener.RULE)
			.setFamily(OpenerResourceChangeListener.FAMILY)
			.setDelay(1000)
			.start();
	}
}
