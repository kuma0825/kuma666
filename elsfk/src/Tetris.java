

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
    // 添加菜单，这里只是意思一下，留待大家自己做更详细的
    // 菜单条
    JMenuBar menubar = new JMenuBar();
    setJMenuBar(menubar);
    // 菜单
    JMenu menuGame = new JMenu("游戏");
    menubar.add(menuGame);
    // 菜单项
    JMenuItem mi1 = new JMenuItem("新游戏");
    mi1.setActionCommand("new");
    JMenuItem mi2 = new JMenuItem("暂停");
    mi2.setActionCommand("pause");
    JMenuItem mi3 = new JMenuItem("继续");
    mi3.setActionCommand("continue");
    JMenuItem mi4 = new JMenuItem("退出");
    mi4.setActionCommand("exit");

    menuGame.add(mi1);
    menuGame.add(mi2);
    menuGame.add(mi3);
    menuGame.add(mi4);

    //菜单项监听
    MenuListener menuListener = new MenuListener();
    mi1.addActionListener(menuListener);
    mi2.addActionListener(menuListener);
    mi3.addActionListener(menuListener);
    mi4.addActionListener(menuListener);

    // 版本菜单
    JMenu menuHelp = new JMenu("帮助");
    menubar.add(menuHelp);
    menuHelp.add("版本所有@熊");


    setLocation(700, 200);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(220, 275);
    setResizable(false);
    tp = new TetrisPanel();
    getContentPane().add(tp);

    // 让整个画布添加键盘监听
    // tp.addKeyListener(tp.listener);//不行,画布不方便获得键盘焦点
    this.addKeyListener(tp.listener); // 让框架来监听键盘
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
        getContentPane().validate();//校验当前容器，有刷新功能       
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
  private int map[][] = new int[13][23];// map[列号][行号]。真正的方块区是:21行*10列。边框(2列，1行)

  // 方块的形状：
  // 第一维代表方块类型(包括7种:S、Z、L、J、I、O、T)
  // 第二维代表旋转次数
  // 第三四维代表方块矩阵
  // shapes[type][turnState][i] i--> block[i/4][i%4]
  int shapes[][][] = new int[][][] {
  /*
   * 模板 { {0,0,0,0,0,0,0,0, 0,0,0,0, 0,0,0,0}, {0,0,0,0,0,0,0,0, 0,0,0,0,
   * 0,0,0,0}, {0,0,0,0,0,0,0,0, 0,0,0,0, 0,0,0,0}, {0,0,0,0,0,0,0,0, 0,0,0,0,
   * 0,0,0,0} }
   */
      // I (※把版本1中的横条从第1行换到第2行)
      { { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0 },
          { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0 } },
      // S
      { { 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
          { 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
          { 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0 } },
      // Z 第3行: shapes[2][2][]
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
  private int x, y;// 当前块的位置---左上角的坐标
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
    // 初始化游戏地图
    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 21; j++) {
        if (i == 0 || i == 11) {// 边框
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
          "Game Over!....笨蛋，敢再来一局吗?!");
      if (op == JOptionPane.YES_OPTION) {
        newGame();
      } else if (op == JOptionPane.NO_OPTION) {
        System.exit(0);
      }
    }
  }

  private void down() {
    if (crash(x, y + 1, type, turnState) == 0) {// 判断当前块往下落一格后是否和地图存在填充块完全重合---注意实参:y+1
      add(x, y, type, turnState);// 把该块加到地图---形成堆积块
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

  // 让一个块堆积，其实是把当前块中的填充块信息记录到map[][]中
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

  // 消块
  private void tryDelLine() {
    // 从上往下，一行行依次遍历，如果某一行的map[i][j]值全是1，则把这一行消掉---上一行往下落
    for (int b = 0; b < 21; b++) {
      int c = 1;
      for (int a = 0; a < 12; a++) {
        c &= map[a][b];
      }
      if (c == 1) {// 全是1--下落一行
        score += 10;
        for (int d = b; d > 0; d--) {
          for (int e = 0; e < 11; e++) {
            map[e][d] = map[e][d - 1];
          }
        }

        // 更改游戏的难度(加快下落速度)
        delay /= 2;
        timer.setDelay(delay);
      }

    }

  }

  private int crash(int x, int y, int blockType, int turnState) {
    for (int a = 0; a < 4; a++) {
      for (int b = 0; b < 4; b++) {
        if ((shapes[blockType][turnState][a * 4 + b] & map[x + b + 1][y
            + a]) == 1) {// 和填充块或框架重合，都算碰撞
          return 0; // 碰撞了---方块的填充块和地图中的填充块完全重合
        }
      }
    }
    return 1;// 没有碰撞
  }

  // 表现层
  @Override
  public void paint(Graphics g) {
    super.paint(g);// 清除残影

    // 画当前块
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

    // 画地图(整个游戏的方块区和边框)
    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 22; j++) {
        if (map[i][j] == 1) {
          g.setColor(Color.red);
          g.fillRect(i * 10, j * 10, 10, 10);// 填充

          g.setColor(Color.yellow);
          g.drawRect(i * 10, j * 10, 10, 10);// 格线
        } else if (map[i][j] == 3) {
          g.setColor(Color.red);
          g.drawRect(i * 10, j * 10, 10, 10);
        }
      }
    }

    // 显示分数，同时为版面美观，在界面上再加点东西
    // 画方块区右侧部分
    g.setColor(Color.blue);
    g.setFont(new Font("aa", Font.BOLD, 18));
    g.drawString("得分=" + score, 130, 20);

    g.setFont(new Font("aa", Font.PLAIN, 13));
    g.drawString("老熊", 130, 70);
    g.drawString("出品", 130, 90);
    g.drawString("爱玩玩", 130, 110);
    g.drawString("不玩滚", 130, 130);
   

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
