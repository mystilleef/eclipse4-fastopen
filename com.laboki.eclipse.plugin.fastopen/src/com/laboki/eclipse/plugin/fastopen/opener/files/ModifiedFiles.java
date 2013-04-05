package com.laboki.eclipse.plugin.fastopen.opener.files;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;
import com.laboki.eclipse.plugin.fastopen.opener.listeners.OpenerResourceChangeListener;

public final class ModifiedFiles implements IResourceDeltaVisitor, Runnable {

	private final List<String> modifiedFiles;
	private final OpenerResourceChangeListener listener = new OpenerResourceChangeListener(this);

	@Inject
	public ModifiedFiles(final WorkspaceFiles workspaceFiles) {
		this.modifiedFiles = workspaceFiles.getFilePaths();
		this.listener.start();
		EventBus.post(this.emitFileEvent());
	}

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException {
		final IResource resource = delta.getResource();
		if (EditorContext.isValid(resource)) this.updateModifiedFiles(delta, resource);
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
		EventBus.post(this.emitFileEvent());
	}

	private void remove(final IResource resource) {
		this.modifiedFiles.remove(EditorContext.getURIPath(resource));
		EventBus.post(this.emitFileEvent());
	}

	@Override
	public void run() {
		EventBus.post(this.emitFileEvent());
	}

	protected ModifiedFilesEvent emitFileEvent() {
		this.removeFakePaths();
		return new ModifiedFilesEvent(ImmutableList.copyOf(this.modifiedFiles));
	}

	private void removeFakePaths() {
		this.modifiedFiles.removeAll(EditorContext.nonExistentFilePaths(this.modifiedFiles));
	}
}
