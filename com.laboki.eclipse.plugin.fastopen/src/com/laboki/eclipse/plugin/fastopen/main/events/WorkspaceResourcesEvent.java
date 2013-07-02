package com.laboki.eclipse.plugin.fastopen.main.events;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.ImmutableList;

public final class WorkspaceResourcesEvent {

	private final ImmutableList<IFile> resources;

	public WorkspaceResourcesEvent(final ImmutableList<IFile> resources) {
		this.resources = resources;
	}

	public ImmutableList<IFile> getResources() {
		return this.resources;
	}
}
