package de.finnos.southparkdownloader.gui.components;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FileChooser extends JPanel {

    private final Consumer<String> setter;
    private final Supplier<String> getter;

    private JTextField textFieldPath;
    private JButton buttonOpen;

    public FileChooser(Consumer<String> setter, Supplier<String> getter){
        super(new MigLayout());
        this.setter = setter;
        this.getter = getter;
        init();
    }

    private void init () {
        setLayout(new MigLayout("insets 0"));

        textFieldPath = new JTextField();
        textFieldPath.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                write();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                write();
            }

            @Override
            public void changedUpdate(DocumentEvent e) { }
        });

        buttonOpen = new JButton("...");
        buttonOpen.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("EXECUTABLES", "exe"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.FILES_ONLY) {
                textFieldPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        add(textFieldPath, "w 100%");
        add(buttonOpen);

        read();
    }

    private void write () {
        final File ffmpegFile = new File(textFieldPath.getText());
        if (ffmpegFile.exists()) {
            setter.accept(ffmpegFile.getAbsolutePath());
        }
    }

    private void read () {
        textFieldPath.setText(getter.get());
    }
}
