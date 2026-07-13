import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

// המחלקה הראשית של המשחק - יורשת מ-JPanel ומממשת ממשקים להאזנה לאירועים
public class PacMan extends JPanel implements ActionListener, KeyListener, MouseListener {
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
    // *** הגדרות גודל - כאן אתה משנה את הכל! ***
    
    // 1. גודל הבלוקים (פקמן, רוחות, קירות)
    private int tileSize = 32; // שנה כאן: יותר גדול = בלוקים גדולים, יותר קטן = בלוקים קטנים
    
    // 2. כמות משבצות במשחק
    private int rowCount = 17; // מספר שורות בלוח (בלי קירות חיצוניים)
    private int columnCount = 19; // מספר עמודות בלוח
    
    // 3. הזזת המשחק בתוך התמונה (כדי שיהיה באמצע)
    private int offsetX = 3; // הזזה ימינה - ממזער את הרקע בצדדים
    private int offsetY = 3; // הזזה למטה - ממזער את הרקע למעלה ולמטה
    
    // 4. גודל החלון (אוטומטי לפי הגדרות למעלה)
    private int gameAreaWidth = columnCount * tileSize + offsetX * 2; // רוחב אזור המשחק
    private int boardWidth = gameAreaWidth; // רוחב הלוח (בלי פאנל צדדי)
    private int boardHeight = rowCount * tileSize + offsetY * 2 + 6; // גובה הלוח - ממזער רקע

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
    private Image heartImage; // תמונת לב לחיים

    // מפת המשחק - כל תו מייצג אלמנט שונה:
    // X = קיר, O = דלג (ריק), P = פקמן, ' ' = אוכל
    // רוחות: b = כחול, o = כתום, p = ורוד, r = אדום
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXX XX",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X   X   X    X",
        "XXXX XXX X XXX XXXX",
        "   X X       X X   ",
        "XXXX X XXrXX X XXXX",
        "         bpo       ",
        "XXXX X XXXXX X XXXX",
        "   X X       X X   ",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "XXXXXXXXXXXXXXXX XX"
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
    int totalFoodCount = 0; // סה"כ אוכל בהתחלה
    int lives = 3; // מספר החיים
    boolean gameOver = false; // האם המשחק הסתיים
    boolean isPaused = false; // האם המשחק מושהה אחרי פסילה
    char nextDirection = 'R'; // הכיוון הבא שהשחקן רוצה לפנות אליו
    
    // מסך פתיחה ולוח תוצאות
    boolean showStartScreen = true; // האם להציג מסך פתיחה
    boolean showHighScores = false; // האם להציג לוח תוצאות
    String[][] highScores = {
        {"NOT.N.T", "481"},
        {"MenMen", "411"},
        {"Igor", "399"},
        {"Shmuel", "350"},
        {"Oded", "294"},
        {"Eitan", "288"},
        {"Nattai", "277"},
        {"Dvir", "255"},
        {"Shneor", "241"},
        {"LeviYitzchak", "228"}
    };
    
    // מיקום וגודל כפתור הסגירה
    int closeButtonX;
    int closeButtonY;
    int closeButtonSize = 18;

    // בנאי - מאתחל את המשחק
    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight)); // קובע את גודל הפאנל
        setBackground(Color.BLACK); // רקע שחור (גיבוי למקרה שהתמונה לא תיטען)
        addKeyListener(this); // מאזין ללחיצות מקלדת
        addMouseListener(this); // מאזין ללחיצות עכבר
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
        
        // טעינת תמונת הלב (אם קיימת)
        try {
            heartImage = new ImageIcon(getClass().getResource("./heart.png")).getImage();
        } catch (Exception e) {
            heartImage = null;
        }

        loadMap(); // טוען את המפה
        // נותן לכל רוח כיוון אקראי בהתחלה
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        // יצירת טיימר שמריץ את לולאת המשחק כל 50 מילישניות (20 פריימים לשניה)
        gameLoop = new Timer(50, this); 
        // הטיימר יתחיל רק לאחר לחיצה על רווח
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

                int x = c*tileSize + offsetX ; // מיקום אופקי בפיקסלים + הזזה למרכז
                int y = r*tileSize + offsetY +15; // מיקום אנכי בפיקסלים + הזזה למרכז

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
        totalFoodCount = foods.size(); // שומר את כמות האוכל ההתחלתית
    }

    // פונקציה שמציירת את הרכיבים (נקראת אוטומטית על ידי Swing)
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // מצייר את הרקע
        draw(g); // קורא לפונקציה שמצייר את כל המשחק
    }

    // פונקציה שמצייר את כל האלמנטים של המשחק
    public void draw(Graphics g) {
        // אם מסך הפתיחה מוצג
        if (showStartScreen) {
            drawStartScreen(g);
            return;
        }
        
        // אם לוח התוצאות מוצג
        if (showHighScores) {
            drawHighScores(g);
            return;
        }
        
        // מצייר את תמונת הרקע (עם הארונות) רק על אזור המשחק - לא על הפאנל הצדדי
        if (floorImage != null) {
            // מצייר את התמונה רק על אזור המשחק (לא מותח לכל החלון)
            g.drawImage(floorImage, 0, 0, gameAreaWidth, boardHeight, null);
        }
        
        // טקסט ניקוד ולבבות בלי מלבן - למעלה בצד שמאל
        int startX = 75; // 2.5 ס"מ זה בערך 75 פיקסלים (בהנחה של 96 DPI)
        int startY = 3;
        
        // כפתור X לסגירת המשחק - בפינה השמאלית העליונה
        closeButtonSize = 14; // כפתור קטן יותר
        closeButtonX = 35; // 1 ס"מ ימינה (בערך 30 פיקסלים)
        closeButtonY = 3;
        
        // רקע אדום לכפתור
        g.setColor(new Color(220, 50, 50));
        g.fillRoundRect(closeButtonX, closeButtonY, closeButtonSize, closeButtonSize, 4, 4);
        
        // מסגרת כהה לכפתור
        g.setColor(new Color(150, 30, 30));
        g.drawRoundRect(closeButtonX, closeButtonY, closeButtonSize, closeButtonSize, 4, 4);
        
        // X לבן בתוך הכפתור
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 11));
        g.drawString("X", closeButtonX + 4, closeButtonY + 11);
        
        // ניקוד בפורמט "כמה אכלת\סה"כ"
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 13));
        String scoreStr = score + "\\" + totalFoodCount;
        g.drawString(scoreStr, startX, startY + 15);
        int scoreWidth = g.getFontMetrics().stringWidth(scoreStr);
        
        // לבבות אחרי הניקוד עם רווח
        int heartSize = 16;
        int heartSpacing = 20;
        int heartY = startY + 2;
        int heartStartX = startX + scoreWidth + 15; // רווח גדול יותר אחרי הניקוד
        
        for (int i = 0; i < lives; i++) {
            int heartX = heartStartX + (i * heartSpacing);
            if (heartImage != null) {
                g.drawImage(heartImage, heartX, heartY, heartSize, heartSize, null);
            } else {
                drawHeart(g, heartX, heartY, heartSize);
            }
        }

        
        // מצייר את פקמן
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        // מצייר את כל הרוחות
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // מצייר את הקירות הפנימיים
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        // מצייר את האוכל (ריבועים לבנים קטנים)
        g.setColor(Color.BLACK);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        
        // הודעת Game Over במרכז המסך
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, boardWidth, boardHeight);
            
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String gameOverText = "!המשחק נגמר";
            int textWidth = g.getFontMetrics().stringWidth(gameOverText);
            g.drawString(gameOverText, (gameAreaWidth - textWidth) / 2, boardHeight / 2 - 40);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            String finalScore = "ניקוד סופי: " + score;
            textWidth = g.getFontMetrics().stringWidth(finalScore);
            g.drawString(finalScore, (gameAreaWidth - textWidth) / 2, boardHeight / 2 + 20);
        }
    }
    
    // פונקציה שמציירת לב
    private void drawHeart(Graphics g, int x, int y, int size) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(Color.RED);
        int[] xPoints = {x + size/2, x + (int)(size * 0.2), x, x, x + size/2, x + size, x + size, x + (int)(size * 0.8), x + size/2};
        int[] yPoints = {y + (int)(size * 0.3), y + (int)(size * 0.1), y + (int)(size * 0.1), y + (int)(size * 0.4), y + size, y + (int)(size * 0.4), y + (int)(size * 0.1), y + (int)(size * 0.1), y + (int)(size * 0.3)};
        g2d.fillPolygon(xPoints, yPoints, 9);
        
        g2d.setColor(new Color(139, 0, 0));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(xPoints, yPoints, 9);
    }
    
    // מסך פתיחה
    private void drawStartScreen(Graphics g) {
        // רקע עם תמונת המשחק
        if (floorImage != null) {
            g.drawImage(floorImage, 0, 0, boardWidth, boardHeight, null);
        }
        
        // שכבת כהייה
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, boardWidth, boardHeight);
        
        // כותרת
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "HASIDIC PAC-MAN";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (boardWidth - titleWidth) / 2, 150);
        
        // הוראות
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String instruction1 = "לחץ SPACE להתחיל משחק";
        int inst1Width = g.getFontMetrics().stringWidth(instruction1);
        g.drawString(instruction1, (boardWidth - inst1Width) / 2, 280);
        
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String instruction2 = "לחץ H או CTRL לראות ציונים גבוהים";
        int inst2Width = g.getFontMetrics().stringWidth(instruction2);
        g.drawString(instruction2, (boardWidth - inst2Width) / 2, 320);
    }
    
    // לוח תוצאות
    private void drawHighScores(Graphics g) {
        // רקע שחור
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, boardWidth, boardHeight);
        
        // כותרת
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Arial", Font.BOLD, 42));
        String title = "TOP 10 HIGH SCORES";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (boardWidth - titleWidth) / 2, 80);
        
        // רשימת שחקנים
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        int startY = 140;
        int lineHeight = 35;
        
        for (int i = 0; i < highScores.length; i++) {
            // מספר מקום
            g.setColor(new Color(255, 215, 0));
            String rank = (i + 1) + ".";
            g.drawString(rank, 100, startY + i * lineHeight);
            
            // שם שחקן
            g.setColor(Color.WHITE);
            g.drawString(highScores[i][0], 150, startY + i * lineHeight);
            
            // ניקוד
            g.setColor(new Color(100, 200, 255));
            String scoreStr = highScores[i][1];
            int scoreWidth = g.getFontMetrics().stringWidth(scoreStr);
            g.drawString(scoreStr, boardWidth - 150 - scoreWidth, startY + i * lineHeight);
        }
        
        // הוראה לחזרה
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        String backInstruction = "לחץ SPACE לחזור";
        int backWidth = g.getFontMetrics().stringWidth(backInstruction);
        g.drawString(backInstruction, (boardWidth - backWidth) / 2, boardHeight - 50);
    }

    // פונקציה שמזיזה את כל הדמויות ובודקת התנגשויות
    public void move() {
        // אם המשחק מושהה - לא מזיזים כלום
        if (isPaused) {
            return;
        }
        
        // בודק אם הכיוון המבוקש שונה מהכיוון הנוכחי
        if (nextDirection != pacman.direction) {
            // מנסה לפנות לכיוון החדש
            char oldDirection = pacman.direction;
            pacman.direction = nextDirection;
            pacman.updateVelocity();
            
            // בודק צעד אחד קדימה אם יש קיר
            int testX = pacman.x + pacman.velocityX;
            int testY = pacman.y + pacman.velocityY;
            boolean hitWall = false;
            
            for (Block wall : walls) {
                if (testX < wall.x + wall.width && 
                    testX + pacman.width > wall.x && 
                    testY < wall.y + wall.height && 
                    testY + pacman.height > wall.y) {
                    // יש קיר - חוזר לכיוון הקודם
                    hitWall = true;
                    pacman.direction = oldDirection;
                    pacman.updateVelocity();
                    break;
                }
            }
            
            // אם הצלחנו לפנות - מעדכן את התמונה
            if (!hitWall) {
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
        // מיד כשפקמן חוצה את הקיר החיצוני
        if (pacman.x < offsetX) {
            // אם יצא משמאל - מופיע מימין
            pacman.x = gameAreaWidth - offsetX - tileSize;
        }
        else if (pacman.x > gameAreaWidth - offsetX - tileSize) {
            // אם יצא מימין - מופיע משמאל
            pacman.x = offsetX;
        }
        
        if (pacman.y < offsetY) {
            // אם יצא מלמעלה - מופיע מלמטה
            pacman.y = boardHeight - offsetY - tileSize;
        }
        else if (pacman.y > boardHeight - offsetY - tileSize) {
            // אם יצא מלמטה - מופיע מלמעלה
            pacman.y = offsetY;
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
                isPaused = true; // משהה את המשחק אחרי איבוד חיים
                break; // יוצא מהלולאה אחרי התנגשות
            }

            // אם הרוח נמצאת בשורה 9 ולא זז למעלה/למטה - כופה אותה ללכת למעלה
            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            
            // הזזת הרוח
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            
            // בדיקת מעבר דרך המנהרות לרוחות (טלפורטציה)
            if (ghost.x < offsetX) {
                ghost.x = gameAreaWidth - offsetX - tileSize;
            }
            else if (ghost.x > gameAreaWidth - offsetX - tileSize) {
                ghost.x = offsetX;
            }
            if (ghost.y < offsetY) {
                ghost.y = boardHeight - offsetY - tileSize;
            }
            else if (ghost.y > boardHeight - offsetY - tileSize) {
                ghost.y = offsetY;
            }
            
            // בדיקה אם הרוח התנגשה בקיר
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
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
                score += 1; // כל אכילה שווה נקודה אחת
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
        // במסך הפתיחה
        if (showStartScreen) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                // התחלת משחק
                showStartScreen = false;
                loadMap();
                resetPositions();
                gameLoop.start();
            }
            else if (e.getKeyCode() == KeyEvent.VK_H || e.getKeyCode() == KeyEvent.VK_CONTROL) {
                // הצגת לוח תוצאות
                showStartScreen = false;
                showHighScores = true;
            }
            repaint();
            return;
        }
        
        // בלוח התוצאות
        if (showHighScores) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                showHighScores = false;
                showStartScreen = true;
            }
            repaint();
            return;
        }
        
        // אם המשחק נגמר - איפוס המשחק בלחיצה על כל מקש
        if (gameOver) {
            loadMap(); // טוען מפה חדשה
            resetPositions(); // מאפס מיקומים
            lives = 3; // מאפס את מספר החיים
            score = 0; // מאפס את הניקוד
            gameOver = false; // מבטל את מצב סיום המשחק
            nextDirection = 'R'; // מאפס את הכיוון המבוקש
            gameLoop.start(); // מתחיל את הטיימר מחדש
            return;
        }
        
        // שומר את הכיוון שהשחקן רוצה לפנות אליו
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            nextDirection = 'U'; // חץ למעלה
            if (isPaused) isPaused = false; // מבטל השהיה אם המשחק מושהה
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            nextDirection = 'D'; // חץ למטה
            if (isPaused) isPaused = false; // מבטל השהיה אם המשחק מושהה
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            nextDirection = 'L'; // חץ שמאלה
            if (isPaused) isPaused = false; // מבטל השהיה אם המשחק מושהה
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            nextDirection = 'R'; // חץ ימינה
            if (isPaused) isPaused = false; // מבטל השהיה אם המשחק מושהה
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
    
    // פונקציות MouseListener
    @Override
    public void mouseClicked(MouseEvent e) {
        // בדיקה אם לחצו על כפתור הסגירה
        int mouseX = e.getX();
        int mouseY = e.getY();
        
        if (mouseX >= closeButtonX && mouseX <= closeButtonX + closeButtonSize &&
            mouseY >= closeButtonY && mouseY <= closeButtonY + closeButtonSize) {
            // יציאה מהמשחק
            System.exit(0);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {}
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
}
