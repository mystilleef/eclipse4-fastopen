package com.laboki.eclipse.plugin.fastopen.main.resources;

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
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Instance;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.events.FileResourcesMapEvent;
import com.laboki.eclipse.plugin.fastopen.main.events.IndexResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.main.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.main.events.WorkspaceResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.main.listeners.OpenerResourceChangeListener;

public final class FileResources implements IResourceDeltaVisitor, Instance {

	private final OpenerResourceChangeListener listener = new OpenerResourceChangeListener(this);

	@Subscribe
	@AllowConcurrentEvents
	public static void worskpaceResources(final WorkspaceResourcesEvent event) {
		new Task("FASTOPEN_INDEX_RESOURCES", 250) {

			private final Map<String, IFile> fileResourcesMap = Maps.newHashMap();
			private final List<String> modifiedFiles = Lists.newArrayList();
			ImmutableList<IFile> resources = event.getResources();

			@Override
			public void execute() {
				this.buildFileResourcesMap();
				this.buildModifiedFilesList();
			}

			private void buildFileResourcesMap() {
				this.fileResourcesMap.clear();
				for (final IFile file : this.resources)
					this.fileResourcesMap.put(EditorContext.getURIPath(file), file);
			}

			private void buildModifiedFilesList() {
				this.modifiedFiles.clear();
				for (final IFile file : this.resources)
					this.modifiedFiles.add(EditorContext.getURIPath(file));
			}

			@Override
			public void postExec() {
				EventBus.post(new FileResourcesMapEvent(ImmutableMap.copyOf(this.fileResourcesMap)));
				EventBus.post(new ModifiedFilesEvent(ImmutableList.copyOf(this.modifiedFiles)));
			}
		}.begin();
	}

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException {
		switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				FileResources.indexResource();
				break;
			case IResourceDelta.REMOVED:
				FileResources.indexResource();
				break;
			default:
				break;
		}
		return true;
	}

	private static void indexResource() {
		EditorContext.asyncExec(new Task("FASTOPEN_INDEX_RESOURCES", 1000) {

			@Override
			public void execute() {
				EditorContext.cancelJobsBelongingTo("FASTOPEN_INDEX_RESOURCES");
				EventBus.post(new IndexResourcesEvent());
			}
		});
	}

	@Override
	public Instance begin() {
		EventBus.register(this);
		this.listener.start();
		return this;
	}

	@Override
	public Instance end() {
		EventBus.unregister(this);
		this.listener.stop();
		return this;
	}
}
