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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.java.Log;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

@Log
public final class EditorContext {

	public static final Display DISPLAY = EditorContext.getDisplay();
	public static final String PLUGIN_NAME = "com.laboki.eclipse.plugin.fastopen";
	private static EditorContext instance;
	private static final FlushEventsRunnable FLUSH_EVENTS_RUNNABLE = new EditorContext.FlushEventsRunnable();

	private EditorContext() {}

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

	public static boolean isValid(final IResource file) {
		if (!EditorContext.isTextFile(file)) return false;
		if (EditorContext.isHiddenFile((IFile) file)) return false;
		return true;
	}

	private static boolean isTextFile(final IResource file) {
		if (file.getType() != IResource.FILE) return false;
		if (EditorContext.getBaseTypeID((IFile) file).equals("org.eclipse.core.runtime.text")) return true;
		return false;
	}

	private static String getBaseTypeID(final IFile file) {
		try {
			return EditorContext.getBaseType(file).getId().trim();
		} catch (final Exception e) {
			return "";
		}
	}

	private static IContentType getBaseType(final IFile file) throws CoreException {
		val contentType = file.getContentDescription().getContentType();
		val baseType = contentType.getBaseType();
		return baseType == null ? contentType : baseType;
	}

	static boolean isHiddenFile(final IFile file) {
		return file.getLocation().toFile().isHidden() || file.isHidden();
	}

	@SuppressWarnings("unused")
	private static boolean isWierdFile(final IFile file) {
		if (file.isDerived() || file.isVirtual() || file.isPhantom() || file.isTeamPrivateMember()) return true;
		return false;
	}

	public static String getURIPath(final IResource resource) {
		return resource.getLocationURI().getPath();
	}

	public static List<String> nonExistentFilePaths(final List<String> filePaths) {
		final List<String> unfoundFiles = new ArrayList<>();
		for (final String path : filePaths)
			if (!(new File(path).exists())) unfoundFiles.add(path);
		return unfoundFiles;
	}

	public static void removeFakePaths(final List<String> files) {
		files.removeAll(EditorContext.nonExistentFilePaths(files));
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
		} catch (final IOException ex) {
			EditorContext.log.log(Level.SEVERE, "Cannot perform output.", ex);
		}
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
		} catch (final ClassNotFoundException ex) {
			EditorContext.log.log(Level.SEVERE, "Cannot perform input. Class not found.", ex);
		} catch (final IOException ex) {
			EditorContext.log.log(Level.SEVERE, "Cannot perform input.", ex);
		}
		return null;
	}

	public static IPartService getPartService() {
		return (IPartService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IPartService.class);
	}

	public static IFile getFile(final String filePathString) {
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(new File(filePathString).getAbsolutePath()));
	}
}
