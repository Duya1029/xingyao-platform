' 星曜数字化平台 - 开机自动后台启动（无窗口）
Set shell = CreateObject("WScript.Shell")
shell.CurrentDirectory = "D:\xingyao-platform\backend"
javaExe = "C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot\bin\java.exe"
If Not CreateObject("Scripting.FileSystemObject").FileExists(javaExe) Then
    javaExe = "java"
End If
shell.Run """" & javaExe & """ -jar target\xingyao-platform-1.0.0.jar --server.port=8091", 0, False
