import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.stream.Collectors;

public class MyFrame extends JFrame {

    private JPanel panel;
    private final JPanel mainPanel = mainPanel();
    private final JButton mainMenuButton = mainMenuButton();
    private  JButton cancelMove;

    public MyFrame(){
        super();
        this.setLayout(null);
        this.setBackground(Color.black);
        this.setSize(Const.FRAME_SIZE);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panel = mainPanel;
        this.add(panel);
        this.setVisible(true);
    }


    private JButton vsComputerButton(){
        MyFrame frame = this;
        JButton button = new JButton("1 Player");
        button.setBounds((688-300)/2 , 200, 300, 50);
        button.addActionListener(e -> {
            setVisible(false);
            getContentPane().remove(panel);
            panel = new BoardComp(this);
            cancelMove = cancelMove();
            add(panel);
            add(cancelMove);
            add(mainMenuButton);
            repaint();
            setVisible(true);
        });
        return button;

    }

    private JButton cancelMove(){
        MyFrame myFrame = this;
        BoardComp board = (BoardComp) panel;
        JButton button = new JButton("Cancel");
        button.setBounds(1100 , 200, 200, 50);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(board.previousMove.isEmpty() || board.previousSelected.isEmpty())
                    return;
                Move prevMove = board.previousMove.pop();
                board.unmakeMove(prevMove);
                board.selected = board.previousSelected.pop();
                if(prevMove.isKill){
                    board.possibleSquares = board.getKills(prevMove.piece).stream().map(k->k.newPosition).collect(Collectors.toList());
                }else {
                    board.possibleSquares = board.getMoves(prevMove.piece).stream().map(m->m.newPosition).collect(Collectors.toList());
                }
                myFrame.repaint();
            }
        });
        return button;
    }
    private JButton mainMenuButton(){
        JButton button = new JButton("Main Menu");
        button.setBounds(1100 , 100, 200, 50);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                remove(panel);
                remove(cancelMove);
                remove(button);
                panel = mainPanel;
                add(panel);
                repaint();
                setVisible(true);
            }
        });
        return button;
    }

    private JPanel mainPanel(){
        JPanel result = new JPanel();
        result.setLayout(null);
        result.setBounds(Const.PANEL_BOUNDS);
        result.setSize(Const.PANEL_SIZE, Const.PANEL_SIZE);
        result.setBackground(Color.gray);
        result.add(vsComputerButton());
        return result;
    }

    private JPanel gameOverPanel(int who_won){
        JPanel result = new JPanel();
        result.setLayout(null);
        result.setBounds(Const.PANEL_BOUNDS);
        result.setSize(Const.PANEL_SIZE, Const.PANEL_SIZE);
        result.setBackground(Color.gray);
        JLabel label = new JLabel();
        label.setLayout(null);
        label.setBounds((688-300)/2 , 200, 300, 50);
        String res = who_won == Const.BLACK ? "Player 2 Wins" : "Player 1 Wins";
        label.setText(res);
        result.add(label);
        return result;
    }

    public void gameOver(int who_won){
        getContentPane().remove(panel);
        panel = gameOverPanel(who_won);
        add(panel);
        repaint();

    }

}
