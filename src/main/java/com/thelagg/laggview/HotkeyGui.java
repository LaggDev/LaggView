package com.thelagg.laggview;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class HotkeyGui extends JPanel implements KeyListener {
	private static final long serialVersionUID = -4503747634363844880L;
	String toggleOnHotkey;
	String toggleOffHotkey;
	boolean hotkey1Changing;
	boolean hotkey2Changing;
	boolean shift;
	boolean ctrl;
	boolean alt;
	JButton button1;
	JButton button2;
	LaggView laggView;
	
	public HotkeyGui(String toggleOnHotkey, String toggleOffHotkey, LaggView laggView) {
		super();
		this.laggView = laggView;
		JFrame frame = new JFrame("Recording Settings");
		frame.setSize(290, 110);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		frame.add(this);
		frame.toFront();
		this.addKeyListener(this);
		frame.addKeyListener(this);
		this.toggleOnHotkey = toggleOnHotkey;
		this.toggleOffHotkey = toggleOffHotkey;
		hotkey1Changing = false;
		hotkey2Changing = false;
		button1 = new JButton(toggleOnHotkey);
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				hotkey1Changing = true;
				HotkeyGui.this.repaint();
			}			
		});
		button1.addKeyListener(this);
		button2 = new JButton(toggleOffHotkey);
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				hotkey2Changing = true;
				HotkeyGui.this.repaint();
			}			
		});
		button2.addKeyListener(this);
		this.add(button1);
		this.add(button2);
		this.shift = false;
		this.ctrl = false;
		this.alt = false;
	}
	
	public void paintComponent(Graphics g1) {
		while(button1==null && button2==null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Graphics2D g = (Graphics2D)g1;
		g.setColor(new Color(255,51,51));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		if(!hotkey1Changing) {
			button1.setText(toggleOnHotkey);
		} else {
			String txt = " ";
			if(ctrl) {
				txt += "CTRL + ";
			}
			if(shift) {
				txt += "SHIFT + ";
			}
			if(alt) {
				txt += "ALT + ";
			}
			button1.setText(txt);
		}
		
		if(!hotkey2Changing) {
			button2.setText(toggleOffHotkey);
		} else {
			String txt = " ";
			if(ctrl) {
				txt += "CTRL + ";
			}
			if(shift) {
				txt += "SHIFT + ";
			}
			if(alt) {
				txt += "ALT + ";
			}
			button2.setText(txt);
		}
		Font f = new Font("Comic Sans MS", Font.BOLD, this.getFont().getSize()*10/8);
		g.setFont(f);
		g.setColor(Color.DARK_GRAY);
		g.drawString("Start Recording: ", 10, button1.getHeight());
		g.drawString("Stop Recording: ", 10, button1.getHeight() + button2.getHeight());
		button1.setLocation(g.getFontMetrics().stringWidth("Start Recording:  "), button1.getHeight()/4);
		button2.setLocation(g.getFontMetrics().stringWidth("Stop Recording:  "), button1.getHeight() + button1.getHeight()/3);
	}
	
	public void updateKeyCombos() {
		laggView.hackerMonitor.setStartRecordingHotkey(toggleOnHotkey);
		laggView.hackerMonitor.setStopRecordingHotkey(toggleOffHotkey);
		laggView.settings.setToggleRecordingOnHotkey(toggleOnHotkey);
		laggView.settings.setToggleRecordingOffHotkey(toggleOffHotkey);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(hotkey1Changing || hotkey2Changing) {
			switch(arg0.getKeyCode()) {
			case KeyEvent.VK_SHIFT:
				shift = true;
				break;
			case KeyEvent.VK_CONTROL:
				ctrl = true;
				break;
			case KeyEvent.VK_ALT:
				alt = true;
				break;
			default:
				String txt = "";
				if(ctrl) {
					txt += "CTRL + ";
				}
				if(shift) {
					txt += "SHIFT + ";
				}
				if(alt) {
					txt += "ALT + ";
				}
				txt += ((char)arg0.getKeyCode());
				if(hotkey1Changing) {
					toggleOnHotkey = txt;
					button1.setText(txt);
				} else {
					toggleOffHotkey = txt;
					button2.setText(txt);
				}
				hotkey1Changing = false;
				hotkey2Changing = false;
				this.updateKeyCombos();
				break;
			}
			repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		switch(arg0.getKeyCode()) {
		case KeyEvent.VK_SHIFT:
			shift = false;
			break;
		case KeyEvent.VK_CONTROL:
			ctrl = false;
			break;
		case KeyEvent.VK_ALT:
			alt = false;
			break;
		default:
			break;
		}
		if(hotkey1Changing || hotkey2Changing) {
			repaint();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {}
}
