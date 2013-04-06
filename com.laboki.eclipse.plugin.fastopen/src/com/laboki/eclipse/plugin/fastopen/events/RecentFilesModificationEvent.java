package com.laboki.eclipse.plugin.fastopen.events;

import lombok.Getter;

import com.google.common.collect.ImmutableList;

public final class RecentFilesModificationEvent {

	@Getter private final ImmutableList<String> files;

	public RecentFilesModificationEvent(final ImmutableList<String> files) {
		this.files = files;
	}
}
