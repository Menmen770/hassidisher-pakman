' Silent launcher — no CMD window; always recompile first
Set fso = CreateObject("Scripting.FileSystemObject")
Set sh = CreateObject("WScript.Shell")
dir = fso.GetParentFolderName(WScript.ScriptFullName)
sh.CurrentDirectory = dir

If fso.FolderExists(dir & "\bin\com") Then fso.DeleteFolder dir & "\bin\com", True
If fso.FolderExists(dir & "\bin\src") Then fso.DeleteFolder dir & "\bin\src", True

cp = "bin"
If fso.FolderExists(dir & "\lib") Then
  Set libFolder = fso.GetFolder(dir & "\lib")
  For Each f In libFolder.Files
    If LCase(fso.GetExtensionName(f.Name)) = "jar" Then
      cp = cp & ";" & f.Path
    End If
  Next
End If

rc = sh.Run("cmd /c javac -encoding UTF-8 -cp """ & cp & """ -d bin src\com\hasidicmaze\*.java src\com\hasidicmaze\*\*.java", 0, True)
If rc <> 0 Then
  MsgBox "Compile failed", 16, "Hassidisher Pakman"
  WScript.Quit rc
End If

sh.Run "javaw -cp """ & cp & """ com.hasidicmaze.App", 1, False
