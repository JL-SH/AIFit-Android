param([string]$Command = "help", [string]$Feature = "")

switch ($Command) {
    "test" {
        ./gradlew testDebugUnitTest
        Start-Process "app\build\reports\tests\testDebugUnitTest\index.html"
    }
    "test-feature" {
        if (-not $Feature) { Write-Host "Uso: .\make.ps1 test-feature -Feature user"; exit 1 }
        ./gradlew test --tests "com.jlsh.aifit.feature.$Feature.*"
        Start-Process "app\build\reports\tests\testDebugUnitTest\index.html"
    }
    "screenshot-record" {
        ./gradlew recordRoborazziDebug
    }
    "screenshot-verify" {
        ./gradlew verifyRoborazziDebug
        Start-Process "app\build\reports\roborazzi\index.html"
    }
    "clean-test" {
        ./gradlew clean test
        Start-Process "app\build\reports\tests\testDebugUnitTest\index.html"
    }
    "help" {
        Write-Host "Available commands:"
        Write-Host "  .\make.ps1 test                        "
        Write-Host "  .\make.ps1 test-feature -Feature user  "
        Write-Host "  .\make.ps1 screenshot-record           "
        Write-Host "  .\make.ps1 screenshot-verify           "
        Write-Host "  .\make.ps1 clean-test                  "
    }
}