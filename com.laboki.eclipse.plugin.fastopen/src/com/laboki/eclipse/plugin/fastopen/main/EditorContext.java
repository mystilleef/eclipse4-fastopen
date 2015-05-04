package com.laboki.eclipse.plugin.fastopen.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.net.MediaType;
import com.laboki.eclipse.plugin.fastopen.Activator;
import com.laboki.eclipse.plugin.fastopen.context.ContentTypeContext;
import com.laboki.eclipse.plugin.fastopen.context.FileContext;

public enum EditorContext {
	INSTANCE;

	public static final String UPDATE_R_FILES_TASK =
		"Eclipse Fast Open Plugin: update rFiles task";
	public static final String FILTER_RECENT_FILES_TASK =
		"Eclipse Fast Open Plugin: filter recent files task";
	public static final String CORE_WORKSPACE_INDEXER_TASK =
		"Eclipse Fast Open Plugin: Core Workspace Indexer Task";
	public static final String EMIT_UPDATED_RECENT_FILES_TASK =
		"Eclipse Fast Open Plugin: Emit updated recent files task";
	public static final String UPDATE_ACCESSED_FILES_TASK =
		"Eclipse Fast Open Plugin: Update accessed files task.";
	public static final String EMIT_INDEX_RESOURCE_TASK =
		"Eclipse Fast Open Plugin: Emit index resource task.";
	public static final String INDEX_WORKSPACE_RESOURCES_TASK =
		"Eclipse Fast Open Plugin: Index workspace resources tasks.";
	public static final String INDEX_RESOURCES_TASK =
		"Eclipse Fast Open Plugin: Index resources task.";
	public static final int SHORT_DELAY_IN_MILLISECONDS = 60;
	public static final int SHORT_DELAY_TIME = 250;
	public static final Display DISPLAY = PlatformUI.getWorkbench().getDisplay();
	public static final String PLUGIN_NAME =
		"com.laboki.eclipse.plugin.fastopen";
	private static final IContentType CONTENT_TYPE_TEXT = Platform
		.getContentTypeManager()
		.getContentType("org.eclipse.core.runtime.text");
	public static final IJobManager JOB_MANAGER = Job.getJobManager();
	private static final Logger LOGGER = Logger.getLogger(EditorContext.class
		.getName());

	private EditorContext() {}

	public static void
	asyncExec(final Runnable runnable) {
		if ((EditorContext.DISPLAY == null) || EditorContext.DISPLAY.isDisposed()) return;
		EditorContext.DISPLAY.asyncExec(runnable);
	}

	public static void
	syncExec(final Runnable runnable) {
		if ((EditorContext.DISPLAY == null) || EditorContext.DISPLAY.isDisposed()) return;
		EditorContext.DISPLAY.syncExec(runnable);
	}

	public static void
	closeEditor(final IFile file) {
		final Optional<IWorkbenchPage> page = EditorContext.getActivePage();
		if (!page.isPresent()) return;
		final Optional<String> editorID = EditorContext.getEditorID(file);
		if (!editorID.isPresent()) return;
		page
			.get()
			.closeEditors(page
				.get()
				.findEditors(new FileEditorInput(file), editorID.get(), IWorkbenchPage.MATCH_ID
					| IWorkbenchPage.MATCH_INPUT), true);
	}

	public static Object
	deserialize(final String filePath) {
		return EditorContext.readObjectInput(EditorContext
			.tryToGetNewObjectInputStream(filePath));
	}

	public static void
	emptyFile(final String filePath) {
		final File f = new File(filePath);
		if (f.exists()) return;
		try {
			@SuppressWarnings("resource") final BufferedWriter out =
				new BufferedWriter(new FileWriter(f));
			out.write("");
			out.close();
		}
		catch (final IOException e) {
			EditorContext.LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public static IEditorPart[]
	getActiveEditorParts() {
		final List<IEditorPart> parts = Lists.newArrayList();
		for (final IEditorReference editorReference : EditorContext
			.getActiveEditorReferences()) {
			final IEditorPart editorPart =
				EditorContext.getEditorPart(editorReference);
			if (EditorContext.isValidPart(editorPart)) parts.add(editorPart);
		}
		return parts.toArray(new IEditorPart[parts.size()]);
	}

	public static IEditorReference[]
	getActiveEditorReferences() {
		final Optional<IWorkbenchPage> page = EditorContext.getActivePage();
		if (!page.isPresent()) return new IEditorReference[0];
		return page.get().getEditorReferences();
	}

	public static String[]
	getActiveFilePathStrings() {
		final List<String> strings = Lists.newArrayList();
		for (final IFile file : EditorContext.getActiveFiles()) {
			final Optional<String> path = EditorContext.getURIPath(file);
			if (!path.isPresent()) continue;
			strings.add(path.get());
		}
		return strings.toArray(new String[strings.size()]);
	}

	public static IFile[]
	getActiveFiles() {
		final List<IFile> files = Lists.newArrayList();
		for (final IEditorPart editorPart : EditorContext.getActiveEditorParts())
			EditorContext.addActiveFile(files, editorPart);
		return files.toArray(new IFile[files.size()]);
	}

	private static void
	addActiveFile(final List<IFile> files, final IEditorPart editorPart) {
		final Optional<IFile> file =
			EditorContext.getFile(Optional.fromNullable(editorPart));
		if (!file.isPresent()) return;
		files.add(file.get());
	}

	public static Optional<IContentType>
	getContentType(final IFile file) {
		final Optional<IContentType> type =
			ContentTypeContext.getContentTypeFromFile(Optional.fromNullable(file));
		if (type.isPresent()) return type;
		return EditorContext.getContentTypeFromMediaType(file);
	}

	public static Display
	getDisplay() {
		return EditorContext.DISPLAY;
	}

	public static Optional<IEditorPart>
	getEditor() {
		final Optional<IWorkbenchPage> page = EditorContext.getActivePage();
		if (!page.isPresent()) return Optional.absent();
		return Optional.fromNullable(page.get().getActiveEditor());
	}

	public static Optional<IWorkbenchPage>
	getActivePage() {
		final Optional<IWorkbenchWindow> window =
			EditorContext.getActiveWorkbenchWindow();
		if (!window.isPresent()) return Optional.absent();
		return Optional.fromNullable(window.get().getActivePage());
	}

	private static Optional<IWorkbenchWindow>
	getActiveWorkbenchWindow() {
		return Optional.fromNullable(EditorContext
			.getWorkbench()
			.getActiveWorkbenchWindow());
	}

	private static IWorkbench
	getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	public static Optional<IEditorDescriptor>
	getEditorDescriptor(final IFile file) {
		try {
			return Optional.fromNullable(IDE.getEditorDescriptor(file));
		}
		catch (final PartInitException e) {
			return Optional.absent();
		}
	}

	public static Optional<String>
	getEditorID(final IFile file) {
		final Optional<IEditorDescriptor> descriptor =
			EditorContext.getEditorDescriptor(file);
		if (!descriptor.isPresent()) return Optional.absent();
		return Optional.fromNullable(descriptor.get().getId());
	}

	public static Optional<IFile>
	getFile(final Optional<IEditorPart> editorPart) {
		return FileContext.getFile(editorPart);
	}

	public static Optional<IFile>
	getFile(final String filePathString) {
		return FileContext.getFile(filePathString);
	}

	private static Optional<IFile>
	getFile() {
		return FileContext.getFile();
	}

	public static String
	getFilePathFromEditorReference(final IEditorReference file) {
		try {
			final Optional<String> path =
				EditorContext.getURIPath(((IFileEditorInput) file
					.getEditorInput()
					.getAdapter(IFileEditorInput.class)).getFile());
			if (!path.isPresent()) return "";
			return path.get();
		}
		catch (final Exception e) {
			return "";
		}
	}

	public static Image
	getImage(final String filename, final IContentType contentType) {
		return EditorContext
			.getWorkbench()
			.getEditorRegistry()
			.getImageDescriptor(filename, contentType)
			.createImage();
	}

	public static String[]
	getOpenEditorFilePaths() {
		final List<String> filepaths = Lists.newArrayList();
		for (final IEditorReference file : EditorContext
			.getActiveEditorReferences())
			EditorContext.populateOpenEditorFilePaths(filepaths, file);
		return filepaths.toArray(new String[filepaths.size()]);
	}

	public static List<IFile>
	getOpenFiles() {
		final List<IFile> files = Lists.newArrayList();
		for (final IEditorReference reference : EditorContext
			.getActiveEditorReferences()) {
			final Optional<IFile> file =
				EditorContext
					.getFile(Optional.fromNullable(reference.getEditor(false)));
			if (file.isPresent()) files.add(file.get());
		}
		return files;
	}

	public static Optional<IPartService>
	getPartService() {
		final Optional<IWorkbenchWindow> window =
			EditorContext.getActiveWorkbenchWindow();
		if (!window.isPresent()) return Optional.absent();
		return Optional.fromNullable((IPartService) window
			.get()
			.getService(IPartService.class));
	}

	public static String
	getPath() {
		final Optional<IFile> file = EditorContext.getFile();
		if (!file.isPresent()) return "";
		final Optional<URI> location = EditorContext.getUriLocation(file);
		if (!location.isPresent()) return "";
		return location.get().getPath();
	}

	private static Optional<URI>
	getUriLocation(final Optional<IFile> file) {
		return Optional.fromNullable(file.get().getLocationURI());
	}

	public static String
	getPluginFolderPath() {
		return Activator.getInstance().getStateLocation().toOSString();
	}

	public static String
	getSerializableFilePath(final String fileName) {
		return Paths.get(EditorContext.getPluginFolderPath(), fileName).toString();
	}

	public static Shell
	getShell() {
		return EditorContext
			.getWorkbench()
			.getModalDialogShellProvider()
			.getShell();
	}

	public static Optional<String>
	getURIPath(final IResource resource) {
		return Optional.fromNullable(resource.getLocationURI().getPath());
	}

	public static boolean
	isContentTypeText(final IFile file) {
		try {
			final Optional<IContentType> contentType =
				EditorContext.getContentType(file);
			if (!contentType.isPresent()) return false;
			return contentType.get().isKindOf(EditorContext.CONTENT_TYPE_TEXT);
		}
		catch (final Exception e) {
			return false;
		}
	}

	public static boolean
	isHiddenFile(final IResource resource) {
		return resource.getLocation().toFile().isHidden() || resource.isHidden();
	}

	public static boolean
	isInvalidPart(final IWorkbenchPart part) {
		return !EditorContext.isValidPart(part);
	}

	public static boolean
	isNotResourceFile(final IResource resource) {
		return !EditorContext.isResourceFile(resource);
	}

	public static boolean
	isNotValidResourceFile(final IResource resource) {
		return !EditorContext.isValidResourceFile(resource);
	}

	public static boolean
	isResourceFile(final IResource resource) {
		return resource.getType() == IResource.FILE;
	}

	public static boolean
	isValidPart(final IWorkbenchPart part) {
		if (part instanceof IEditorPart) return true;
		return false;
	}

	public static boolean
	isValidResourceFile(final IResource resource) {
		if (EditorContext.isNotResourceFile(resource)) return false;
		if (EditorContext.isHiddenFile(resource)) return false;
		return EditorContext.isTextFile((IFile) resource);
	}

	public static boolean
	isWierd(final IResource resource) {
		return resource.isVirtual()
			|| resource.isPhantom()
			|| resource.isTeamPrivateMember();
	}

	public static List<String>
	nonExistentFilePaths(final List<String> filePaths) {
		final List<String> unfoundFiles = new ArrayList<>();
		for (final String path : filePaths)
			if (!(new File(path).exists())) unfoundFiles.add(path);
		return unfoundFiles;
	}

	public static void
	openEditor(final IFile file) throws PartInitException {
		final Optional<IWorkbenchPage> page = EditorContext.getActivePage();
		if (!page.isPresent()) return;
		final Optional<String> editorID = EditorContext.getEditorID(file);
		if (!editorID.isPresent()) return;
		page.get().openEditor(new FileEditorInput(file), editorID.get());
	}

	public static void
	openLink(final IFile file) throws PartInitException, CoreException {
		final Optional<IWorkbenchPage> page = EditorContext.getActivePage();
		if (!page.isPresent()) return;
		IDE.openEditorOnFileStore(page.get(), EFS.getStore(file
			.getRawLocationURI()));
	}

	public static void
	populateOpenEditorFilePaths(final List<String> filepaths,
															final IEditorReference file) {
		final String path = EditorContext.getFilePathFromEditorReference(file);
		if (path.length() == 0) return;
		filepaths.add(path);
	}

	public static void
	removeFakePaths(final List<String> files) {
		files.removeAll(EditorContext.nonExistentFilePaths(files));
	}

	public static void
	serialize(final String filePath, final Object serializable) {
		try {
			EditorContext.writeSerializableToFile(serializable, EditorContext
				.getObjectOutput(filePath));
		}
		catch (final Exception e) {
			EditorContext.LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private static void
	closeObjectInput(final ObjectInput input) {
		try {
			if (input != null) input.close();
		}
		catch (final Exception e) {
			EditorContext.LOGGER.log(Level.OFF, e.getMessage(), e);
		}
	}

	private static void
	closeOutput(final ObjectOutput output) {
		try {
			output.close();
		}
		catch (final Exception e) {
			EditorContext.LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private static Optional<IContentType>
	getContentTypeFromMediaType(final IFile file) {
		final Optional<MediaType> mediaType = EditorContext.getMediaType(file);
		if (!mediaType.isPresent()) return Optional.absent();
		return Optional.fromNullable(Platform
			.getContentTypeManager()
			.getContentType(mediaType.get().toString().trim()));
	}

	private static IEditorPart
	getEditorPart(final IEditorReference editorReference) {
		return (IEditorPart) editorReference.getPart(false);
	}

	private static Optional<MediaType>
	getMediaType(final IFile file) {
		final Optional<Path> path = EditorContext.getFileSystemPath(file);
		if (!path.isPresent()) return Optional.absent();
		try {
			final Optional<String> contentType =
				Optional.fromNullable(Files.probeContentType(path.get()));
			if (!contentType.isPresent()) return Optional.absent();
			return Optional.fromNullable(MediaType.parse(contentType.get()));
		}
		catch (final IOException | SecurityException | IllegalArgumentException e) {
			EditorContext.LOGGER.log(Level.WARNING, e.getMessage(), e);
			return Optional.absent();
		}
	}

	private static Optional<Path>
	getFileSystemPath(final IFile file) {
		final Optional<String> path = EditorContext.getURIPath(file);
		if (!path.isPresent()) return Optional.absent();
		return Optional.fromNullable(FileSystems.getDefault().getPath(path.get()));
	}

	private static ObjectOutput
	getObjectOutput(final String filePath) throws Exception {
		return new ObjectOutputStream(new BufferedOutputStream(
			new FileOutputStream(filePath)));
	}

	private static boolean
	hasValidCharSet(final IFile file) {
		try {
			if (file.getCharset(false) != null) return true;
		}
		catch (final CoreException e) {
			EditorContext.LOGGER.log(Level.FINEST, e.getMessage(), e);
		}
		return false;
	}

	private static boolean
	isMediaTypeText(final IFile file) {
		try {
			final Optional<MediaType> mediaType = EditorContext.getMediaType(file);
			if (!mediaType.isPresent()) return false;
			return mediaType.get().is(MediaType.ANY_TEXT_TYPE);
		}
		catch (final Exception e) {
			return false;
		}
	}

	public static boolean
	isTextFile(final IFile file) {
		return EditorContext.isMediaTypeText(file)
			|| EditorContext.isContentTypeText(file)
			|| EditorContext.hasValidCharSet(file)
			|| file.isLinked();
	}

	private static InputStream
	newBufferInputStream(final String filePath) throws FileNotFoundException {
		return new BufferedInputStream(EditorContext.newFileInputStream(filePath));
	}

	private static InputStream
	newFileInputStream(final String filePath) throws FileNotFoundException {
		return new FileInputStream(filePath);
	}

	private static ObjectInput
	newObjectInputStream(final String filePath)
		throws FileNotFoundException,
			IOException {
		return new ObjectInputStream(EditorContext.newBufferInputStream(filePath));
	}

	private static Object
	readObjectInput(final ObjectInput input) {
		try {
			return input.readObject();
		}
		catch (final Exception e) {
			EditorContext.LOGGER.log(Level.FINEST, e.getMessage(), e);
		}
		finally {
			EditorContext.closeObjectInput(input);
		}
		return null;
	}

	private static ObjectInput
	tryToGetNewObjectInputStream(final String filePath) {
		try {
			return EditorContext.newObjectInputStream(filePath);
		}
		catch (final Exception e) {
			EditorContext.LOGGER.log(Level.FINEST, e.getMessage(), e);
		}
		return null;
	}

	private static void
	writeOutput(final Object serializable, final ObjectOutput output) {
		try {
			output.writeObject(serializable);
		}
		catch (final Exception e) {
			EditorContext.LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private static void
	writeSerializableToFile(final Object serializable, final ObjectOutput output) {
		EditorContext.writeOutput(serializable, output);
		EditorContext.closeOutput(output);
	}

	public static boolean
	isWindows() {
		return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
	}

	public static void
	cancelAllJobs() {
		EditorContext
			.cancelJobsBelongingTo(EditorContext.INDEX_RESOURCES_TASK, EditorContext.INDEX_WORKSPACE_RESOURCES_TASK, EditorContext.EMIT_INDEX_RESOURCE_TASK);
		EditorContext
			.cancelJobsBelongingTo(EditorContext.UPDATE_ACCESSED_FILES_TASK, EditorContext.EMIT_UPDATED_RECENT_FILES_TASK, EditorContext.CORE_WORKSPACE_INDEXER_TASK);
		EditorContext
			.cancelJobsBelongingTo(EditorContext.FILTER_RECENT_FILES_TASK, EditorContext.UPDATE_R_FILES_TASK);
	}

	public static void
	cancelJobsBelongingTo(final String... jobNames) {
		for (final String jobName : jobNames)
			EditorContext.JOB_MANAGER.cancel(jobName);
	}
}
