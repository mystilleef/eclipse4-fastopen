package com.laboki.eclipse.plugin.fastopen.opener.files;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Instance;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.opener.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.events.RecentFilesEvent;

public final class RecentFiles implements Instance {

	private final List<String> recentFiles = Lists.newArrayList();

	@Subscribe
	@AllowConcurrentEvents
	public void postModifiedUpdatedRecentFiles(final ModifiedFilesEvent event) {
		new Task() {

			@Override
			public void execute() {
				RecentFiles.this.updateRecentFiles(event.getFiles());
			}

			@Override
			public void postExecute() {
				RecentFiles.this.postRecentFilesEvent();
			};
		}.begin();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void postAccessedUpdatedRecentFiles(final AccessedFilesEvent event) {
		new Task() {

			private final List<String> rfiles = Lists.newArrayList(RecentFiles.this.getRecentFiles());

			@Override
			public void execute() {
				this.mergeAccessedAndRecentFiles(event.getFiles());
			}

			private void mergeAccessedAndRecentFiles(final ImmutableList<String> files) {
				RecentFiles.this.updateRecentFiles(files);
			}

			@SuppressWarnings("unused")
			private void update(final ImmutableList<String> files) {
				this.rfiles.removeAll(files);
				this.rfiles.addAll(0, files);
			}

			@Override
			public void postExecute() {
				RecentFiles.this.postRecentFilesEvent();
			}
		}.begin();
	}

	private synchronized void updateRecentFiles(final ImmutableList<String> files) {
		this.recentFiles.removeAll(files);
		this.recentFiles.addAll(0, files);
		this.recentFiles.remove("");
	}

	private void postRecentFilesEvent() {
		EventBus.post(new RecentFilesEvent(this.getRecentFiles()));
	}

	private synchronized ImmutableList<String> getRecentFiles() {
		return ImmutableList.copyOf(this.recentFiles);
	}

	@Override
	public Instance begin() {
		EventBus.register(this);
		return this;
	}

	@Override
	public Instance end() {
		EventBus.unregister(this);
		this.recentFiles.clear();
		return this;
	}
}
