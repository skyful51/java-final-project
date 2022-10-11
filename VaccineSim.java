import userclass.*;
import java.util.Arrays;

public class VaccineSim {
	public static void main(String[] args) {
		int size = 25;
		int n = 0;
		
		double infectionRate = 0.25;
		double recoveryRate = 0.50;
		double vaccineRatio = 0.75;
		
		Vaccine sim = new Vaccine(size, vaccineRatio);
		
		sim.PrintStep(0);
		
		while(true) {
			sim.step(infectionRate, recoveryRate);
			sim.PrintStep(n+1);
			
			Integer[] counts = sim.countStates();
			if(counts[1] == 0)
				break;
			
			n++;
		}
	}
}