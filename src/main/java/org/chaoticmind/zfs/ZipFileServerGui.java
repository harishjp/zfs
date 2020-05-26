/*
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. See accompanying LICENSE file.
*/
package org.chaoticmind.zfs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A GUI to start and stop the server and add and remove zip files.
 */
public class ZipFileServerGui extends JFrame {
  private static final long serialVersionUID = -4973572371975713825L;

  private final DefaultListModel<String> fileListModel;
  private final JList<String> fileList;
  private final JButton command;
  private final JTextField port;

  private final ZipFileServer server;

  ZipFileServerGui() {
    server = new ZipFileServer();

    // Create the UI pieces
    fileListModel = new DefaultListModel<>();
    fileList = new JList<String>(fileListModel);
    command = new JButton("Start");
    command.setActionCommand("start");
    port = new JTextField(5);
    port.setText("8080");
    JPanel mainPanel = new JPanel();
    JButton add = new JButton("Add");
    JButton remove = new JButton("Remove");

    // Do the layout.
    Container contentPane = getContentPane();
    contentPane.add(new JScrollPane(fileList));
    mainPanel.add(add);
    mainPanel.add(remove);
    mainPanel.add(port);
    mainPanel.add(command);
    contentPane.add(mainPanel, BorderLayout.SOUTH);

    // Register listeners
    add.addActionListener(e -> addFile());
    remove.addActionListener(e -> removeFile());
    command.addActionListener(e -> {
      if (e.getActionCommand().equals("start")) start(); else stop();
    });
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (command.getActionCommand().equals("stop")) {
          stop();
        }
        System.exit(0);
      }
    });
    setResizable(false);
  }

  private void addFile() {
    JFileChooser jc = new JFileChooser();
    jc.setFileFilter(new FileNameExtensionFilter("JAR & Zip Files", "jar", "zip"));
    if (jc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      String file = jc.getSelectedFile().getPath();
      String name = JOptionPane.showInputDialog(this, "Enter the name");
      if (name != null && !name.trim().isEmpty()) {
        try {
          name = name.trim();
          if (server.addFile(name, file)) {
            fileListModel.addElement(name);
          }
        } catch (IOException e) {
          JOptionPane.showMessageDialog(this, e);
        }
      }
    }
  }

  private void removeFile() {
    String name = fileList.getSelectedValue();
    if (name == null) {
      return;
    }
    fileListModel.removeElement(name);
    try {
      server.removeFile(name);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, e);
    }
  }

  private void start() {
    try {
      server.start(Integer.parseInt(port.getText()));
      command.setText("Stop");
      command.setActionCommand("stop");
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(this, "Invalid number: " + port.getText());
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, e);
    }
  }

  private void stop() {
    server.stop();
    command.setText("Start");
    command.setActionCommand("start");
  }

  public static void main(String[] args) {
    ZipFileServerGui gui = new ZipFileServerGui();
    gui.setTitle("Zip File Server");
    gui.pack();
    gui.setVisible(true);
  }
}
