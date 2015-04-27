package com.laboki.eclipse.plugin.fastopen.resources;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesMapEvent;
import com.laboki.eclipse.plugin.fastopen.events.IndexResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.WorkspaceResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.instance.AbstractEventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.listeners.OpenerResourceChangeListener;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;

public final class FileResources extends AbstractEventBusInstance
	implements
		IResourceDeltaVisitor {

	private final OpenerResourceChangeListener listener =
		new OpenerResourceChangeListener(this);

	@Subscribe
	@AllowConcurrentEvents
	public static void
	worskpaceResources(final WorkspaceResourcesEvent event) {
		EditorContext.cancelJobsBelongingTo(EditorContext.INDEX_RESOURCES_TASK);
		FileResources.indexResources(event);
	}

	private static void
	indexResources(final WorkspaceResourcesEvent event) {
		new Task(EditorContext.INDEX_RESOURCES_TASK, 1000) {

			private final Map<String, IFile> fileResourcesMap = Maps.newHashMap();
			private final List<String> modifiedFiles = Lists.newArrayList();
			ImmutableList<IFile> resources = event.getResources();

			@Override
			public void
			execute() {
				this.buildFileResourcesMap();
				this.buildModifiedFilesList();
				this.postEvents();
			}

			private void
			buildFileResourcesMap() {
				for (final IFile file : this.resources)
					this.fileResourcesMap.put(EditorContext.getURIPath(file), file);
			}

			private void
			buildModifiedFilesList() {
				for (final IFile file : this.resources)
					this.modifiedFiles.add(EditorContext.getURIPath(file));
			}

			private void
			postEvents() {
				EventBus.post(new FileResourcesMapEvent(ImmutableMap
					.copyOf(this.fileResourcesMap)));
				EventBus.post(new ModifiedFilesEvent(ImmutableList
					.copyOf(this.modifiedFiles)));
			}
		}.begin();
	}

	@Override
	public boolean
	visit(final IResourceDelta delta) throws CoreException {
		if (FileResources.isAddedOrRemoved(delta.getKind())) FileResources
			.indexResources();
		return true;
	}

	private static boolean
	isAddedOrRemoved(final int kind) {
		return (kind == IResourceDelta.ADDED) || (kind == IResourceDelta.REMOVED);
	}

	private static void
	indexResources() {
		EditorContext.cancelAllJobs();
		FileResources.emitIndexResource();
	}

	private static void
	emitIndexResource() {
		new Task(EditorContext.EMIT_INDEX_RESOURCE_TASK, 1000) {

			@Override
			public void
			execute() {
				EventBus.post(new IndexResourcesEvent());
			}
		}.begin();
	}

	@Override
	public Instance
	begin() {
		this.listener.start();
		return super.begin();
	}

	@Override
	public Instance
	end() {
		this.listener.stop();
		return super.end();
	}
}
