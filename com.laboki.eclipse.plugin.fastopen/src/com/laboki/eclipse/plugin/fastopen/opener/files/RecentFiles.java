package com.laboki.eclipse.plugin.fastopen.opener.files;

import java.util.ArrayList;
import java.util.List;

import lombok.Synchronized;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.DelayedTask;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.RecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class RecentFiles {

	private final List<String> recentFiles = Lists.newArrayList();

	@Synchronized("recentFiles")
	protected ArrayList<String> getRecentFiles() {
		return Lists.newArrayList(this.recentFiles);
	}

	@Synchronized("recentFiles")
	protected void updateRecentFiles(final List<String> files) {
		this.recentFiles.clear();
		this.recentFiles.addAll(files);
	}

	protected RecentFilesEvent emitFileEvent() {
		return new RecentFilesEvent(ImmutableList.copyOf(this.recentFiles));
	}

	@Subscribe
	@AllowConcurrentEvents
	public void modifiedFilesChanged(final ModifiedFilesEvent event) {
		EditorContext.asyncExec(new Task("") {

			private final List<String> rfiles = RecentFiles.this.getRecentFiles();

			@Override
			public void execute() {
				this.mergeModifiedAndRecentFiles(event.getFiles());
			}

			private void mergeModifiedAndRecentFiles(final ImmutableList<String> files) {
				this.update(files);
				RecentFiles.this.updateRecentFiles(this.rfiles);
				EventBus.post(RecentFiles.this.emitFileEvent());
			}

			private void update(final ImmutableList<String> files) {
				this.rfiles.removeAll(files);
				this.rfiles.addAll(files);
			}
		});
	}

	@Subscribe
	@AllowConcurrentEvents
	public void accessedFilesChanged(final AccessedFilesEvent event) {
		EditorContext.asyncExec(new DelayedTask("", 50) {

			private final List<String> rfiles = RecentFiles.this.getRecentFiles();

			@Override
			public void execute() {
				this.mergeAccessedAndRecentFiles(event.getFiles());
			}

			private void mergeAccessedAndRecentFiles(final ImmutableList<String> files) {
				this.update(files);
				RecentFiles.this.updateRecentFiles(this.rfiles);
				EventBus.post(RecentFiles.this.emitFileEvent());
			}

			private void update(final ImmutableList<String> files) {
				this.rfiles.removeAll(files);
				this.rfiles.addAll(0, files);
			}
		});
	}
}
