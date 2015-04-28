package com.laboki.eclipse.plugin.fastopen.resources;

import java.io.File;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.swt.graphics.Image;
import org.ocpsoft.prettytime.PrettyTime;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;

public final class RFile {

	private final IFile file;
	private final String name;
	private final String folder;
	private final String contentTypeString;
	private final IContentType contentType;
	private final Image contentTypeImage;
	private final String filePath;
	private static final PrettyTime PRETTY_TIME = new PrettyTime();

	public RFile(final IFile file) {
		this.file = file;
		this.name = this.file.getName();
		this.folder =
			CharMatcher.anyOf(File.separator).trimFrom(
				this.file.getParent().getFullPath().toOSString());
		this.contentType = this.getPrivateContentType().get();
		this.contentTypeString = this.getPrivateContentTypeString().toLowerCase();
		this.contentTypeImage =
			EditorContext
				.getImage(file.getFullPath().toOSString(), this.contentType);
		this.filePath = this.file.getLocation().toOSString();
	}

	private Optional<IContentType>
	getPrivateContentType() {
		final Optional<IContentType> type =
			EditorContext.getContentType(this.file);
		if (!type.isPresent()) return Optional.absent();
		return type;
	}

	private String
	getPrivateContentTypeString() {
		try {
			return this.contentType.getName();
		}
		catch (final Exception e) {
			return "text file";
		}
	}

	public String
	getModificationTime() {
		return RFile.PRETTY_TIME.format(new Date(this.getLastModified()));
	}

	private long
	getLastModified() {
		return this.file.getLocation().toFile().lastModified();
	}

	@Override
	public String
	toString() {
		return String.format(
			"===\nName=%s\nFolder=%s\nContentType=%s\nModificationTime=%s\n===",
			this.getName(),
			this.getFolder(),
			this.getPrivateContentTypeString(),
			this.getModificationTime());
	}

	public IContentType
	getContentType() {
		return this.contentType;
	}

	public Image
	getContentTypeImage() {
		return this.contentTypeImage;
	}

	public String
	getContentTypeString() {
		return this.contentTypeString;
	}

	public IFile
	getFile() {
		return this.file;
	}

	public String
	getFilePath() {
		return this.filePath;
	}

	public String
	getFolder() {
		return this.folder;
	}

	public String
	getName() {
		return this.name;
	}

	public static PrettyTime
	getPrettyTime() {
		return RFile.PRETTY_TIME;
	}

	@Override
	public boolean
	equals(final Object obj) {
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
	public int
	hashCode() {
		return (31 * 1) + ((this.filePath == null) ? 0 : this.filePath.hashCode());
	}
}
