package com.laboki.eclipse.plugin.fastopen.main.files;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Instance;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.main.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.main.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.main.events.RecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.main.events.RecentFilesModificationEvent;

public final class RecentFiles implements Instance {

	private final List<String> recentFiles = Lists.newArrayList();

	@Subscribe
	@AllowConcurrentEvents
	public void postModifiedRecentFiles(final ModifiedFilesEvent event) {
		new Task() {

			@Override
			public void execute() {
				this.resetRecentFiles(event.getFiles());
			}

			private void resetRecentFiles(final ImmutableList<String> files) {
				synchronized (RecentFiles.this.recentFiles) {
					this.reset(files);
				}
			}

			private void reset(final ImmutableList<String> files) {
				RecentFiles.this.recentFiles.clear();
				RecentFiles.this.recentFiles.addAll(files);
				RecentFiles.this.recentFiles.remove("");
			}

			@Override
			public void postExec() {
				EventBus.post(new RecentFilesModificationEvent(RecentFiles.this.getRecentFiles()));
			};
		}.begin();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void postAccessedRecentFiles(final AccessedFilesEvent event) {
		new Task() {

			private final ImmutableList<String> files = event.getFiles();

			@Override
			public void execute() {
				this.mergeAccessedToRecentFiles();
			}

			private void mergeAccessedToRecentFiles() {
				synchronized (RecentFiles.this.recentFiles) {
					this.merge(this.files);
				}
			}

			private void merge(final ImmutableList<String> files) {
				RecentFiles.this.recentFiles.removeAll(files);
				RecentFiles.this.recentFiles.addAll(0, files);
				RecentFiles.this.recentFiles.remove("");
			}

			@Override
			public void postExec() {
				EventBus.post(new RecentFilesEvent(RecentFiles.this.getRecentFiles()));
			}
		}.begin();
	}

	private synchronized ImmutableList<String> getRecentFiles() {
		return ImmutableList.copyOf(Sets.newLinkedHashSet(this.recentFiles));
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
