package com.laboki.eclipse.plugin.fastopen.events;

import lombok.Getter;

import com.google.common.collect.ImmutableList;

public class FilesEvent {

	@Getter private final ImmutableList<String> files;

	public FilesEvent(final ImmutableList<String> files) {
		this.files = files;
	}
}
