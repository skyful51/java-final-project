import java.awt.*;
import javax.swing.*;
import userclass.*;

public class VaccineGui {
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new VaccineFrame();
				frame.setTitle("VaccineSim");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}