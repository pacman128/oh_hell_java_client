package ohhell.gui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.Document;

class IntTextField extends JTextField {
    public IntTextField(Integer defval, int size) {
        super( defval != null ? defval.toString(): "", size);
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

    public int getValue() {
        try {
            return Integer.parseInt(getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void clear() {
        try {
            getDocument().remove(0, getDocument().getLength());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

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