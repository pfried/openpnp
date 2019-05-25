/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.reference.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.machine.reference.ReferenceNozzleTip;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.spi.Camera;
import org.openpnp.spi.HeadMountable;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;
import org.openpnp.util.VisionUtils;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.ui.CvPipelineEditor;
import org.openpnp.vision.pipeline.ui.CvPipelineEditorDialog;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.Bindings;
import java.awt.FlowLayout;

public class ReferenceNozzleTipCalibrationWizard extends AbstractConfigurationWizard {
    private final ReferenceNozzleTip nozzleTip;

    private JPanel panelCalibration;
    private JButton btnEditPipeline;
    private JButton btnResetPipeline;
    private JButton buttonCenterTool;

    private JLabel lblCompensationAlgorithm;
    private JComboBox compensationAlgorithmCb;
    private JLabel lblAngleIncrements;
    private JTextField angleIncrementsTf;
    private JLabel lblOffsetThreshold;
    private JTextField offsetThresholdTf;
    private JButton btnCalibrate;
    private JButton btnReset;
    private JLabel lblCalibrationInfo;
    private JLabel lblCalibrationResults;
    private JCheckBox calibrationEnabledCheckbox;


    public ReferenceNozzleTipCalibrationWizard(ReferenceNozzleTip nozzleTip) {
        this.nozzleTip = nozzleTip;

        CellConstraints cc = new CellConstraints();


        panelCalibration = new JPanel();
        panelCalibration.setBorder(new TitledBorder(null, "Calibration (EXPERIMENTAL!)",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(panelCalibration);
        panelCalibration.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.UNRELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.UNRELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,}));

        calibrationEnabledCheckbox = new JCheckBox("Enable?");
        panelCalibration.add(calibrationEnabledCheckbox, "2, 2, right, default");


        buttonCenterTool = new JButton(positionToolAction);
        buttonCenterTool.setHideActionText(true);
        panelCalibration.add(buttonCenterTool, "4, 2, left, default");


        lblCalibrationInfo = new JLabel("Status");
        panelCalibration.add(lblCalibrationInfo, "2, 4, right, default");

        lblCalibrationResults = new JLabel(nozzleTip.getCalibration()
                                                    .getRunoutCompensationInformation());
        panelCalibration.add(lblCalibrationResults, "4, 4, left, default");
                        
                        lblCalibrate = new JLabel("Calibration");
                        panelCalibration.add(lblCalibrate, "2, 6, right, default");
        
        panel_1 = new JPanel();
        panel_1.setBorder(null);
        FlowLayout flowLayout_1 = (FlowLayout) panel_1.getLayout();
        flowLayout_1.setHgap(0);
        flowLayout_1.setVgap(0);
        panelCalibration.add(panel_1, "4, 6, left, fill");
        
                btnCalibrate = new JButton("Calibrate");
                panel_1.add(btnCalibrate);
                
                
                        btnReset = new JButton("Reset");
                        panel_1.add(btnReset);
                        btnReset.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                nozzleTip.getCalibration()
                                         .reset();
                            }
                        });
                        btnCalibrate.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                calibrate();
                            }
                        });

        lblCompensationAlgorithm = new JLabel("Calibration System");
        panelCalibration.add(lblCompensationAlgorithm, "2, 8, right, default");

        compensationAlgorithmCb =
                new JComboBox(ReferenceNozzleTip.Calibration.RunoutCompensationAlgorithm.values());
        panelCalibration.add(compensationAlgorithmCb, "4, 8, left, default");

        lblAngleIncrements = new JLabel("Circle Divisions");
        panelCalibration.add(lblAngleIncrements, "2, 10, right, default");

        angleIncrementsTf = new JTextField();
        panelCalibration.add(angleIncrementsTf, "4, 10, left, default");
        angleIncrementsTf.setColumns(3);

        lblOffsetThreshold = new JLabel("Offset Threshold");
        panelCalibration.add(lblOffsetThreshold, "2, 12, right, default");

        offsetThresholdTf = new JTextField();
        panelCalibration.add(offsetThresholdTf, "4, 12, left, default");
        offsetThresholdTf.setColumns(6);

        lblNewLabel = new JLabel("Pipeline");
        panelCalibration.add(lblNewLabel, "2, 14, right, default");
                        
                        panel = new JPanel();
                        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
                        flowLayout.setVgap(0);
                        panelCalibration.add(panel, "4, 14, left, default");
                        
                                btnEditPipeline = new JButton("Edit");
                                panel.add(btnEditPipeline);
                                
                                        btnResetPipeline = new JButton("Reset");
                                        panel.add(btnResetPipeline);
                        btnResetPipeline.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                resetCalibrationPipeline();
                            }
                        });
                btnEditPipeline.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        UiUtils.messageBoxOnException(() -> {
                            editCalibrationPipeline();
                        });
                    }
                });
        initDataBindings();

    }

    @SuppressWarnings("serial")
    private Action positionToolAction = new AbstractAction("Position Tool", Icons.centerTool) {
        {
            putValue(Action.SHORT_DESCRIPTION, "Position the tool over the bottom camera.");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            UiUtils.submitUiMachineTask(() -> {
                HeadMountable nozzle = nozzleTip.getParentNozzle();
                Camera camera = VisionUtils.getBottomVisionCamera();
                Location location = camera.getLocation();

                MovableUtils.moveToLocationAtSafeZ(nozzle, location);
            });
        }
    };
    private JLabel lblNewLabel;
    private JLabel lblCalibrate;
    private JPanel panel;
    private JPanel panel_1;

    private void resetCalibrationPipeline() {
        nozzleTip.getCalibration()
                 .resetPipeline();
    }

    private void editCalibrationPipeline() throws Exception {
        CvPipeline pipeline = nozzleTip.getCalibration()
                                       .getPipeline();
        CvPipelineEditor editor = new CvPipelineEditor(pipeline);
        JDialog dialog = new CvPipelineEditorDialog(MainFrame.get(), "Calibration Pipeline", editor);
        dialog.setVisible(true);
}

    private void calibrate() {
        UiUtils.submitUiMachineTask(() -> {
            nozzleTip.getCalibration()
                     .calibrate(nozzleTip);
        });
    }


    @Override
    public void createBindings() {
        IntegerConverter intConverter = new IntegerConverter();
        DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());

        addWrappedBinding(nozzleTip.getCalibration(), "enabled", calibrationEnabledCheckbox,
                "selected");
        addWrappedBinding(nozzleTip.getCalibration(), "runoutCompensationAlgorithm",
                compensationAlgorithmCb, "selectedItem");
        addWrappedBinding(nozzleTip.getCalibration(), "angleSubdivisions", angleIncrementsTf,
                "text", intConverter);
        addWrappedBinding(nozzleTip.getCalibration(), "offsetThreshold", offsetThresholdTf,
                "text", doubleConverter);
        bind(UpdateStrategy.READ, nozzleTip.getCalibration(), "runoutCompensationInformation",
                lblCalibrationResults, "text");
    }
    protected void initDataBindings() {
        BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
        BeanProperty<JButton, Boolean> jButtonBeanProperty = BeanProperty.create("enabled");
        AutoBinding<JCheckBox, Boolean, JButton, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, calibrationEnabledCheckbox, jCheckBoxBeanProperty, btnCalibrate, jButtonBeanProperty);
        autoBinding.bind();
        //
        AutoBinding<JCheckBox, Boolean, JButton, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, calibrationEnabledCheckbox, jCheckBoxBeanProperty, btnReset, jButtonBeanProperty);
        autoBinding_1.bind();
        //
        BeanProperty<JComboBox, Boolean> jComboBoxBeanProperty = BeanProperty.create("enabled");
        AutoBinding<JCheckBox, Boolean, JComboBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, calibrationEnabledCheckbox, jCheckBoxBeanProperty, compensationAlgorithmCb, jComboBoxBeanProperty);
        autoBinding_2.bind();
        //
        BeanProperty<JTextField, Boolean> jTextFieldBeanProperty = BeanProperty.create("enabled");
        AutoBinding<JCheckBox, Boolean, JTextField, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, calibrationEnabledCheckbox, jCheckBoxBeanProperty, angleIncrementsTf, jTextFieldBeanProperty);
        autoBinding_3.bind();
        //
        AutoBinding<JCheckBox, Boolean, JButton, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, calibrationEnabledCheckbox, jCheckBoxBeanProperty, btnEditPipeline, jButtonBeanProperty);
        autoBinding_4.bind();
        //
        AutoBinding<JCheckBox, Boolean, JButton, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, calibrationEnabledCheckbox, jCheckBoxBeanProperty, btnResetPipeline, jButtonBeanProperty);
        autoBinding_5.bind();
    }
}
