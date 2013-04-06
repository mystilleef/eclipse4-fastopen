package com.laboki.eclipse.plugin.fastopen.events;

import lombok.Getter;

import com.google.common.collect.ImmutableList;
import com.laboki.eclipse.plugin.fastopen.opener.resources.File;

public final class FilterRecentFilesResultEvent {

	@Getter private final ImmutableList<File> files;

	public FilterRecentFilesResultEvent(final ImmutableList<File> files) {
		this.files = files;
	}
}
