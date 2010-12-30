package de.wusel.partyplayer.gui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;

class RichJLabel extends JLabel {

    public RichJLabel(String text) {
        super(text);
    }

    @Override
    public Dimension getPreferredSize() {
        String text = getText();
        FontMetrics fm = this.getFontMetrics(getFont());
        int w = fm.stringWidth(text);
        w += (text.length() - 1);
        int h = fm.getHeight();
        return new Dimension(w, h);
    }

    @Override
    public void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        char[] chars = getText().toCharArray();
        FontMetrics fm = this.getFontMetrics(getFont());
        int h = fm.getAscent();
        g.setFont(getFont());
        int x = 0;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            int w = fm.charWidth(ch);
            g.setColor(getForeground());
            g.drawString("" + chars[i], x, h);
            x += w;
        }
    }
}
