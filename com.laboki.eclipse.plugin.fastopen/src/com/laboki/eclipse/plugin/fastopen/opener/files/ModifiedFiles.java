package com.laboki.eclipse.plugin.fastopen.opener.files;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.DelayedTask;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.WorkspaceFilesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;
import com.laboki.eclipse.plugin.fastopen.opener.listeners.OpenerResourceChangeListener;

public final class ModifiedFiles implements IResourceDeltaVisitor {

	private final List<String> modifiedFiles = Lists.newArrayList();
	private final OpenerResourceChangeListener listener = new OpenerResourceChangeListener(this);

	public ModifiedFiles() {
		this.listener.start();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void worskpaceFiles(final WorkspaceFilesEvent event) {
		EditorContext.asyncExec(new DelayedTask("", 50) {

			@Override
			public void execute() {
				ModifiedFiles.this.modifiedFiles.clear();
				ModifiedFiles.this.modifiedFiles.addAll(event.getFiles());
				ModifiedFiles.this.postEvent();
			}
		});
	}

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException {
		final IResource resource = delta.getResource();
		if (EditorContext.isValidResourceFile(resource)) this.updateModifiedFiles(delta, resource);
		return true;
	}

	private void updateModifiedFiles(final IResourceDelta delta, final IResource resource) {
		switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				this.update(resource);
				break;
			case IResourceDelta.REMOVED:
				this.remove(resource);
				break;
			default:
				break;
		}
	}

	private void update(final IResource resource) {
		final String filepath = EditorContext.getURIPath(resource);
		this.modifiedFiles.remove(filepath);
		this.modifiedFiles.add(0, filepath);
		this.postEvent();
	}

	private void remove(final IResource resource) {
		this.modifiedFiles.remove(EditorContext.getURIPath(resource));
		this.postEvent();
	}

	private void postEvent() {
		EditorContext.removeFakePaths(this.modifiedFiles);
		EventBus.post(new ModifiedFilesEvent(ImmutableList.copyOf(this.modifiedFiles)));
	}
}
