package com.laboki.eclipse.plugin.fastopen.events;

import lombok.Getter;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.ImmutableMap;

public class FileResourcesMapEvent {

	@Getter private final ImmutableMap<String, IFile> map;

	public FileResourcesMapEvent(final ImmutableMap<String, IFile> map) {
		this.map = map;
	}
}
