// $codepro.audit.disable methodChainLength
package com.laboki.eclipse.plugin.fastopen.opener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lombok.Synchronized;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.laboki.eclipse.plugin.fastopen.Activator;

public final class EditorContext {

	public static final Display DISPLAY = EditorContext.getDisplay();
	public static final String PLUGIN_NAME = "com.laboki.eclipse.plugin.fastopen";
	private static EditorContext instance;
	private static final FlushEventsRunnable FLUSH_EVENTS_RUNNABLE = new EditorContext.FlushEventsRunnable();
	private static final IContentType CONTENT_TYPE_TEXT = Platform.getContentTypeManager().getContentType("org.eclipse.core.runtime.text");

	private EditorContext() {
		System.out.println(EditorContext.CONTENT_TYPE_TEXT);
	}

	@Synchronized
	public static EditorContext instance() {
		if (EditorContext.instance == null) EditorContext.instance = new EditorContext();
		return EditorContext.instance;
	}

	public static Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}

	public static Shell getShell() {
		return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
	}

	public static void asyncExec(final Runnable runnable) {
		EditorContext.DISPLAY.asyncExec(runnable);
	}

	public static void flushEvents() {
		EditorContext.asyncExec(EditorContext.FLUSH_EVENTS_RUNNABLE);
	}

	public static IWorkbenchPage getActivePage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	public static IEditorDescriptor getEditorDescriptor(final IFile file) {
		return PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
	}

	public static void openEditor(final IFile file) throws Exception {
		EditorContext.getActivePage().openEditor(new FileEditorInput(file), EditorContext.getEditorDescriptor(file).getId());
	}

	public static void openLink(final IFile file) throws Exception {
		IDE.openEditorOnFileStore(EditorContext.getActivePage(), EFS.getStore(file.getRawLocationURI()));
	}

	public static IEditorPart getEditor() {
		return EditorContext.getActivePage().getActiveEditor();
	}

	private static IFile getFile() {
		return ((FileEditorInput) EditorContext.getEditor().getEditorInput()).getFile();
	}

	public static String getPath() {
		return EditorContext.getFile().getLocationURI().getPath();
	}

	private static final class FlushEventsRunnable implements Runnable {

		public FlushEventsRunnable() {}

		@Override
		public void run() {
			while (EditorContext.DISPLAY.readAndDispatch())
				EditorContext.DISPLAY.update();
			EditorContext.DISPLAY.update();
		}
	}

	public static boolean isValidResourceFile(final IResource resource) {
		if (EditorContext.isNotResourceFile(resource)) return false;
		if (EditorContext.isHiddenFile((IFile) resource)) return false;
		return EditorContext.isTextFile((IFile) resource);
	}

	private static boolean isNotResourceFile(final IResource resource) {
		return !EditorContext.isResourceFile(resource);
	}

	private static boolean isResourceFile(final IResource resource) {
		return resource.getType() == IResource.FILE;
	}

	public static boolean isHiddenFile(final IFile file) {
		return file.getLocation().toFile().isHidden() || file.getParent().getLocation().toFile().isHidden();
	}

	private static boolean isTextFile(final IFile file) {
		return EditorContext.isTextContent(file);
	}

	private static boolean isTextContent(final IFile file) {
		try {
			return file.getContentDescription().getContentType().isKindOf(EditorContext.CONTENT_TYPE_TEXT);
		} catch (final Exception e) {
			return file.isLinked();
		}
	}

	public static String getURIPath(final IResource resource) {
		return resource.getLocationURI().getPath();
	}

	public static void removeFakePaths(final List<String> files) {
		files.removeAll(EditorContext.nonExistentFilePaths(files));
	}

	public static List<String> nonExistentFilePaths(final List<String> filePaths) {
		final List<String> unfoundFiles = new ArrayList<>();
		for (final String path : filePaths)
			if (!(new File(path).exists())) unfoundFiles.add(path);
		return unfoundFiles;
	}

	public static void emptyFile(final String filePath) {
		final File f = new File(filePath);
		if (f.exists()) return;
		try {
			final BufferedWriter out = new BufferedWriter(new FileWriter(f));
			out.write("");
			out.close();
		} catch (final IOException e) {}
	}

	public static void serialize(final String filePath, final Object serializable) {
		try {
			final OutputStream file = new FileOutputStream(filePath);
			final OutputStream buffer = new BufferedOutputStream(file);
			final ObjectOutput output = new ObjectOutputStream(buffer);
			try {
				output.writeObject(serializable);
			} finally {
				output.close();
			}
		} catch (final Exception ex) {}
	}

	public static Object deserialize(final String filePath) {
		try {
			final InputStream file = new FileInputStream(filePath);
			final InputStream buffer = new BufferedInputStream(file);
			final ObjectInput input = new ObjectInputStream(buffer);
			try {
				return input.readObject();
			} finally {
				input.close();
			}
		} catch (final Exception ex) {}
		return null;
	}

	public static String getSerializableFilePath(final String fileName) {
		return Paths.get(EditorContext.getPluginFolderPath(), fileName).toString();
	}

	public static String getPluginFolderPath() {
		return Activator.getInstance().getStateLocation().toOSString();
	}

	public static IPartService getPartService() {
		return (IPartService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IPartService.class);
	}

	public static IFile getFile(final String filePathString) {
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(new File(filePathString).getAbsolutePath()));
	}
}
