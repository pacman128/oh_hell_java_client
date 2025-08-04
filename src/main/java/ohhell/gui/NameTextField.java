package ohhell.gui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.Document;

class NameTextField extends JTextField {
    public NameTextField(String defaultValue, int size) {
        super(defaultValue, size);
    }

    @Override
    protected Document createDefaultModel() {
        return new NameTextDocument();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && ! getText().contains(" ");
    }

    static private class NameTextDocument extends PlainDocument {
        @Override
        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {
            if (str == null)
                return;
            String oldString = getText(0, getLength());
            String newString = oldString.substring(0, offs) + str
                    + oldString.substring(offs);
            if (! newString.contains(" ")) {
                super.insertString(offs, str, a);
            }
        }
    }

}