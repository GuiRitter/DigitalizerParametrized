package io.github.guiritter.digitalizer_parametrized;

import javax.swing.plaf.basic.BasicArrowButton;

import static io.github.guiritter.graphical_user_interface.LabelledComponentFactory.buildFileChooser;
import static io.github.guiritter.graphical_user_interface.LabelledComponentFactory.buildLabelledComponent;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.OffsetDateTime;
import java.util.IllegalFormatException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.wia.api.Wia4j;
import com.wia.api.WiaOperationException;

import io.github.guiritter.graphical_user_interface.FileChooserResponse;;

public class DigitalizerParametrized {

	private static final int HALF_PADDING = 5;

	private static final int FULL_PADDING = 2 * HALF_PADDING;

	private static File outputFile;

	private static File outputFolder;

	private static String outputName;

	private static Wia4j wia4j;

	static {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
	}

	private static final GridBagConstraints buildGBC(int y, int topPadding, int bottomPadding) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(topPadding, FULL_PADDING, bottomPadding, FULL_PADDING);
		return gbc;
	}

	private static void treatSelectedFile(FileChooserResponse response, AtomicReference<File> file) {
		if (
			(response.state == JFileChooser.APPROVE_OPTION)
			&& (response.selectedFile != null)
		) {
			file.set(response.selectedFile);
		} else {
			file.set(null);
		}
	}

	private static String replaceTimeStamp(String format) {
		return format.replaceAll("%z", OffsetDateTime.now().toString()).replaceAll(":", "：");
	}

	private static String getFormattedFileName(JTextField outputFileNameFormatField, JSpinner outputFileNameIndexSpinner) {
		return String.format(replaceTimeStamp(outputFileNameFormatField.getText()), outputFileNameIndexSpinner.getValue());
	} 

	private static void testFormat(JFrame frame, JTextField outputFileNameFormatField, JSpinner outputFileNameIndexSpinner) {
		showMessageDialog(frame, getFormattedFileName(outputFileNameFormatField, outputFileNameIndexSpinner), "Info", INFORMATION_MESSAGE);
	}

	private static void scanImage(AtomicReference<File> file, JFrame frame, JTextField outputFileNameFormatField, JSpinner outputFileNameIndexSpinner) {
		try {
			outputFolder = file.get();
			if (outputFolder == null) {
				showMessageDialog(frame, "Choose a folder to write the scanned images to.", "Reminder", WARNING_MESSAGE);
				return;
			}
			try {
				outputName = getFormattedFileName(outputFileNameFormatField, outputFileNameIndexSpinner);
			} catch (IllegalFormatException ex) {
				showMessageDialog(frame, "The format used for the output image name is invalid.", "Warning", WARNING_MESSAGE);
				return;
			}
			outputFile = file.get().toPath().resolve(outputName).toFile();
			try {
				if (outputFile.createNewFile()) {
					outputFile.delete();
				} else {
					showMessageDialog(frame, "File " + outputFile.getAbsolutePath() + " already exists.", "Reminder", WARNING_MESSAGE);
					return;
				}
			} catch (Exception ex) {
				showMessageDialog(frame, "Can't write to " + outputFile.getAbsolutePath(), "Warning", WARNING_MESSAGE);
				return;
			}
			try {
				wia4j.scan(outputFile.getAbsolutePath());
			} catch (WiaOperationException ex) {
				ex.printStackTrace();
				showMessageDialog(frame, ex.getMessage(), "Error", ERROR_MESSAGE);
			}
			((BasicArrowButton) outputFileNameIndexSpinner.getComponent(0)).doClick();
		} catch (Exception ex) {
			ex.printStackTrace();
			showMessageDialog(frame, ex.getMessage(), "Error", ERROR_MESSAGE);
		}
	}

	public static void main(String args[]) {
		var frame = new JFrame("Digitalizer Parametrized");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridBagLayout());

		int y = 0;

		var file = new AtomicReference<File>();
		frame.getContentPane().add(buildFileChooser(
				"Select Output Path",
				SwingConstants.CENTER,
				SwingConstants.LEFT,
				0,
				JFileChooser.DIRECTORIES_ONLY,
				(FileChooserResponse response) -> treatSelectedFile(response, file)
		), buildGBC(y++, FULL_PADDING, HALF_PADDING));
		
		var outputFileNameFormatField = new JTextField("%d.png");
		outputFileNameFormatField.setToolTipText("Use %z for an ISO 8601 timestamp at file creation time.");

		frame.getContentPane().add(buildLabelledComponent(
				"Name Format",
				outputFileNameFormatField,
				SwingConstants.CENTER,
				SwingConstants.LEFT,
				0
		), buildGBC(y++, HALF_PADDING, HALF_PADDING));

		var outputFileNameIndexSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

		frame.getContentPane().add(buildLabelledComponent(
				"Name Index",
				outputFileNameIndexSpinner,
				SwingConstants.CENTER,
				SwingConstants.LEFT,
				0
		), buildGBC(y++, HALF_PADDING, HALF_PADDING));

		var testButton = new JButton("Test Format");
		testButton.addActionListener((ActionEvent event) -> testFormat(frame, outputFileNameFormatField, outputFileNameIndexSpinner));

		frame.getContentPane().add(testButton, buildGBC(y++, HALF_PADDING, HALF_PADDING));

		wia4j = new Wia4j();

		var scanButton = new JButton("Scan");
		scanButton.addActionListener((ActionEvent event) -> scanImage(file, frame, outputFileNameFormatField, outputFileNameIndexSpinner));

		frame.getContentPane().add(scanButton, buildGBC(y++, HALF_PADDING, FULL_PADDING));

		frame.setVisible(true);
		frame.pack();
		frame.setLocationRelativeTo(null);
	}
}
