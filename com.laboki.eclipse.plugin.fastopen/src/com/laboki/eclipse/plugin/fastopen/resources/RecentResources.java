package com.laboki.eclipse.plugin.fastopen.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesMapEvent;
import com.laboki.eclipse.plugin.fastopen.events.RecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.instance.AbstractEventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;

public final class RecentResources extends AbstractEventBusInstance {

	private static final String EMIT_FIRLE_RESOURCES_EVENT_TASK = "Eclipse Fast Open Plugin: emit file resources event task";
	private static final String UPDATE_RESOURCES_TASK = "Eclipse Fast Open Plugin: update resources task";
	private final Map<String, IFile> fileResourcesMap = Maps.newHashMap();
	private final Map<String, RFile> cachedResourceFiles = Maps.newHashMap();
	private final List<RFile> fileResources = Lists.newArrayList();

	@Subscribe
	@AllowConcurrentEvents
	public void fileResourcesMapEventHandler(final FileResourcesMapEvent event) {
		EditorContext.cancelJobsBelongingTo(RecentResources.UPDATE_RESOURCES_TASK);
		this.updateResources(event);
	}

	private void updateResources(final FileResourcesMapEvent event) {
		new Task(RecentResources.UPDATE_RESOURCES_TASK, 60) {

			@Override
			public void execute() {
				RecentResources.this.clearResources();
				RecentResources.this.updateFileResourcesMap(event.getMap());
				RecentResources.this.makeResourceFiles(ImmutableList.copyOf(event.getMap().keySet()));
			}
		}.begin();
	}

	private void clearResources() {
		this.fileResourcesMap.clear();
		this.cachedResourceFiles.clear();
		this.fileResources.clear();
	}

	private void updateFileResourcesMap(final ImmutableMap<String, IFile> map) {
		this.fileResourcesMap.putAll(map);
	}

	@Subscribe
	@AllowConcurrentEvents
	public void recentFilesEventHandler(final RecentFilesEvent event) {
		EditorContext.cancelJobsBelongingTo(RecentResources.EMIT_FIRLE_RESOURCES_EVENT_TASK);
		this.emitFileResourcesEvent(event);
	}

	private void emitFileResourcesEvent(final RecentFilesEvent event) {
		new Task(RecentResources.EMIT_FIRLE_RESOURCES_EVENT_TASK, 60) {

			private final List<RFile> rFiles = Lists.newArrayList();

			@Override
			public void execute() {
				this.update(event);
				this.postFileResourcesEvent();
			}

			private void update(final RecentFilesEvent event) {
				this.rFiles.addAll(RecentResources.this.makeResourceFiles(event.getFiles()));
				RecentResources.this.updateFileResources(this.rFiles);
			}

			private void postFileResourcesEvent() {
				EventBus.post(new FileResourcesEvent(ImmutableList.copyOf(RecentResources.this.getFileResources())));
			}
		}.begin();
	}

	private synchronized List<RFile> makeResourceFiles(final ImmutableList<String> immutableList) {
		final ArrayList<RFile> rFiles = Lists.newArrayList();
		for (final String filePath : immutableList)
			this.updateLocalFilesList(rFiles, filePath);
		return rFiles;
	}

	private void updateLocalFilesList(final ArrayList<RFile> rFiles, final String filePath) {
		if (this.cachedResourceFiles.containsKey(filePath)) rFiles.add(this.cachedResourceFiles.get(filePath));
		else this.addNewResourceFiles(rFiles, filePath);
	}

	private void addNewResourceFiles(final ArrayList<RFile> rFiles, final String filePath) {
		if (this.filePathIsNotCached(filePath)) return;
		final RFile rFile = new RFile(this.fileResourcesMap.get(filePath));
		this.cachedResourceFiles.put(filePath, rFile);
		rFiles.add(rFile);
	}

	private boolean filePathIsNotCached(final String filePath) {
		return !this.filePathIsCached(filePath);
	}

	private boolean filePathIsCached(final String filePath) {
		return this.fileResourcesMap.containsKey(filePath);
	}

	protected synchronized ArrayList<RFile> getFileResources() {
		return Lists.newArrayList(this.fileResources);
	}

	private synchronized void updateFileResources(final List<RFile> rFiles) {
		this.fileResources.clear();
		this.fileResources.addAll(rFiles);
	}

	@Override
	public Instance end() {
		this.fileResources.clear();
		this.cachedResourceFiles.clear();
		this.fileResourcesMap.clear();
		return super.end();
	}
}
