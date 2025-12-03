import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

// המחלקה הראשית של המשחק - יורשת מ-JPanel ומממשת ממשקים להאזנה לאירועים
public class PacMan extends JPanel implements ActionListener, KeyListener {
    // מחלקה פנימית שמייצגת בלוק/אובייקט במשחק (פקמן, רוחות, קירות, אוכל)
    class Block {
        int x; // מיקום אופקי
        int y; // מיקום אנכי
        int width; // רוחב הבלוק
        int height; // גובה הבלוק
        Image image; // התמונה של הבלוק

        int startX; // מיקום התחלתי אופקי (לאיפוס)
        int startY; // מיקום התחלתי אנכי (לאיפוס)
        char direction = 'U'; // כיוון תנועה: U=למעלה, D=למטה, L=שמאלה, R=ימינה
        int velocityX = 0; // מהירות תנועה אופקית
        int velocityY = 0; // מהירות תנועה אנכית

        // בנאי - יוצר בלוק חדש
        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x; // שומר את המיקום ההתחלתי
            this.startY = y;
        }

        // מעדכן את כיוון התנועה של הבלוק
        void updateDirection(char direction) {
            char prevDirection = this.direction; // שומר את הכיוון הקודם
            this.direction = direction; // מעדכן לכיוון החדש
            updateVelocity(); // מעדכן את המהירות לפי הכיוון החדש
            this.x += this.velocityX; // מזיז את הבלוק
            this.y += this.velocityY;
            // בדיקה אם יש התנגשות עם קיר
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    // אם יש התנגשות - מבטל את התנועה וחוזר לכיוון הקודם
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        // מעדכן את המהירות בהתאם לכיוון התנועה
        void updateVelocity() {
            if (this.direction == 'U') { // למעלה
                this.velocityX = 0;
                this.velocityY = -tileSize/4; // תנועה שלילית בציר Y
            }
            else if (this.direction == 'D') { // למטה
                this.velocityX = 0;
                this.velocityY = tileSize/4; // תנועה חיובית בציר Y
            }
            else if (this.direction == 'L') { // שמאלה
                this.velocityX = -tileSize/4; // תנועה שלילית בציר X
                this.velocityY = 0;
            }
            else if (this.direction == 'R') { // ימינה
                this.velocityX = tileSize/4; // תנועה חיובית בציר X
                this.velocityY = 0;
            }
        }

        // מאפס את הבלוק למיקום ההתחלתי שלו
        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }
//+++++++++++++++++++++++++++++++++++++++=
    // הגדרות גודל הלוח והמשבצות
    private int rowCount = 21; // מספר שורות בלוח
    private int columnCount = 19; // מספר עמודות בלוח
    private int tileSize = 32; // גודל משבצת בפיקסלים
    private int boardWidth = 642; // רוחב הלוח בפיקסלים - שנה כאן!
    private int boardHeight = 715; // גובה הלוח בפיקסלים - שנה כאן!

    // תמונות של אלמנטים במשחק
    private Image floorImage; // תמונת רקע - רצפה
    private Image wallImage; // תמונת קיר
    private Image blueGhostImage; // תמונת רוח כחולה
    private Image orangeGhostImage; // תמונת רוח כתומה
    private Image pinkGhostImage; // תמונת רוח ורודה
    private Image redGhostImage; // תמונת רוח אדומה

    // תמונות של פקמן בכיוונים שונים
    private Image pacmanUpImage; // פקמן מסתכל למעלה
    private Image pacmanDownImage; // פקמן מסתכל למטה
    private Image pacmanLeftImage; // פקמן מסתכל שמאלה
    private Image pacmanRightImage; // פקמן מסתכל ימינה

    // מפת המשחק - כל תו מייצג אלמנט שונה:
    // X = קיר, O = דלג (ריק), P = פקמן, ' ' = אוכל
    // רוחות: b = כחול, o = כתום, p = ורוד, r = אדום
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X   X   X    X",
        "XXXX XXX X XXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    // קבוצות של אובייקטים במשחק
    HashSet<Block> walls; // כל הקירות
    HashSet<Block> foods; // כל האוכל
    HashSet<Block> ghosts; // כל הרוחות
    Block pacman; // דמות הפקמן

    Timer gameLoop; // טיימר שמריץ את לולאת המשחק
    char[] directions = {'U', 'D', 'L', 'R'}; // מערך כיוונים אפשריים
    Random random = new Random(); // מחולל מספרים אקראיים
    int score = 0; // ניקוד השחקן
    int lives = 3; // מספר החיים
    boolean gameOver = false; // האם המשחק הסתיים
    char nextDirection = 'R'; // הכיוון הבא שהשחקן רוצה לפנות אליו

    // בנאי - מאתחל את המשחק
    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight)); // קובע את גודל הפאנל
        setBackground(Color.BLACK); // רקע שחור (גיבוי למקרה שהתמונה לא תיטען)
        addKeyListener(this); // מאזין ללחיצות מקלדת
        setFocusable(true); // מאפשר לפאנל לקבל פוקוס

        // טעינת כל התמונות מהקבצים
        floorImage = new ImageIcon(getClass().getResource("./bg.png")).getImage(); // תמונת הרקע
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap(); // טוען את המפה
        // נותן לכל רוח כיוון אקראי בהתחלה
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        // יצירת טיימר שמריץ את לולאת המשחק כל 50 מילישניות (20 פריימים לשנייה)
        gameLoop = new Timer(50, this); 
        gameLoop.start(); // מתחיל את הטיימר

    }

    // פונקציה שטוענת את המפה ויוצרת את כל האובייקטים
    public void loadMap() {
        walls = new HashSet<Block>(); // אתחול קבוצת הקירות
        foods = new HashSet<Block>(); // אתחול קבוצת האוכל
        ghosts = new HashSet<Block>(); // אתחול קבוצת הרוחות

        // מעבר על כל משבצת במפה
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r]; // השורה הנוכחית במפה
                char tileMapChar = row.charAt(c); // התו במיקום הנוכחי

                int x = c*tileSize + 16; // מיקום אופקי בפיקסלים + הזזה של 2 פיקסלים ימינה
                int y = r*tileSize + 27; // מיקום אנכי בפיקסלים + הזזה של 2 פיקסלים למטה

                if (tileMapChar == 'X') { // אם זה קיר
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { // רוח כחולה
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { // רוח כתומה
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { // רוח ורודה
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { // רוח אדומה
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { // פקמן
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { // אוכל (נקודה קטנה)
                    // האוכל קטן יותר וממוקם במרכז המשבצת
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    // פונקציה שמציירת את הרכיבים (נקראת אוטומטית על ידי Swing)
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // מצייר את הרקע
        draw(g); // קורא לפונקציה שמצייר את כל המשחק
    }

    // פונקציה שמצייר את כל האלמנטים של המשחק
    public void draw(Graphics g) {
        // מצייר את תמונת הרקע (עם הארונות) על כל המסך
        if (floorImage != null) {
            // מצייר את התמונה בגודל מלא - מותאם לגודל הלוח
            g.drawImage(floorImage, 0, 0, boardWidth, boardHeight, null);
        }
        
        // מצייר את פקמן
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        // מצייר את כל הרוחות
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // מצייר את כל הקירות
        // for (Block wall : walls) {
        //     g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        // }

        // מצייר את האוכל (ריבועים לבנים קטנים)
        g.setColor(Color.BLACK);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        
        // מצייר את הניקוד ומספר החיים
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            // אם המשחק נגמר - מציג הודעת סיום
            g.drawString("Game Over: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
        else {
            // אם המשחק פעיל - מציג חיים וניקוד
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
    }

    // פונקציה שמזיזה את כל הדמויות ובודקת התנגשויות
    public void move() {
        // בודק אם הכיוון המבוקש שונה מהכיוון הנוכחי ואם אפשר לפנות אליו
        if (nextDirection != pacman.direction) {
            // שומר את המיקום והכיוון הנוכחיים
            int oldX = pacman.x;
            int oldY = pacman.y;
            char oldDirection = pacman.direction;
            
            // מנסה לעדכן לכיוון החדש
            pacman.updateDirection(nextDirection);
            
            // בודק אם יש התנגשות עם קיר בכיוון החדש
            boolean canTurn = true;
            for (Block wall : walls) {
                if (collision(pacman, wall)) {
                    // אם יש קיר - לא ניתן לפנות, חוזר למצב הקודם
                    canTurn = false;
                    pacman.x = oldX;
                    pacman.y = oldY;
                    pacman.direction = oldDirection;
                    pacman.updateVelocity();
                    break;
                }
            }
            
            // אם הצלחנו לפנות - מעדכן את התמונה
            if (canTurn) {
                if (pacman.direction == 'U') {
                    pacman.image = pacmanUpImage;
                }
                else if (pacman.direction == 'D') {
                    pacman.image = pacmanDownImage;
                }
                else if (pacman.direction == 'L') {
                    pacman.image = pacmanLeftImage;
                }
                else if (pacman.direction == 'R') {
                    pacman.image = pacmanRightImage;
                }
            }
        }
        
        // הזזת פקמן לפי המהירות שלו
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // בדיקת מעבר דרך המנהרות (טלפורטציה בין הצדדים)
        // רק אם פקמן יוצא מהמסך לגמרי
        if (pacman.x + pacman.width < 0) {
            // אם יצא משמאל - מופיע מימין
            pacman.x = boardWidth;
        }
        else if (pacman.x > boardWidth) {
            // אם יצא מימין - מופיע משמאל
            pacman.x = -pacman.width;
        }
        
        if (pacman.y + pacman.height < 0) {
            // אם יצא מלמעלה - מופיע מלמטה
            pacman.y = boardHeight;
        }
        else if (pacman.y > boardHeight) {
            // אם יצא מלמטה - מופיע מלמעלה
            pacman.y = -pacman.height;
        }

        // בדיקת התנגשות עם קירות
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                // אם פקמן התנגש בקיר - מבטל את התנועה
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // בדיקת התנגשות עם רוחות ותנועת הרוחות
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                // אם רוח נגעה בפקמן - מפסיד חיים
                lives -= 1;
                if (lives == 0) {
                    // אם אין יותר חיים - המשחק נגמר
                    gameOver = true;
                    return;
                }
                resetPositions(); // מאפס את מיקומי הדמויות
            }

            // אם הרוח נמצאת בשורה 9 ולא זז למעלה/למטה - כופה אותה ללכת למעלה
            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            
            // הזזת הרוח
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            
            // בדיקה אם הרוח התנגשה בקיר או בגבול המסך
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    // אם יש התנגשות - מבטל את התנועה ונותן כיוון אקראי חדש
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        // בדיקת אכילת אוכל
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                // אם פקמן נגע באוכל - שומר אותו למחיקה ומוסיף ניקוד
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten); // מוחק את האוכל שנאכל

        // אם כל האוכל נאכל - טוען מפה חדשה ומאפס מיקומים
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    // פונקציה שבודקת אם שני בלוקים מתנגשים
    public boolean collision(Block a, Block b) {
        // בדיקת חפיפה בין שני מלבנים
        return  a.x < b.x + b.width && // הצד השמאלי של a שמאלה לצד הימני של b
                a.x + a.width > b.x && // הצד הימני של a ימינה לצד השמאלי של b
                a.y < b.y + b.height && // הצד העליון של a מעל לצד התחתון של b
                a.y + a.height > b.y; // הצד התחתון של a מתחת לצד העליון של b
    }

    // פונקציה שמאפסת את כל הדמויות למיקומים ההתחלתיים
    public void resetPositions() {
        pacman.reset(); // מאפס את פקמן למיקום ההתחלתי
        pacman.velocityX = 0; // מאפס את המהירות
        pacman.velocityY = 0;
        pacman.direction = 'R'; // מאפס את הכיוון לימין
        nextDirection = 'R'; // מאפס את הכיוון המבוקש
        pacman.image = pacmanRightImage; // מאפס את התמונה
        // מאפס את כל הרוחות ונותן להן כיוונים אקראיים חדשים
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    // פונקציה שנקראת כל פעם שהטיימר מתעורר (כל 50 מילישניות)
    @Override
    public void actionPerformed(ActionEvent e) {
        move(); // מזיז את כל הדמויות
        repaint(); // מצייר מחדש את המסך
        if (gameOver) {
            gameLoop.stop(); // עוצר את הטיימר אם המשחק נגמר
        }
    }

    // פונקציה שנקראת כשמקלידים תו (לא משתמשים בה כרגע)
    @Override
    public void keyTyped(KeyEvent e) {}

    // פונקציה שנקראת כשלוחצים על מקש - שומר את הכיוון המבוקש
    @Override
    public void keyPressed(KeyEvent e) {
        // שומר את הכיוון שהשחקן רוצה לפנות אליו
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            nextDirection = 'U'; // חץ למעלה
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            nextDirection = 'D'; // חץ למטה
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            nextDirection = 'L'; // חץ שמאלה
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            nextDirection = 'R'; // חץ ימינה
        }
    }

    // פונקציה שנקראת כשמשחררים מקש
    @Override
    public void keyReleased(KeyEvent e) {
        // אם המשחק נגמר - איפוס המשחק בלחיצה על כל מקש
        if (gameOver) {
            loadMap(); // טוען מפה חדשה
            resetPositions(); // מאפס מיקומים
            lives = 3; // מאפס את מספר החיים
            score = 0; // מאפס את הניקוד
            gameOver = false; // מבטל את מצב סיום המשחק
            nextDirection = 'R'; // מאפס את הכיוון המבוקש
            gameLoop.start(); // מתחיל את הטיימר מחדש
        }
    }
}
