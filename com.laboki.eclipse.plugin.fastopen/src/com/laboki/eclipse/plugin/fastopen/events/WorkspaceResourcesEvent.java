package com.laboki.eclipse.plugin.fastopen.events;

import lombok.Getter;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.ImmutableList;

public final class WorkspaceResourcesEvent {

	@Getter private final ImmutableList<IFile> resources;

	public WorkspaceResourcesEvent(final ImmutableList<IFile> resources) {
		this.resources = resources;
	}
}
