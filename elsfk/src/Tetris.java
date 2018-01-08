

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Tetris extends JFrame {
  private TetrisPanel tp;

  public Tetris() {
    // ��Ӳ˵�������ֻ����˼һ�£���������Լ�������ϸ��
    // �˵���
    JMenuBar menubar = new JMenuBar();
    setJMenuBar(menubar);
    // �˵�
    JMenu menuGame = new JMenu("��Ϸ");
    menubar.add(menuGame);
    // �˵���
    JMenuItem mi1 = new JMenuItem("����Ϸ");
    mi1.setActionCommand("new");
    JMenuItem mi2 = new JMenuItem("��ͣ");
    mi2.setActionCommand("pause");
    JMenuItem mi3 = new JMenuItem("����");
    mi3.setActionCommand("continue");
    JMenuItem mi4 = new JMenuItem("�˳�");
    mi4.setActionCommand("exit");

    menuGame.add(mi1);
    menuGame.add(mi2);
    menuGame.add(mi3);
    menuGame.add(mi4);

    //�˵������
    MenuListener menuListener = new MenuListener();
    mi1.addActionListener(menuListener);
    mi2.addActionListener(menuListener);
    mi3.addActionListener(menuListener);
    mi4.addActionListener(menuListener);

    // �汾�˵�
    JMenu menuHelp = new JMenu("����");
    menubar.add(menuHelp);
    menuHelp.add("�汾����@��");


    setLocation(700, 200);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(220, 275);
    setResizable(false);
    tp = new TetrisPanel();
    getContentPane().add(tp);

    // ������������Ӽ��̼���
    // tp.addKeyListener(tp.listener);//����,�����������ü��̽���
    this.addKeyListener(tp.listener); // �ÿ������������
  }

  public static void main(String[] args) {
    Tetris te = new Tetris();
    te.setVisible(true);
  }

  class MenuListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equalsIgnoreCase("new")){
        getContentPane().remove(tp);
        tp = new TetrisPanel();
        getContentPane().add(tp);
        getContentPane().validate();//У�鵱ǰ��������ˢ�¹���       
      }else if(e.getActionCommand().equalsIgnoreCase("pause")){
        tp.getTimer().stop();
      }else if(e.getActionCommand().equalsIgnoreCase("continue")){
        tp.getTimer().restart();
      }else if(e.getActionCommand().equalsIgnoreCase("exit")){
        System.exit(0);
      }
    }
  }
}

class TetrisPanel extends JPanel {
  private int map[][] = new int[13][23];// map[�к�][�к�]�������ķ�������:21��*10�С��߿�(2�У�1��)

  // �������״��
  // ��һά����������(����7��:S��Z��L��J��I��O��T)
  // �ڶ�ά������ת����
  // ������ά���������
  // shapes[type][turnState][i] i--> block[i/4][i%4]
  int shapes[][][] = new int[][][] {
  /*
   * ģ�� { {0,0,0,0,0,0,0,0, 0,0,0,0, 0,0,0,0}, {0,0,0,0,0,0,0,0, 0,0,0,0,
   * 0,0,0,0}, {0,0,0,0,0,0,0,0, 0,0,0,0, 0,0,0,0}, {0,0,0,0,0,0,0,0, 0,0,0,0,
   * 0,0,0,0} }
   */
      // I (���Ѱ汾1�еĺ����ӵ�1�л�����2��)
      { { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0 },
          { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0 } },
      // S
      { { 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
          { 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0 } },
      // Z ��3��: shapes[2][2][]
      { { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 },
          { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 } },
      // J
      { { 0, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0 },
          { 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 },
          { 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
      // O
      { { 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
      // L
      { { 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0 },
          { 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
          { 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
      // T
      { { 0, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
          { 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0 } } };

  private int type;
  private int turnState;
  private int x, y;// ��ǰ���λ��---���Ͻǵ�����
  private int score = 0;
  private Timer timer = null;
  private int delay = 1000;

  TimerListener listener = null;

  public TetrisPanel() {
    newGame();
    nextBlock();

    listener = new TimerListener();
    timer = new Timer(delay, listener);
    timer.start();

  }

  private void newGame() {
    // ��ʼ����Ϸ��ͼ
    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 21; j++) {
        if (i == 0 || i == 11) {// �߿�
          map[i][j] = 3;
        } else {
          map[i][j] = 0;
        }
      }
      map[i][21] = 3;
    }
    score = 0;
  }

  private void nextBlock() {
    type = (int) (Math.random() * 1000) % 7; // type=5;
    turnState = (int) (Math.random() * 1000) % 4; // turnState=3;
    x = 4;
    y = 0;
    if (crash(x, y, type, turnState) == 0) {
      timer.stop();
      int op = JOptionPane.showConfirmDialog(null,
          "Game Over!....������������һ����?!");
      if (op == JOptionPane.YES_OPTION) {
        newGame();
      } else if (op == JOptionPane.NO_OPTION) {
        System.exit(0);
      }
    }
  }

  private void down() {
    if (crash(x, y + 1, type, turnState) == 0) {// �жϵ�ǰ��������һ����Ƿ�͵�ͼ����������ȫ�غ�---ע��ʵ��:y+1
      add(x, y, type, turnState);// �Ѹÿ�ӵ���ͼ---�γɶѻ���
      nextBlock();
    } else {
      y++;
    }
    repaint();
  }

  private void left() {
    if (x >= 0) {
      x -= crash(x - 1, y, type, turnState);
    }
    repaint();
  }

  private void right() {
    if (x < 8) {
      x += crash(x + 1, y, type, turnState);
    }
    repaint();
  }

  private void turn() {
    if (crash(x, y, type, (turnState + 1) % 4) == 1) {
      turnState = (turnState + 1) % 4;
    }
    repaint();
  }

  // ��һ����ѻ�����ʵ�ǰѵ�ǰ���е�������Ϣ��¼��map[][]��
  private void add(int x, int y, int type, int turnState) {
    for (int a = 0; a < 4; a++) {
      for (int b = 0; b < 4; b++) {
        if (shapes[type][turnState][a * 4 + b] == 1) {
          map[x + b + 1][y + a] = 1;
        }
      }
    }
    tryDelLine();
  }

  // ����
  private void tryDelLine() {
    // �������£�һ�������α��������ĳһ�е�map[i][j]ֵȫ��1�������һ������---��һ��������
    for (int b = 0; b < 21; b++) {
      int c = 1;
      for (int a = 0; a < 12; a++) {
        c &= map[a][b];
      }
      if (c == 1) {// ȫ��1--����һ��
        score += 10;
        for (int d = b; d > 0; d--) {
          for (int e = 0; e < 11; e++) {
            map[e][d] = map[e][d - 1];
          }
        }

        // ������Ϸ���Ѷ�(�ӿ������ٶ�)
        delay /= 2;
        timer.setDelay(delay);
      }

    }

  }

  private int crash(int x, int y, int blockType, int turnState) {
    for (int a = 0; a < 4; a++) {
      for (int b = 0; b < 4; b++) {
        if ((shapes[blockType][turnState][a * 4 + b] & map[x + b + 1][y
            + a]) == 1) {// ����������غϣ�������ײ
          return 0; // ��ײ��---���������͵�ͼ�е�������ȫ�غ�
        }
      }
    }
    return 1;// û����ײ
  }

  // ���ֲ�
  @Override
  public void paint(Graphics g) {
    super.paint(g);// �����Ӱ

    // ����ǰ��
    for (int j = 0; j < 16; j++) {
      if (shapes[type][turnState][j] == 1) {
        g.setColor(Color.green);
        g.fillRect((j % 4 + x + 1) * 10, (j / 4 + y) * 10, 10, 10);
      }
    }

    /*
     * for(int a=0;a<4;a++){ for(int b=0;b<4;b++){
     * if(shapes[type][turnState][a*4+b]==1){ g.fillRect((b+x+1)*10,
     * (a+y)*10, 10, 10); } } }
     */

    // ����ͼ(������Ϸ�ķ������ͱ߿�)
    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 22; j++) {
        if (map[i][j] == 1) {
          g.setColor(Color.red);
          g.fillRect(i * 10, j * 10, 10, 10);// ���

          g.setColor(Color.yellow);
          g.drawRect(i * 10, j * 10, 10, 10);// ����
        } else if (map[i][j] == 3) {
          g.setColor(Color.red);
          g.drawRect(i * 10, j * 10, 10, 10);
        }
      }
    }

    // ��ʾ������ͬʱΪ�������ۣ��ڽ������ټӵ㶫��
    // ���������Ҳಿ��
    g.setColor(Color.blue);
    g.setFont(new Font("aa", Font.BOLD, 18));
    g.drawString("�÷�=" + score, 130, 20);

    g.setFont(new Font("aa", Font.PLAIN, 13));
    g.drawString("����", 130, 70);
    g.drawString("��Ʒ", 130, 90);
    g.drawString("������", 130, 110);
    g.drawString("�����", 130, 130);
   

  }

  class TimerListener extends KeyAdapter implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      down();
    }

    @Override
    public void keyPressed(KeyEvent e) {
      // System.out.println("aaaaa");
      switch (e.getKeyCode()) {
      case KeyEvent.VK_DOWN:
        down();
        break;
      case KeyEvent.VK_LEFT:
        left();
        break;
      case KeyEvent.VK_RIGHT:
        right();
        break;
      case KeyEvent.VK_UP:
        turn();
      }
    }

  }

  public Timer getTimer() {
    return timer;
  }
}
