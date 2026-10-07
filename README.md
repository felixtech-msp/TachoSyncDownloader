# Teltonika TachoSync Downloader

This program downloads tachograph files from Teltonika TachoSync to a local folder.
This can then be used to import the files into your tachograph management software (e.g. VDO Fleet).

## Setup

The program is based on Java 8 to maximize compatibility.

### Configuration

A `settings.properties` file is required next to the application executable and needs to have your
TachoSync API key, the company ID and the target folder configured. You can also set how often
the program checks for new files. Default is 15 minutes.

If the program managing your .DDD files moves successfully processed files to a different directory,
you need to set the optional SUCCESS_DIRECTORY property to that folder. This prevents files from
being downloaded multiple times.

For example:
```
API_KEY=SGVsbG8gV29ybGQgOik=
COMPANY=6954c47a-a5c5-4753-86f7-d82472524aee
TARGET_DIRECTORY=E:\\tachodata
SUCCESS_DIRECTORY=E:\\tachodata\\Success
DOWNLOAD_FREQUENCY_MINUTES=15
```

The company ID can be found in the TachoSync UI under Companies, click on your company, then Details > General > API Company ID.

The API key can be configured under Integrations > API.

### Windows Service

#### Installation

To run the program automatically in the background, NSSM (Non-Sucking Service Manager) is recommended.

To install the Windows Service, first download NSSM from the official website (https://nssm.cc/download),
then run the following commands as Administrator (make sure the paths are set according to your environment):

```
.\nssm.exe install TachoSyncDL "C:\Program Files\Java\jre1.8.0_503\bin\java.exe" "-jar C:\TachoSyncDL\teltonika-tachosync-downloader.jar"
.\nssm.exe set TachoSyncDL Description "Teltonika TachoSync Downloader"
.\nssm.exe set TachoSyncDL AppDirectory "C:\TachoSyncDL"
.\nssm.exe set TachoSyncDL AppStdout "C:\TachoSyncDL\service_out.log"
.\nssm.exe set TachoSyncDL AppStderr "C:\TachoSyncDL\service_err.log"
.\nssm.exe set TachoSyncDL Start SERVICE_AUTO_START
```

Attention! Windows is very bugged when it comes to environment variables and services. Make sure
to set the full path to your Java installation and reinstall the service when the path changes
(after a Java update).

#### Uninstallation

To uninstall the Windows Service run: `.\nssm.exe remove TachoSyncDL confirm`
