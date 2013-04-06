package com.laboki.eclipse.plugin.fastopen.opener;

import java.util.Date;

import lombok.Getter;

import org.eclipse.core.resources.IFile;
import org.ocpsoft.prettytime.PrettyTime;

public final class File {

	@Getter private final IFile file;
	@Getter private final String name;
	@Getter private final String folder;
	@Getter private final String contentType;
	@Getter private final String filePath;
	private static final PrettyTime PRETTY_TIME = new PrettyTime();

	public File(final IFile file) {
		this.file = file;
		this.name = this.file.getName();
		this.folder = this.file.getParent().getFullPath().toPortableString();
		this.contentType = this.getPrivateContentType();
		this.filePath = this.file.getLocation().toOSString();
	}

	private String getPrivateContentType() {
		try {
			return this.file.getContentDescription().getContentType().getName();
		} catch (final Exception e) {
			return "Linked File";
		}
	}

	public String getModificationTime() {
		return File.PRETTY_TIME.format(new Date(this.getLastModified()));
	}

	private long getLastModified() {
		return this.file.getLocation().toFile().lastModified();
	}

	@Override
	public String toString() {
		return String.format("===\nName=%s\nFolder=%s\nContentType=%s\nModificationTime=%s\n===", this.getName(), this.getFolder(), this.getPrivateContentType(), this.getModificationTime());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof File)) return false;
		final File other = (File) obj;
		if (this.filePath == null) {
			if (other.filePath != null) return false;
		} else if (!this.filePath.equals(other.filePath)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.filePath == null) ? 0 : this.filePath.hashCode());
		return result;
	}
}
