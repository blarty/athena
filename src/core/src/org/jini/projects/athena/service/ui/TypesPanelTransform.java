/*
 * Created by IntelliJ IDEA.
 * User: calum
 * Date: 02-Sep-2002
 * Time: 09:49:38
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package org.jini.projects.athena.service.ui;

import javax.swing.DefaultListModel;

public class TypesPanelTransform extends javax.swing.JPanel {
    public DefaultListModel dlm = new DefaultListModel();

    /** Creates new form TypesPanelTransform */
    public TypesPanelTransform() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        setSize(400, 300);
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList(dlm);

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel1.setText("Transformer Class");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel1, gridBagConstraints);

        jTextField1.setBackground(new java.awt.Color(224, 224, 255));
        jTextField1.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 12);
        add(jTextField1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel2.setText("Allowable Transforms");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        add(jLabel2, gridBagConstraints);

        jScrollPane1.setToolTipText("null");
        jList1.setBackground(new java.awt.Color(224, 224, 255));
        jScrollPane1.setViewportView(jList1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 12);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

    }


    // Variables declaration - do not modify
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JTextField jTextField1;
    private javax.swing.JList jList1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration

}
