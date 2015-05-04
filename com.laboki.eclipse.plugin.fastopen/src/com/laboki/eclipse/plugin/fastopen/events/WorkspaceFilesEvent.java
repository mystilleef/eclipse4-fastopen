package com.laboki.eclipse.plugin.fastopen.events;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.ImmutableList;

public final class WorkspaceFilesEvent {

	private final ImmutableList<IFile> resources;

	public WorkspaceFilesEvent(final ImmutableList<IFile> resources) {
		this.resources = resources;
	}

	public ImmutableList<IFile>
	getFiles() {
		return this.resources;
	}
}
