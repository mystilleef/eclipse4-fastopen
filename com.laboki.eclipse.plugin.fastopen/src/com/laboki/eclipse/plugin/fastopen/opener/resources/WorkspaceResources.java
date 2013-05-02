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
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Instance;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;
import com.laboki.eclipse.plugin.fastopen.opener.events.IndexResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.events.WorkspaceResourcesEvent;

public final class WorkspaceResources implements IResourceVisitor, Comparator<IFile>, Instance {

	private final List<IFile> resources = Lists.newArrayList();

	@Override
	public Instance begin() {
		EventBus.register(this);
		this.indexResources();
		return this;
	}

	@Subscribe
	@AllowConcurrentEvents
	public void indexResources(@SuppressWarnings("unused") final IndexResourcesEvent event) {
		new Task("FASTOPEN_INDEX_RESOURCES", 250) {

			@Override
			public void execute() {
				WorkspaceResources.this.indexResources();
			}
		}.begin();
	}

	private synchronized void indexResources() {
		new Task() {

			private final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			private final IWorkspaceRoot root = this.workspace.getRoot();

			@Override
			public void execute() {
				WorkspaceResources.this.resources.clear();
				this.updateFilesFromWorkspace();
				this.sortFilesByModificationTime();
			}

			private void updateFilesFromWorkspace() {
				try {
					this.root.accept(WorkspaceResources.this);
				} catch (final Exception e) {
					// Do nothing.
				}
			}

			private void sortFilesByModificationTime() {
				Collections.sort(WorkspaceResources.this.resources, WorkspaceResources.this);
			}

			@Override
			public void postExecute() {
				this.postEvent();
			}

			private void postEvent() {
				EventBus.post(new WorkspaceResourcesEvent(ImmutableList.copyOf(WorkspaceResources.this.resources)));
			}
		}.begin();
	}

	@Override
	public Instance end() {
		EventBus.unregister(this);
		this.resources.clear();
		return this;
	}

	@Override
	public boolean visit(final IResource resource) throws CoreException {
		if (EditorContext.isHiddenFile(resource) || EditorContext.isWierd(resource)) return false;
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
}
