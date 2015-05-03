package com.laboki.eclipse.plugin.fastopen.resources;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesMapEvent;
import com.laboki.eclipse.plugin.fastopen.events.IndexFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.WorkspaceFilesEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.listeners.OpenerResourceChangeListener;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class FileResources extends EventBusInstance
	implements
		IResourceDeltaVisitor {

	private static final TaskMutexRule RULE = new TaskMutexRule();
	private final OpenerResourceChangeListener listener =
		new OpenerResourceChangeListener(this);

	@Subscribe
	@AllowConcurrentEvents
	public static void
	eventHandler(final WorkspaceFilesEvent event) {
		EditorContext.cancelJobsBelongingTo(EditorContext.INDEX_RESOURCES_TASK);
		FileResources.indexResources(event.getResources());
	}

	private static void
	indexResources(final ImmutableList<IFile> resources) {
		new Task() {

			private final Map<String, IFile> fileResourcesMap = Maps.newHashMap();
			private final List<String> modifiedFiles = Lists.newArrayList();

			@Override
			public void
			execute() {
				this.buildFileResourcesMap();
				this.buildModifiedFilesList();
				this.postEvents();
			}

			private void
			buildFileResourcesMap() {
				for (final IFile file : resources) {
					final Optional<String> path = EditorContext.getURIPath(file);
					if (!path.isPresent()) continue;
					this.fileResourcesMap.put(path.get(), file);
				}
			}

			private void
			buildModifiedFilesList() {
				for (final IFile file : resources) {
					final Optional<String> path = EditorContext.getURIPath(file);
					if (!path.isPresent()) continue;
					this.modifiedFiles.add(path.get());
				}
			}

			private void
			postEvents() {
				EventBus.post(new FileResourcesMapEvent(ImmutableMap
					.copyOf(this.fileResourcesMap)));
				EventBus.post(new ModifiedFilesEvent(ImmutableList
					.copyOf(this.modifiedFiles)));
			}
		}.setRule(FileResources.RULE)
			.setFamily(EditorContext.INDEX_RESOURCES_TASK)
			.setDelay(250)
			.start();
	}

	@Override
	public boolean
	visit(final IResourceDelta delta) throws CoreException {
		if (FileResources.isDelta(delta.getKind())) FileResources.indexResources();
		return true;
	}

	private static boolean
	isDelta(final int kind) {
		return (kind == IResourceDelta.ADDED) || (kind == IResourceDelta.REMOVED);
	}

	private static void
	indexResources() {
		EditorContext.cancelAllJobs();
		FileResources.emitIndexResource();
	}

	private static void
	emitIndexResource() {
		new Task() {

			@Override
			public void
			execute() {
				EventBus.post(new IndexFilesEvent());
			}
		}.setRule(FileResources.RULE)
			.setFamily(EditorContext.EMIT_INDEX_RESOURCE_TASK)
			.setDelay(250)
			.start();
	}

	@Override
	public Instance
	start() {
		this.listener.start();
		return super.start();
	}

	@Override
	public Instance
	stop() {
		this.listener.stop();
		return super.stop();
	}
}
