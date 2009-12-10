package com.bc.ceres.binding.swing;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.binding.swing.internal.TextComponentAdapter;
import junit.framework.TestCase;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

public class BindingContextTest extends TestCase {

    private BindingContext bindingContextVB;
    private PropertyContainer propertyContainerVB;

    private PropertyContainer propertyContainerOB;
    private BindingContext bindingContextOB;
    private TestPojo pojo;

    private Exception error;
    private JComponent component;

    @Override
    protected void setUp() throws Exception {
        propertyContainerVB = PropertyContainer.createValueBacked(TestPojo.class);
        propertyContainerVB.getDescriptor("valueSetBoundIntValue").setValueSet(new ValueSet(TestPojo.intValueSet));
        bindingContextVB = new BindingContext(propertyContainerVB, null);
        bindingContextVB.addProblemListener(new MyBindingProblemListener());

        pojo = new TestPojo();
        propertyContainerOB = PropertyContainer.createObjectBacked(pojo);
        propertyContainerOB.getDescriptor("valueSetBoundIntValue").setValueSet(new ValueSet(TestPojo.intValueSet));
        bindingContextOB = new BindingContext(propertyContainerOB, null);
        bindingContextVB.addProblemListener(new MyBindingProblemListener());

        error = null;
        component = null;
    }

    private void clearError() {
        error = null;
        component = null;
    }

    public void testBindSpinner() throws ValidationException {
        JSpinner spinner = new JSpinner();
        Binding binding = bindingContextVB.bind("intValue", spinner);
        assertNotNull(binding);
        assertSame(spinner, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(1, binding.getComponents().length);

        assertEquals("intValue", spinner.getName());

        spinner.setValue(3);
        assertEquals(3, propertyContainerVB.getValue("intValue"));

        propertyContainerVB.setValue("intValue", 76);
        assertEquals(76, spinner.getValue());

    }

    public void testBindComboBox() throws ValidationException {
        JComboBox comboBox = new JComboBox(new Integer[]{1, 3, 7});
        comboBox.setEditable(false);

        Binding binding = bindingContextVB.bind("intValue", comboBox);
        assertNotNull(binding);
        assertSame(comboBox, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(1, binding.getComponents().length);

        assertEquals("intValue", comboBox.getName());

        comboBox.setSelectedItem(3);
        assertEquals(3, comboBox.getSelectedItem());
        assertEquals(3, binding.getPropertyValue());

        propertyContainerVB.setValue("intValue", 1);
        assertEquals(1, comboBox.getSelectedItem());
        assertEquals(1, binding.getPropertyValue());

        // "4" is not allowed, because it is not in the comboBox's model
        comboBox.setSelectedItem(4);
        assertEquals(1, comboBox.getSelectedItem());
        assertEquals(1, binding.getPropertyValue());

        comboBox.setEditable(true);

        // Now "4" is allowed, because the comboBox is editable
        comboBox.setSelectedItem(4);
        assertEquals(4, comboBox.getSelectedItem());
        assertEquals(4, binding.getPropertyValue());

        propertyContainerVB.setValue("intValue", 5);
        assertEquals(5, comboBox.getSelectedItem());
        assertEquals(5, binding.getPropertyValue());

        comboBox.setEditable(false);

        final ValueSet valueSet = new ValueSet(new Object[]{10, 20});
        final PropertyDescriptor descriptor = binding.getContext().getPropertySet().getDescriptor("intValue");
        descriptor.setValueSet(valueSet);

        assertEquals(2, comboBox.getModel().getSize());
        assertEquals(10, comboBox.getModel().getElementAt(0));
        assertEquals(20, comboBox.getModel().getElementAt(1));

        assertEquals(10, comboBox.getSelectedItem());
        assertEquals(10, binding.getPropertyValue());
        assertNull(binding.getProblem());
    }

    public void testBindTextField() throws ValidationException {
        JTextField textField = new JTextField();
        Binding binding = bindingContextVB.bind("stringValue", textField);
        assertNotNull(binding);
        assertSame(textField, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(1, binding.getComponents().length);

        assertEquals("stringValue", textField.getName());

        textField.setText("Bibo");
        textField.postActionEvent();
        assertEquals("Bibo", propertyContainerVB.getValue("stringValue"));

        propertyContainerVB.setValue("stringValue", "Samson");
        assertEquals("Samson", textField.getText());
    }

    public void testBindTextField2() throws ValidationException {
        JTextField textField = new JTextField();
        Binding binding = bindingContextOB.bind("stringValue", textField);
        assertNotNull(binding);
        assertSame(textField, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(1, binding.getComponents().length);

        assertEquals("stringValue", textField.getName());

        textField.setText("Bibo");
        textField.postActionEvent();
        assertEquals("Bibo", propertyContainerOB.getValue("stringValue"));

        propertyContainerOB.setValue("stringValue", "Samson");
        assertEquals("Samson", pojo.stringValue);
        assertEquals("Samson", textField.getText());

        pojo.stringValue = "Oscar";
        assertSame("Oscar", propertyContainerOB.getValue("stringValue"));
        assertNotSame("Oscar", textField.getText()); // value change not detected by binding
    }

    public void testBindFormattedTextFieldToString() throws ValidationException {
        JFormattedTextField textField = new JFormattedTextField();
        Binding binding = bindingContextVB.bind("stringValue", textField);
        assertNotNull(binding);
        assertSame(textField, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(1, binding.getComponents().length);

        assertEquals("stringValue", textField.getName());

        textField.setValue("Bibo");
        assertEquals("Bibo", propertyContainerVB.getValue("stringValue"));

        propertyContainerVB.setValue("stringValue", "Samson");
        assertEquals("Samson", textField.getValue());
    }

    public void testBindFormattedTextFieldToDouble() throws ValidationException {
        JFormattedTextField textField = new JFormattedTextField();
        Binding binding = bindingContextVB.bind("doubleValue", textField);
        assertNotNull(binding);
        assertSame(textField, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(1, binding.getComponents().length);

        assertEquals("doubleValue", textField.getName());

        textField.setValue(3.14);
        assertEquals(3.14, propertyContainerVB.getValue("doubleValue"));

        propertyContainerVB.setValue("doubleValue", 2.71);
        assertEquals(2.71, textField.getValue());
    }

    public void testBindTextArea() throws ValidationException, BadLocationException {
        JTextArea textArea = new JTextArea();
        TextComponentAdapter textComponentAdapter = new TextComponentAdapter(textArea);
        Binding binding = bindingContextVB.bind("stringValue", textComponentAdapter);
        assertNotNull(binding);
        assertSame(textArea, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(1, binding.getComponents().length);

        assertEquals("stringValue", textArea.getName());

        textArea.setText("Bibo");
        textComponentAdapter.actionPerformed(null);
        assertEquals("Bibo", propertyContainerVB.getValue("stringValue"));

        propertyContainerVB.setValue("stringValue", "Samson");
        assertEquals("Samson", textArea.getText());
    }

    public void testBindCheckBox() throws ValidationException {
        JCheckBox checkBox = new JCheckBox();
        Binding binding = bindingContextVB.bind("booleanValue", checkBox);
        assertNotNull(binding);
        assertSame(checkBox, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(1, binding.getComponents().length);

        assertEquals("booleanValue", checkBox.getName());

        checkBox.doClick();
        assertEquals(true, propertyContainerVB.getValue("booleanValue"));

        propertyContainerVB.setValue("booleanValue", false);
        assertEquals(false, checkBox.isSelected());
    }

    public void testBindRadioButton() throws ValidationException {
        JRadioButton radioButton = new JRadioButton();
        Binding binding = bindingContextVB.bind("booleanValue", radioButton);
        assertNotNull(binding);
        assertSame(radioButton, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(1, binding.getComponents().length);

        assertEquals("booleanValue", radioButton.getName());

        radioButton.doClick();
        assertEquals(true, propertyContainerVB.getValue("booleanValue"));

        propertyContainerVB.setValue("booleanValue", false);
        assertEquals(false, radioButton.isSelected());
    }

    public void testBindButtonGroup() throws ValidationException {
        JRadioButton radioButton1 = new JRadioButton();
        JRadioButton radioButton2 = new JRadioButton();
        JRadioButton radioButton3 = new JRadioButton();

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButton1);
        buttonGroup.add(radioButton2);
        buttonGroup.add(radioButton3);

        Property m = propertyContainerVB.getProperty("valueSetBoundIntValue");

        m.setValue(TestPojo.intValueSet[0]);

        Binding binding = bindingContextVB.bind("valueSetBoundIntValue", buttonGroup);
        assertNotNull(binding);
        assertSame(radioButton1, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(3, binding.getComponents().length);
        assertSame(radioButton1, binding.getComponents()[0]);
        assertSame(radioButton2, binding.getComponents()[1]);
        assertSame(radioButton3, binding.getComponents()[2]);

        assertEquals(true, radioButton1.isSelected());
        assertEquals(false, radioButton2.isSelected());
        assertEquals(false, radioButton3.isSelected());
        assertEquals(TestPojo.intValueSet[0], m.getValue());

        radioButton3.doClick();
        assertEquals(false, radioButton1.isSelected());
        assertEquals(false, radioButton2.isSelected());
        assertEquals(true, radioButton3.isSelected());
        assertEquals(TestPojo.intValueSet[2], m.getValue());

        radioButton2.doClick();
        assertEquals(false, radioButton1.isSelected());
        assertEquals(true, radioButton2.isSelected());
        assertEquals(false, radioButton3.isSelected());
        assertEquals(TestPojo.intValueSet[1], m.getValue());

        m.setValue(TestPojo.intValueSet[0]);
        assertEquals(true, radioButton1.isSelected());
        assertEquals(false, radioButton2.isSelected());
        assertEquals(false, radioButton3.isSelected());
        assertEquals(TestPojo.intValueSet[0], m.getValue());

        m.setValue(TestPojo.intValueSet[2]);
        assertEquals(false, radioButton1.isSelected());
        assertEquals(false, radioButton2.isSelected());
        assertEquals(true, radioButton3.isSelected());
        assertEquals(TestPojo.intValueSet[2], m.getValue());

        m.setValue(TestPojo.intValueSet[1]);
        assertEquals(false, radioButton1.isSelected());
        assertEquals(true, radioButton2.isSelected());
        assertEquals(false, radioButton3.isSelected());
        assertEquals(TestPojo.intValueSet[1], m.getValue());
    }

    public void testBindListSelection() throws ValidationException {
        JList list = new JList(new Integer[]{3, 4, 5, 6, 7});
        Binding binding = bindingContextVB.bind("listValue", list, true);
        assertNotNull(binding);
        assertSame(list, getPrimaryComponent(binding));
        assertNotNull(binding.getComponents());
        assertEquals(1, binding.getComponents().length);

        assertEquals("listValue", list.getName());

        list.setSelectedIndex(2);
        assertTrue(Arrays.equals(new int[]{5}, (int[]) propertyContainerVB.getValue("listValue")));

        propertyContainerVB.setValue("listValue", new int[]{6});
        assertEquals(6, list.getSelectedValue());
    }

    public void testAdjustComponents() throws ValidationException {
        JTextField textField1 = new JTextField();
        JTextField textField2 = new JTextField();
        JCheckBox checkBox = new JCheckBox();

        pojo.booleanValue = true;
        pojo.doubleValue = 3.2;
        pojo.stringValue = "ABC";

        bindingContextOB.bind("booleanValue", checkBox);
        bindingContextOB.bind("doubleValue", textField1);
        bindingContextOB.bind("stringValue", textField2);

        assertEquals(true, checkBox.isSelected());
        assertEquals("3.2", textField1.getText());
        assertEquals("ABC", textField2.getText());

        pojo.booleanValue = false;
        pojo.doubleValue = 1.5;
        pojo.stringValue = "XYZ";

        assertEquals(true, checkBox.isSelected());
        assertEquals("3.2", textField1.getText());
        assertEquals("ABC", textField2.getText());

        bindingContextOB.adjustComponents();

        assertEquals(false, checkBox.isSelected());
        assertEquals("1.5", textField1.getText());
        assertEquals("XYZ", textField2.getText());
    }

    public void testSecondaryComponent() throws Exception {
        JTextField textField = new JTextField();
        Binding binding = bindingContextVB.bind("stringValue", textField);
        JLabel label = new JLabel("myLabel");
        binding.addComponent(label);

        JComponent[] components = binding.getComponents();
        assertNotNull(components);
        assertEquals(2, components.length);
        assertSame(getPrimaryComponent(binding), components[0]);
        assertSame(label, components[1]);
    }

    public void testProblemManagement() {
        JTextField intTextField = new JTextField();
        JTextField stringTextField = new JTextField();
        final MyChangeListener listener = new MyChangeListener();

        clearError();
        bindingContextVB.addProblemListener(listener);
        bindingContextVB.addPropertyChangeListener(listener);
        bindingContextVB.bind("intValue", intTextField);
        bindingContextVB.bind("stringValue", stringTextField);

        assertEquals("", listener.trace);
        assertEquals(false, bindingContextVB.hasProblems());
        assertNotNull(bindingContextVB.getProblems());
        assertEquals(0, bindingContextVB.getProblems().length);
        assertNull(error);
        assertNull(component);

        clearError();
        bindingContextVB.getBinding("intValue").setPropertyValue("a");

        assertEquals("P;", listener.trace);
        assertEquals(true, bindingContextVB.hasProblems());
        assertNotNull(bindingContextVB.getProblems());
        assertEquals(1, bindingContextVB.getProblems().length);
        assertNotNull(error);
        assertSame(intTextField, component);

        clearError();
        bindingContextVB.getBinding("stringValue").setPropertyValue(5);

        assertEquals("P;P;", listener.trace);
        assertEquals(true, bindingContextVB.hasProblems());
        assertNotNull(bindingContextVB.getProblems());
        assertEquals(2, bindingContextVB.getProblems().length);
        assertNotNull(error);
        assertSame(stringTextField, component);

        clearError();
        bindingContextVB.getBinding("intValue").setPropertyValue(5);

        assertEquals("P;P;V;C;", listener.trace);
        assertEquals(true, bindingContextVB.hasProblems());
        assertNotNull(bindingContextVB.getProblems());
        assertEquals(1, bindingContextVB.getProblems().length);
        assertNull(error);
        assertNull(component);

        clearError();
        bindingContextVB.getBinding("stringValue").setPropertyValue("a");

        assertEquals("P;P;V;C;V;C;", listener.trace);
        assertEquals(false, bindingContextVB.hasProblems());
        assertNotNull(bindingContextVB.getProblems());
        assertEquals(0, bindingContextVB.getProblems().length);
        assertNull(error);
        assertNull(component);
    }


    private static JComponent getPrimaryComponent(Binding binding) {
        return binding.getComponents()[0];
    }

    private static class TestPojo {
        boolean booleanValue;
        int intValue;
        double doubleValue;
        String stringValue;
        int[] listValue;

        int valueSetBoundIntValue;
        static Integer[] intValueSet = new Integer[]{101, 102, 103};
    }

    private static class MyChangeListener implements BindingProblemListener, PropertyChangeListener {
        String trace = "";

        @Override
        public void problemReported(BindingProblem newProblem, BindingProblem oldProblem) {
            trace += "P;";
        }

        @Override
        public void problemCleared(BindingProblem oldProblem) {
            trace += "C;";
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            trace += "V;";
        }
    }

    private class MyBindingProblemListener implements BindingProblemListener {
        @Override
            public void problemReported(BindingProblem newProblem, BindingProblem oldProblem) {
            error = newProblem.getCause();
            component = newProblem.getBinding().getComponents()[0];
        }

        @Override
            public void problemCleared(BindingProblem oldProblem) {
            error = null;
            component = null;
        }
    }
}
