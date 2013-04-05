package com.laboki.eclipse.plugin.fastopen.opener.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

final class WorkspaceFiles implements IResourceVisitor, Comparator<IFile> {

	private final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private final IWorkspaceRoot root = this.workspace.getRoot();
	@Getter private final List<IFile> files = new ArrayList<>();

	public WorkspaceFiles() {
		this.updateFilesFromWorkspace();
		this.sortFilesByModificationTime();
	}

	private void updateFilesFromWorkspace() {
		try {
			this.root.accept(this);
		} catch (final CoreException e) {}
	}

	private void sortFilesByModificationTime() {
		Collections.sort(this.files, this);
	}

	public List<String> getFilePaths() {
		final List<String> list = new ArrayList<>();
		for (final IFile iFile : this.files)
			list.add(iFile.getLocationURI().getPath());
		return list;
	}

	@Override
	public boolean visit(final IResource resource) throws CoreException {
		this.updateFiles(resource);
		return true;
	}

	private void updateFiles(final IResource resource) {
		if (resource.getType() == IResource.FILE) if (EditorContext.isValid(resource)) this.files.add((IFile) resource);
	}

	@Override
	public int compare(final IFile o1, final IFile o2) {
		final long lastModified = o1.getLocation().toFile().lastModified();
		final long lastModified2 = o2.getLocation().toFile().lastModified();
		return lastModified < lastModified2 ? 1 : (lastModified > lastModified2 ? -1 : 0);
	}
}
