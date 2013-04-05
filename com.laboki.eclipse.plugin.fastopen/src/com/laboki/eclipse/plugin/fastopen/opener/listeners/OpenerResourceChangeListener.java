package com.laboki.eclipse.plugin.fastopen.opener.listeners;

import lombok.ToString;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

@ToString
public final class OpenerResourceChangeListener extends AbstractOpenerListener implements IResourceChangeListener {

	private final IResourceDeltaVisitor handler;
	private final IWorkspace workspace = ResourcesPlugin.getWorkspace();

	public OpenerResourceChangeListener(final IResourceDeltaVisitor handler) {
		this.handler = handler;
	}

	@Override
	public void add() {
		this.workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void remove() {
		this.workspace.removeResourceChangeListener(this);
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		this.findChangedResource(event);
	}

	private void findChangedResource(final IResourceChangeEvent event) {
		if (event.getType() != IResourceChangeEvent.POST_CHANGE) return;
		this.tryToFindResourceDeltaChanges(event);
	}

	private void tryToFindResourceDeltaChanges(final IResourceChangeEvent event) {
		try {
			event.getDelta().accept(this.handler);
		} catch (final CoreException e) {}
	}
}
