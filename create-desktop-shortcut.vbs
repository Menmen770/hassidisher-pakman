' Creates a Desktop shortcut for Hassidisher Pakman with the game icon
Set fso = CreateObject("Scripting.FileSystemObject")
Set sh = CreateObject("WScript.Shell")
dir = fso.GetParentFolderName(WScript.ScriptFullName)
desktop = sh.SpecialFolders("Desktop")
linkPath = desktop & "\Hassidisher Pakman.lnk"

Set link = sh.CreateShortcut(linkPath)
link.TargetPath = dir & "\run.bat"
link.WorkingDirectory = dir
link.WindowStyle = 1
link.Description = "Hassidisher Pakman"
link.IconLocation = dir & "\pac-mandy.ico"
link.Save

WScript.Echo "Desktop shortcut created: Hassidisher Pakman"
