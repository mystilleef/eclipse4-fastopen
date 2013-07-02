package com.laboki.eclipse.plugin.fastopen.main.events;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.ImmutableMap;

public class FileResourcesMapEvent {

	private final ImmutableMap<String, IFile> map;

	public FileResourcesMapEvent(final ImmutableMap<String, IFile> map) {
		this.map = map;
	}

	public ImmutableMap<String, IFile> getMap() {
		return this.map;
	}
}
