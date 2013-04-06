package com.laboki.eclipse.plugin.fastopen.opener;

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

public final class RecentResources {

	private ImmutableMap<String, IFile> fileResourcesMap;
	private final Map<String, File> cachedResourceFiles = Maps.newHashMap();
	private final List<File> fileResources = Lists.newArrayList();

	@Synchronized("fileResources")
	protected ArrayList<File> getFileResources() {
		return Lists.newArrayList(this.fileResources);
	}

	@Synchronized("fileResources")
	protected void updateFileResources(final List<File> files) {
		this.fileResources.clear();
		this.fileResources.addAll(files);
	}

	@Synchronized("cachedResourceFiles")
	protected List<File> makeResourceFiles(final ImmutableList<String> immutableList) {
		final ArrayList<File> files = new ArrayList<>();
		for (final String filePath : immutableList)
			this.updateLocalFilesList(files, filePath);
		return files;
	}

	private void updateLocalFilesList(final ArrayList<File> files, final String filePath) {
		if (this.cachedResourceFiles.containsKey(filePath)) files.add(this.cachedResourceFiles.get(filePath));
		else this.addNewResourceFiles(files, filePath);
	}

	private void addNewResourceFiles(final ArrayList<File> files, final String filePath) {
		if (!this.fileResourcesMap.containsKey(filePath)) return;
		final File file = new File(this.fileResourcesMap.get(filePath));
		this.cachedResourceFiles.put(filePath, file);
		files.add(file);
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

			private final List<File> files = Lists.newArrayList();

			@Override
			public void execute() {
				this.update(event);
			}

			private void update(final RecentFilesEvent event) {
				this.files.addAll(RecentResources.this.makeResourceFiles(event.getFiles()));
				RecentResources.this.updateFileResources(this.files);
				EventBus.post(new FileResourcesEvent(ImmutableList.copyOf(RecentResources.this.getFileResources())));
			}
		});
	}
}
