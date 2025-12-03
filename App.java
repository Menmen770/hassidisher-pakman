import javax.swing.JFrame;

// המחלקה הראשית שמריצה את המשחק
public class App {
    public static void main(String[] args) throws Exception {
        // הגדרת גודל הלוח - 21 שורות, 19 עמודות
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32; // גודל כל משבצת בפיקסלים
        int boardWidth = columnCount * tileSize; // רוחב הלוח הכולל
        int boardHeight = rowCount * tileSize; // גובה הלוח הכולל

        // יצירת החלון הראשי של המשחק
        JFrame frame = new JFrame("Pac Man");
        // frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight); // הגדרת גודל החלון
        frame.setLocationRelativeTo(null); // מרכז את החלון במסך
        frame.setResizable(false); // לא ניתן לשנות את גודל החלון
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // סגירת התוכנית בלחיצה על X

        // יצירת אובייקט המשחק והוספתו לחלון
        PacMan pacmanGame = new PacMan();
        frame.add(pacmanGame); // הוספת פאנל המשחק לחלון
        frame.pack(); // התאמת גודל החלון לתכולה
        pacmanGame.requestFocus(); // מעביר פוקוס למשחק כדי לקלוט לחיצות מקלדת
        frame.setVisible(true); // הצגת החלון על המסך

    }
}
