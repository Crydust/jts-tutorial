package com.smartycoder.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
/**
 * 
 * @see https://www.smartycoder.com
 *
 */
@SuppressWarnings("serial")
public class JTSVisualisationPanel extends JPanel {

	private List<DrawingCommand> drawPathCommand = new ArrayList<>();
	
	public JTSVisualisationPanel(){
		setSize(450, 450);
	
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (g instanceof Graphics2D g2d) {
			g2d.addRenderingHints(Map.of(
					RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
					RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY,
					RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY,
					RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY,
					RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC,
					RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE,
					RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE,
					RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
					RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON
			));
		}

		g.setColor(Color.darkGray);
		
		g.fillRect(0, 0, 600, 600);
		
		g.setColor(Color.WHITE);
		
		g.fillRect(10, 10, 4, 4);
	 
		g.drawLine(10, 10, 10, 600);
		
		g.drawLine(10, 10, 600, 10);
		
		for(int i = 10; i <= 600; i += 50) {
			
			g.drawString(Integer.toString(i), i, 10);
			
			g.drawString(Integer.toString(i), 10, i);
		}
		
		g.setColor(Color.BLACK);
		
		for (DrawingCommand drawingCommand : drawPathCommand) {
			
			drawingCommand.doDrawing(g);
		}
		
	}

	public void addDrawCommand(DrawingCommand c) {
		this.drawPathCommand.add(c);
		this.repaint();
	}
}
