/*
 * www.javagl.de - DependencyCleaner
 *
 * Copyright (c) 2018 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.dependencycleaner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import de.javagl.common.ui.JTables;
import de.javagl.common.ui.JTrees;
import de.javagl.common.ui.LocationBasedPopupHandler;
import de.javagl.swing.tasks.SwingTask;
import de.javagl.swing.tasks.SwingTaskExecutors;
import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;

/**
 * The main class of the dependency cleaner application. Hence the name.
 */
class DependencyCleanerApplication
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(DependencyCleanerApplication.class.getName());
    
    /**
     * The main frame
     */
    private JFrame frame;
    
    /**
     * A container for the tree table showing the dependencies
     */
    private JPanel treeTableContainer;

    /**
     * The label for dropping the POM
     */
    private JLabel pomDropLabel;
    
    /**
     * The current {@link ArtifactInfo} instances
     */
    private List<? extends ArtifactInfo> artifactInfos;
    
    /**
     * The current {@link DependencyStatus}
     */
    private DependencyStatus dependencyStatus;
    
    /**
     * Default constructor
     */
    DependencyCleanerApplication()
    {
        frame = new JFrame("DependencyCleaner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel mainPanel = createMainPanel();
        frame.getContentPane().add(mainPanel);
        
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
    }
    
    /**
     * Returns the main frame
     * 
     * @return The main frame
     */
    JFrame getFrame()
    {
        return frame;
    }
    
    /**
     * Creates the main panel 
     * 
     * @return The main panel
     */
    private JPanel createMainPanel()
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        treeTableContainer = new JPanel(new GridLayout(1,1));
        mainPanel.add(treeTableContainer, BorderLayout.CENTER);
        setDependencyStatus(null);
        
        return mainPanel;
    }
    
    
    /**
     * Creates the control panel
     * 
     * @return The control panel
     */
    private JPanel createControlPanel()
    {
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        JLabel helpLabel = new JLabel(
            "<html>"
            + "1. Drag-and-drop the pom.xml into the area below." + "<br>"
            + "2. Press 'Resolve' to resolve the dependencies "
            + "of the POM" + "<br>"
            + "3. See the dependency structure of the POM, and invalid "
            + "JAR files being highlighted in the table." + "<br>"
            + "4. Press 'Remove selected' to remove the JAR files that are " 
            + "marked for removal in the table." + "<br>"
            + "5. Press 'Resolve' again to download fresh copies of the "
            + "JARs from Maven Central" + "<br>"
            + "<br>"
            + "By default, all invalid JAR files will be marked "
            + "for removal. Right-click on the 'path' entry in the "
            + "table to open the directory that contains the JAR." + "<br>"
            + "</html>");
        helpLabel.setBorder(BorderFactory.createTitledBorder("Instructions:"));
        controlPanel.add(helpLabel, BorderLayout.NORTH);
        
        JPanel inputPanel = new JPanel(new GridLayout(1,1));
        
        pomDropLabel = new JLabel("");
        pomDropLabel.setBorder(BorderFactory.createTitledBorder(
            "Drag and drop your pom.xml here:"));
        pomDropLabel.setPreferredSize(new Dimension(400, 60));
        
        FileTransferHandler transferHandler = 
            new FileTransferHandler(this::acceptPomFiles);
        pomDropLabel.setTransferHandler(transferHandler);
        inputPanel.add(pomDropLabel);
        
        controlPanel.add(inputPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton resolveButton = new JButton("Resolve");
        resolveButton.addActionListener(
            e ->  resolveDependenciesInBackground());
        buttonPanel.add(resolveButton);
        
        JButton removeButton = new JButton("Remove selected");
        removeButton.addActionListener(
            e -> removeDependenciesInBackground());
        buttonPanel.add(removeButton);

        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return controlPanel;
    }

    /**
     * Accept the given files (from a drag-and-drop operation), and pass
     * the first one to {@link #loadPomInBackground(Path)}
     * 
     * @param files The files
     */
    private void acceptPomFiles(List<? extends File> files) 
    {
        pomDropLabel.setText("");
        setArtifactInfos(null);
        if (!files.isEmpty())
        {
            File file = files.get(0);
            pomDropLabel.setText(file.toString());
            loadPomInBackground(file.toPath());
        }
    }
    
    /**
     * Load the POM from the given path, in a background thread, and pass
     * the extracted {@link ArtifactInfo} objects to {@link #setArtifactInfos}
     * 
     * @param path The path
     */
    private void loadPomInBackground(Path path)
    {
        SwingTask<?, ?> swingTask = new SwingTask<List<ArtifactInfo>, Void>()
        {
            @Override
            protected List<ArtifactInfo> doInBackground() throws Exception
            {
                List<ArtifactInfo> artifactInfos = 
                    MavenModelUtils.readDependencyArtifactInfos(path);
                return artifactInfos;
            }
            
            @Override
            protected void done()
            {
                try
                {
                    setArtifactInfos(get());
                }
                catch (InterruptedException | ExecutionException e)
                {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    setArtifactInfos(null);
                }
            }
        };
        SwingTaskExecutors.create(swingTask).build().execute();
    }
    
    /**
     * Set the {@link ArtifactInfo} objects that are currently displayed
     * 
     * @param artifactInfos The {@link ArtifactInfo} objects
     */
    private void setArtifactInfos(List<? extends ArtifactInfo> artifactInfos)
    {
        this.artifactInfos = artifactInfos;
    }
    
    /**
     * Resolve the dependencies of the current artifact in a background
     * thread, and pass the resulting {@link DependencyStatus} to 
     * {@link #setDependencyStatus(DependencyStatus)}.<br>
     * <br>
     * If any of the text fields is empty, nothing is done.
     */
    private void resolveDependenciesInBackground()
    {
        if (artifactInfos == null)
        {
            setDependencyStatus(null);
            return;
        }
        resolveDependenciesInBackground(artifactInfos);
    }

    /**
     * Resolve the dependencies described by the given {@link ArtifactInfo} 
     * objects in a background thread, and pass the resulting 
     * {@link DependencyStatus} 
     * to {@link #setDependencyStatus(DependencyStatus)}
     * 
     * @param artifactInfos The {@link ArtifactInfo} objects
     */
    private void resolveDependenciesInBackground(
        List<? extends ArtifactInfo> artifactInfos)
    {
        SwingTask<?, ?> swingTask = new SwingTask<Void, Void>()
        {
            /**
             * The computed DependencyStatus
             */
            private DependencyStatus dependencyStatus;
            
            @Override
            protected Void doInBackground() throws Exception
            {
                dependencyStatus = null;
                try
                {
                    dependencyStatus =
                        DependencyStatuses.compute(artifactInfos);
                }
                catch (Throwable e)
                {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    throw new Exception(e);
                }
                return null;
            }
            
            @Override
            protected void done()
            {
                setDependencyStatus(dependencyStatus);
            }

        };
        SwingTaskExecutors.create(swingTask)
            .setTitle("Resolving dependencies")
            .setDialogUncaughtExceptionHandler()
            .build()
            .execute();
    }
    
    
    /**
     * Remove all dependencies that are selected for removal in the current
     * {@link DependencyStatus}
     */
    private void removeDependenciesInBackground()
    {
        if (dependencyStatus == null)
        {
            return;
        }
        Set<Path> pathsToRemove = dependencyStatus.getPathsToRemove();
        SwingTask<?, ?> swingTask = new SwingTask<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                try
                {
                    deleteFiles(pathsToRemove);
                }
                catch (Throwable e)
                {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    throw new Exception(e);
                }
                return null;
            }
            
            @Override
            protected void done()
            {
                setDependencyStatus(dependencyStatus);
            }

        };
        SwingTaskExecutors.create(swingTask)
            .setTitle("Deleting invalid dependencies")
            .setDialogUncaughtExceptionHandler()
            .build()
            .execute();
        
    }
    
    /**
     * Delete the files with the given paths, and update the current
     * {@link DependencyStatus} accordingly
     * 
     * @param paths The paths
     */
    private void deleteFiles(Iterable<? extends Path> paths)
    {
        for (Path path : paths)
        {
            boolean deleted = path.toFile().delete();
            if (!deleted)
            {
                logger.warning("Could not delete " + path);
            }
            else
            {
                dependencyStatus.setValidPath(path, null);
                dependencyStatus.setPathToRemove(path, false);
            }
        }
    }
    

    /**
     * Set the {@link DependencyStatus} that is currently shown in the
     * tree table
     * 
     * @param dependencyStatus The {@link DependencyStatus}
     */
    private void setDependencyStatus(
        DependencyStatus dependencyStatus)
    {
        this.dependencyStatus = dependencyStatus;
        
        TreeTableModel treeTableModel = 
            DependencyStatusTreeTableModels.create(dependencyStatus); 
        JTreeTable treeTable = new JTreeTable(treeTableModel);
        JTrees.expandAll(treeTable.getTree());
        JTables.adjustColumnWidths(treeTable, 600);
        
        TableColumn validColumn = treeTable.getColumnModel().getColumn(4);
        validColumn.setCellRenderer(new DefaultTableCellRenderer()
        {
            /**
             * Serial UID
             */
            private static final long serialVersionUID = 7236093182616007757L;

            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column)
            {
                super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                if (Boolean.FALSE.equals(value))
                {
                    setBackground(Color.RED);
                    setText("false");
                }
                else if (value == null && row != 0)
                {
                    setBackground(Color.ORANGE);
                    setText("missing");
                }
                else
                {
                    setBackground(table.getBackground());
                    setText("true");
                }
                return this;
            }
        });
        
        TableColumn pathColumn = treeTable.getColumnModel().getColumn(6);
        pathColumn.setPreferredWidth(100);
        
        
        JPopupMenu popupMenu = new JPopupMenu();
        treeTable.addMouseListener(new LocationBasedPopupHandler(popupMenu));

        int fileNameColumnIndex = 6;
        Action openContainingFolderAction =
            new OpenFolderAction(fileNameColumnIndex);
        popupMenu.add(new JMenuItem(openContainingFolderAction));
        
        
        treeTableContainer.removeAll();
        treeTableContainer.add(new JScrollPane(treeTable));
        treeTableContainer.revalidate();
        treeTableContainer.repaint();
    }
    
    
}