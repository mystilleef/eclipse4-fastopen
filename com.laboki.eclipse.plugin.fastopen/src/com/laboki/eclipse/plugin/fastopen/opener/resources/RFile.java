package com.laboki.eclipse.plugin.fastopen.opener.resources;

import java.io.File;
import java.util.Date;

import lombok.Getter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.swt.graphics.Image;
import org.ocpsoft.prettytime.PrettyTime;

import com.google.common.base.CharMatcher;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class RFile {

	@Getter private final IFile file;
	@Getter private final String name;
	@Getter private final String folder;
	@Getter private final String contentTypeString;
	@Getter private final IContentType contentType;
	@Getter private final Image contentTypeImage;
	@Getter private final String filePath;
	private static final PrettyTime PRETTY_TIME = new PrettyTime();

	public RFile(final IFile file) {
		this.file = file;
		this.name = this.file.getName();
		this.folder = CharMatcher.anyOf(File.separator).trimFrom(this.file.getParent().getFullPath().toOSString());
		this.contentType = this.getPrivateContentType();
		this.contentTypeString = this.getPrivateContentTypeString().toLowerCase();
		this.contentTypeImage = EditorContext.getImage(file.getFullPath().toOSString(), this.contentType);
		this.filePath = this.file.getLocation().toOSString();
	}

	private IContentType getPrivateContentType() {
		return EditorContext.getContentType(this.file);
	}

	private String getPrivateContentTypeString() {
		try {
			return this.contentType.getName();
		} catch (final Exception e) {
			return "text file";
		}
	}

	public String getModificationTime() {
		return RFile.PRETTY_TIME.format(new Date(this.getLastModified()));
	}

	private long getLastModified() {
		return this.file.getLocation().toFile().lastModified();
	}

	@Override
	public String toString() {
		return String.format("===\nName=%s\nFolder=%s\nContentType=%s\nModificationTime=%s\n===", this.getName(), this.getFolder(), this.getPrivateContentTypeString(), this.getModificationTime());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof RFile)) return false;
		final RFile other = (RFile) obj;
		if (this.filePath == null) {
			if (other.filePath != null) return false;
		} else if (!this.filePath.equals(other.filePath)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return (31 * 1) + ((this.filePath == null) ? 0 : this.filePath.hashCode());
	}
}
