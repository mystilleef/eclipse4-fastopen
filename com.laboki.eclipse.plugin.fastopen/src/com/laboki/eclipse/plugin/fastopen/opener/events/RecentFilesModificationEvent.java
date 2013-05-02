package com.laboki.eclipse.plugin.fastopen.opener.events;

import com.google.common.collect.ImmutableList;

public final class RecentFilesModificationEvent {

	private final ImmutableList<Object> files;

	public RecentFilesModificationEvent(final ImmutableList<Object> immutableList) {
		this.files = immutableList;
	}

	public ImmutableList<Object> getFiles() {
		return this.files;
	}
}
