package com.laboki.eclipse.plugin.fastopen.events;

import com.google.common.collect.ImmutableList;

public final class WorkspaceFilesEvent {

	private final ImmutableList<String> files;

	public WorkspaceFilesEvent(final ImmutableList<String> files) {
		this.files = files;
	}

	public ImmutableList<String>
	getFiles() {
		return this.files;
	}
}
