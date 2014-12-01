package org.fao.unredd.adjuster;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UI {

	final static JFileChooser chooser = new JFileChooser();

	public static void main(String[] args) {
		final JFrame frame = new JFrame();
		Container framePanel = frame.getContentPane();
		final JPanel adjusting = getFilePanel("SHP a ajustar",
				JFileChooser.FILES_AND_DIRECTORIES);
		final JPanel reference = getFilePanel("SHP de referencia",
				JFileChooser.FILES_AND_DIRECTORIES);
		final JPanel result = getFilePanel("Resultado",
				JFileChooser.FILES_AND_DIRECTORIES);
		framePanel.setLayout(new GridLayout(4, 1));
		framePanel.add(adjusting);
		framePanel.add(reference);
		framePanel.add(result);
		JButton btnExecute = new JButton("Ejecutar");
		btnExecute.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String referencePath = ((JTextField) reference.getComponent(0))
						.getText();
				String adjustingPath = ((JTextField) adjusting.getComponent(0))
						.getText();
				String resultPath = ((JTextField) result.getComponent(0))
						.getText();
				try {
					Adjuster.adjust(referencePath, adjustingPath, resultPath);
					JOptionPane.showMessageDialog(frame, "Terminado");
				} catch (Exception exception) {
					JOptionPane.showMessageDialog(frame,
							exception.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		framePanel.add(btnExecute);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

	}

	private static JPanel getFilePanel(String title, int fileMode) {
		JPanel pnl = new JPanel();
		JTextField txtFile = new JTextField(50);
		pnl.add(txtFile);
		pnl.add(createExaminar(txtFile, fileMode));
		pnl.setBorder(BorderFactory.createTitledBorder(title));

		return pnl;
	}

	private static JButton createExaminar(final JTextField txt,
			final int fileMode) {
		final JButton btn = new JButton("Examinar");
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(fileMode);
				int returnVal = chooser.showOpenDialog(btn);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selectedFile = chooser.getSelectedFile();

					txt.setText(selectedFile.getAbsolutePath());
				}
			}
		});
		return btn;
	}
}
