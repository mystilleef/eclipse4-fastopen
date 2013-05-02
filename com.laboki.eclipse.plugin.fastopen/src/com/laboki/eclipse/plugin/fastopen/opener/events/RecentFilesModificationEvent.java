package com.laboki.eclipse.plugin.fastopen.opener.events;

import com.google.common.collect.ImmutableList;

public final class RecentFilesModificationEvent {

	private final ImmutableList<String> files;

	public RecentFilesModificationEvent(final ImmutableList<String> files) {
		this.files = files;
	}

	public ImmutableList<String> getFiles() {
		return this.files;
	}
}
