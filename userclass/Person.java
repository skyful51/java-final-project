package userclass;

import java.util.*;

public class Person {
	private State state;        //state
	private boolean infected;   //infection flag
	private Person[] neighbors; //neighbors
	
	//constructor
	public Person() {
		state = State.SUSCEPTIBLE; //처음 상태는 SUSCEPTIBLE 상태로 설정한다
		infected = false; //처음 객채가 생성될 때 감염 플래그는 false로 설정
		Person[] neighbors = new Person[8]; //본인 주위로 8명의 사람들은 이웃
	}
	
	//neighbors (N, E, W, S ,NE, NW, SE, SW)
	public void setNeighbor(Person[] neighbors) {
		this.neighbors = neighbors; //Vaccine 클래스에서 시뮬레이션 세계를 만들 때 이웃을 설정해 neighbors에 저장
	}
	public Person[] getNeighbors() {
		return this.neighbors;
	}
	
	//infect neighbors
	//an infectious person can infect a susceptible person
	//with probability infectionRate
	//0과 1사이의 난수를 생성한다
	//난수 + infectionRate는 infectionRate의 확률로 1보다 커지므로 이를 이용해 감염 여부를 정한다
	//단, 백신을 접종한 사람은 감염되지 않는다
	public void infectNeighbors(double infectionRate) {
		for(int i=0; i<8; i++) {
			if((Math.random()+infectionRate >= 1) && (neighbors[i].isSusceptible()))
				neighbors[i].infected = true;
		}
	}
	
	//update the state
	//SUSCEPTIBLE -> INFECTIOUS if infected
	//INFECTIOUS -> SUSCEPTIBLE with probability recoveryRate
	public void update(double recoveryRate) {
		
		//이미 감염된 사람은 일정 확률로 자가 치유
		if((Math.random()+recoveryRate >= 1) && (isInfectious()))
			setState(State.SUSCEPTIBLE);
		
		//감염 플래그가 뜬 사람은 감염 상태로 변경
		if(infected)
			setState(State.INFECTIOUS);
	}
	
	//set/get state
	public void setState(State state) {
		this.state = state;
		
		//감염이 되지 않은 사람은 감염 플래그를 지운다
		if(getState() == State.INFECTIOUS)
			infected = true;
		else
			infected = false;
	}
	public State getState() {
		return state;
	}
	
	//return true if this person is SUSCEPTIBLE
	public boolean isSusceptible() {
		if(getState() == State.SUSCEPTIBLE)
			return true;
		else
			return false;
	}
	
	//return true if this person is INFECTIOUS
	public boolean isInfectious() {
		if(getState() == State.INFECTIOUS)
			return true;
		else
			return false;
	}
	
	//return true if this person is VACCINATED
	public boolean isVaccinated() {
		if(getState() == State.VACCINATED)
			return true;
		else
			return false;
	}
	
	//return string representation
	//SUSCEPTIBLE ., INFECTIOUS *, VACCINATED o
	public String toString() {
		if(getState() == State.SUSCEPTIBLE)
			return ". ";
		else if(getState() == State.INFECTIOUS)
			return "x ";
		else
			return "o ";
	}
}