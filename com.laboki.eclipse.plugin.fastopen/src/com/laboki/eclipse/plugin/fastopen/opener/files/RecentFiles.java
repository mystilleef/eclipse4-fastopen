package com.laboki.eclipse.plugin.fastopen.opener.files;

import java.util.Collections;
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
import com.laboki.eclipse.plugin.fastopen.opener.events.RecentFilesModificationEvent;

public final class RecentFiles implements Instance {

	private final List<Object> recentFiles = Collections.synchronizedList(Lists.newArrayList());

	@Subscribe
	@AllowConcurrentEvents
	public void postModifiedUpdatedRecentFiles(final ModifiedFilesEvent event) {
		new Task() {

			@Override
			public void execute() {
				this.resetRecentFiles(event.getFiles());
			}

			private void resetRecentFiles(final ImmutableList<Object> immutableList) {
				synchronized (RecentFiles.this.recentFiles) {
					this.update(immutableList);
				}
			}

			private void update(final ImmutableList<Object> immutableList) {
				RecentFiles.this.recentFiles.clear();
				RecentFiles.this.recentFiles.addAll(immutableList);
				RecentFiles.this.recentFiles.remove("");
			}

			@Override
			public void postExecute() {
				EventBus.post(new RecentFilesModificationEvent(RecentFiles.this.getRecentFiles()));
			};
		}.begin();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void postAccessedUpdatedRecentFiles(final AccessedFilesEvent event) {
		new Task() {

			private final ImmutableList<Object> files = event.getFiles();

			@Override
			public void execute() {
				this.mergeAccessedAndRecentFiles();
			}

			private void mergeAccessedAndRecentFiles() {
				synchronized (RecentFiles.this.recentFiles) {
					this.update(this.files);
				}
			}

			private void update(final ImmutableList<Object> files) {
				RecentFiles.this.recentFiles.removeAll(files);
				RecentFiles.this.recentFiles.addAll(0, files);
				RecentFiles.this.recentFiles.remove("");
			}

			@Override
			public void postExecute() {
				EventBus.post(new RecentFilesEvent(RecentFiles.this.getRecentFiles()));
			}
		}.begin();
	}

	private synchronized ImmutableList<Object> getRecentFiles() {
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
