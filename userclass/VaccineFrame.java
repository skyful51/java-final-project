package userclass;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class VaccineFrame extends JFrame {
	private final int DEFAULT_LENGTH = 800; //창 가로 크기
	private final int DEFAULT_WIDTH = DEFAULT_LENGTH + 140; //창 세로 크기

	private final int size = 100; //시뮬레이션 세계 크기(size x size 명의 사람이 존재하는 세계)
	private ArrayList<JPanel> peoplePanels; //각각의 사람을 표현하는 JPanel을 모은 ArrayList
		
	//각종 파라미터를 조절, 확인할 수 있는 패널
	private JPanel controlPanel = new JPanel(); //슬라이더, 버튼 등을 모아서 배치한 패널
	private JPanel sliderPanel = new JPanel(); //슬라이더를 모아둔 패널
	private ArrayList<JTextField> textFields = new ArrayList<>(); //각 컨트롤 파라미터들의 값을 표시해주는 텍스트 필드 ArrayList
	private JPanel statusPanel = new JPanel(); //상태를 나타내는 표를 담은 패널
	private JPanel buttonPanel = new JPanel(); //Reset, Run ,Step 버튼 패널
	private JTable statusTable; //접종자 수, 감염자 수, 시뮬레이션 단계 등을 표시해주는 표
	private ArrayList<JButton> buttons = new ArrayList<>(); //각 동작 버튼들을 담은 ArrayList
	
	private String[] controlParam = new String[] 
							{"Infection Rate (%)", "Recovery Rate (%)", "Vaccine Ratio (%)"};
	private String[] action = new String[]
							{"Reset", "Run", "Step"};

	//시뮬레이션 세계 생성
	private Vaccine simWorld = new Vaccine(size);
	
	//시뮬레이션에 필요한 파라미터들 (감염률, 회복률, 접종률)
	private double[] parameters = {0, 0, 0};
	private int step = 0; //시뮬레이션 단계
	
	public VaccineFrame() {
		//시뮬레이션 세계 패널
		//size x size 패널을 2의 간격을 두고 그리드 레이아웃으로 정렬
		JPanel simWorldPanel = new JPanel();
		simWorldPanel.setLayout(new GridLayout(size,size,2,2));
		
		//시뮬레이션 세계의 사람들은 각각의 peoplePanels ArrayList의 요소
		peoplePanels = new ArrayList<>();
		for(int i=0; i<size*size; i++) {
			peoplePanels.add(new JPanel());
		}
		
		//사람을 표현한 패널들을 시뮬레이션 세계 패널에 추가
		for(int i=0; i<size*size; i++) {		
			simWorldPanel.add(peoplePanels.get(i));
			peoplePanels.get(i).setBackground(Color.WHITE);
		}
		
		add(simWorldPanel);
		colorWorld();
		
		//접종자, 감염자, 시뮬레이션 단계 등을 볼 수 있는 표
		statusTable = new JTable(4, 2);
		statusTable.setValueAt("Step",0,0);
		statusTable.setValueAt("Susceptible",1,0);
		statusTable.setValueAt("Infectious",2,0);
		statusTable.setValueAt("Vaccinated",3,0);
		statusPanel.add(statusTable);
		
		//컨트롤 슬라이더 만들기
		for(int i=0; i<3; i++)
			addSlider(controlParam[i], i);	
		
		//시뮬레이션 버튼 만들기
		for(int i=0; i<3; i++)
			addButton(action[i], i);
		
		//만든 패널들을 배치
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
		controlPanel.add(sliderPanel);
		controlPanel.add(statusPanel);
		controlPanel.add(buttonPanel);
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		
		add(controlPanel, BorderLayout.SOUTH);
		pack();
	}
	
	//컨트롤 파라미터들의 값을 조정할 수 있는 슬라이더를 만들고 배치하는 메서드
	public void addSlider(String controlParam, int i) {
		//각 파라미터의 이름, 슬라이더, 값을 배치해 놓은 JPanel
		JPanel param = new JPanel();
		param.setLayout(new BoxLayout(param, BoxLayout.X_AXIS));
		JSlider slider = new JSlider();
		slider.setValue(0);
		
		param.add(new JLabel(controlParam));
		param.add(slider);
		textFields.add(new JTextField());
		param.add(textFields.get(i));
		
		sliderPanel.add(param);	
		
		//슬라이더 이벤트 리스너
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				textFields.get(i).setText("" + (double) (slider.getValue()));
				parameters[i] = (double)(slider.getValue()/100.0);
				simWorld.vaccinate(parameters[2]);
				colorWorld();
				Integer[] counts = simWorld.countStates();
				statusTable.setValueAt(counts[0], 1, 1);
				statusTable.setValueAt(counts[1], 2, 1);
				statusTable.setValueAt(counts[2], 3, 1);
			}
		});
	}
	
	//Reset, Run, Stop 버튼을 생성하고 배치하는 메서드
	public void addButton(String action, int i) {
		buttons.add(new JButton(action));
		
		buttonPanel.add(buttons.get(i));
		buttons.get(i).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//Reset 버튼을 누르면 다시 가운데 9명의 감염자를 배치시킨다
				if(action == "Reset") {
					step = 0;
					simWorld = new Vaccine(size, parameters[2]);
					colorWorld();
					Integer[] counts = simWorld.countStates();
					showState();
					statusTable.setValueAt(0, 0, 1);
				}
				
				//Run 버튼을 누르면 시뮬레이션을 반복한다
				else if(action == "Run") {
					
					//스레드를 생성해 시뮬레이션 무한루프 중에도 다른 설정을 할 수 있도록 한다
					Runnable r1 = new Runnable() {
						public void run() {
							try {
								int n = 0;
								
								//감염자가 0명이 될 때 까지 시뮬레이션을 반복한다
								while(true) {
									simWorld.step(parameters[0], parameters[1]);
									colorWorld();
									Thread.sleep(150);
									showState();
									Integer[] counts = simWorld.countStates();
									if(counts[1] == 0)
										break;
								}
							} catch (InterruptedException e) {}
						}
					};
					Thread t1 = new Thread(r1);
					t1.start();
				}
				
				//Step 버튼을 누르면 시뮬레이션을 한 단계 진행한다
				else if(action == "Step") {
					simWorld.step(parameters[0], parameters[1]);
					colorWorld();
					showState();
				}
			}
		});
	}
	
	//시뮬레이션 세계에 색상을 채워넣는 메서드
	public void colorWorld() {
		State[][] peopleState = new State[size][size];
		peopleState = simWorld.getPeopleState();
		
		//INFECTIOUS:빨간색, VACCINATED:파란색, SUSCEPTIBLE:흰색 으로 칠한다
		for(int i=0; i<size*size; i++) {
			if(peopleState[i/size][i%size] == State.INFECTIOUS)
				peoplePanels.get(i).setBackground(Color.RED);
			else if(peopleState[i/size][i%size] == State.VACCINATED)
				peoplePanels.get(i).setBackground(Color.BLUE);
			else if(peopleState[i/size][i%size] == State.SUSCEPTIBLE)
				peoplePanels.get(i).setBackground(Color.WHITE);
		}
	}
	
	//시뮬레이션 상태표의 값을 시뮬레이션을 진행함에 따라 갱신하는 메서드
	public void showState() {
		step++;
		Integer[] stateArray = simWorld.countStates();
		statusTable.setValueAt(step, 0, 1);
		statusTable.setValueAt(stateArray[0],1,1);
		statusTable.setValueAt(stateArray[1],2,1);
		statusTable.setValueAt(stateArray[2],3,1);
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(DEFAULT_LENGTH, DEFAULT_WIDTH);
	}
}