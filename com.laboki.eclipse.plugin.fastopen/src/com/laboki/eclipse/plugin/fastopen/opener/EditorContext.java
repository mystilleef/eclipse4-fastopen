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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lombok.Synchronized;
import lombok.val;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.google.common.collect.Lists;
import com.google.common.net.MediaType;
import com.laboki.eclipse.plugin.fastopen.Activator;

public final class EditorContext {

	public static final Display DISPLAY = EditorContext.getDisplay();
	public static final String PLUGIN_NAME = "com.laboki.eclipse.plugin.fastopen";
	private static EditorContext instance;
	private static final FlushEventsRunnable FLUSH_EVENTS_RUNNABLE = new EditorContext.FlushEventsRunnable();
	private static final IContentType CONTENT_TYPE_TEXT = Platform.getContentTypeManager().getContentType("org.eclipse.core.runtime.text");

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
		EditorContext.getActivePage().openEditor(new FileEditorInput(file), EditorContext.getEditorID(file));
	}

	public static void closeEditor(final IFile file) {
		EditorContext.getActivePage().closeEditors(EditorContext.getActivePage().findEditors(new FileEditorInput(file), EditorContext.getEditorID(file), IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT), true);
	}

	public static String getEditorID(final IFile file) {
		return EditorContext.getEditorDescriptor(file).getId();
	}

	public static void openLink(final IFile file) throws Exception {
		IDE.openEditorOnFileStore(EditorContext.getActivePage(), EFS.getStore(file.getRawLocationURI()));
	}

	public static IEditorPart getEditor() {
		return EditorContext.getActivePage().getActiveEditor();
	}

	public static IEditorReference[] getActiveEditorReferences() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
	}

	public static IEditorPart[] getActiveEditorParts() {
		val parts = Lists.newArrayList();
		for (final IEditorReference editorReference : EditorContext.getActiveEditorReferences()) {
			val editorPart = EditorContext.getEditorPart(editorReference);
			if (EditorContext.isValidPart(editorPart)) parts.add(editorPart);
		}
		return parts.toArray(new IEditorPart[parts.size()]);
	}

	private static IEditorPart getEditorPart(final IEditorReference editorReference) {
		return (IEditorPart) editorReference.getPart(false);
	}

	public static boolean isInvalidPart(final IWorkbenchPart part) {
		return !EditorContext.isValidPart(part);
	}

	public static boolean isValidPart(final IWorkbenchPart part) {
		if (part instanceof IEditorPart) return true;
		return false;
	}

	public static IFile[] getActiveFiles() {
		val files = Lists.newArrayList();
		for (final IEditorPart editorPart : EditorContext.getActiveEditorParts())
			files.add(EditorContext.getFile(editorPart));
		return files.toArray(new IFile[files.size()]);
	}

	public static String[] getActiveFilePathStrings() {
		val strings = Lists.newArrayList();
		for (final IFile file : EditorContext.getActiveFiles())
			strings.add(EditorContext.getURIPath(file));
		return strings.toArray(new String[strings.size()]);
	}

	private static IFile getFile() {
		try {
			return ((FileEditorInput) EditorContext.getEditor().getEditorInput()).getFile();
		} catch (final Exception e) {
			return null;
		}
	}

	public static IFile getFile(final IEditorPart editorPart) {
		try {
			return ((FileEditorInput) editorPart.getEditorInput()).getFile();
		} catch (final Exception e) {
			return null;
		}
	}

	public static String getPath() {
		try {
			return EditorContext.getFile().getLocationURI().getPath();
		} catch (final Exception e) {
			return "";
		}
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

	public static boolean isNotValidResourceFile(final IResource resource) {
		return !EditorContext.isValidResourceFile(resource);
	}

	public static boolean isValidResourceFile(final IResource resource) {
		if (EditorContext.isNotResourceFile(resource)) return false;
		if (EditorContext.isHiddenFile(resource)) return false;
		return EditorContext.isTextFile((IFile) resource);
	}

	public static boolean isNotResourceFile(final IResource resource) {
		return !EditorContext.isResourceFile(resource);
	}

	public static boolean isResourceFile(final IResource resource) {
		return resource.getType() == IResource.FILE;
	}

	public static boolean isHiddenFile(final IResource resource) {
		return resource.getLocation().toFile().isHidden() || resource.isHidden();
	}

	public static boolean isWierd(final IResource resource) {
		return resource.isDerived() || resource.isPhantom() || resource.isTeamPrivateMember() || resource.isVirtual();
	}

	private static boolean isTextFile(final IFile file) {
		return EditorContext.isMediaContentTypeText(file) || EditorContext.isContentTypeText(file) || EditorContext.hasValidCharSet(file) || file.isLinked();
	}

	private static boolean isMediaContentTypeText(final IFile file) {
		try {
			return EditorContext.getMediaContentType(file).is(MediaType.ANY_TEXT_TYPE);
		} catch (final Exception e) {
			return false;
		}
	}

	private static MediaType getMediaContentType(final IFile file) throws IOException {
		return MediaType.parse(Files.probeContentType(FileSystems.getDefault().getPath(EditorContext.getURIPath(file))));
	}

	public static boolean isContentTypeText(final IFile file) {
		try {
			return EditorContext.getContentType(file).isKindOf(EditorContext.CONTENT_TYPE_TEXT);
		} catch (final Exception e) {
			return false;
		}
	}

	public static IContentType getContentType(final IFile file) {
		try {
			return EditorContext.tryToGetContentType(file);
		} catch (final Exception e) {
			return EditorContext.getContentTypeFromMediaType(file);
		}
	}

	private static IContentType tryToGetContentType(final IFile file) throws CoreException {
		final IContentType contentType = file.getContentDescription().getContentType();
		if (contentType == null) return EditorContext.getContentTypeFromMediaType(file);
		return contentType;
	}

	private static IContentType getContentTypeFromMediaType(final IFile file) {
		try {
			return Platform.getContentTypeManager().getContentType(EditorContext.getMediaContentType(file).toString().trim());
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static boolean hasValidCharSet(final IFile file) {
		try {
			if (file.getCharset(false) != null) return true;
		} catch (final CoreException e) {}
		return false;
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

	public static Image getImage(final String filename, final IContentType contentType) {
		return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(filename, contentType).createImage();
	}
}
