package com.laboki.eclipse.plugin.fastopen.files;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.IndexFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.WorkspaceFilesEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class FilesIndexer extends EventBusInstance
	implements
		IResourceVisitor,
		Comparator<IFile> {

	private static final String FAMILY = "FastOpen: FilesIndexer task family";
	private static final TaskMutexRule RULE = new TaskMutexRule();
	protected final List<IFile> files = Lists.newArrayList();
	protected final IWorkspaceRoot root = FilesIndexer.getRootWorkspace();
	protected final static Logger LOGGER =
		Logger.getLogger(FilesIndexer.class.getName());

	@Override
	public Instance
	start() {
		this.indexfiles();
		return super.start();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void
	eventHandler(final IndexFilesEvent event) {
		EditorContext.cancelJobsBelongingTo(FilesIndexer.FAMILY);
		this.indexfiles();
	}

	protected void
	indexfiles() {
		new Task() {

			@Override
			public void
			execute() {
				this.removeFiles();
				this.updateFiles();
				this.sortFiles();
				this.broadcastEvent();
			}

			private void
			removeFiles() {
				FilesIndexer.this.files.clear();
			}

			private void
			updateFiles() {
				try {
					FilesIndexer.this.root.accept(FilesIndexer.this);
				}
				catch (final CoreException e) {
					FilesIndexer.LOGGER.log(Level.WARNING, e.getMessage(), e);
				}
			}

			private void
			sortFiles() {
				Collections.sort(FilesIndexer.this.files, FilesIndexer.this);
			}

			private void
			broadcastEvent() {
				EventBus.post(this.createEvent());
			}

			private WorkspaceFilesEvent
			createEvent() {
				final ImmutableList<IFile> files =
					ImmutableSet.copyOf(FilesIndexer.this.files).asList();
				return new WorkspaceFilesEvent(files);
			}
		}.setFamily(FilesIndexer.FAMILY).setRule(FilesIndexer.RULE).start();
	}

	@Override
	public boolean
	visit(final IResource resource) throws CoreException {
		this.updateFiles(resource);
		return true;
	}

	private void
	updateFiles(final IResource resource) {
		if (FilesIndexer.isInvalid(resource)) return;
		this.files.add((IFile) resource);
	}

	private static boolean
	isInvalid(final IResource resource) {
		return !FilesIndexer.isValid(resource);
	}

	private static boolean
	isValid(final IResource resource) {
		if (FilesIndexer.doesNotExist(resource)) return false;
		if (FilesIndexer.isNotFile(resource)) return false;
		return EditorContext.isTextFile((IFile) resource);
	}

	private static boolean
	doesNotExist(final IResource resource) {
		return !resource.exists();
	}

	private static boolean
	isNotFile(final IResource resource) {
		return resource.getType() != IResource.FILE;
	}

	@Override
	public int
	compare(final IFile o1, final IFile o2) {
		final long fmod = o1.getLocation().toFile().lastModified();
		final long smod = o2.getLocation().toFile().lastModified();
		return fmod < smod ? 1 : (fmod > smod ? -1 : 0);
	}

	private static IWorkspaceRoot
	getRootWorkspace() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public Instance
	stop() {
		EditorContext.cancelJobsBelongingTo(FilesIndexer.FAMILY);
		this.files.clear();
		return super.stop();
	}
}
