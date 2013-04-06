package com.laboki.eclipse.plugin.fastopen.opener.files;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Synchronized;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.DelayedTask;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.DeserializedAccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.PartActivationEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class AccessedFiles {

	private static final int ACCESSED_FILES_REINDEX_WATERMARK = 3;
	private final List<String> accessedFiles = Lists.newArrayList();

	@Subscribe
	@AllowConcurrentEvents
	public void partActivationChanged(@SuppressWarnings("unused") final PartActivationEvent event) {
		EditorContext.asyncExec(new Task("") {

			private final List<String> aFiles = AccessedFiles.this.getAccessedFiles();

			@Override
			public void execute() {
				this.moveCurrentFileToTopOfList();
				this.update(this.getAccessedFilesInsertionIndex(), EditorContext.getPath());
				AccessedFiles.this.updateAccessedFiles(ImmutableList.copyOf(this.aFiles));
				AccessedFiles.this.postEvent();
			}

			protected void moveCurrentFileToTopOfList() {
				if (this.aFiles.size() < AccessedFiles.ACCESSED_FILES_REINDEX_WATERMARK) return;
				this.update(0, this.aFiles.get(1));
			}

			protected void update(final int index, final String path) {
				this.aFiles.remove(path);
				this.aFiles.add(index, path);
			}

			protected int getAccessedFilesInsertionIndex() {
				return this.aFiles.size() == 0 ? 0 : 1;
			}
		});
	}

	@Subscribe
	@AllowConcurrentEvents
	public void deserializedAccessedFiles(final DeserializedAccessedFilesEvent event) {
		EditorContext.asyncExec(new Task("") {

			private final List<String> aFiles = Lists.newArrayList(event.getFiles());

			@Override
			public void execute() {
				AccessedFiles.this.updateAccessedFiles(ImmutableList.copyOf(this.aFiles));
			}
		});
	}

	@Subscribe
	@AllowConcurrentEvents
	public void modifiedFilesChanged(final ModifiedFilesEvent event) {
		EditorContext.asyncExec(new DelayedTask("", 50) {

			@Override
			public void execute() {
				AccessedFiles.this.updateAccessedFiles(ImmutableList.copyOf(this.removeDeletedFiles(event.getFiles())));
				AccessedFiles.this.postEvent();
			}

			private Collection<String> removeDeletedFiles(final ImmutableList<String> files) {
				return Collections2.filter(AccessedFiles.this.getAccessedFiles(), new Predicate<String>() {

					@Override
					public boolean apply(final String file) {
						return files.contains(file);
					}
				});
			}
		});
	}

	@Synchronized("accessedFiles")
	private void updateAccessedFiles(final List<String> files) {
		this.accessedFiles.clear();
		this.accessedFiles.addAll(files);
	}

	private void postEvent() {
		EventBus.post(new AccessedFilesEvent(ImmutableList.copyOf(this.getAccessedFiles())));
	}

	@Synchronized("accessedFiles")
	private ArrayList<String> getAccessedFiles() {
		return Lists.newArrayList(this.accessedFiles);
	}
}
