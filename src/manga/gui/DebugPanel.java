package manga.gui;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;


public class DebugPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private int [] datas;
	private int sensivity;
	private BufferedImage mask;

	public void setDatas(double[] datas) {
		this.datas = new int[datas.length];
		for (int i = 0; i < datas.length; i++) {
			this.datas[i] = (int) Math.round(datas[i]);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int length = datas.length;
		g.setColor(Color.BLACK);
		g.drawLine(0, 100, length, 100);
		g.setColor(Color.RED);
		g.drawLine(0, 100 - sensivity, length, 100 - sensivity);
		g.setColor(Color.BLUE);
		for(int x = 0; x < length; x++) {
			g.drawLine(x, 100, x, 100 - datas[x]);
		}
		g.drawImage(mask, 0, 101, null);
	}

	public void setSensivity(double moyenneVariations) {
		sensivity = (int) moyenneVariations;
	}

	public void setMask(BufferedImage mask) {
		this.mask = mask;
	}
}
