@echo off
java --module-path "C:\Program Files\javafx-sdk-23.0.1\lib" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web ^
     --add-exports javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED ^
     -jar "C:\Users\Admin\Desktop\office\cake order system\out\artifacts\foodOrderSystem_jar\foodOrderSystem.jar"
pause
