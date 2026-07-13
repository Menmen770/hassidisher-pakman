' Silent launcher — no CMD window; always recompile first
Set fso = CreateObject("Scripting.FileSystemObject")
Set sh = CreateObject("WScript.Shell")
dir = fso.GetParentFolderName(WScript.ScriptFullName)
sh.CurrentDirectory = dir

If fso.FolderExists(dir & "\bin\com") Then fso.DeleteFolder dir & "\bin\com", True
If fso.FolderExists(dir & "\bin\src") Then fso.DeleteFolder dir & "\bin\src", True

rc = sh.Run("cmd /c javac -encoding UTF-8 -d bin src\com\hasidicmaze\*.java src\com\hasidicmaze\*\*.java", 0, True)
If rc <> 0 Then
  MsgBox "Compile failed", 16, "Hasidic Maze"
  WScript.Quit rc
End If

sh.Run "javaw -cp bin com.hasidicmaze.App", 1, False
