package com.laboki.eclipse.plugin.fastopen.opener.resources;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.events.WorkspaceResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class WorkspaceResources implements IResourceVisitor, Comparator<IFile> {

	private final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private final IWorkspaceRoot root = this.workspace.getRoot();
	private final List<IFile> resources = Lists.newArrayList();

	public WorkspaceResources() {
		this.updateFilesFromWorkspace();
		this.sortFilesByModificationTime();
		this.postEvent();
	}

	private void updateFilesFromWorkspace() {
		try {
			this.root.accept(this);
		} catch (final CoreException e) {}
	}

	private void sortFilesByModificationTime() {
		Collections.sort(this.resources, this);
	}

	@Override
	public boolean visit(final IResource resource) throws CoreException {
		this.updateFiles(resource);
		return true;
	}

	private void updateFiles(final IResource resource) {
		if (EditorContext.isValidResourceFile(resource)) this.resources.add((IFile) resource);
	}

	@Override
	public int compare(final IFile o1, final IFile o2) {
		final long lastModified = o1.getLocation().toFile().lastModified();
		final long lastModified2 = o2.getLocation().toFile().lastModified();
		return lastModified < lastModified2 ? 1 : (lastModified > lastModified2 ? -1 : 0);
	}

	private void postEvent() {
		EventBus.post(new WorkspaceResourcesEvent(ImmutableList.copyOf(this.resources)));
	}
}
