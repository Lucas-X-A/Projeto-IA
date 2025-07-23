package org.example.qlearning;

import java.text.DecimalFormat;

public class QTableRow {
    private final String estado;
    private final String up;
    private final String down;
    private final String left;
    private final String right;
    private boolean isHighlighted = false;

    public QTableRow(String estado, double[] valores) {
        DecimalFormat df = new DecimalFormat("0.00");
        this.estado = estado;
        this.up = df.format(valores[0]);
        this.down = df.format(valores[1]);
        this.left = df.format(valores[2]);
        this.right = df.format(valores[3]);
    }

    public String getEstado() { return estado; }
    public String getUp() { return up; }
    public String getDown() { return down; }
    public String getLeft() { return left; }
    public String getRight() { return right; }
    public boolean isHighlighted() { return isHighlighted; }
    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

}
