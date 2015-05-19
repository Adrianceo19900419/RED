package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.InputLoadingFormComposite;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateSettingKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.cmd.DeleteSettingKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class ImportLibraryComposite extends InputLoadingFormComposite {

    private final RobotEditorCommandsStack commandsStack;
    private final RobotSuiteFile fileModel;
    private TableViewer leftViewer;
    private TableViewer rightViewer;

    public ImportLibraryComposite(final Composite parent, final RobotEditorCommandsStack commandsStack,
            final RobotSuiteFile fileModel) {
        super(parent, SWT.NONE, "Import");
        this.commandsStack = commandsStack;
        this.fileModel = fileModel;
        createComposite();
    }

    @Override
    protected Composite createControl(final Composite parent) {
        setFormImage(RobotImages.getLibraryImage());

        final Composite actualComposite = getToolkit().createComposite(parent);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(3, 3).extendedMargins(0, 0, 0, 3)
                .applyTo(actualComposite);

        final Label titleLabel = getToolkit().createLabel(actualComposite,
                "Standard libraries available in '" + fileModel.getProject().getName() + "'");
        titleLabel.setFont(JFaceResources.getBannerFont());
        titleLabel.setForeground(getToolkit().getColors().getColor(IFormColors.TITLE));
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).hint(500, SWT.DEFAULT).applyTo(titleLabel);

        leftViewer = new TableViewer(actualComposite);
        leftViewer.setContentProvider(new LibrariesToImportContentProvider());
        GridDataFactory.fillDefaults().span(1, 2).grab(true, false).hint(220, 150).applyTo(leftViewer.getControl());
        ViewerColumnsFactory.newColumn("").withWidth(200).labelsProvidedBy(new LibrariesLabelProvider())
                .createFor(leftViewer);

        final Button toImported = getToolkit().createButton(actualComposite, ">>", SWT.PUSH);
        toImported.setEnabled(false);
        toImported.addSelectionListener(createToImportedListener());
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(toImported);

        rightViewer = new TableViewer(actualComposite);
        rightViewer.setContentProvider(new LibrariesAlreadyImportedContentProvider());
        GridDataFactory.fillDefaults().span(1, 2).grab(true, false).hint(220, 150).applyTo(rightViewer.getControl());
        ViewerColumnsFactory.newColumn("").withWidth(200).labelsProvidedBy(new LibrariesLabelProvider())
                .createFor(rightViewer);

        final Button fromImported = getToolkit().createButton(actualComposite, "<<", SWT.PUSH);
        fromImported.setEnabled(false);
        fromImported.addSelectionListener(createFromImportedListener());
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(fromImported);

        final Label separator = getToolkit().createLabel(actualComposite, "", SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().indent(0, 5).grab(false, false).span(3, 1).applyTo(separator);
        final Label tooltipLabel = getToolkit().createLabel(actualComposite,
                "Choose libraries to import. Only libraries which are imported by project are available. "
                        + "Edit project properties if you need other libraries",
                SWT.WRAP);
        GridDataFactory.fillDefaults().span(3, 1).hint(500, SWT.DEFAULT).applyTo(tooltipLabel);

        createViewerSelectionListener(leftViewer, toImported);
        createViewerSelectionListener(rightViewer, fromImported);

        return actualComposite;
    }

    private SelectionListener createToImportedListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Libraries libs = (Libraries) leftViewer.getInput();
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) leftViewer.getSelection(), LibrarySpecification.class);

                libs.getLibrariesToImport().removeAll(specs);
                libs.getImportedLibraries().addAll(specs);

                final Optional<RobotElement> section = fileModel.findSection(RobotSuiteSettingsSection.class);
                final RobotSuiteSettingsSection settingsSection = (RobotSuiteSettingsSection) section.get();
                for (final LibrarySpecification spec : specs) {
                    commandsStack.execute(new CreateSettingKeywordCall(settingsSection, "Library", newArrayList(spec
                            .getName())));
                }

                leftViewer.refresh();
                rightViewer.refresh();
            }
        };
    }

    private SelectionListener createFromImportedListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Libraries libs = (Libraries) rightViewer.getInput();
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) rightViewer.getSelection(), LibrarySpecification.class);

                libs.getImportedLibraries().removeAll(specs);
                libs.getLibrariesToImport().addAll(specs);

                final Optional<RobotElement> section = fileModel.findSection(RobotSuiteSettingsSection.class);
                final RobotSuiteSettingsSection settingsSection = (RobotSuiteSettingsSection) section.get();
                final List<RobotSetting> settingsToRemove = getSettingsToRemove(settingsSection, specs);
                commandsStack.execute(new DeleteSettingKeywordCall(settingsToRemove));

                leftViewer.refresh();
                rightViewer.refresh();
            }

            private List<RobotSetting> getSettingsToRemove(final RobotSuiteSettingsSection settingsSection,
                    final List<LibrarySpecification> specs) {
                final List<RobotSetting> settings = newArrayList();
                final List<String>specNames = Lists.transform(specs, new Function<LibrarySpecification, String>(){
                    @Override
                    public String apply(final LibrarySpecification spec) {
                        return spec.getName();
                    }}); 
                for (final RobotElement element : settingsSection.getImportSettings()) {
                    final RobotSetting setting = (RobotSetting) element;
                    final String name = setting.getArguments().isEmpty() ? null : setting.getArguments().get(0);
                    if (specNames.contains(name)) {
                        settings.add(setting);
                    }
                }
                return settings;
            }
        };
    }

    private void createViewerSelectionListener(final TableViewer viewer, final Button buttonToActivate) {
        final ISelectionChangedListener listener = new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) event.getSelection(), LibrarySpecification.class);
                buttonToActivate.setEnabled(!specs.isEmpty() && doesNotContainAlwaysAccessible(specs));
            }

            private boolean doesNotContainAlwaysAccessible(final List<LibrarySpecification> specs) {
                for (final LibrarySpecification spec : specs) {
                    if (spec.isAccessibleWithoutImport()) {
                        return false;
                    }
                }
                return true;
            }
        };
        viewer.addSelectionChangedListener(listener);
        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                viewer.removeSelectionChangedListener(listener);
            }
        });
    }

    @Override
    protected void createActions() {
        // nothing to do
    }

    @Override
    protected InputLoadingFormComposite.InputJob provideInputCollectingJob() {
        return new InputJob("Gathering resources for import") {

            @Override
            protected Object createInput(final IProgressMonitor monitor) {
                setStatus(Status.OK_STATUS);
                return Libraries.create(fileModel);
            }
        };
    }

    @Override
    protected void fillControl(final Object jobResult) {
        final Libraries libs = (Libraries) jobResult;
        leftViewer.setInput(libs);
        rightViewer.setInput(libs);
    }

    private static class LibrariesLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {
        @Override
        public Image getImage(final Object element) {
            return RobotImages.getBookImage().createImage();
        }

        @Override
        public String getText(final Object element) {
            return ((LibrarySpecification) element).getName();
        }

        @Override
        public StyledString getStyledText(final Object element) {
            final LibrarySpecification spec = (LibrarySpecification) element;
            final StyledString text = new StyledString(spec.getName()) ;
            if (spec.isAccessibleWithoutImport()) {
                text.append(" ");
                text.append("always accessible", new Styler() {
                    @Override
                    public void applyStyles(final TextStyle textStyle) {
                        textStyle.foreground = RobotTheme.getEclipseDecorationColor();
                    }
                });
            }
            return text;
        }
    }
}
