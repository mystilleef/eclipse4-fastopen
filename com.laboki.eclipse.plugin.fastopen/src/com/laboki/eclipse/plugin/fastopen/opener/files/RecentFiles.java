package com.laboki.eclipse.plugin.fastopen.opener.files;

import java.util.List;

import lombok.Synchronized;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Instance;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.RecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.RecentFilesModificationEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class RecentFiles implements Instance {

	private final List<String> recentFiles = Lists.newArrayList();

	@Subscribe
	@AllowConcurrentEvents
	public void modifiedFilesChanged(final ModifiedFilesEvent event) {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				RecentFiles.this.updateRecentFiles(event.getFiles());
				this.postRecentFilesModificationEvent();
			}

			private void postRecentFilesModificationEvent() {
				EventBus.post(new RecentFilesModificationEvent(RecentFiles.this.getRecentFiles()));
			}
		});
	}

	@Subscribe
	@AllowConcurrentEvents
	public void accessedFilesChanged(final AccessedFilesEvent event) {
		EditorContext.asyncExec(new Task() {

			private final List<String> rfiles = Lists.newArrayList(RecentFiles.this.getRecentFiles());

			@Override
			public void execute() {
				this.mergeAccessedAndRecentFiles(event.getFiles());
			}

			private void mergeAccessedAndRecentFiles(final ImmutableList<String> files) {
				this.update(files);
				RecentFiles.this.updateRecentFiles(ImmutableList.copyOf(this.rfiles));
				this.postRecentFilesEvent();
			}

			private void update(final ImmutableList<String> files) {
				this.rfiles.removeAll(files);
				this.rfiles.addAll(0, files);
			}

			private void postRecentFilesEvent() {
				EventBus.post(new RecentFilesEvent(RecentFiles.this.getRecentFiles()));
			}
		});
	}

	@Synchronized("recentFiles")
	protected void updateRecentFiles(final ImmutableList<String> files) {
		this.recentFiles.clear();
		this.recentFiles.addAll(files);
	}

	@Synchronized("recentFiles")
	private ImmutableList<String> getRecentFiles() {
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
