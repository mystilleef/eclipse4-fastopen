package com.laboki.eclipse.plugin.fastopen.opener.ui;

import java.util.List;
import java.util.regex.Pattern;

import lombok.Synchronized;
import lombok.val;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesResultEvent;
import com.laboki.eclipse.plugin.fastopen.events.ShowFastOpenDialogEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;
import com.laboki.eclipse.plugin.fastopen.opener.File;

public final class Dialog {

	private static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ | Pattern.UNICODE_CASE;
	private static final Pattern TEXT_PATTERN = Pattern.compile("\\p{Punct}*|\\w*| *", Dialog.PATTERN_FLAGS);
	private static final int HEIGHT = 480;
	private static final int WIDTH = Dialog.HEIGHT * 2;
	private static final int SPACING_SIZE_IN_PIXELS = 10;
	private static final Shell SHELL = new Shell(EditorContext.getEditor().getSite().getShell(), SWT.RESIZE | SWT.APPLICATION_MODAL);
	private static final Text TEXT = new Text(Dialog.SHELL, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH | SWT.NO_FOCUS);
	private static final TableViewer VIEWER = new TableViewer(Dialog.SHELL, SWT.VIRTUAL);

	public Dialog() {
		Dialog.arrangeWidgets();
		Dialog.setupDialog();
		Dialog.setupText();
		this.setupViewer();
		this.addListeners();
	}

	private static void openFiles() {
		for (final int index : Dialog.VIEWER.getTable().getSelectionIndices())
			Dialog.openFile(((File) Dialog.VIEWER.getElementAt(index)).getFile());
		Dialog.SHELL.close();
	}

	private static void openFile(final IFile file) {
		try {
			EditorContext.openEditor(file);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void backspace() {
		final int end = Dialog.TEXT.getCaretPosition();
		if (end < 1) return;
		// Dialog.TEXT.clearSelection();
		final int start = end - 1;
		Dialog.TEXT.setSelection(start, end);
		Dialog.TEXT.cut();
		Dialog.TEXT.setSelection(start, start);
		// Dialog.TEXT.clearSelection();
	}

	private static void updateText(final char character) {
		Dialog.TEXT.insert(String.valueOf(character));
	}

	private static void setupDialog() {
		Dialog.SHELL.setTabList(Lists.newArrayList(Dialog.VIEWER.getControl()).toArray(new Control[1]));
		Dialog.SHELL.setSize(Dialog.WIDTH, Dialog.HEIGHT);
	}

	private void addListeners() {
		Dialog.SHELL.addShellListener(new DialogShellListener());
		Dialog.VIEWER.getTable().addFocusListener(new ViewerFocusListener());
		Dialog.TEXT.addFocusListener(new TextFocusListener());
		Dialog.SHELL.addFocusListener(new TextFocusListener());
		Dialog.VIEWER.getTable().addKeyListener(new ShellKeyListener());
		Dialog.TEXT.addModifyListener(new TextModifyListener());
	}

	private static void arrangeWidgets() {
		Dialog.setDialogLayout();
		Dialog.setTextLayout();
		Dialog.setViewerLayout();
		Dialog.SHELL.pack();
	}

	private static void setDialogLayout() {
		final GridLayout layout = new GridLayout(1, true);
		Dialog.spaceDialogLayout(layout);
		Dialog.SHELL.setLayout(layout);
		Dialog.SHELL.setLayoutData(Dialog.createFillGridData());
	}

	private static void spaceDialogLayout(final GridLayout layout) {
		layout.marginLeft = Dialog.SPACING_SIZE_IN_PIXELS;
		layout.marginTop = Dialog.SPACING_SIZE_IN_PIXELS;
		layout.marginRight = Dialog.SPACING_SIZE_IN_PIXELS;
		layout.marginBottom = Dialog.SPACING_SIZE_IN_PIXELS;
		layout.horizontalSpacing = Dialog.SPACING_SIZE_IN_PIXELS;
		layout.verticalSpacing = Dialog.SPACING_SIZE_IN_PIXELS;
	}

	private static void setTextLayout() {
		final GridData textGridData = new GridData();
		textGridData.horizontalAlignment = GridData.FILL;
		textGridData.grabExcessHorizontalSpace = true;
		Dialog.TEXT.setLayoutData(textGridData);
	}

	private static void filterViewer() {
		val searchString = Dialog.TEXT.getText().trim();
		EventBus.post(new FilterRecentFilesEvent(searchString));
	}

	private static void setViewerLayout() {
		Dialog.VIEWER.getControl().setLayoutData(Dialog.createFillGridData());
	}

	private static GridData createFillGridData() {
		final GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		return gridData;
	}

	private static void setupText() {
		Dialog.TEXT.setMessage("start typing to filter files...");
	}

	private void setupViewer() {
		Dialog.VIEWER.setLabelProvider(this.new LabelProvider());
		Dialog.VIEWER.setContentProvider(this.new ContentProvider());
		Dialog.VIEWER.setUseHashlookup(true);
	}

	private static boolean isValidCharacter(final String character) {
		return Dialog.TEXT_PATTERN.matcher(character).matches();
	}

	@SuppressWarnings("static-method")
	@Subscribe
	@AllowConcurrentEvents
	public void showDialog(@SuppressWarnings("unused") final ShowFastOpenDialogEvent event) {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				Dialog.SHELL.open();
				Dialog.focusViewer();
			}
		});
	}

	@Subscribe
	@AllowConcurrentEvents
	public void fileResourcesChanged(final FileResourcesEvent event) {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				Dialog.this.updateViewer(event.getFiles());
			}
		});
	}

	@Subscribe
	@AllowConcurrentEvents
	public void fileResourcesChanged(final FilterRecentFilesResultEvent event) {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				Dialog.this.updateViewer(event.getFiles());
			}
		});
	}

	@Synchronized
	protected void updateViewer(final List<File> files) {
		Dialog.VIEWER.getControl().setRedraw(false);
		EditorContext.flushEvents();
		Dialog.VIEWER.getTable().clearAll();
		Dialog.VIEWER.setInput(files.toArray(new File[files.size()]));
		Dialog.VIEWER.setItemCount(files.size());
		Dialog.refresh();
		EditorContext.flushEvents();
		Dialog.VIEWER.getControl().setRedraw(true);
		Dialog.focusViewer();
	}

	private static void refresh() {
		Dialog.VIEWER.refresh(true, true);
	}

	private static void focusViewer() {
		Dialog._focusViewer();
		Dialog.VIEWER.getTable().setSelection(Dialog.VIEWER.getTable().getTopIndex());
	}

	private static void refocusViewer() {
		Dialog._focusViewer();
		Dialog.VIEWER.getTable().setSelection(Dialog.VIEWER.getTable().getSelectionIndex());
	}

	private static void _focusViewer() {
		Dialog.VIEWER.getTable().forceFocus();
	}

	private final class ContentProvider implements ILazyContentProvider {

		private File[] files;

		public ContentProvider() {}

		@Override
		public void dispose() {}

		@Override
		public void inputChanged(final Viewer arg0, final Object oldInput, final Object newInput) {
			this.files = (File[]) newInput;
		}

		@Override
		public void updateElement(final int index) {
			try {
				Dialog.VIEWER.replace(this.files[index], index);
			} catch (final Exception e) {}
		}
	}

	private final class LabelProvider implements ILabelProvider {

		public LabelProvider() {}

		@Override
		public Image getImage(final Object arg0) {
			return null;
		}

		@Override
		public String getText(final Object file) {
			val _file = (File) file;
			return _file.getName() + " - " + _file.getFolder();
		}

		@Override
		public void addListener(final ILabelProviderListener arg0) {}

		@Override
		public void dispose() {}

		@Override
		public boolean isLabelProperty(final Object arg0, final String arg1) {
			return false;
		}

		@Override
		public void removeListener(final ILabelProviderListener arg0) {}
	}

	private final class DialogShellListener implements ShellListener {

		public DialogShellListener() {}

		@Override
		public void shellActivated(final ShellEvent arg0) {
			EditorContext.asyncExec(new Task("") {

				@Override
				public void execute() {
					Dialog.focusViewer();
				}
			});
		}

		@Override
		public void shellClosed(final ShellEvent event) {
			event.doit = false;
			Dialog.SHELL.setVisible(false);
			Dialog.reset();
		}

		@Override
		public void shellDeactivated(final ShellEvent arg0) {
			EditorContext.asyncExec(new Task("") {

				@Override
				public void execute() {
					Dialog.refresh();
				}
			});
		}

		@Override
		public void shellDeiconified(final ShellEvent arg0) {}

		@Override
		public void shellIconified(final ShellEvent arg0) {}
	}

	private final class TextFocusListener implements FocusListener {

		public TextFocusListener() {}

		@Override
		public void focusGained(final FocusEvent arg0) {
			Dialog.refocusViewer();
		}

		@Override
		public void focusLost(final FocusEvent arg0) {}
	}

	private final class ViewerFocusListener implements FocusListener {

		public ViewerFocusListener() {}

		@Override
		public void focusGained(final FocusEvent arg0) {}

		@Override
		public void focusLost(final FocusEvent arg0) {
			EditorContext.asyncExec(new Task("") {

				@Override
				public void execute() {
					Dialog.refocusViewer();
				}
			});
		}
	}

	private final class ShellKeyListener implements KeyListener {

		public ShellKeyListener() {}

		@Override
		public void keyPressed(final KeyEvent event) {
			if (Dialog.isValidCharacter(String.valueOf(event.character))) {
				event.doit = false;
				EditorContext.asyncExec(new Task("") {

					@Override
					public void execute() {
						Dialog.updateText(event.character);
					}
				});
			} else if (event.keyCode == SWT.BS) {
				event.doit = false;
				EditorContext.asyncExec(new Task("") {
					;

					@Override
					public void execute() {
						Dialog.backspace();
					}
				});
			} else if ((event.keyCode == SWT.CR) || (event.keyCode == SWT.KEYPAD_CR)) {
				event.doit = false;
				EditorContext.asyncExec(new Task("") {

					@Override
					public void execute() {
						Dialog.openFiles();
					}
				});
			}
		}

		@Override
		public void keyReleased(final KeyEvent event) {}
	}

	private final class TextModifyListener implements ModifyListener {

		public TextModifyListener() {}

		@Override
		public void modifyText(final ModifyEvent arg0) {
			EditorContext.asyncExec(new Task("") {

				@Override
				public void execute() {
					Dialog.filterViewer();
				}
			});
		}
	}

	public static void reset() {
		Dialog.TEXT.setText("");
	}

	@SuppressWarnings("unused")
	private static void delete() {
		final int start = Dialog.TEXT.getCaretPosition();
		Dialog.TEXT.setSelection(start, start + 1);
		Dialog.TEXT.cut();
	}

	@SuppressWarnings("unused")
	private static void moveCaretForward() {
		val position = Dialog.TEXT.getCaretPosition() + 1;
		Dialog.TEXT.clearSelection();
		Dialog.TEXT.setSelection(position, position);
	}

	@SuppressWarnings("unused")
	private static void moveCaretBackward() {
		val position = Dialog.TEXT.getCaretPosition() - 1;
		Dialog.TEXT.clearSelection();
		Dialog.TEXT.setSelection(position, position);
	}

	@SuppressWarnings("unused")
	private static void moveCaretToStart() {
		Dialog.TEXT.clearSelection();
		Dialog.TEXT.setSelection(0, 0);
	}

	@SuppressWarnings("unused")
	private static void moveCaretToEnd() {
		val position = Dialog.TEXT.getCharCount();
		Dialog.TEXT.clearSelection();
		Dialog.TEXT.setSelection(position, position);
	}
}
