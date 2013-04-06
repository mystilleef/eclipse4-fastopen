package com.laboki.eclipse.plugin.fastopen.opener.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Synchronized;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesMapEvent;
import com.laboki.eclipse.plugin.fastopen.events.RecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class RecentResources {

	private ImmutableMap<String, IFile> fileResourcesMap;
	private final Map<String, RFile> cachedResourceFiles = Maps.newHashMap();
	private final List<RFile> fileResources = Lists.newArrayList();

	@Synchronized("fileResources")
	protected ArrayList<RFile> getFileResources() {
		return Lists.newArrayList(this.fileResources);
	}

	@Synchronized("fileResources")
	protected void updateFileResources(final List<RFile> rFiles) {
		this.fileResources.clear();
		this.fileResources.addAll(rFiles);
	}

	@Synchronized("cachedResourceFiles")
	protected List<RFile> makeResourceFiles(final ImmutableList<String> immutableList) {
		final ArrayList<RFile> rFiles = new ArrayList<>();
		for (final String filePath : immutableList)
			this.updateLocalFilesList(rFiles, filePath);
		return rFiles;
	}

	private void updateLocalFilesList(final ArrayList<RFile> rFiles, final String filePath) {
		if (this.cachedResourceFiles.containsKey(filePath)) rFiles.add(this.cachedResourceFiles.get(filePath));
		else this.addNewResourceFiles(rFiles, filePath);
	}

	private void addNewResourceFiles(final ArrayList<RFile> rFiles, final String filePath) {
		if (!this.fileResourcesMap.containsKey(filePath)) return;
		final RFile rFile = new RFile(this.fileResourcesMap.get(filePath));
		this.cachedResourceFiles.put(filePath, rFile);
		rFiles.add(rFile);
	}

	@Subscribe
	@AllowConcurrentEvents
	public void fileResourcesMap(final FileResourcesMapEvent event) {
		RecentResources.this.fileResourcesMap = event.getMap();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void updateResourceFiles(final RecentFilesEvent event) {
		EditorContext.asyncExec(new Task("") {

			private final List<RFile> rFiles = Lists.newArrayList();

			@Override
			public void execute() {
				this.update(event);
			}

			private void update(final RecentFilesEvent event) {
				this.rFiles.addAll(RecentResources.this.makeResourceFiles(event.getFiles()));
				RecentResources.this.updateFileResources(this.rFiles);
				EventBus.post(new FileResourcesEvent(ImmutableList.copyOf(RecentResources.this.getFileResources())));
			}
		});
	}
}
