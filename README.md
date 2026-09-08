# Hassidisher Pakman

משחק מבוך בסגנון Pac-Man, בעברית, עם אווירת ישיבה — בנוי ב־**Java Swing**.

אסוף נקודות, הימנע מרוחות, אסוף פרסים בכל סדר, והשאיר את שמך בטבלת השיאים.

<p align="center">
  <img src="docs/screenshots/shots.jpg" alt="Hassidisher Pakman — תפריט ומשחק" width="900">
</p>

## הורדה לשחק (בלי Java)

למי שרוצה רק לשחק — **לא צריך** להתקין Java / JDK:

1. הורד את הקובץ: **[Hassidisher-Pakman-Windows.zip](https://github.com/Menmen770/hassidisher-pakman/releases/latest/download/Hassidisher-Pakman-Windows.zip)**
2. חלץ את ה־ZIP
3. לחץ על **`Hassidisher Pakman.exe`** (עם אייקון המשחק) — והמשחק נפתח

או מכל הגרסאות: [Releases](https://github.com/Menmen770/hassidisher-pakman/releases)

## מה יש במשחק

- **4 סדרים** — שחרית · סדר עיונא · סדר גירסא · סדר ערב
- **פרס בכל שלב** — תפילין, ספרים, כובע (נקודות בונוס)
- **סאונד** — אפקטים במשחק ובתפריט
- **תפריט בעברית** עם רקע מותאם ועיצוב זהב/כחול
- **טבלת שיאים** שמורה מקומית
- חלון קבוע, תנועה על רשת אריחים, רקעי מבוך מותאמים לכל שלב

## מקשים

| מקש | פעולה |
|-----|--------|
| חיצים | תנועה |
| ESC | חזרה לתפריט |
| Enter | אישור (סוף משחק / שיא חדש) |

## למפתחים (הרצה מהקוד)

**דרישה:** JDK 17 ומעלה

```bash
# קומפילציה + הרצה
run.bat
```

או:

```bash
javac -encoding UTF-8 -cp "bin;lib/*" -d bin src/com/hasidicmaze/*.java src/com/hasidicmaze/*/*.java
java -cp "bin;lib/*" com.hasidicmaze.App
```

לבנות מחדש את חבילת ה־Windows (EXE + ZIP):

```bash
build-release.bat
```

הפלט: `dist/Hassidisher-Pakman-Windows.zip` ו־`dist/stage/Hassidisher Pakman/Hassidisher Pakman.exe`

## מבנה הפרויקט (בקצרה)

```
src/com/hasidicmaze/   קוד המשחק (UI, מבוך, AI, ניקוד, סאונד)
assets/                תמונות + sounds/
data/scores.txt        שיאים שמורים
lib/                   ספריות ל־MP3
docs/screenshots/      צילומי מסך ל־README
build-release.bat      בניית EXE לשחקנים
```

## קרדיטים

בסיס הלמידה המקורי: [ImKennyYip / pacman-java](https://github.com/ImKennyYip/pacman-java)

האמנות, התפריטים בעברית, השלבים והעיצוב — מקוריים ל־**Hassidisher Pakman**.

—

**menmen770** · [github.com/Menmen770/hassidisher-pakman](https://github.com/Menmen770/hassidisher-pakman)
