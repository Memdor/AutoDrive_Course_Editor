package AutoDriveEditor.Utils;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.Listeners.CurvePanelListener;
import AutoDriveEditor.Listeners.EditorListener;
import AutoDriveEditor.Listeners.MenuListener;

import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Locale.LocaleManager.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;

public class GUIUtils {

    // 1st part of fix for alpha cascading errors on radio buttons.

    public static class AlphaContainer extends JComponent
    {
        private final JComponent component;

        public AlphaContainer(JComponent component)
        {
            this.component = component;
            setLayout( new BorderLayout() );
            setOpaque( false );
            component.setOpaque( false );
            add( component );
        }

        /**
         *  Paint the background using the background Color of the
         *  contained component
         */
        @Override
        public void paintComponent(Graphics g)
        {
            g.setColor( component.getBackground() );
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // 2nd part of fix for alpha cascading errors on radio buttons

    static class TransparentRadioButton extends JRadioButton {
        public TransparentRadioButton(String string) {
            super(string);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(Color.ORANGE);
            setBackground(new Color(0,0,0,0));
        }
    }

    //
    // Button Creation functions
    //

    public static JButton makeButton(String actionCommand,String toolTipText,String altText, JPanel panel, boolean enabled) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(localeString.getString(toolTipText));
        button.setText(localeString.getString(altText));
        panel.add(button);
        button.setEnabled(enabled);

        return button;
    }
    public static JButton makeButton(String actionCommand,String toolTipText,String altText, JPanel panel, ButtonGroup group, boolean isGroupDefault, EditorListener editorListener, boolean enabled) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(localeString.getString(toolTipText));
        button.addActionListener(editorListener);
        button.setText(localeString.getString(altText));
        panel.add(button);
        if (group != null) {
            group.add(button);
            if (isGroupDefault) {
                //ButtonModel groupDefault = menuItem.getModel();
                group.setSelected(button.getModel(), true);
            }
        }
        button.setEnabled(enabled);

        return button;
    }

    public static JToggleButton makeImageToggleButton(String imageName, String actionCommand, String toolTipText, String altText, JPanel panel, ButtonGroup group, boolean isGroupDefault, EditorListener editorListener) {
        return makeImageToggleButton(imageName, null, actionCommand, toolTipText, altText, panel, group, isGroupDefault, editorListener);
    }

    public static JToggleButton makeImageToggleButton(String imageName, String selectedImageName, String actionCommand, String toolTipText, String altText, JPanel panel, ButtonGroup group, boolean isGroupDefault, EditorListener editorListener) {

        JToggleButton toggleButton = new JToggleButton();

        toggleButton.setActionCommand(actionCommand);
        toggleButton.setToolTipText(localeString.getString(toolTipText));
        toggleButton.addActionListener(editorListener);
        toggleButton.setFocusPainted(false);
        toggleButton.setSelected(false);

        //Load image

        String imgLocation = "/editor/" + imageName + ".png";
        URL imageURL = AutoDriveEditor.class.getResource(imgLocation);
        if (imageURL != null) {
            //image found
            toggleButton.setIcon(new ImageIcon(imageURL, altText));
            toggleButton.setBorder(BorderFactory.createEmptyBorder());
            //selectedImageName="deletemarker";
            if (selectedImageName !=  null) {
                String selectedImagePath = "/editor/" + selectedImageName + ".png";
                URL selectedImageURL = AutoDriveEditor.class.getResource(selectedImagePath);
                if (selectedImageURL != null) {
                    toggleButton.setSelectedIcon(new ImageIcon(selectedImageURL, altText));
                }
            }
        } else {
            //no image found
            toggleButton.setText(localeString.getString(altText));
        }

        panel.add(toggleButton);
        if (group != null) {
            group.add(toggleButton);
            if (isGroupDefault) {
                //ButtonModel groupDefault = menuItem.getModel();
                group.setSelected(toggleButton.getModel(), true);
            }
        }

        return toggleButton;
    }

    //
    // special version of JToggleButton using a separate listener to change it's right click behaviour

    //
    public static JToggleButton makeStateChangeImageToggleButton (String imageName, String selectedImageName, String actionCommand, String toolTipText, String altText, JPanel panel, ButtonGroup group,  boolean isGroupDefault, EditorListener editorListener) {
        JToggleButton button = makeImageToggleButton(imageName, selectedImageName, actionCommand, toolTipText, altText, panel, group, isGroupDefault, editorListener);
        button.addMouseListener(editorListener);

        return button;
    }

    public static JRadioButton makeRadioButton(String text, String actionCommand, String toolTipText, Color textColour, boolean isSelected, boolean isOpaque, JPanel panel, ButtonGroup group,  boolean isGroupDefault, EditorListener actionListener) {
        return makeRadioButton(text, actionCommand, toolTipText, textColour, isSelected, isOpaque, panel, group, isGroupDefault, actionListener, null);
    }

    public static JRadioButton makeRadioButton(String text, String actionCommand, String toolTipText, Color textColour, boolean isSelected, boolean isOpaque, JPanel panel, ButtonGroup group,  boolean isGroupDefault, CurvePanelListener itemListener) {
        return makeRadioButton(text, actionCommand, toolTipText, textColour, isSelected, isOpaque, panel, group, isGroupDefault, null, itemListener);
    }

    public static JRadioButton makeRadioButton(String text, String actionCommand, String toolTipText, Color textColour, boolean isSelected, boolean isOpaque, JPanel panel, ButtonGroup group,  boolean isGroupDefault, EditorListener actionListener, CurvePanelListener itemListener) {
        TransparentRadioButton radioButton = new TransparentRadioButton(localeString.getString(text));
        radioButton.setActionCommand(actionCommand);
        radioButton.setToolTipText(localeString.getString(toolTipText));
        radioButton.setSelected(isSelected);
        radioButton.setOpaque(false);
        radioButton.setForeground(textColour);
        radioButton.setHorizontalAlignment(SwingConstants.LEADING);
        if (actionListener != null ) radioButton.addActionListener(actionListener);
        if (itemListener !=null ) radioButton.addItemListener(itemListener);
        panel.add(radioButton);
        if (group != null) {
            group.add(radioButton);
            if (isGroupDefault) {
                //ButtonModel groupDefault = menuItem.getModel();
                group.setSelected(radioButton.getModel(), true);
            }
        }

        return radioButton;
    }


    //
    // Menu Creation Functions

    public static JMenu makeMenu(String menuName, int keyEvent, String accString, JMenuBar parentMenu) {
        JMenu newMenu = new JMenu(localeString.getString(menuName));
        newMenu.setMnemonic(keyEvent);
        newMenu.getAccessibleContext().setAccessibleDescription(localeString.getString(accString));
        parentMenu.add(newMenu);
        return newMenu;
    }

    public static JMenu makeSubMenu(String menuName, int keyEvent, String accString, JMenu parentMenu) {
        JMenu newMenu = new JMenu(localeString.getString(menuName));
        newMenu.setMnemonic(keyEvent);
        newMenu.getAccessibleContext().setAccessibleDescription(localeString.getString(accString));
        parentMenu.add(newMenu);
        return newMenu;
    }

    public static JMenuItem makeMenuItem(String menuName, String accString, JMenu menu, MenuListener listener, String actionCommand, Boolean enabled) {
        return makeMenuItem(menuName, accString, KeyEvent_NONE, InputEvent_NONE, menu, listener, actionCommand, enabled);
    }

    public static JMenuItem makeMenuItem(String menuName, String accString, int keyEvent, int inputEvent, JMenu menu, MenuListener listener, String actionCommand, Boolean enabled) {
        JMenuItem menuItem = new JMenuItem(localeString.getString(menuName));
        if (keyEvent != 0 && inputEvent != 0) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
        }
        menuItem.getAccessibleContext().setAccessibleDescription(localeString.getString(accString));
        menuItem.setEnabled(enabled);
        if (actionCommand != null) menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        return menuItem;
    }

    public static JCheckBoxMenuItem makeCheckBoxMenuItem (String text, String accString, Boolean isSelected, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled) {
        return makeCheckBoxMenuItem(text, accString, KeyEvent_NONE, InputEvent_NONE, isSelected, menu, itemListener, actionCommand, enabled);
    }
    public static JCheckBoxMenuItem makeCheckBoxMenuItem (String text, String accString, int keyEvent, Boolean isSelected, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled) {
        return makeCheckBoxMenuItem(text, accString, keyEvent, InputEvent_NONE, isSelected, menu, itemListener, actionCommand, enabled);
    }

    public static JCheckBoxMenuItem makeCheckBoxMenuItem (String text, String accString, int keyEvent, int inputEvent, Boolean isSelected, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled) {
        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(localeString.getString(text), isSelected);
        cbMenuItem.setActionCommand(actionCommand);
        if (inputEvent != 0 && keyEvent != 0) {
            cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
            cbMenuItem.setMnemonic(keyEvent);
        } else if (inputEvent == InputEvent_NONE && keyEvent != 0){
            cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, 0));
        }
        cbMenuItem.setSelected(isSelected);
        cbMenuItem.getAccessibleContext().setAccessibleDescription(localeString.getString(accString));
        cbMenuItem.addItemListener(itemListener);
        cbMenuItem.setEnabled(enabled);
        menu.add(cbMenuItem);

        return cbMenuItem;
    }


    // if no button group is required, set buttonGroup to null and isGroupDefault will be ignored
    public static JRadioButtonMenuItem makeRadioButtonMenuItem(String menuName, String accString, int keyEvent, int inputEvent, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled, ButtonGroup buttonGroup, boolean isGroupDefault) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(localeString.getString(menuName));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
        menuItem.getAccessibleContext().setAccessibleDescription(localeString.getString(accString));
        menuItem.setEnabled(enabled);
        if (actionCommand != null) menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(itemListener);
        if (buttonGroup != null) {
            buttonGroup.add(menuItem);
            if (isGroupDefault) {
                //ButtonModel groupDefault = menuItem.getModel();
                buttonGroup.setSelected(menuItem.getModel(), true);
            }
        }
        menu.add(menuItem);
        return menuItem;
    }

    public static void showInTextArea(String text, boolean clearAll, boolean outputToLogFile) {
        if (clearAll) {
            textArea.selectAll();
            textArea.replaceSelection("");
        }
        if (outputToLogFile) LOG.info(text);
        textArea.append(text + "\n");
    }
}
