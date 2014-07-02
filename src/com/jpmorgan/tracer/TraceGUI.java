/*
 * TraceGUI.java
 *
 * Created on 17 juin 2002, 17:21
 */

package com.jpmorgan.tracer;

import java.io.*;
/**
 *
 * @author  ffleurey
 */
public class TraceGUI extends javax.swing.JFrame {

    public TraceProvider tracer;
    
    /** Creates new form TraceGUI */
    public TraceGUI() {
        tracer = new TraceProvider();
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tb_jvmArgs = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        tb_outFile = new javax.swing.JTextField();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jButton2.setText("Options...");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel1.add(jButton2, java.awt.BorderLayout.WEST);

        jButton3.setText("Quit");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jPanel1.add(jButton3, java.awt.BorderLayout.EAST);

        jButton9.setText("TRACE !");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jPanel1.add(jButton9, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setLayout(new java.awt.GridLayout(2, 0, 0, 5));

        jPanel3.setLayout(new java.awt.BorderLayout(4, 0));

        jLabel1.setText("Java command :");
        jPanel3.add(jLabel1, java.awt.BorderLayout.WEST);

        tb_jvmArgs.setText("java <yourclass> <arguments>");
        jPanel3.add(tb_jvmArgs, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel3);

        jPanel5.setLayout(new java.awt.BorderLayout(4, 0));

        jLabel5.setText("Output File : ");
        jPanel5.add(jLabel5, java.awt.BorderLayout.WEST);

        tb_outFile.setText("output.txt");
        jPanel5.add(tb_outFile, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel5);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        pack();
    }//GEN-END:initComponents

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        TraceProvider tp = TraceProvider.getInstance();
        tp.setArgs(tb_jvmArgs.getText());
        TraceThread tt = tp.generateTrace();
        try {
        FileWriter fw = new FileWriter(tb_outFile.getText());
        fw.write(tt.toString());
        fw.close();
        }
        catch(Exception e) { e.printStackTrace(); }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        new OptionsDialog(this, true).show();
    }//GEN-LAST:event_jButton2ActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new TraceGUI().show();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton9;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jButton2;
    private javax.swing.JTextField tb_outFile;
    private javax.swing.JTextField tb_jvmArgs;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

}