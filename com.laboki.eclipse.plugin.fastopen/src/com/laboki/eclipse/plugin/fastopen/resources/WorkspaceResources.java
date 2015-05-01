package com.laboki.eclipse.plugin.fastopen.resources;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.laboki.eclipse.plugin.fastopen.events.IndexResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.events.WorkspaceResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class WorkspaceResources extends EventBusInstance
	implements
		IResourceVisitor,
		Comparator<IFile> {

	private static final TaskMutexRule RULE = new TaskMutexRule();
	protected final List<IFile> resources = Lists.newArrayList();
	protected final static Logger LOGGER = Logger
		.getLogger(WorkspaceResources.class.getName());

	@Override
	public Instance
	start() {
		this.indexResources();
		return super.start();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void
	indexResources(final IndexResourcesEvent event) {
		new Task() {

			@Override
			public void
			execute() {
				EditorContext
					.cancelJobsBelongingTo(EditorContext.CORE_WORKSPACE_INDEXER_TASK);
				WorkspaceResources.this.indexResources();
			}
		}.setFamily(EditorContext.INDEX_WORKSPACE_RESOURCES_TASK)
			.setDelay(250)
			.setRule(WorkspaceResources.RULE)
			.start();
	}

	protected void
	indexResources() {
		new Task() {

			private final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			private final IWorkspaceRoot root = this.workspace.getRoot();

			@Override
			public void
			execute() {
				WorkspaceResources.this.resources.clear();
				this.updateFilesFromWorkspace();
				this.sortFilesByModificationTime();
				this.postEvent();
			}

			private void
			updateFilesFromWorkspace() {
				try {
					this.root.accept(WorkspaceResources.this);
				}
				catch (final Exception e) {
					WorkspaceResources.LOGGER.log(Level.WARNING, e.getMessage(), e);
				}
			}

			private void
			sortFilesByModificationTime() {
				try {
					Collections
						.sort(WorkspaceResources.this.resources, WorkspaceResources.this);
				}
				catch (final Exception e) {
					WorkspaceResources.LOGGER.log(Level.WARNING, e.getMessage(), e);
				}
			}

			private void
			postEvent() {
				EventBus.post(new WorkspaceResourcesEvent(ImmutableList
					.copyOf(WorkspaceResources.this.resources)));
			}
		}.setFamily(EditorContext.CORE_WORKSPACE_INDEXER_TASK)
			.setRule(WorkspaceResources.RULE)
			.start();
	}

	@Override
	public Instance
	stop() {
		this.resources.clear();
		return super.stop();
	}

	@Override
	public boolean
	visit(final IResource resource) throws CoreException {
		if (EditorContext.isHiddenFile(resource)
			|| EditorContext.isWierd(resource)) return false;
		this.updateFiles(resource);
		return true;
	}

	private void
	updateFiles(final IResource resource) {
		if (EditorContext.isValidResourceFile(resource)) this.resources
			.add((IFile) resource);
	}

	@Override
	public int
	compare(final IFile o1, final IFile o2) {
		final long lastModified = o1.getLocation().toFile().lastModified();
		final long lastModified2 = o2.getLocation().toFile().lastModified();
		return lastModified < lastModified2 ? 1 : (lastModified > lastModified2
			? -1
			: 0);
	}
}
