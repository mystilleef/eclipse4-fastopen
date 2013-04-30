package com.laboki.eclipse.plugin.fastopen.opener.resources;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Instance;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;
import com.laboki.eclipse.plugin.fastopen.opener.events.FileResourcesMapEvent;
import com.laboki.eclipse.plugin.fastopen.opener.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.events.WorkspaceResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.listeners.OpenerResourceChangeListener;

public final class FileResources implements IResourceDeltaVisitor, Instance {

	private final Map<String, IFile> fileResourcesMap = Maps.newHashMap();
	private final List<String> modifiedFiles = Lists.newArrayList();
	private final OpenerResourceChangeListener listener = new OpenerResourceChangeListener(this);

	@Subscribe
	@AllowConcurrentEvents
	public void worskpaceResources(final WorkspaceResourcesEvent event) {
		new Task("fastopen file resource updater task") {

			ImmutableList<IFile> resources = event.getResources();

			@Override
			public void execute() {
				this.buildFileResourcesMap();
				this.updateModifiedFiles();
			}

			private void buildFileResourcesMap() {
				FileResources.this.fileResourcesMap.putAll(this.buildMapFromResources());
			}

			private Map<String, IFile> buildMapFromResources() {
				return Maps.uniqueIndex(this.resources, new Function<IFile, String>() {

					@Override
					public String apply(final IFile file) {
						return EditorContext.getURIPath(file);
					}
				});
			}

			private void updateModifiedFiles() {
				FileResources.this.modifiedFiles.clear();
				FileResources.this.modifiedFiles.addAll(this.getPathsFromResources());
			}

			private List<String> getPathsFromResources() {
				return Lists.transform(this.resources, new Function<IFile, String>() {

					@Override
					public String apply(final IFile file) {
						return EditorContext.getURIPath(file);
					}
				});
			}

			@Override
			public void postExecute() {
				FileResources.this.postEvents();
			}
		}.begin();
	}

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException {
		switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				this.addResource(delta.getResource());
				break;
			case IResourceDelta.REMOVED:
				this.removeResource(delta.getResource());
				break;
			default:
				break;
		}
		return true;
	}

	private synchronized void addResource(final IResource file) {
		if ((file == null) || EditorContext.isNotValidResourceFile(file)) return;
		final String filepath = EditorContext.getURIPath(file);
		this.fileResourcesMap.put(filepath, (IFile) file);
		this.modifiedFiles.remove(filepath);
		this.modifiedFiles.add(0, filepath);
		this.postEvents();
	}

	private synchronized void removeResource(final IResource file) {
		if (file == null) return;
		final String filePath = EditorContext.getURIPath(file);
		this.fileResourcesMap.remove(filePath);
		this.modifiedFiles.remove(filePath);
		this.postEvents();
	}

	private void postEvents() {
		EventBus.post(new FileResourcesMapEvent(ImmutableMap.copyOf(this.fileResourcesMap)));
		EventBus.post(new ModifiedFilesEvent(ImmutableList.copyOf(this.modifiedFiles)));
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
		this.fileResourcesMap.clear();
		this.modifiedFiles.clear();
		this.listener.stop();
		return this;
	}
}
