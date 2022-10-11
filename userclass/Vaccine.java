package userclass;

import java.util.*;

public class Vaccine {
	//people
	private Person[][] people; //사람 객채를 모은 이차원 배열
	private int size; //시뮬레이션 세계의 가로 세로 크기
	private int[] randOrder; //백신 접종하는 무작위 순서를 정한 배열
	
	//history of state counts
	private List<Integer[]> history; //시뮬레이션 단계마다 사람들의 상태 배열을 담은 List
	
	//construct a vaccination model of size x size people
	//place infectious persons at the center
	public Vaccine(int size) {
		this.size = size;
		people = new Person[size+2][size+2]; //도넛 모양 세계를 만들기 위해 행과 열을 추가
		
		history = new ArrayList<>();
		
		//size x size 세계를 구성하는 사람 객체 생성
		for(int i=1; i<=size; i++) {
			for(int j=1; j<=size; j++) {
				people[i][j] = new Person();
			}
		}
		
		//세계의 위쪽을 아래쪽과, 왼쪽을 오른쪽과 서로 연결
		for(int j=1; j<=size; j++) {
			people[0][j] = people[size][j];
			people[size+1][j] = people[1][j];
		}
		for(int i=1; i<=size; i++) {
			people[i][0] = people[i][size];
			people[i][size+1] = people[i][1];
		}
		//세계의 모서리에 있는 사람들끼리 연결
		people[0][0] = people[size][size];
		people[0][size+1] = people[size][1];
		people[size+1][0] = people[1][size];
		people[size+1][size+1] = people[1][1];
		
		//각 사람마다 이웃 8명을 설정해서 neighbors 배열에 저장
		for(int i=1; i<=size; i++) {
			for(int j=1; j<=size; j++) {
				Person[] neighbors = new Person[8];
				neighbors[0] = people[i-1][j-1];
				neighbors[1] = people[i-1][j];
				neighbors[2] = people[i-1][j+1];
				neighbors[3] = people[i][j-1];
				neighbors[4] = people[i][j+1];
				neighbors[5] = people[i+1][j-1];
				neighbors[6] = people[i+1][j];
				neighbors[7] = people[i+1][j+1];
				people[i][j].setNeighbor(neighbors);
			}
		}
		
		randOrder = new int[size*size];
		randOrder = randPerm(size*size);
		
		//최초 감염자 9명 설정
		initialInfection();
	}
	
	//construct a vaccination model with the vaccination parameter
	public Vaccine(int size, double vaccineRatio) {
		this.size = size;
		people = new Person[size+2][size+2]; //도넛 모양 세계를 만들기 위해 행과 열을 추가
		
		history = new ArrayList<>();
		
		//size x size 세계를 구성하는 사람 객체 생성
		for(int i=1; i<=size; i++) {
			for(int j=1; j<=size; j++) {
				people[i][j] = new Person();
			}
		}
		
		//세계의 위쪽을 아래쪽과, 왼쪽을 오른쪽과 서로 연결
		for(int j=1; j<=size; j++) {
			people[0][j] = people[size][j];
			people[size+1][j] = people[1][j];
		}
		for(int i=1; i<=size; i++) {
			people[i][0] = people[i][size];
			people[i][size+1] = people[i][1];
		}
		//세계의 모서리에 있는 사람들끼리 연결
		people[0][0] = people[size][size];
		people[0][size+1] = people[size][1];
		people[size+1][0] = people[1][size];
		people[size+1][size+1] = people[1][1];
		
		//각 사람마다 이웃 8명을 설정해서 neighbors 배열에 저장
		for(int i=1; i<=size; i++) {
			for(int j=1; j<=size; j++) {
				Person[] neighbors = new Person[8];
				neighbors[0] = people[i-1][j-1];
				neighbors[1] = people[i-1][j];
				neighbors[2] = people[i-1][j+1];
				neighbors[3] = people[i][j-1];
				neighbors[4] = people[i][j+1];
				neighbors[5] = people[i+1][j-1];
				neighbors[6] = people[i+1][j];
				neighbors[7] = people[i+1][j+1];
				people[i][j].setNeighbor(neighbors);
			}
		}
		
		// 백신 접종자 설정
		randOrder = new int[size*size];
		randOrder = randPerm(size*size);
		vaccinate(vaccineRatio);
		
		//최초 감염자 9명 설정
		initialInfection();
	}
	
	//initial infection
	//infectious center and its neighbors
	private void initialInfection() {
		for(int i=(size/2); i<=(size/2)+2; i++) {
			for(int j=(size/2); j<=(size/2)+2; j++) {
				people[i][j].setState(State.INFECTIOUS);
			}
		}
	}
	
	//vaccinate
	public void vaccinate(double vaccineRatio) {
		int n = (int) (size*size*vaccineRatio); //백신 접종률에 따른 백신을 맞은 사람 수
		
		//people은 이차원 배열이므로 일차원 배열인 randOrder을 이차원 인덱스로 바꿔준다
		//백신 접종률을 조절하면 randOrder 배열의 n번째 사람까지는 VACCINATED 상태를 유지하고
		//그 뒤의 사람들은 SUSCEPTIBLE 상태가 된다
		for(int i=0; i<size*size; i++) {
			
			//randOrder의 첫번째 사람부터 n번째 사람은 VACCINATED 상태를 유지
			if(i<n)
				people[randOrder[i]/size + 1][randOrder[i]%size + 1].setState(State.VACCINATED);
			
			//그 뒤의 사람들 중 이미 감염된 사람은 그대로 INFECTIOUS 상태를 유지
			else if(people[randOrder[i]/size + 1][randOrder[i]%size + 1].isInfectious())
				people[randOrder[i]/size + 1][randOrder[i]%size + 1].setState(State.INFECTIOUS);
			
			//나머지 사람들은 SUSCEPTIBLE 상태로 바꾼다
			//이미 VACCINATED인 사람도 접종률이 떨어지면 randOrder에 따라 SUSCEPTIBLE로 바뀔 가능성이 있다
			else
				people[randOrder[i]/size + 1][randOrder[i]%size + 1].setState(State.SUSCEPTIBLE);
		}
	}
	
	//return a random order of vaccination
	private int[] randPerm(int n) {
		int[] order = new int[n];
		int randNum;
		
		//1부터 n 사이의 랜덤한 숫자를 배열에 저장
		for(int i=0; i<n; i++) {
			randNum = (int)(Math.random()*n) + 1;
			order[i] = randNum;
			
			//랜덤으로 생성된 숫자가 이미 존재하면 새로운 랜덤 숫자가 나올 때 까지 반복
			for(int j=0; j<i; j++) {
				if(order[i] == order[j]) {
					i--;
				}
			}
		}
		return order;
	}
	
	//proceed a step
	//1. each infectious person can infect his neighbors
	//2. update the state of each person
	public void step(double infectionRate, double recoveryRate) {
		for(int i=1; i<=size; i++) {
			for(int j=1; j<=size; j++) {
				if(people[i][j].isInfectious()) {
					people[i][j].infectNeighbors(infectionRate);
				}
			}
		}
		for(int i=1; i<=size; i++) {
			for(int j=1; j<=size; j++) {
				people[i][j].update(recoveryRate);
			}
		}
		
		//한 단계를 진행할 때 마다 사람들의 상태 배열을 history에 저장
		history.add(countStates());
	}
	
	//return an array of state counts
	//[susceptible, infectious, vaccinated]
	public Integer[] countStates() {
		int[] count = new int[3];
		Integer[] countOfEachState = new Integer[3];
		
		//int형 count 배열에 state를 저장
		for(int i=1; i<=size; i++) {
			for(int j=1; j<=size; j++) {
				if(people[i][j].isSusceptible())
					count[0]++;
				else if(people[i][j].isInfectious())
					count[1]++;
				else if(people[i][j].isVaccinated())
					count[2]++;
			}
		}
		
		//int형 배열 count의 요소들을 Integer형 배열 countOfEachState에 담고 반환한다
		for(int i=0; i<count.length; i++)
			countOfEachState[i] = count[i];
		
		return countOfEachState;
	}
	
	//return the state of people
	public State[][] getPeopleState() {
		State[][] peopleState = new State[size][size];
		
		for(int i=1; i<=size; i++) {
			for(int j=1; j<=size; j++) {
				peopleState[i-1][j-1] = people[i][j].getState();
			}
		}
		
		return peopleState;
	}
	
	//return the history of state counts
	public List<Integer[]> getHistory() {
		return history;
	}
	
	//print the state of people: size x size
	public void printPeople() {
		
		//*,.,o로 사람들의 상태를 출력
		for(int i=1; i<=size; i++) {
			for(int j=1; j<=size; j++) {
				System.out.print(people[i][j].toString());
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	//print the statistics and the states of people
	public void PrintStep(int n) {
		Integer[] countStates;
		countStates = countStates();
		
		System.out.printf("===== %d ===== (백신 접종자 %d 명/감염자 %d 명)\n",
		                n, countStates[2], countStates[1]);
		printPeople();
	}
}