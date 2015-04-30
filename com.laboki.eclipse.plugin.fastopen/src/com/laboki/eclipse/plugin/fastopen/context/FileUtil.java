package com.laboki.eclipse.plugin.fastopen.context;

import java.io.File;
import java.util.Date;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.swt.graphics.Image;
import org.ocpsoft.prettytime.PrettyTime;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;

public enum FileUtil {
	INSTANCE;

	private static final PrettyTime TIME = new PrettyTime();

	public static Optional<String>
	getName(final Optional<IFile> file) {
		if (!file.isPresent()) return Optional.absent();
		return Optional.fromNullable(file.get().getName());
	}

	public static Optional<String>
	getPath(final Optional<IFile> file) {
		if (!file.isPresent()) return Optional.absent();
		final Optional<IPath> location =
			Optional.fromNullable(file.get().getLocation());
		if (!location.isPresent()) return Optional.absent();
		return Optional.fromNullable(location.get().toOSString());
	}

	public static Optional<String>
	getFolder(final Optional<IFile> file) {
		if (!file.isPresent()) return Optional.absent();
		final Optional<IContainer> parent =
			Optional.fromNullable(file.get().getParent());
		if (!parent.isPresent()) return Optional.absent();
		return Optional.fromNullable(CharMatcher.anyOf(File.separator).trimFrom(
			parent.get().getFullPath().toOSString()));
	}

	public static Optional<IContentType>
	getContentType(final Optional<IFile> file) {
		return ContentTypeContext.getContentTypeFromFile(file);
	}

	public static Optional<String>
	getContentTypeName(final Optional<IFile> file) {
		final Optional<IContentType> type =
			ContentTypeContext.getContentTypeFromFile(file);
		if (!type.isPresent()) return Optional.absent();
		return Optional.fromNullable(type.get().getName().toLowerCase());
	}

	public static Optional<Image>
	getContentTypeImage(final Optional<IFile> file) {
		final Optional<IContentType> type =
			ContentTypeContext.getContentTypeFromFile(file);
		if (!type.isPresent()) return Optional.absent();
		return Optional.fromNullable(EditorContext.getImage(file
			.get()
			.getFullPath()
			.toOSString(), type.get()));
	}

	public static Optional<String>
	getModificationTime(final Optional<IFile> file) {
		final Optional<Long> lastModified =
			FileUtil.getLastModified(file);
		if (!lastModified.isPresent()) return Optional.absent();
		return Optional.fromNullable(FileUtil.TIME.format(new Date(
			lastModified.get())));
	}

	public static Optional<Long>
	getLastModified(final Optional<IFile> file) {
		if (!file.isPresent()) return Optional.absent();
		return Optional.fromNullable(file
			.get()
			.getLocation()
			.toFile()
			.lastModified());
	}
}
