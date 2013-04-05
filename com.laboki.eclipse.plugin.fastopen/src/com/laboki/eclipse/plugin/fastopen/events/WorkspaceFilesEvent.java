package com.laboki.eclipse.plugin.fastopen.events;

import lombok.Getter;

import com.google.common.collect.ImmutableList;

public final class WorkspaceFilesEvent {

	@Getter private final ImmutableList<String> files;

	public WorkspaceFilesEvent(final ImmutableList<String> files) {
		this.files = files;
	}
}
