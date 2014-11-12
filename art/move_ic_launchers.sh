#!/bin/bash
#
# Renames and moves the ic_launcher-*dpi.png files exported from
# ic_launcher.sketch (http://bohemiancoding.com/sketch/). Assumes they were
# exported to this directory.

mv ic_launcher-mdpi.png ../app/src/main/res/drawable-mdpi/ic_launcher.png
mv ic_launcher-hdpi.png ../app/src/main/res/drawable-hdpi/ic_launcher.png
mv ic_launcher-xhdpi.png ../app/src/main/res/drawable-xhdpi/ic_launcher.png
mv ic_launcher-xxhdpi.png ../app/src/main/res/drawable-xxhdpi/ic_launcher.png
mv ic_launcher-xxxhdpi.png ../app/src/main/res/drawable-xxxhdpi/ic_launcher.png
