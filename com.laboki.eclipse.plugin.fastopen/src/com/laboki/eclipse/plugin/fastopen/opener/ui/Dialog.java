package com.laboki.eclipse.plugin.fastopen.opener.ui;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.java.Log;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
import com.laboki.eclipse.plugin.fastopen.opener.resources.RFile;

@Log
public final class Dialog {

	private static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ | Pattern.UNICODE_CASE;
	private static final Pattern TEXT_PATTERN = Pattern.compile("\\p{Punct}*|\\w*| *", Dialog.PATTERN_FLAGS);
	private static final int HEIGHT = 480;
	private static final int WIDTH = Dialog.HEIGHT * 2;
	private static final int SPACING_SIZE_IN_PIXELS = 10;
	private static final Shell SHELL = new Shell(EditorContext.getShell(), SWT.RESIZE | SWT.APPLICATION_MODAL);
	private static final Text TEXT = new Text(Dialog.SHELL, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH | SWT.NO_FOCUS);
	private static final TableViewer VIEWER = new TableViewer(Dialog.SHELL, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

	public Dialog() {
		Dialog.arrangeWidgets();
		Dialog.setupDialog();
		Dialog.setupText();
		this.setupViewer();
		this.addListeners();
	}

	private static void openFiles() {
		for (final int index : Dialog.VIEWER.getTable().getSelectionIndices())
			EditorContext.asyncExec(new Task("") {

				@Override
				public void execute() {
					Dialog.openFile(((RFile) Dialog.VIEWER.getElementAt(index)).getFile());
				}
			});
	}

	private static void openFile(final IFile file) {
		try {
			EditorContext.openEditor(file);
		} catch (final Exception e) {
			Dialog.openLink(file);
		}
	}

	private static void openLink(final IFile file) {
		try {
			EditorContext.openLink(file);
		} catch (final Exception e) {
			Dialog.log.log(Level.SEVERE, "Failed to open linked file", e);
		}
	}

	private static void closeFiles() {
		for (final int index : Dialog.VIEWER.getTable().getSelectionIndices())
			EditorContext.asyncExec(new Task("") {

				@Override
				public void execute() {
					EditorContext.closeEditor(((RFile) Dialog.VIEWER.getElementAt(index)).getFile());
				}
			});
	}

	private static void backspace() {
		final int end = Dialog.TEXT.getCaretPosition();
		if (end < 1) return;
		final int start = end - 1;
		Dialog.TEXT.setSelection(start, end);
		Dialog.TEXT.cut();
		Dialog.TEXT.setSelection(start, start);
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
		Dialog.VIEWER.getTable().setLinesVisible(true);
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
				Dialog.this.updateViewer(event.getRFiles());
			}
		});
	}

	@Subscribe
	@AllowConcurrentEvents
	public void fileResourcesChanged(final FilterRecentFilesResultEvent event) {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				Dialog.this.updateViewer(event.getRFiles());
			}
		});
	}

	@Synchronized
	protected void updateViewer(final List<RFile> rFiles) {
		Dialog.VIEWER.getControl().setRedraw(false);
		EditorContext.flushEvents();
		Dialog.VIEWER.getTable().clearAll();
		Dialog.VIEWER.setInput(rFiles.toArray(new RFile[rFiles.size()]));
		Dialog.VIEWER.setItemCount(rFiles.size());
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

		private RFile[] rFiles;

		public ContentProvider() {}

		@Override
		public void dispose() {}

		@Override
		public void inputChanged(final Viewer arg0, final Object oldInput, final Object newInput) {
			this.rFiles = (RFile[]) newInput;
		}

		@Override
		public void updateElement(final int index) {
			try {
				Dialog.VIEWER.replace(this.rFiles[index], index);
			} catch (final Exception e) {
				Dialog.log.log(Level.FINE, "Failed to update index for lazy content provider.", e);
			}
		}
	}

	private final class LabelProvider extends StyledCellLabelProvider {

		StyledString.Styler filenameStyler = this.styler(FONT.LARGE_BOLD_FONT, null);
		StyledString.Styler inStyler = this.styler(FONT.ITALIC_FONT, this.color(SWT.COLOR_GRAY));
		StyledString.Styler folderStyler = this.styler(null, this.color(SWT.COLOR_DARK_GRAY));
		StyledString.Styler modifiedStyler = this.styler(FONT.SMALL_ITALIC_FONT, this.color(SWT.COLOR_GRAY));
		StyledString.Styler timeStyler = this.styler(FONT.SMALL_BOLD_FONT, this.color(SWT.COLOR_DARK_RED));
		StyledString.Styler typeStyler = this.styler(FONT.SMALL_BOLD_FONT, this.color(SWT.COLOR_DARK_BLUE));

		public LabelProvider() {}

		@Override
		public void update(final ViewerCell cell) {
			val file = (RFile) cell.getElement();
			final StyledString text = new StyledString();
			text.append(file.getName() + "\n", this.filenameStyler);
			text.append("in  ", this.inStyler);
			text.append(file.getFolder() + "\n", this.folderStyler);
			text.append("modified  ", this.modifiedStyler);
			text.append(file.getModificationTime() + "  ", this.timeStyler);
			text.append(file.getContentTypeString(), this.typeStyler);
			cell.setText(text.toString());
			cell.setImage(file.getContentTypeImage());
			cell.setStyleRanges(text.getStyleRanges());
			super.update(cell);
		}

		private StyledString.Styler styler(final Font font, final Color color) {
			return new StyledString.Styler() {

				@Override
				public void applyStyles(final TextStyle textStyle) {
					textStyle.font = font;
					textStyle.foreground = color;
				}
			};
		}

		private Color color(final int color) {
			return Display.getCurrent().getSystemColor(color);
		}
	}

	private enum FONT {
		FONT;

		private static final FontData[] FONT_DATAS = Dialog.VIEWER.getTable().getFont().getFontData();
		public static final Font LARGE_BOLD_FONT = FONT.makeLargeBoldFont();
		public static final Font ITALIC_FONT = FONT.makeItalicizedFont();
		public static final Font SMALL_ITALIC_FONT = FONT.makeSmallItalicizedFont();
		public static final Font SMALL_BOLD_FONT = FONT.makeSmallBoldFont();

		private static Font makeLargeBoldFont() {
			return new Font(EditorContext.getDisplay(), FONT.getDefaultFontName(), FONT.getDefaultFontHeight() + 2, SWT.BOLD);
		}

		private static Font makeItalicizedFont() {
			return new Font(EditorContext.getDisplay(), FONT.getDefaultFontName(), FONT.getDefaultFontHeight(), SWT.ITALIC);
		}

		private static Font makeSmallItalicizedFont() {
			return new Font(EditorContext.getDisplay(), FONT.getDefaultFontName(), FONT.getDefaultFontHeight() - 2, SWT.ITALIC);
		}

		private static Font makeSmallBoldFont() {
			return new Font(EditorContext.getDisplay(), FONT.getDefaultFontName(), FONT.getDefaultFontHeight() - 2, SWT.BOLD);
		}

		public static int getDefaultFontHeight() {
			return FONT.FONT_DATAS[0].getHeight();
		}

		public static String getDefaultFontName() {
			return FONT.FONT_DATAS[0].getName();
		}
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
						Dialog.SHELL.close();
						Dialog.openFiles();
					}
				});
			} else if ((event.keyCode == SWT.DEL)) {
				event.doit = false;
				EditorContext.asyncExec(new Task("") {

					@Override
					public void execute() {
						Dialog.SHELL.close();
						Dialog.closeFiles();
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
}
