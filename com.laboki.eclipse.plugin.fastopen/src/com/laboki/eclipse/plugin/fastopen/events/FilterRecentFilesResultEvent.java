package com.laboki.eclipse.plugin.fastopen.events;

import lombok.Getter;

import com.google.common.collect.ImmutableList;
import com.laboki.eclipse.plugin.fastopen.opener.resources.RFile;

public final class FilterRecentFilesResultEvent {

	@Getter private final ImmutableList<RFile> rFiles;

	public FilterRecentFilesResultEvent(final ImmutableList<RFile> rFiles) {
		this.rFiles = rFiles;
	}
}
