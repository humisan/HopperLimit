@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem  Uses local gradle installation if available
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Check for local gradle installation
if exist "C:\MSYS64\tmp\gradle_dl\gradle-8.10\bin\gradle.bat" (
    call "C:\MSYS64\tmp\gradle_dl\gradle-8.10\bin\gradle.bat" %*
    goto end
) else if exist "/tmp/gradle_dl/gradle-8.10/bin/gradle" (
    bash -c "export PATH=/tmp/gradle_dl/gradle-8.10/bin:$PATH && gradle %*"
    goto end
) else (
    REM Fallback to gradle on PATH
    gradle %*
    goto end
)

:end
@endlocal & set ERROR_CODE=%errorlevel%
exit /b %ERROR_CODE%
