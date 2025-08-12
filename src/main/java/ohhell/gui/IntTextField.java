package ohhell.gui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.Document;

/**
 * Integer input text field
 *
 * @see https://web.archive.org/web/20210108210848/http://www.java2s.com/Code/Java/Swing-JFC/Textfieldonlyacceptsnumbers.htm
 */
class IntTextField extends JTextField {

    /**
     * Create new field
     * @param defaultValue Default value (can be null)
     * @param size Size of field
     */
    public IntTextField(Integer defaultValue, int size) {
        super( defaultValue != null ? defaultValue.toString(): "", size);
    }

    @Override
    protected Document createDefaultModel() {
        return new IntTextDocument();
    }

    @Override
    public boolean isValid() {
        try {
            if (getText().isBlank()) {
                return true;
            }
            Integer.parseInt(getText());
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Get value of field
     * @return Value of field (0 on error)
     */
    public int getValue() {
        try {
            return Integer.parseInt(getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Clear the field
     */
    public void clear() {
        try {
            getDocument().remove(0, getDocument().getLength());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Document class for field
     */
    static private class IntTextDocument extends PlainDocument {
        @Override
        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {
            if (str == null)
                return;
            String oldString = getText(0, getLength());
            String newString = oldString.substring(0, offs) + str
                    + oldString.substring(offs);
            try {
                Integer.parseInt(newString + "0");
                super.insertString(offs, str, a);
            } catch (NumberFormatException e) {
            }
        }
    }

}