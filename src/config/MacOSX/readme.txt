In Mac OS X 10.5, there is a problem caused by launching our PACS from outside a logged in shell.  This problem presents itself whenever a grayscale JPEG is sent to the PACS (such as from Ultrasounds) and possibly in other yet to be discovered ways.

To fix the problem, the PACS must be launched through the Mac launchd process.  This directory contains two example launchd configuration files:
net.metafusion.plist is the one that tells Mac to launch the PACS when the login window appears, so no one has to be present to login.  net.metafusion.nologin.plist is one that will launch the PACS outside of the login window, i.e. from the command line.

To configure, adjust the settings of the plist file to match the location and name of the PACS sh file, here is an example:
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
        <key>Label</key>
        <string>net.metafusion</string>
        <key>LimitLoadToSessionType</key>
        <string>LoginWindow</string>
        <key>KeepAlive</key>
        <true/>
        <key>ThrottleInterval</key>
        <integer>60</integer>
        <key>StandardOutPath</key>
        <string>/dev/null</string>
        <key>ProgramArguments</key>
        <array>
                <string>/metafusion/bin/medusa.sh</string>
        </array>
<key>RunAtLoad</key>
        <true/>
</dict>
</plist>

All you should need to change is the ProgramArguments to point to the appropriate shell file.  This file needs to be placed in /Library/LaunchDaemons so that Mac OSX will find it and launch it.

You can manually launch/start/stop launchd processes from the command line:
launchctl load net.metafusion.nologin.plist    (This will load and start the service)
launchctl unload net.metafusion.nologin.plist  (This will stop and unload the service)

Our plist file tells the OS to keep our process alive, so launchctl stop will not work for us.
